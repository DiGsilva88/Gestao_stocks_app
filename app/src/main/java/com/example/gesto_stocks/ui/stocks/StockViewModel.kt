package com.example.gesto_stocks.ui.stocks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.gesto_stocks.data.model.Produto

class StockViewModel: ViewModel()
{
//    dados ficticios
    private val todos = listOf(
    Produto(1, "Motor Elétrico X200",      "SKU-1042", "Motores",  quantidade = 84,  stockMinimo = 20),
    Produto(2, "Sensor de Proximidade S3", "SKU-2087", "Sensores", quantidade = 12,  stockMinimo = 25),
    Produto(3, "Cabo Blindado 4mm",        "SKU-3391", "Cabos",    quantidade = 0,   stockMinimo = 50),
    Produto(4, "Filtro Industrial F9",     "SKU-4120", "Filtros",  quantidade = 156, stockMinimo = 40),
    Produto(5, "Válvula Pneumática V7",    "SKU-5205", "Válvulas", quantidade = 8,   stockMinimo = 15)

    )

    private val _produtos = MutableLiveData(todos)
    val produtos: LiveData<List<Produto>> = _produtos

    private var textoAtual = ""
    private var categoriaAtual = "Todos"

    fun pesquisar(texto: String) {
        textoAtual = texto
        aplicarFiltros()
    }

    fun filtrarCategoria(categoria: String) {
        categoriaAtual = categoria
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        _produtos.value = todos.filter { pr ->
            val correspondeTexto = textoAtual.isBlank() ||
                    pr.nome.contains(textoAtual, ignoreCase = true) ||
                    pr.sku.contains(textoAtual, ignoreCase = true)

            val correspondeCategoria = categoriaAtual == "Todos" ||
                    pr.categoria == categoriaAtual

            correspondeTexto && correspondeCategoria
        }
    }
}