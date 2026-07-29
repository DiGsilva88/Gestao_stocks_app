# Implementation Plan - Navigation and Login Flow

The goal is to implement the navigation logic in `LoginActivity` and update the application flow so that the user starts at the Login screen after the Splash screen.

## User Review Required

> [!IMPORTANT]
> This plan changes the initial screen after the Splash screen from `MainActivity` to `LoginActivity`.
> It also creates a placeholder `RegisterActivity` to handle the "Ainda não tenho conta" link.

## Proposed Changes

### [app]

#### [MODIFY] [SplashActivity.kt](file:///C:/Users/diana/AndroidStudioProjects/gesto_stocks/app/src/main/java/com/example/gesto_stocks/SplashActivity.kt)
- Change the intent destination from `MainActivity::class.java` to `LoginActivity::class.java`.

#### [MODIFY] [LoginActivity.kt](file:///C:/Users/diana/AndroidStudioProjects/gesto_stocks/app/src/main/java/com/example/gesto_stocks/LoginActivity.kt)
- Implement `btnEntrar` click listener to navigate to `MainActivity`.
- Implement `txtIrRegisto` click listener to navigate to `RegisterActivity`.
- Add simple validation for email and password fields.

#### [NEW] [RegisterActivity.kt](file:///C:/Users/diana/AndroidStudioProjects/gesto_stocks/app/src/main/java/com/example/gesto_stocks/RegisterActivity.kt)
- Create a basic Activity for registration.

#### [NEW] [activity_register.xml](file:///C:/Users/diana/AndroidStudioProjects/gesto_stocks/app/src/main/res/layout/activity_register.xml)
- Create a layout for the registration screen, similar to the login screen.

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/diana/AndroidStudioProjects/gesto_stocks/app/src/main/AndroidManifest.xml)
- Register `RegisterActivity` in the manifest.

## Verification Plan

### Manual Verification
- Deploy the app and verify:
  1. Splash screen appears and transitions to Login screen.
  2. Clicking "Entrar" (with mock data) navigates to the Main screen.
  3. Clicking "Ainda não tenho conta" navigates to the Register screen.
