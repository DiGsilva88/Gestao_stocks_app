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

    val produtosEmAlerta: LiveData<List<Produto>> = dao.listarEmAlerta()

    val stockTotal: LiveData<Int> = produtos.map { lista ->
        lista.sumOf { it.quantidade }
    }

    val emAlerta: LiveData<Int> = produtos.map { lista ->
        lista.count { it.quantidade < it.stockMinimo }
    }

    val receitaTotal: LiveData<Double> = produtos.map { lista ->
        lista.sumOf { it.preco * it.quantidade }
    }

    val lucroTotal: LiveData<Double> = produtos.map { lista ->
        lista.sumOf { (it.preco - it.precoCusto) * it.quantidade }
    }

    // Receita agrupada por categoria, da maior para a menor
    val vendasPorCategoria: LiveData<List<Pair<String, Double>>> = produtos.map { lista ->
        lista.groupBy { it.categoria }
            .map { (categoria, ps) -> categoria to ps.sumOf { it.preco * it.quantidade } }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
    }

    val topProdutos: LiveData<List<Produto>> = produtos.map { lista ->
        lista.sortedByDescending { it.preco * it.quantidade }.take(3)
    }
}