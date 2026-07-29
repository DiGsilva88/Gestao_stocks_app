# Fix Unresolved Reference 'StockViewModel' and Package Inconsistencies

The build error `Unresolved reference 'StockViewModel'` in `StockFragment.kt` is caused by a package mismatch. `StockViewModel` is defined in the package `com.example.gesto_stocks.ui.stocks` (plural), while `StockFragment` is declared in `com.example.gesto_stocks.ui.stock` (singular), despite being in the same physical directory. Additionally, there are incorrect imports for ViewBinding.

## User Review Required

> [!IMPORTANT]
> I will be renaming the package in `StockFragment.kt` and `ProdutoAdapter.kt` from `ui.stock` to `ui.stocks` to match their directory structure and `StockViewModel.kt`.

## Proposed Changes

### UI Components

#### [MODIFY] [StockFragment.kt](file:///C:/Users/diana/AndroidStudioProjects/gesto_stocks/app/src/main/java/com/example/gesto_stocks/ui/stocks/StockFragment.kt)
- Update package declaration to `com.example.gesto_stocks.ui.stocks`.
- Correct the ViewBinding import to `com.example.gesto_stocks.databinding.FragmentStockBinding`.
- Since it will be in the same package as `StockViewModel`, the unresolved reference will be resolved.

#### [MODIFY] [ProdutoAdapter.kt](file:///C:/Users/diana/AndroidStudioProjects/gesto_stocks/app/src/main/java/com/example/gesto_stocks/ui/stocks/ProdutoAdapter.kt)
- Update package declaration to `com.example.gesto_stocks.ui.stocks`.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to verify that the build error is resolved.

### Manual Verification
- Verify in Android Studio that the red "Unresolved reference" markers are gone in `StockFragment.kt`.
