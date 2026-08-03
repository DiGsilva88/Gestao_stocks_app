package com.example.gesto_stocks.ui.stocks

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.gesto_stocks.data.local.StockifyDatabase
import com.example.gesto_stocks.data.model.Produto
import com.example.gesto_stocks.databinding.FragmentProdutoFormBinding
import com.example.gesto_stocks.util.guardarImagemProduto
import com.example.gesto_stocks.util.mostrarImagemProduto
import com.google.android.material.chip.Chip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Guarda temporariamente os dados de um produto novo que não chegou a ser
 * gravado, para os repor caso o utilizador retroceda e volte ao formulário.
 * O rascunho é limpo quando o produto é guardado com sucesso.
 */
private object Rascunho {
    var nome = ""
    var sku = ""
    var categoria = ""
    var fornecedor = ""
    var preco = ""
    var quantidade = ""
    var stockMinimo = ""
    var imagemPath: String? = null
    var existe = false

    fun limpar() {
        nome = ""; sku = ""; categoria = ""; fornecedor = ""
        preco = ""; quantidade = ""; stockMinimo = ""
        imagemPath = null
        existe = false
    }
}

/**
 * Formulário de criação e edição de produtos.
 * O mesmo ecrã serve os dois casos: quando o argumento produtoId vale -1
 * está em modo criação; caso contrário edita o produto correspondente.
 */
class ProdutoFormFragment : Fragment() {

    private var _binding: FragmentProdutoFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StockViewModel by viewModels()

    private var produtoId = -1
    private var produtoAtual: Produto? = null
    private var imagemPathAtual: String? = null

    private val escolherImagem = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri == null) return@registerForActivityResult

        lifecycleScope.launch {
            imagemPathAtual = withContext(Dispatchers.IO) {
                guardarImagemProduto(requireContext(), uri)
            }
            binding.imgProduto.mostrarImagemProduto(imagemPathAtual)
        }
    }

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
        binding.imgProduto.mostrarImagemProduto(null)

        if (produtoId != -1) {
            // Modo edição: carrega os dados guardados na base de dados
            binding.txtTitulo.text = "Editar Produto"
            binding.btnEliminar.visibility = View.VISIBLE
            carregarProduto()
        } else if (Rascunho.existe) {
            // Modo criação: repõe os dados introduzidos antes de retroceder
            reporRascunho()
        }

        binding.btnGuardar.setOnClickListener { guardar() }
        binding.btnEliminar.setOnClickListener { confirmarEliminar() }
        binding.btnFechar.setOnClickListener { findNavController().navigateUp() }
        binding.btnEscolherImagem.setOnClickListener {
            escolherImagem.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    }

    /** Carrega da base de dados o produto que está a ser editado. */
    private fun carregarProduto() {
        val dao = StockifyDatabase.obter(requireContext()).produtoDao()

        lifecycleScope.launch {
            val pr = dao.buscarPorId(produtoId) ?: return@launch
            produtoAtual = pr
            imagemPathAtual = pr.imagemPath
            binding.imgProduto.mostrarImagemProduto(imagemPathAtual)

            binding.editNome.setText(pr.nome)
            binding.editSku.setText(pr.sku)
            binding.editFornecedor.setText(pr.fornecedor)
            binding.editPreco.setText(pr.preco.toString())
            binding.editQuantidade.setText(pr.quantidade.toString())
            binding.editStockMinimo.setText(pr.stockMinimo.toString())

            selecionarChip(pr.categoria)
        }
    }

    /** Repõe no formulário os dados guardados antes de retroceder. */
    private fun reporRascunho() {
        binding.editNome.setText(Rascunho.nome)
        binding.editSku.setText(Rascunho.sku)
        binding.editFornecedor.setText(Rascunho.fornecedor)
        binding.editPreco.setText(Rascunho.preco)
        binding.editQuantidade.setText(Rascunho.quantidade)
        binding.editStockMinimo.setText(Rascunho.stockMinimo)
        selecionarChip(Rascunho.categoria)
        imagemPathAtual = Rascunho.imagemPath
        binding.imgProduto.mostrarImagemProduto(imagemPathAtual)
    }
//
//    /** Guarda o conteúdo dos campos para o caso de o utilizador voltar. */
    private fun guardarRascunho() {
        Rascunho.nome = binding.editNome.text.toString()
        Rascunho.sku = binding.editSku.text.toString()
        Rascunho.categoria = categoriaEscolhida()
        Rascunho.fornecedor = binding.editFornecedor.text.toString()
        Rascunho.preco = binding.editPreco.text.toString()
        Rascunho.quantidade = binding.editQuantidade.text.toString()
        Rascunho.stockMinimo = binding.editStockMinimo.text.toString()
        Rascunho.imagemPath = imagemPathAtual
        Rascunho.existe = true
    }
//
//    /** Marca o chip correspondente à categoria indicada. */
    private fun selecionarChip(categoria: String) {
        for (i in 0 until binding.chipCategorias.childCount) {
            val chip = binding.chipCategorias.getChildAt(i) as Chip
            if (chip.text.toString() == categoria) {
                chip.isChecked = true
                return
            }
        }
    }

//    /** Devolve o texto do chip de categoria selecionado. */
    private fun categoriaEscolhida(): String {
        val idChip = binding.chipCategorias.checkedChipId
        if (idChip == View.NO_ID) return "Motores"
        return binding.chipCategorias.findViewById<Chip>(idChip).text.toString()
    }
//
//    /** Valida os campos obrigatórios e grava o produto. */
    private fun guardar() {
        val nome = binding.editNome.text.toString().trim()
        val sku = binding.editSku.text.toString().trim()

        if (nome.isEmpty()) {
            binding.editNome.error = "O nome é obrigatório"
            return
        }
        if (sku.isEmpty()) {
            binding.editSku.error = "O SKU é obrigatório"
            return
        }

//         id igual a zero indica ao Room que se trata de uma inserção
        val produto = Produto(
            id = produtoId.takeIf { it != -1 } ?: 0,
            nome = nome,
            sku = sku,
            categoria = categoriaEscolhida(),
            fornecedor = binding.editFornecedor.text.toString().trim(),
            // OrNull evita que a aplicação feche se o campo ficar vazio
            preco = binding.editPreco.text.toString().toDoubleOrNull() ?: 0.0,
            quantidade = binding.editQuantidade.text.toString().toIntOrNull() ?: 0,
            stockMinimo = binding.editStockMinimo.text.toString().toIntOrNull() ?: 0,
            imagemPath = imagemPathAtual
        )

        viewModel.guardar(produto)
        Rascunho.limpar()
        Toast.makeText(requireContext(), "Produto guardado", Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }

//    /** Pede confirmação antes de eliminar, por ser uma ação irreversível. */
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

        // Guarda o rascunho antes de a vista ser destruída, e apenas em modo criação

        if (produtoId == -1) guardarRascunho()

        super.onDestroyView()
        _binding = null
    }
}