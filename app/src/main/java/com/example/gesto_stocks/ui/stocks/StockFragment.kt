package com.example.gesto_stocks.ui.stocks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gesto_stocks.R
import com.example.gesto_stocks.databinding.FragmentStockBinding
import com.google.android.material.chip.Chip

class StockFragment : Fragment() {

    private var _binding: FragmentStockBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StockViewModel by viewModels()

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

        binding.recyclerProdutos.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerProdutos.adapter = adapter

        viewModel.produtos.observe(viewLifecycleOwner) { lista ->
            adapter.submitList(lista)
        }

        binding.editPesquisa.doAfterTextChanged { texto ->
            viewModel.pesquisar(texto.toString())
        }

        binding.chipCategorias.setOnCheckedStateChangeListener { grupo, ids ->
            val categoria = if (ids.isEmpty()) {
                "Todos"
            } else {
                grupo.findViewById<Chip>(ids.first()).text.toString()
            }
            viewModel.filtrarCategoria(categoria)
        }

        binding.fabAdicionar.setOnClickListener {
            findNavController().navigate(R.id.acaoStockParaForm)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}