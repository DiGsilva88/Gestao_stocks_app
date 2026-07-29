# Implementation Plan - Resolve Persistent ActivityLoginBinding Error

The IDE is failing to resolve `ActivityLoginBinding` in `LoginActivity.kt`, incorrectly identifying the binding root as a `File` object. This is likely a caching or indexing conflict in the IDE's internal model for that specific name.

## Proposed Changes

### UI Components

#### [MODIFY] [activity_login.xml](file:///C:/Users/diana/AndroidStudioProjects/gesto_stocks/app/src/main/res/layout/activity_login.xml)
- Add standard namespaces (`xmlns:app`, `xmlns:tools`).
- Add `android:id="@+id/login_root"` to the root tag.
- Add `tools:context=".LoginActivity"` to strengthen the link between layout and class.

#### [MODIFY] [LoginActivity.kt](file:///C:/Users/diana/AndroidStudioProjects/gesto_stocks/app/src/main/java/com/example/gesto_stocks/LoginActivity.kt)
- Refactor to use a `private lateinit var binding` member variable, matching the successful pattern in `MainActivity.kt`.
- Use the FQN for inflation to ensure the compiler and IDE are looking at the correct generated class.

#### [RENAME] (Alternative)
- If the above fails, rename `activity_login.xml` to `view_login.xml` to generate `ViewLoginBinding` and avoid any potential name collisions with the `ActivityLogin` string.

## Verification Plan

### Automated Tests
- Run `gradle_build` to ensure the project still compiles (it should, as the compiler previously succeeded).
- Run `analyze_file` on `LoginActivity.kt` to verify that the IDE resolution errors are resolved.

### Manual Verification
- Check if red underlines persist in the IDE.
