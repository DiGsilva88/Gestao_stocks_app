package com.example.gesto_stocks.ui.stocks

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import com.example.gesto_stocks.data.local.StockifyDatabase
import com.example.gesto_stocks.data.model.Produto
import kotlinx.coroutines.launch

const val CATEGORIA_TODOS = "Todos"

/**
 * Aplica pesquisa, filtro de categoria e ordenação à lista de produtos.
 * É função de topo e sem dependências do Android para poder ser testada
 * sem emulador — o ViewModel só lhe passa o estado dos controlos.
 *
 * @param ordenacao 0 nome, 1 quantidade, 2 valor em stock.
 */
fun filtrarOrdenar(
    lista: List<Produto>,
    texto: String,
    categoria: String,
    ordenacao: Int
): List<Produto> {
    val filtrada = lista.filter { pr ->
        val correspondeTexto = texto.isBlank() ||
                pr.nome.contains(texto, ignoreCase = true) ||
                pr.sku.contains(texto, ignoreCase = true)

        val correspondeCategoria = categoria == CATEGORIA_TODOS ||
                pr.categoria == categoria

        correspondeTexto && correspondeCategoria
    }

    return when (ordenacao) {
        1 -> filtrada.sortedBy { it.quantidade }
        2 -> filtrada.sortedByDescending { it.preco * it.quantidade }
        else -> filtrada.sortedBy { it.nome.lowercase() }
    }
}

class StockViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = StockifyDatabase.obter(app).produtoDao()

    private val todos: LiveData<List<Produto>> = dao.listarTodos()

    private var textoAtual = ""
    private var categoriaAtual = CATEGORIA_TODOS

    private var ordenacao = 0   // 0 nome, 1 quantidade, 2 valor

    fun ordenar(modo: Int) {
        ordenacao = modo
        aplicarFiltros()
    }

    val produtos = MediatorLiveData<List<Produto>>().apply {
        addSource(todos) { aplicarFiltros() }
    }

    fun pesquisar(texto: String) {
        textoAtual = texto
        aplicarFiltros()
    }

    fun filtrarCategoria(categoria: String) {
        categoriaAtual = categoria
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        val lista = todos.value ?: return
        produtos.value = filtrarOrdenar(lista, textoAtual, categoriaAtual, ordenacao)
    }



    // O SKU tem índice único: o formulário avisa antes, mas se dois écrans
    // gravarem o mesmo SKU ao mesmo tempo a exceção chega aqui e não pode
    // rebentar a aplicação
    fun guardar(produto: Produto) = viewModelScope.launch {
        try {
            if (produto.id == 0) dao.inserir(produto)
            else dao.atualizar(produto)
        } catch (e: SQLiteConstraintException) {
            Log.w("StockViewModel", "SKU duplicado, produto não guardado", e)
        }
    }

    fun eliminar(produto: Produto) = viewModelScope.launch {
        dao.eliminar(produto)
    }

}