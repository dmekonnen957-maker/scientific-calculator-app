package com.example.cscalc.history;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class HistoryRepositoryTest {

    private FakeCalculationRecordDao fakeDao;
    private HistoryRepository repository;

    @Before
    public void setUp() {
        fakeDao = new FakeCalculationRecordDao();
        repository = new HistoryRepository(fakeDao, Runnable::run);
    }

    @Test
    public void saveAndReadRecordsNewestFirst() {
        CalculationRecord r1 = new CalculationRecord(
                HistoryRepository.MODULE_BINARY,
                "Addition (+)",
                "1011 + 0111",
                "10010 (DEC: 18)",
                "Binary",
                1000L,
                "Carries step"
        );

        CalculationRecord r2 = new CalculationRecord(
                HistoryRepository.MODULE_BITWISE,
                "AND",
                "0x0F & 0x55",
                "0x05",
                "8-bit",
                2000L,
                "Masking explanation"
        );

        repository.insertSync(r1);
        repository.insertSync(r2);

        List<CalculationRecord> all = repository.getAllSync();
        assertEquals(2, all.size());
        // Newest timestamp first: r2 must be first!
        assertEquals(HistoryRepository.MODULE_BITWISE, all.get(0).getModuleName());
        assertEquals("0x0F & 0x55", all.get(0).getInputExpression());
        assertEquals(HistoryRepository.MODULE_BINARY, all.get(1).getModuleName());
    }

    @Test
    public void searchRecordsByExpressionOrResult() {
        CalculationRecord r1 = new CalculationRecord(
                HistoryRepository.MODULE_CONVERTER,
                "Base Conversion",
                "255",
                "HEX FF",
                "Source: DEC",
                1000L,
                "Positional"
        );

        CalculationRecord r2 = new CalculationRecord(
                HistoryRepository.MODULE_MEMORY,
                "Unit Conversion",
                "1 TB",
                "931.32 GiB",
                "TB → GiB",
                1500L,
                "Discrepancy"
        );

        repository.insertSync(r1);
        repository.insertSync(r2);

        List<CalculationRecord> search1 = repository.searchSync("255", HistoryRepository.MODULE_ALL);
        assertEquals(1, search1.size());
        assertEquals("255", search1.get(0).getInputExpression());

        List<CalculationRecord> search2 = repository.searchSync("931.32", HistoryRepository.MODULE_ALL);
        assertEquals(1, search2.size());
        assertEquals("1 TB", search2.get(0).getInputExpression());
    }

    @Test
    public void filterRecordsByModule() {
        CalculationRecord r1 = new CalculationRecord(
                HistoryRepository.MODULE_LOGIC,
                "AND",
                "A=1, B=0",
                "Y=0",
                "Gate: AND",
                1000L,
                "Truth row"
        );

        CalculationRecord r2 = new CalculationRecord(
                HistoryRepository.MODULE_BOOLEAN,
                "Simplify",
                "A AND A",
                "A",
                "SOP",
                1100L,
                "Idempotent"
        );

        repository.insertSync(r1);
        repository.insertSync(r2);

        List<CalculationRecord> logicOnly = repository.searchSync("", HistoryRepository.MODULE_LOGIC);
        assertEquals(1, logicOnly.size());
        assertEquals(HistoryRepository.MODULE_LOGIC, logicOnly.get(0).getModuleName());

        List<CalculationRecord> booleanOnly = repository.searchSync("", HistoryRepository.MODULE_BOOLEAN);
        assertEquals(1, booleanOnly.size());
        assertEquals(HistoryRepository.MODULE_BOOLEAN, booleanOnly.get(0).getModuleName());

        List<CalculationRecord> all = repository.searchSync("", HistoryRepository.MODULE_ALL);
        assertEquals(2, all.size());
    }

    @Test
    public void deleteIndividualRecord() {
        CalculationRecord record = new CalculationRecord(
                HistoryRepository.MODULE_CALCULATOR,
                "Evaluate",
                "7 + 5",
                "12",
                "Base: DEC",
                1000L,
                "Simple addition"
        );

        long id = repository.insertSync(record);
        assertTrue(id > 0);

        List<CalculationRecord> before = repository.getAllSync();
        assertEquals(1, before.size());

        int deleted = repository.deleteByIdSync(id);
        assertEquals(1, deleted);

        List<CalculationRecord> after = repository.getAllSync();
        assertTrue(after.isEmpty());
    }

    @Test
    public void clearAllRecords() {
        for (int i = 0; i < 5; i++) {
            repository.insertSync(new CalculationRecord(
                    HistoryRepository.MODULE_BINARY,
                    "Add",
                    "1 + " + i,
                    String.valueOf(1 + i),
                    "Binary",
                    1000L + i,
                    ""
            ));
        }

        assertEquals(5, repository.getAllSync().size());
        repository.clearAllSync();
        assertEquals(0, repository.getAllSync().size());
    }

    @Test
    public void getRecentLimitsOutput() {
        for (int i = 0; i < 10; i++) {
            repository.insertSync(new CalculationRecord(
                    HistoryRepository.MODULE_BINARY,
                    "Add",
                    "Input " + i,
                    "Result " + i,
                    "Binary",
                    1000L + i,
                    ""
            ));
        }

        List<CalculationRecord> recent2 = fakeDao.getRecent(2);
        assertEquals(2, recent2.size());
        assertEquals("Input 9", recent2.get(0).getInputExpression());
        assertEquals("Input 8", recent2.get(1).getInputExpression());
    }

    @Test
    public void asynchronousInsertAndCallback() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean success = new AtomicBoolean(false);

        CalculationRecord r = new CalculationRecord(
                HistoryRepository.MODULE_BITWISE,
                "XOR",
                "0xAA ^ 0x55",
                "0xFF",
                "8-bit",
                5000L,
                "XOR"
        );

        repository.insert(r, new HistoryRepository.Callback<Long>() {
            @Override
            public void onSuccess(Long id) {
                success.set(id != null && id > 0);
                latch.countDown();
            }

            @Override
            public void onError(Exception e) {
                latch.countDown();
            }
        });

        assertTrue(latch.await(3, TimeUnit.SECONDS));
        assertTrue(success.get());
    }

    /**
     * In-memory test double of CalculationRecordDao for fast, deterministic JVM unit tests.
     */
    private static class FakeCalculationRecordDao implements CalculationRecordDao {
        private final List<CalculationRecord> storage = new ArrayList<>();
        private long idGen = 0;

        @Override
        public synchronized long insert(CalculationRecord record) {
            record.setId(++idGen);
            storage.add(record);
            return record.getId();
        }

        @Override
        public synchronized void insertAll(List<CalculationRecord> records) {
            for (CalculationRecord r : records) {
                insert(r);
            }
        }

        @Override
        public synchronized int update(CalculationRecord record) {
            for (int i = 0; i < storage.size(); i++) {
                if (storage.get(i).getId() == record.getId()) {
                    storage.set(i, record);
                    return 1;
                }
            }
            return 0;
        }

        @Override
        public synchronized int delete(CalculationRecord record) {
            return deleteById(record.getId());
        }

        @Override
        public synchronized int deleteById(long id) {
            boolean removed = storage.removeIf(r -> r.getId() == id);
            return removed ? 1 : 0;
        }

        @Override
        public synchronized void clearAll() {
            storage.clear();
        }

        @Override
        public synchronized List<CalculationRecord> getAll() {
            List<CalculationRecord> copy = new ArrayList<>(storage);
            copy.sort((a, b) -> {
                int cmp = Long.compare(b.getTimestamp(), a.getTimestamp());
                return cmp != 0 ? cmp : Long.compare(b.getId(), a.getId());
            });
            return copy;
        }

        @Override
        public synchronized List<CalculationRecord> getByModule(String module) {
            List<CalculationRecord> filtered = new ArrayList<>();
            for (CalculationRecord r : getAll()) {
                if (r.getModuleName().equalsIgnoreCase(module)) {
                    filtered.add(r);
                }
            }
            return filtered;
        }

        @Override
        public synchronized List<CalculationRecord> search(String query, String module) {
            String q = query == null ? "" : query.toLowerCase(Locale.ROOT);
            List<CalculationRecord> results = new ArrayList<>();
            for (CalculationRecord r : getAll()) {
                boolean matchModule = (module == null || module.isEmpty() || "All".equalsIgnoreCase(module) || "All Modules".equalsIgnoreCase(module))
                        || r.getModuleName().equalsIgnoreCase(module);
                if (!matchModule) continue;

                boolean matchQuery = q.isEmpty()
                        || r.getInputExpression().toLowerCase(Locale.ROOT).contains(q)
                        || r.getResult().toLowerCase(Locale.ROOT).contains(q)
                        || r.getOperation().toLowerCase(Locale.ROOT).contains(q)
                        || r.getConfiguration().toLowerCase(Locale.ROOT).contains(q)
                        || r.getExtraDetails().toLowerCase(Locale.ROOT).contains(q);

                if (matchQuery) {
                    results.add(r);
                }
            }
            return results;
        }

        @Override
        public synchronized List<CalculationRecord> getRecent(int limit) {
            List<CalculationRecord> all = getAll();
            if (all.size() <= limit) return all;
            return all.subList(0, limit);
        }

        @Override
        public synchronized CalculationRecord getById(long id) {
            for (CalculationRecord r : storage) {
                if (r.getId() == id) return r;
            }
            return null;
        }

        @Override
        public synchronized int count() {
            return storage.size();
        }
    }
}
