package com.example.gesto_stocks.ui.stocks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.gesto_stocks.data.local.StockifyDatabase
import com.example.gesto_stocks.data.model.Produto
import com.example.gesto_stocks.databinding.FragmentProdutoFormBinding
import kotlinx.coroutines.launch

class ProdutoFormFragment : Fragment() {

    private var _binding: FragmentProdutoFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StockViewModel by viewModels()

    private var produtoId = -1
    private var produtoAtual: Produto? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProdutoFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        produtoId = arguments?.getInt("produtoId") ?: -1

        if (produtoId != -1) {
            binding.txtTitulo.text = "Editar produto"
            binding.btnEliminar.visibility = View.VISIBLE
            carregarProduto()
        }


        binding.btnGuardar.setOnClickListener { guardar() }
        binding.btnEliminar.setOnClickListener { confirmarEliminar() }
        binding.btnCancelar.setOnClickListener { findNavController().navigateUp() }
    }

    private fun carregarProduto() {
        val dao = StockifyDatabase.obter(requireContext()).produtoDao()

        lifecycleScope.launch {
            val pr = dao.buscarPorId(produtoId) ?: return@launch
            produtoAtual = pr

            binding.editNome.setText(pr.nome)
            binding.editSku.setText(pr.sku)
            binding.editCategoria.setText(pr.categoria)
            binding.editFornecedor.setText(pr.fornecedor)
            binding.editPreco.setText(pr.preco.toString())
            binding.editQuantidade.setText(pr.quantidade.toString())
            binding.editStockMinimo.setText(pr.stockMinimo.toString())
        }
    }

    private fun guardar() {
        val nome = binding.editNome.text.toString().trim()
        val sku = binding.editSku.text.toString().trim()
        val categoria = binding.editCategoria.text.toString().trim()

        if (nome.isEmpty()) {
            binding.editNome.error = "O nome é obrigatório"
            return
        }
        if (sku.isEmpty()) {
            binding.editSku.error = "O SKU é obrigatório"
            return
        }
        if (categoria.isEmpty()) {
            binding.editCategoria.error = "A categoria é obrigatória"
            return
        }

        val produto = Produto(
            id = produtoId.takeIf { it != -1 } ?: 0,
            nome = nome,
            sku = sku,
            categoria = categoria,
            fornecedor = binding.editFornecedor.text.toString().trim(),
            preco = binding.editPreco.text.toString().toDoubleOrNull() ?: 0.0,
            quantidade = binding.editQuantidade.text.toString().toIntOrNull() ?: 0,
            stockMinimo = binding.editStockMinimo.text.toString().toIntOrNull() ?: 0
        )

        viewModel.guardar(produto)
        Toast.makeText(requireContext(), "Produto guardado", Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }

    private fun confirmarEliminar() {
        val pr = produtoAtual ?: return

        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar produto")
            .setMessage("Esta ação não pode ser desfeita.")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.eliminar(pr)
                findNavController().navigateUp()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}