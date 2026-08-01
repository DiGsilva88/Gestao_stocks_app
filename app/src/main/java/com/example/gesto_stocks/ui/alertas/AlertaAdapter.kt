package com.example.gesto_stocks.ui.alertas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gesto_stocks.R
import com.example.gesto_stocks.data.model.Produto
import com.example.gesto_stocks.databinding.ItemAlertaBinding

class AlertaAdapter(
    private val onClick: (Produto) -> Unit
) : ListAdapter<Produto, AlertaAdapter.AlertaVH>(DIFF) {

    inner class AlertaVH(private val b: ItemAlertaBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(pr: Produto) {
            val ctx = b.root.context

            b.txtNome.text = pr.nome
            b.txtCategoria.text = pr.categoria
            b.txtQuantidade.text = pr.quantidade.toString()
            b.txtMinimo.text = "Mín: ${pr.stockMinimo}"

            val esgotado = pr.quantidade == 0
            val cor = ctx.getColor(
                if (esgotado) R.color.critico else R.color.aviso
            )

            b.txtQuantidade.setTextColor(cor)
            b.txtEstado.setTextColor(cor)
            b.txtEstado.text = if (esgotado) "Esgotado" else "Baixo"
            b.barraNivel.progressTintList =
                android.content.res.ColorStateList.valueOf(cor)

            val percentagem = if (pr.stockMinimo > 0) {
                (pr.quantidade * 100 / pr.stockMinimo).coerceAtMost(100)
            } else 100

            b.barraNivel.progress = percentagem

            b.root.setOnClickListener { onClick(pr) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        AlertaVH(ItemAlertaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: AlertaVH, position: Int) =
        holder.bind(getItem(position))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Produto>() {
            override fun areItemsTheSame(a: Produto, b: Produto) = a.id == b.id
            override fun areContentsTheSame(a: Produto, b: Produto) = a == b
        }
    }
}