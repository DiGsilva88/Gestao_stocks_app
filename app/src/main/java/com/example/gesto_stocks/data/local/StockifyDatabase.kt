package com.example.gesto_stocks.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.gesto_stocks.data.model.Produto
import com.example.gesto_stocks.data.model.Utilizador
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Database(
    entities = [Produto::class, Utilizador::class],
    version = 1
)

abstract class StockifyDatabase: RoomDatabase() {

    abstract fun produtoDao(): ProdutoDao
    abstract fun utilizadorDao(): UtilizadorDao

    companion object {
        @Volatile
        private var INSTANCIA: StockifyDatabase? = null

        fun obter(context: Context): StockifyDatabase {
            return INSTANCIA ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    StockifyDatabase::class.java,
                    "stockify.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCIA?.produtoDao()?.inserirTodos(exemplos())
                            }
                        }
                    })
                    .build()
                INSTANCIA = db
                db
            }
        }

        private fun exemplos() = listOf(
            Produto(nome = "Motor Elétrico X200", sku = "SKU-1042",
                categoria = "Motores", fornecedor = "ElectroTech",
                preco = 189.90, quantidade = 84, stockMinimo = 20),
            Produto(nome = "Sensor de Proximidade S3", sku = "SKU-2087",
                categoria = "Sensores", fornecedor = "SensorLab",
                preco = 45.50, quantidade = 12, stockMinimo = 25),
            Produto(nome = "Cabo Blindado 4mm", sku = "SKU-3391",
                categoria = "Cabos", fornecedor = "CaboPlus",
                preco = 12.30, quantidade = 0, stockMinimo = 50),
            Produto(nome = "Filtro Industrial F9", sku = "SKU-4120",
                categoria = "Filtros", fornecedor = "FiltroMax",
                preco = 78.00, quantidade = 156, stockMinimo = 40),
            Produto(nome = "Válvula Pneumática V7", sku = "SKU-5205",
                categoria = "Válvulas", fornecedor = "PneuSys",
                preco = 134.20, quantidade = 8, stockMinimo = 15)
        )
    }
        }


