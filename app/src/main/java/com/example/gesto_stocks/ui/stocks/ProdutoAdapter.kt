package com.example.gesto_stocks.ui.stocks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gesto_stocks.R
import com.example.gesto_stocks.data.model.Produto
import com.example.gesto_stocks.databinding.ItemProdutoBinding
import com.example.gesto_stocks.util.mostrarImagemProduto
import java.text.NumberFormat
import java.util.Locale



///**
// * Adapter da lista de produtos.
// * Preenche cada linha com os dados de um produto e calcula o estado do stock.
// */
class ProdutoAdapter(
    private val onClick: (Produto) -> Unit,
    private val onMovimento: (Produto) -> Unit
) : ListAdapter<Produto, ProdutoAdapter.ProdutoVH>(DIFF) {

        // Formata valores em euros com o separador decimal português
        private val moeda: NumberFormat =
            NumberFormat.getCurrencyInstance(Locale("pt", "PT"))
    inner class ProdutoVH(private val b: ItemProdutoBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(p: Produto) {
            val ctx = b.root.context

            b.txtNome.text = p.nome
            b.txtSku.text = "${p.sku} · ${p.categoria} · ${moeda.format(p.preco)}"
            b.txtQuantidade.text = p.quantidade.toString()

            when {
                p.quantidade == 0 -> {
                    b.txtEstado.text = "Esgotado"
                    b.txtEstado.setTextColor(ctx.getColor(R.color.critico))
                    b.txtQuantidade.setTextColor(ctx.getColor(R.color.critico))
                }
                p.quantidade < p.stockMinimo -> {
                    b.txtEstado.text = "Baixo"
                    b.txtEstado.setTextColor(ctx.getColor(R.color.aviso))
                    b.txtQuantidade.setTextColor(ctx.getColor(R.color.aviso))
                }
                else -> {
                    b.txtEstado.text = "OK"
                    b.txtEstado.setTextColor(ctx.getColor(R.color.accent))
                    b.txtQuantidade.setTextColor(ctx.getColor(R.color.accent))
                }
            }

            b.root.setOnClickListener { onClick(p) }
            b.root.setOnLongClickListener {
                onMovimento(p)
                true
            }
            b.btnEditar.setOnClickListener { onClick(p) }
            b.btnMovimento.setOnClickListener { onMovimento(p) }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ProdutoVH(ItemProdutoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ProdutoVH, position: Int) =
        holder.bind(getItem(position))


    // Compara listas para atualizar apenas as linhas que mudaram

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Produto>() {
            override fun areItemsTheSame(a: Produto, b: Produto) = a.id == b.id
            override fun areContentsTheSame(a: Produto, b: Produto) = a == b
        }
    }
}