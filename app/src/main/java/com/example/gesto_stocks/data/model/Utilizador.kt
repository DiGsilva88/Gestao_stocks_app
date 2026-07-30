package com.example.gesto_stocks.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "utilizadores")
data class Utilizador(
    @PrimaryKey(autoGenerate = true)
    val id: Int=0,
    val nome: String,
    val email: String,
    val passwordHash: String,


)
