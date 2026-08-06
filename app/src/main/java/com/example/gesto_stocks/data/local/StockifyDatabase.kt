package com.example.gesto_stocks.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.gesto_stocks.data.model.Produto
import com.example.gesto_stocks.data.model.Utilizador
import com.example.gesto_stocks.util.hashPassword
import com.example.gesto_stocks.data.model.Movimento


/**
 * Base de dados local da aplicação.
 * Contém três tabelas: produtos, utilizadores e movimentos.
 */
@Database(
    entities = [Produto::class, Utilizador::class, Movimento::class],
    version = 3
)
abstract class StockifyDatabase : RoomDatabase() {

    abstract fun produtoDao(): ProdutoDao
    abstract fun utilizadorDao(): UtilizadorDao

    abstract fun movimentoDao(): MovimentoDao

    /**
     * Ajusta o stock e regista o movimento como uma só operação.
     * O ajuste é feito em SQL (`quantidade = quantidade + :delta`) em vez de
     * ler-alterar-escrever a partir de uma cópia do produto: assim duas saídas
     * ao mesmo tempo não perdem uma, e a verificação de stock suficiente fica
     * ligada à escrita em vez de ser um `if` antes dela.
     *
     * @param delta positivo numa entrada, negativo numa saída.
     * @return false se não houver stock que chegue — nada é gravado.
     */
    suspend fun registarMovimento(movimento: Movimento, delta: Int): Boolean =
        withTransaction {
            if (produtoDao().ajustarQuantidade(movimento.produtoId, delta) == 0) {
                return@withTransaction false
            }
            movimentoDao().inserir(movimento)
            true
        }

    companion object {
        // Instância única: evita abrir várias ligações à mesma base de dados
        @Volatile
        private var INSTANCIA: StockifyDatabase? = null

        fun obter(context: Context): StockifyDatabase {
            return INSTANCIA ?: synchronized(this) {
                INSTANCIA ?: Room.databaseBuilder(
                    context.applicationContext,
                    StockifyDatabase::class.java,
                    "stockify.db"
                )
                    // ponytail: sem migrações formais, o schema é recriado e os
                    // dados locais perdem-se em cada mudança de versão. Escrever
                    // Migration reais antes de haver stock a sério na app.
                    .fallbackToDestructiveMigration(true)
                    // Semear no onOpen e não no onCreate/onDestructiveMigration.
                    // O Room chama o onDestructiveMigration ENTRE o dropAllTables
                    // e o createAllTables, ou seja, com as tabelas por existir:
                    // semear aí rebentava com "no such table: produtos" ao
                    // atualizar sobre uma base antiga. O onOpen corre sempre com
                    // a base já completa, ficheiro novo ou schema recriado, e
                    // fora da thread principal.
                    .addCallback(object : Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            if (semUtilizadores(db)) dadosIniciais(db)
                        }
                    })
                    .build()
                    .also { INSTANCIA = it }
            }
        }

        /**
         * Serve de sentinela ao seed. É a tabela de utilizadores e não a de
         * produtos porque uma conta nunca é apagada pela app: se o utilizador
         * esvaziar o stock, os produtos de exemplo não devem voltar.
         */
        private fun semUtilizadores(db: SupportSQLiteDatabase): Boolean =
            db.query("SELECT COUNT(*) FROM utilizadores").use { cursor ->
                cursor.moveToFirst() && cursor.getInt(0) == 0
            }

        /** Semeia produtos e contas de demonstração. As tabelas estão vazias. */
        private fun dadosIniciais(db: SupportSQLiteDatabase) {
            exemplos().forEach { produto ->
                db.insert("produtos", SQLiteDatabase.CONFLICT_ABORT, ContentValues().apply {
                    put("nome", produto.nome)
                    put("sku", produto.sku)
                    put("categoria", produto.categoria)
                    put("fornecedor", produto.fornecedor)
                    put("preco", produto.preco)
                    put("precoCusto", produto.precoCusto)
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

        // Produtos de demonstração: incluem os três estados possíveis
        // (esgotado, abaixo do mínimo e normal)
        private fun exemplos() = listOf(
            Produto(nome = "Motor Elétrico X200", sku = "SKU-1042",
                categoria = "Motores", fornecedor = "ElectroTech",
                preco = 189.90, precoCusto = 120.00, quantidade = 84, stockMinimo = 20),
            Produto(nome = "Sensor de Proximidade S3", sku = "SKU-2087",
                categoria = "Sensores", fornecedor = "SensorLab",
                preco = 45.50, precoCusto = 28.00, quantidade = 12, stockMinimo = 25),
            Produto(nome = "Cabo Blindado 4mm", sku = "SKU-3391",
                categoria = "Cabos", fornecedor = "CaboPlus",
                preco = 12.30, precoCusto = 7.50, quantidade = 0, stockMinimo = 50),
            Produto(nome = "Filtro Industrial F9", sku = "SKU-4120",
                categoria = "Filtros", fornecedor = "FiltroMax",
                preco = 78.00, precoCusto = 49.00, quantidade = 156, stockMinimo = 40),
            Produto(nome = "Válvula Pneumática V7", sku = "SKU-5205",
                categoria = "Válvulas", fornecedor = "PneuSys",
                preco = 134.20, precoCusto = 85.00, quantidade = 8, stockMinimo = 15)
        )

        // Contas de acesso definidas no enunciado do trabalho prático.
        // ponytail: credenciais de demonstração em claro no código-fonte — é o
        // teto conhecido desta app. Antes de qualquer distribuição real: gerar
        // no primeiro arranque e forçar mudança de password no primeiro login.
        // As passwords ficam guardadas com PBKDF2 e sal, por isso o ficheiro
        // da base de dados não é, por si só, uma lista de passwords.
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
