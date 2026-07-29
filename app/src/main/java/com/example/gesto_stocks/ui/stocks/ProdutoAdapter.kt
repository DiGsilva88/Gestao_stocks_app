package com.example.gesto_stocks.ui.stocks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gesto_stocks.R
import com.example.gesto_stocks.data.model.Produto
import com.example.gesto_stocks.databinding.ItemProdutoBinding

class ProdutoAdapter(
    private val onClick: (Produto) -> Unit
) : ListAdapter<Produto, ProdutoAdapter.ProdutoVH>(DIFF) {

    inner class ProdutoVH(private val b: ItemProdutoBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(p: Produto) {
            val ctx = b.root.context
            b.txtNome.text = p.nome
            b.txtSku.text = "${p.sku} · ${p.categoria}"
            b.txtQuantidade.text = p.quantidade.toString()

            when {
                p.quantidade == 0 -> {
                    b.txtEstado.text = "Esgotado"
                    b.txtEstado.setTextColor(ctx.getColor(R.color.critico))
                }
                p.quantidade < p.stockMinimo -> {
                    b.txtEstado.text = "Baixo"
                    b.txtEstado.setTextColor(ctx.getColor(R.color.aviso))
                }
                else -> {
                    b.txtEstado.text = "OK"
                    b.txtEstado.setTextColor(ctx.getColor(R.color.accent))
                }
            }

            b.root.setOnClickListener { onClick(p) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ProdutoVH(ItemProdutoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ProdutoVH, position: Int) =
        holder.bind(getItem(position))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Produto>() {
            override fun areItemsTheSame(a: Produto, b: Produto) = a.id == b.id
            override fun areContentsTheSame(a: Produto, b: Produto) = a == b
        }
    }
}