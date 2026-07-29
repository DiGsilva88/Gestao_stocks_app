# Implementation Plan - Fix Warnings and Build Error

Fix the version warnings in `libs.versions.toml` and resolve the "built-in Kotlin" source sets error.

## Problem Summary
1.  **Version Warnings**: Outdated versions for `core-ktx`, `activity-ktx`, and `kotlin`.
2.  **Unused Plugin**: `kotlin-android` is defined but not applied.
3.  **Build Error**: "Using kotlin.sourceSets DSL to add Kotlin sources is not allowed with built-in Kotlin". This is likely due to KSP trying to add generated sources to Kotlin source sets when the `kotlin-android` plugin is not explicitly managed or when using an incompatible AGP version.

## Proposed Changes

### [Build Configuration]

#### [MODIFY] [libs.versions.toml](file:///C:/Users/diana/AndroidStudioProjects/gesto_stocks/gradle/libs.versions.toml)
- Update `coreKtx` to `1.19.0`.
- Update `activityKtx` to `1.13.0`.
- Update `kotlin` to `2.4.10`.
- Update `ksp` to `2.3.10` (matching the latest available plugin version).

#### [MODIFY] [build.gradle.kts (root)](file:///C:/Users/diana/AndroidStudioProjects/gesto_stocks/build.gradle.kts)
- Apply `libs.plugins.kotlin.android` with `apply false`.

#### [MODIFY] [build.gradle.kts (app)](file:///C:/Users/diana/AndroidStudioProjects/gesto_stocks/app/build.gradle.kts)
- Apply `libs.plugins.kotlin.android`.

#### [MODIFY] [gradle.properties](file:///C:/Users/diana/AndroidStudioProjects/gesto_stocks/gradle.properties)
- Add `android.disallowKotlinSourceSets=false` to resolve the KSP source set conflict as suggested by the error message.

## Verification Plan

### Automated Tests
- Run `gradle_sync` to verify the build error is resolved and project syncs successfully.
- Run `analyze_file` on `libs.versions.toml` to confirm warnings are gone.

### Manual Verification
- Verify that Room database classes (if any) still compile correctly with KSP.
