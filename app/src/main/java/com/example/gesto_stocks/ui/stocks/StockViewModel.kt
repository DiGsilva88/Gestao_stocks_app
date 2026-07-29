package com.example.gesto_stocks.ui.stocks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.gesto_stocks.data.model.Produto

class StockViewModel: ViewModel()
{
//    dados ficticios
    private val todos = listOf(
    Produto(2, "Sensor de Proximidade S3", "SKU-2087", "Sensores", quantidade = 12,  stockMinimo = 25),
    Produto(3, "Cabo Blindado 4mm",        "SKU-3391", "Cabos",    quantidade = 0,   stockMinimo = 50),
    Produto(4, "Filtro Industrial F9",     "SKU-4120", "Filtros",  quantidade = 156, stockMinimo = 40),
    Produto(5, "Válvula Pneumática V7",    "SKU-5205", "Válvulas", quantidade = 8,   stockMinimo = 15)

    )

    private val _produtos = MutableLiveData(todos)
    val produtos: LiveData<List<Produto>> = _produtos

    fun pesquisar(texto: String) {
        _produtos.value = if (texto.isBlank()){
            todos
        } else {
            todos.filter {
                it.nome.contains(texto, ignoreCase = true) ||
                        it.sku.contains(texto, ignoreCase = true)
            }
        }
    }


}