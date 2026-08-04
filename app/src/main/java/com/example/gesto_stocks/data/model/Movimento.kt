package com.example.gesto_stocks.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Registo de uma entrada ou saída de stock.
 * A chave estrangeira liga cada movimento ao produto correspondente.
 */
@Entity(
    tableName = "movimentos",
    foreignKeys = [
        ForeignKey(
            entity = Produto::class,
            parentColumns = ["id"],
            childColumns = ["produtoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("produtoId")]
)
data class Movimento(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val produtoId: Int,
    val nomeProduto: String,
    val tipo: String,
    val quantidade: Int,
    val motivo: String = "",
    val utilizador: String = "",
    val dataHora: Long = System.currentTimeMillis()
)