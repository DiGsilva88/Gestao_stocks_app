package com.example.gesto_stocks.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.gesto_stocks.data.model.Movimento

/** Consultas à tabela de movimentos de stock. */
@Dao
interface MovimentoDao {

    @Query("SELECT * FROM movimentos ORDER BY dataHora DESC")
    fun listarTodos(): LiveData<List<Movimento>>

    @Query("SELECT * FROM movimentos ORDER BY dataHora DESC LIMIT :limite")
    fun listarRecentes(limite: Int = 10): LiveData<List<Movimento>>

    @Query("SELECT * FROM movimentos WHERE produtoId = :id ORDER BY dataHora DESC")
    fun listarPorProduto(id: Int): LiveData<List<Movimento>>

    @Insert
    suspend fun inserir(movimento: Movimento)
}