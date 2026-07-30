package com.example.gesto_stocks.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.gesto_stocks.data.model.Produto

@Dao
interface ProdutoDao {

    @Query("SELECT * FROM produtos ORDER BY nome ASC")
    fun listarTodos(): LiveData<List<Produto>>

    @Query("SELECT * FROM produtos WHERE quantidade < stockMinimo ORDER BY quantidade ASC")
    fun listarEmAlerta(): LiveData<List<Produto>>

    @Query("SELECT * FROM produtos WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): Produto?

    @Insert
    suspend fun inserir(produto: Produto)

    @Update
    suspend fun atualizar(produto: Produto)

    @Delete
    suspend fun eliminar(produto: Produto)
}


