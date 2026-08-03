package com.example.gesto_stocks.ui.stocks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gesto_stocks.R
import com.example.gesto_stocks.databinding.FragmentStockBinding
import com.google.android.material.chip.Chip

/**
 * Ecrã de listagem de produtos.
 * Permite pesquisar por nome ou SKU, filtrar por categoria, ordenar a lista
 * e abrir o formulário para criar ou editar um produto.
 */
class StockFragment : Fragment() {

    // O binding é anulável porque a vista de um Fragment pode ser
    // destruída e recriada várias vezes durante o seu ciclo de vida
    private var _binding: FragmentStockBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StockViewModel by viewModels()

    // O clique numa linha leva ao formulário, enviando o id do produto
    // para que o mesmo ecrã saiba que está em modo de edição
    private val adapter = ProdutoAdapter { produto ->
        val args = Bundle().apply { putInt("produtoId", produto.id) }
        findNavController().navigate(R.id.acaoStockParaForm, args)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStockBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // O layoutManager tem de ser definido antes do adapter,
        // caso contrário a lista aparece vazia
        binding.recyclerProdutos.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerProdutos.adapter = adapter

        // A lista vem do Room através de LiveData: qualquer alteração
        // na base de dados atualiza o ecrã automaticamente
        viewModel.produtos.observe(viewLifecycleOwner) { lista ->
            adapter.submitList(lista)

            // Uma lista vazia sem explicação parece uma avaria
            val vazio = lista.isEmpty()
            binding.txtVazio.visibility = if (vazio) View.VISIBLE else View.GONE
            binding.recyclerProdutos.visibility = if (vazio) View.GONE else View.VISIBLE
        }

        binding.editPesquisa.doAfterTextChanged { texto ->
            viewModel.pesquisar(texto.toString())
        }

        // O ChipGroup devolve os ids selecionados; como só permite um,
        // basta ler o primeiro. Sem seleção, mostra todas as categorias
        binding.chipCategorias.setOnCheckedStateChangeListener { grupo, ids ->
            val categoria = if (ids.isEmpty()) {
                "Todos"
            } else {
                grupo.findViewById<Chip>(ids.first()).text.toString()
            }
            viewModel.filtrarCategoria(categoria)
        }

        binding.btnOrdenar.setOnClickListener { mostrarMenuOrdenacao(it) }

        binding.fabAdicionar.setOnClickListener {
            findNavController().navigate(R.id.acaoStockParaForm)
        }
    }

    /** Mostra as opções de ordenação da lista junto ao botão. */
    private fun mostrarMenuOrdenacao(ancora: View) {
        val ctx = android.view.ContextThemeWrapper(requireContext(), R.style.MenuPopup)
        val menu = PopupMenu(ctx, ancora)
        menu.menu.add(0, 0, 0, "Nome (A-Z)")
        menu.menu.add(0, 1, 1, "Quantidade (menor primeiro)")
        menu.menu.add(0, 2, 2, "Valor (maior primeiro)")

        menu.setOnMenuItemClickListener { item ->
            viewModel.ordenar(item.itemId)
            true
        }
        menu.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Sem esta linha o Fragment segura a vista antiga em memória.
        // É a causa mais comum de fugas de memória em aplicações com Fragments
        _binding = null
    }
}