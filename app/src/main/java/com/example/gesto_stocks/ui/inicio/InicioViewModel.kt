package com.example.gesto_stocks.ui.inicio

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.gesto_stocks.data.local.StockifyDatabase
import com.example.gesto_stocks.data.model.Produto

class InicioViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = StockifyDatabase.obter(app).produtoDao()

    val produtos: LiveData<List<Produto>> = dao.listarTodos()

    val stockTotal: LiveData<Int> = produtos.map { lista ->
        lista.sumOf { it.quantidade }
    }

    val numProdutos: LiveData<Int> = produtos.map { it.size }

    val emAlerta: LiveData<Int> = produtos.map { lista ->
        lista.count { it.quantidade < it.stockMinimo }
    }

    val valorTotal: LiveData<Double> = produtos.map { lista ->
        lista.sumOf { it.preco * it.quantidade }
    }
}