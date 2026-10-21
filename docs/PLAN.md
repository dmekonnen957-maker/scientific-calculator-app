# CS Calculator: Project Plan

Android app (Java, XML layouts, Gradle) for computer science students. The
dashboard uses a responsive compact toolkit table, and implemented toolkit
screens open as dedicated destinations with short transitions. All nine toolkit
rows now lead to functional screens.

## Project structure

```
CSCalculator/
├── build.gradle                  root Gradle file
├── settings.gradle               project name + modules
├── gradle.properties             JDK 17 path is already set here
├── local.properties              Android SDK path (C:\Android)
├── gradlew / gradlew.bat         Gradle wrapper (use gradlew.bat on Windows)
├── gradle/wrapper/               wrapper files (do not edit)
├── docs/
│   └── PLAN.md                   implementation status and next modules
└── app/
    ├── build.gradle              app settings (minSdk 21, dependencies)
    └── src/main/
        ├── AndroidManifest.xml   registered app screens
        ├── java/com/example/cscalc/
        │   ├── MainActivity.java            responsive table dashboard + navigation
        │   ├── ToolkitActivity.java         shared toolkit close transition
        │   ├── FlagRegisterActivity.java    8085/8086 flags learning screen
        │   ├── NumberConverterActivity.java working BIN/OCT/DEC/HEX converter
        │   ├── MultiBaseCalculatorActivity.java multi-base expression keypad
        │   ├── HistoryActivity.java        locally saved conversion history
        │   ├── SettingsActivity.java       persistent light/dark setting
        │   ├── UnavailableActivity.java    explicit state for unfinished tools
        │   ├── converter/
        │   │   ├── NumberBase.java          BIN, OCT, DEC, HEX enum
        │   │   └── BaseConverter.java       signed integer parse / format
        │   ├── calculator/
        │   │   ├── MultiBaseCalculator.java exact BigInteger expression engine
        │   │   ├── FlagRegisterCalculator.java processor-specific status flags
        │   │   ├── ExpressionEvaluator.java planned expression evaluator
        │   │   ├── ScientificFunctions.java planned scientific functions
        │   │   └── BitwiseOps.java          planned bitwise operations
        │   └── utils/                       input, history, theme, and insets
        └── res/
            ├── layout/                      dashboard and feature screens
            └── values/                      strings, dimensions, and themes
```

Rule: logic classes (converter, calculator, utils) contain no Android code.
Only the Activities touch views. This keeps the logic easy to test.

## Build order

### Implemented
- [x] Responsive dashboard with nine module cards and settings entry
- [x] Number-system conversion between BIN, OCT, DEC, and HEX
- [x] Multi-base calculator with keypad, parentheses, and BigInteger arithmetic
- [x] Interactive logic gates with truth tables
- [x] Boolean expression evaluation, truth tables, and equivalent SOP simplification
- [x] Binary arithmetic with addition, subtraction, multiplication, division, carries, and borrows
- [x] Bitwise operations with fixed-width masking (8/16/32-bit), bounded NOT, signedness, and shifts
- [x] Memory & data-unit conversion with SI decimal vs IEC binary distinction and worked examples
- [x] Persistent conversion history with dashboard recent items
- [x] Persistent light/dark theme selection
- [x] Compact responsive toolkit table and animated full-screen navigation
- [x] Intel 8085/8086 flag register calculator with processor-specific flag rules
- [x] Focused unit tests for all arithmetic, bitwise, and conversion logic

### Remaining work
- [ ] Runtime tap-through audit on an attached Android emulator/device

## Commands

```
cd C:\Users\lenovo\Desktop\android\CSCalculator
adb devices
gradlew installDebug
adb shell am start -n com.example.cscalc/.MainActivity
```

If the zip extracts into CSCalculator\CSCalculator, run the commands from the inner folder.
