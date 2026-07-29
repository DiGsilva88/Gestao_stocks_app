# Walkthrough - Fixed Syntax Error in LoginActivity

I have fixed the compilation error in `LoginActivity.kt` related to incorrect `setOnClickListener` usage.

## Changes Made

### [app]

#### [LoginActivity.kt](file:///C:/Users/diana/AndroidStudioProjects/gesto_stocks/app/src/main/java/com/example/gesto_stocks/LoginActivity.kt)
Fixed the `setOnClickListener` calls by providing empty lambda blocks `{ }`. This satisfies the Kotlin compiler requirement for a function invocation with a trailing lambda.

```kotlin
// Before
binding.btnEntrar.setOnClickListener
binding.txtIrRegisto.setOnClickListener

// After
binding.btnEntrar.setOnClickListener { }
binding.txtIrRegisto.setOnClickListener { }
```

## Verification Results

### Automated Tests
- Executed `./gradlew :app:compileDebugKotlin`
- **Result**: Build finished successfully.
