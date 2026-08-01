package com.example.gesto_stocks.ui.stocks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import com.example.gesto_stocks.data.local.StockifyDatabase
import com.example.gesto_stocks.data.model.Produto
import kotlinx.coroutines.launch

class StockViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = StockifyDatabase.obter(app).produtoDao()

    private val todos: LiveData<List<Produto>> = dao.listarTodos()

    private var textoAtual = ""
    private var categoriaAtual = "Todos"

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

        val filtrada = lista.filter { pr ->
            val correspondeTexto = textoAtual.isBlank() ||
                    pr.nome.contains(textoAtual, ignoreCase = true) ||
                    pr.sku.contains(textoAtual, ignoreCase = true)

            val correspondeCategoria = categoriaAtual == "Todos" ||
                    pr.categoria == categoriaAtual

            correspondeTexto && correspondeCategoria
        }

        produtos.value = when (ordenacao) {
            1 -> filtrada.sortedBy { it.quantidade }
            2 -> filtrada.sortedByDescending { it.preco * it.quantidade }
            else -> filtrada.sortedBy { it.nome.lowercase() }
        }
    }



    fun guardar(produto: Produto) = viewModelScope.launch {
        if (produto.id == 0) dao.inserir(produto)
        else dao.atualizar(produto)
    }

    fun eliminar(produto: Produto) = viewModelScope.launch {
        dao.eliminar(produto)
    }

}