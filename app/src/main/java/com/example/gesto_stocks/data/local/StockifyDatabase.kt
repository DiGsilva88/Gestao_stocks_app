package com.example.gesto_stocks.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.gesto_stocks.data.model.Produto
import com.example.gesto_stocks.data.model.Utilizador


@Database(
    entities = [Produto::class, Utilizador::class],
    version = 1
)

abstract class StockifyDatabase: RoomDatabase() {

    abstract fun produtoDao(): ProdutoDao
    abstract fun utilizadorDao(): UtilizadorDao

    companion object{
        @Volatile
        private var INSTANCE: StockifyDatabase? = null

        fun obterInstancia(context: Context): StockifyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StockifyDatabase::class.java,
                    "stockify_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
