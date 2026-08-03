package com.example.gesto_stocks.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.gesto_stocks.data.model.Produto
import com.example.gesto_stocks.data.model.Utilizador
import com.example.gesto_stocks.util.hashPassword

/**
 * Base de dados local da aplicação.
 * Contém duas tabelas: produtos e utilizadores.
 */
@Database(
    entities = [Produto::class, Utilizador::class],
    version = 2
)
abstract class StockifyDatabase : RoomDatabase() {

    abstract fun produtoDao(): ProdutoDao
    abstract fun utilizadorDao(): UtilizadorDao

    companion object {
        // Instância única: evita abrir várias ligações à mesma base de dados
        @Volatile
        private var INSTANCIA: StockifyDatabase? = null

        fun obter(context: Context): StockifyDatabase {
            return INSTANCIA ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    StockifyDatabase::class.java,
                    "stockify.db"
                )
                    // Preenche a base de dados na primeira execução. Usa SQL direto
                    // (não os DAOs do Room) e corre de forma síncrona: onCreate() é
                    // chamado a partir da própria abertura da BD, antes de qualquer
                    // query (ex.: o login) poder ser executada — um coroutine lançado
                    // em segundo plano ou um runBlocking a chamar suspend DAOs no
                    // executor do Room causaria uma corrida ou um deadlock.
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)

                            exemplos().forEach { produto ->
                                db.insert("produtos", SQLiteDatabase.CONFLICT_ABORT, ContentValues().apply {
                                    put("nome", produto.nome)
                                    put("sku", produto.sku)
                                    put("categoria", produto.categoria)
                                    put("fornecedor", produto.fornecedor)
                                    put("preco", produto.preco)
                                    put("quantidade", produto.quantidade)
                                    put("stockMinimo", produto.stockMinimo)
                                })
                            }

                            utilizadoresIniciais().forEach { utilizador ->
                                db.insert("utilizadores", SQLiteDatabase.CONFLICT_ABORT, ContentValues().apply {
                                    put("nome", utilizador.nome)
                                    put("email", utilizador.email)
                                    put("passwordHash", utilizador.passwordHash)
                                })
                            }
                        }
                    })
                    // BD local de demonstração: sem migração formal, recria o
                    // schema (perde dados locais) quando a versão muda.
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCIA = db
                db
            }
        }

        // Produtos de demonstração: incluem os três estados possíveis
        // (esgotado, abaixo do mínimo e normal)
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

        // Contas de acesso definidas no enunciado do trabalho prático
        private fun utilizadoresIniciais() = listOf(
            Utilizador(
                nome = "Administrador",
                email = "admin",
                passwordHash = hashPassword("password123")
            ),
            Utilizador(
                nome = "Cesae",
                email = "cesae",
                passwordHash = hashPassword("cesae")
            ),
            Utilizador(
                nome = "Diana",
                email = "diana",
                passwordHash = hashPassword("diana123")
            )
        )
    }
}