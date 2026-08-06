package com.example.gesto_stocks.ui.inicio

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.gesto_stocks.data.local.StockifyDatabase
import com.example.gesto_stocks.data.model.Produto

/*
 * As contas do dashboard são funções de topo e puras: sem Android, sem
 * LiveData, testáveis sem emulador. O ViewModel limita-se a ligá-las à
 * lista que vem da base de dados.
 */

fun somaStock(lista: List<Produto>): Int = lista.sumOf { it.quantidade }

fun contarEmAlerta(lista: List<Produto>): Int = lista.count { it.quantidade < it.stockMinimo }

fun somaReceita(lista: List<Produto>): Double = lista.sumOf { it.preco * it.quantidade }

fun somaLucro(lista: List<Produto>): Double =
    lista.sumOf { (it.preco - it.precoCusto) * it.quantidade }

/** Receita agrupada por categoria, da maior para a menor. */
fun receitaPorCategoria(lista: List<Produto>): List<Pair<String, Double>> =
    lista.groupBy { it.categoria }
        .map { (categoria, ps) -> categoria to ps.sumOf { it.preco * it.quantidade } }
        .filter { it.second > 0 }
        .sortedByDescending { it.second }

fun top3PorValor(lista: List<Produto>): List<Produto> =
    lista.sortedByDescending { it.preco * it.quantidade }.take(3)

class InicioViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = StockifyDatabase.obter(app).produtoDao()

    val produtos: LiveData<List<Produto>> = dao.listarTodos()

    val stockTotal: LiveData<Int> = produtos.map(::somaStock)

    val emAlerta: LiveData<Int> = produtos.map(::contarEmAlerta)

    val receitaTotal: LiveData<Double> = produtos.map(::somaReceita)

    val lucroTotal: LiveData<Double> = produtos.map(::somaLucro)

    val vendasPorCategoria: LiveData<List<Pair<String, Double>>> =
        produtos.map(::receitaPorCategoria)

    val topProdutos: LiveData<List<Produto>> = produtos.map(::top3PorValor)

    private val movDao = StockifyDatabase.obter(app).movimentoDao()

    val movimentosRecentes = movDao.listarRecentes(10)
}
