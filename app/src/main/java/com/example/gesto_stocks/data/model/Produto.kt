package com.example.gesto_stocks.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// O SKU identifica o produto no armazém: dois registos com o mesmo SKU são
// sempre um engano, por isso é a base de dados a recusá-los
@Entity(
    tableName = "produtos",
    indices = [Index(value = ["sku"], unique = true)]
)
data class Produto(
    @PrimaryKey(autoGenerate = true)

    val id: Int=0,
    val nome: String,
    val sku: String,
    val categoria: String,
    val fornecedor: String ="",
    val preco: Double = 0.0,
    val precoCusto: Double = 0.0,
    val quantidade: Int,
    val stockMinimo: Int,
    val imagemPath: String? = null
)
