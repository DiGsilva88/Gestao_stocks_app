package com.example.gesto_stocks

import com.example.gesto_stocks.data.model.Produto
import com.example.gesto_stocks.ui.inicio.contarEmAlerta
import com.example.gesto_stocks.ui.inicio.receitaPorCategoria
import com.example.gesto_stocks.ui.inicio.somaLucro
import com.example.gesto_stocks.ui.inicio.somaReceita
import com.example.gesto_stocks.ui.inicio.somaStock
import com.example.gesto_stocks.ui.inicio.top3PorValor
import org.junit.Assert.assertEquals
import org.junit.Test

class EstatisticasTest {

    private fun produto(
        id: Int, nome: String, categoria: String,
        preco: Double, custo: Double, qtd: Int, minimo: Int
    ) = Produto(
        id = id, nome = nome, sku = "SKU-$id", categoria = categoria,
        preco = preco, precoCusto = custo, quantidade = qtd, stockMinimo = minimo
    )

    private val motor = produto(1, "Motor", "Motores", 100.0, 60.0, 10, 5)
    private val cabo = produto(2, "Cabo", "Cabos", 10.0, 7.0, 100, 20)
    private val esgotado = produto(3, "Filtro", "Filtros", 50.0, 30.0, 0, 10)
    private val prejuizo = produto(4, "Saldo", "Motores", 20.0, 35.0, 4, 1)

    private val lista = listOf(motor, cabo, esgotado, prejuizo)

    @Test
    fun `lista vazia da tudo a zero`() {
        assertEquals(0, somaStock(emptyList()))
        assertEquals(0, contarEmAlerta(emptyList()))
        assertEquals(0.0, somaReceita(emptyList()), 0.001)
        assertEquals(0.0, somaLucro(emptyList()), 0.001)
        assertEquals(emptyList<Pair<String, Double>>(), receitaPorCategoria(emptyList()))
        assertEquals(emptyList<Produto>(), top3PorValor(emptyList()))
    }

    @Test
    fun `soma do stock`() {
        assertEquals(114, somaStock(lista))
    }

    @Test
    fun `alerta conta os que estao abaixo do minimo`() {
        // esgotado (0 < 10) é o único; cabo tem 100 >= 20 e o motor 10 >= 5
        assertEquals(1, contarEmAlerta(lista))
    }

    @Test
    fun `receita e lucro`() {
        // 1000 + 1000 + 0 + 80
        assertEquals(2080.0, somaReceita(lista), 0.001)
        // 400 + 300 + 0 - 60: um produto vendido abaixo do custo baixa o lucro
        assertEquals(640.0, somaLucro(lista), 0.001)
    }

    @Test
    fun `receita por categoria agrupa e esconde as que valem zero`() {
        assertEquals(
            listOf("Motores" to 1080.0, "Cabos" to 1000.0),
            receitaPorCategoria(lista)
        )
    }

    @Test
    fun `top3 devolve no maximo tres, do maior valor para o menor`() {
        assertEquals(listOf(motor, cabo, prejuizo), top3PorValor(lista))
        assertEquals(listOf(motor), top3PorValor(listOf(motor)))
    }
}
