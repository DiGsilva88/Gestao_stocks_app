package com.example.gesto_stocks.data.model

data class Produto(

    val id: Int=0,
    val nome: String,
    val sku: String,
    val categoria: String,
    val fornecedor: String ="",
    val preco: Double = 0.0,
    val quantidade: Int,
    val stockMinimo: Int
)
