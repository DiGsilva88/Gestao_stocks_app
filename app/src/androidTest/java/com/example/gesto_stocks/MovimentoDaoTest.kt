package com.example.gesto_stocks

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.gesto_stocks.data.local.StockifyDatabase
import com.example.gesto_stocks.data.model.Movimento
import com.example.gesto_stocks.data.model.Produto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Garante que um movimento de stock nunca deixa a base de dados num estado
 * impossível: sem stock negativo e sem movimento gravado que não tenha
 * mexido no produto.
 */
@RunWith(AndroidJUnit4::class)
class MovimentoDaoTest {

    private lateinit var db: StockifyDatabase

    private val produto = Produto(
        id = 1, nome = "Motor", sku = "SKU-1", categoria = "Motores",
        preco = 100.0, precoCusto = 60.0, quantidade = 5, stockMinimo = 2
    )

    @Before
    fun criarBaseDeDados() = runBlocking {
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(contexto, StockifyDatabase::class.java).build()
        db.produtoDao().inserir(produto)
    }

    @After
    fun fechar() = db.close()

    @Test
    fun saidaValidaDescontaERegista() = runBlocking {
        assertTrue(db.registarMovimento(movimento("SAIDA", 3), -3))

        assertEquals(2, quantidade())
        assertEquals(1, movimentosGravados())
    }

    @Test
    fun entradaAcrescenta() = runBlocking {
        assertTrue(db.registarMovimento(movimento("ENTRADA", 7), 7))

        assertEquals(12, quantidade())
    }

    @Test
    fun saidaMaiorQueOStockNaoGravaNada() = runBlocking {
        assertFalse(db.registarMovimento(movimento("SAIDA", 6), -6))

        assertEquals(5, quantidade())
        assertEquals(0, movimentosGravados())
    }

    @Test
    fun saidasEmSimultaneoNaoDeixamStockNegativo() = runBlocking {
        // Dez saídas de uma unidade sobre um stock de cinco: só cinco podem
        // passar. Com o antigo ler-alterar-escrever passavam quase todas.
        val resultados = List(10) {
            async(Dispatchers.IO) { db.registarMovimento(movimento("SAIDA", 1), -1) }
        }.awaitAll()

        assertEquals(5, resultados.count { it })
        assertEquals(0, quantidade())
        assertEquals(5, movimentosGravados())
    }

    private fun movimento(tipo: String, qtd: Int) = Movimento(
        produtoId = produto.id, nomeProduto = produto.nome,
        tipo = tipo, quantidade = qtd, utilizador = "teste"
    )

    private suspend fun quantidade() = db.produtoDao().buscarPorId(produto.id)!!.quantidade

    private fun movimentosGravados(): Int =
        db.query("SELECT COUNT(*) FROM movimentos", null).use {
            it.moveToFirst()
            it.getInt(0)
        }
}
