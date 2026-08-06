package com.example.gesto_stocks

import com.example.gesto_stocks.data.model.Produto
import com.example.gesto_stocks.ui.stocks.CATEGORIA_TODOS
import com.example.gesto_stocks.ui.stocks.filtrarOrdenar
import org.junit.Assert.assertEquals
import org.junit.Test

class FiltrosTest {

    private val motor = Produto(id = 1, nome = "Motor Elétrico", sku = "SKU-1042",
        categoria = "Motores", preco = 100.0, quantidade = 5, stockMinimo = 2)
    private val cabo = Produto(id = 2, nome = "Cabo Blindado", sku = "SKU-3391",
        categoria = "Cabos", preco = 10.0, quantidade = 50, stockMinimo = 10)
    private val sensor = Produto(id = 3, nome = "Sensor S3", sku = "SKU-2087",
        categoria = "Sensores", preco = 40.0, quantidade = 1, stockMinimo = 5)

    private val lista = listOf(motor, cabo, sensor)

    @Test
    fun `sem filtros devolve tudo por nome`() {
        assertEquals(
            listOf(cabo, motor, sensor),
            filtrarOrdenar(lista, "", CATEGORIA_TODOS, 0)
        )
    }

    @Test
    fun `pesquisa por nome ignora maiusculas`() {
        assertEquals(listOf(motor), filtrarOrdenar(lista, "motor", CATEGORIA_TODOS, 0))
        assertEquals(listOf(motor), filtrarOrdenar(lista, "ELÉTRICO", CATEGORIA_TODOS, 0))
    }

    @Test
    fun `pesquisa tambem apanha o sku`() {
        assertEquals(listOf(sensor), filtrarOrdenar(lista, "2087", CATEGORIA_TODOS, 0))
    }

    @Test
    fun `filtro de categoria`() {
        assertEquals(listOf(cabo), filtrarOrdenar(lista, "", "Cabos", 0))
        assertEquals(emptyList<Produto>(), filtrarOrdenar(lista, "", "Válvulas", 0))
    }

    @Test
    fun `pesquisa e categoria acumulam`() {
        assertEquals(emptyList<Produto>(), filtrarOrdenar(lista, "motor", "Cabos", 0))
    }

    @Test
    fun `ordenacao por quantidade e crescente`() {
        assertEquals(
            listOf(sensor, motor, cabo),
            filtrarOrdenar(lista, "", CATEGORIA_TODOS, 1)
        )
    }

    @Test
    fun `ordenacao por valor em stock e decrescente`() {
        // motor 500, cabo 500, sensor 40 — o maior valor primeiro
        assertEquals(sensor, filtrarOrdenar(lista, "", CATEGORIA_TODOS, 2).last())
    }

    @Test
    fun `lista vazia nao rebenta`() {
        assertEquals(emptyList<Produto>(), filtrarOrdenar(emptyList(), "motor", "Cabos", 2))
    }
}
