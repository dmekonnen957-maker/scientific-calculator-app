# CS Calculator: Project Plan

Android app (Java, XML layouts, Gradle) for computer science students. The
dashboard, number-system converter, local conversion history, and light/dark
setting are implemented. The remaining toolkit modules are clearly marked as
coming soon until their screens and logic are built.

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
        │   ├── MainActivity.java            responsive toolkit dashboard
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
- [x] Persistent conversion history with dashboard recent items
- [x] Persistent light/dark theme selection
- [x] Explicit unavailable screens for modules still in development

### Next modules
- [ ] Microprocessor flag-register calculator
- [ ] Binary arithmetic and bitwise operations
- [ ] Memory and data-unit conversion
- [x] Focused tests for converter and multi-base calculator logic

## Commands

```
cd C:\Users\lenovo\Desktop\android\CSCalculator
adb devices
gradlew installDebug
adb shell am start -n com.example.cscalc/.MainActivity
```

If the zip extracts into CSCalculator\CSCalculator, run the commands from the inner folder.
