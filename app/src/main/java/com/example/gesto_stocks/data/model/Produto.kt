package com.example.gesto_stocks.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "produtos")
data class Produto(
    @PrimaryKey(autoGenerate = true)

    val id: Int=0,
    val nome: String,
    val sku: String,
    val categoria: String,
    val fornecedor: String ="",
    val preco: Double = 0.0,
    val quantidade: Int,
    val stockMinimo: Int,
    val imagemPath: String? = null
)
