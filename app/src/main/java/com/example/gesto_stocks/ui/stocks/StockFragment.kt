package com.example.gesto_stocks.ui.stocks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gesto_stocks.databinding.FragmentStockBinding

class StockFragment : Fragment() {

    private var _binding: FragmentStockBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StockViewModel by viewModels()

    private val adapter = ProdutoAdapter { produto ->
        // Fase 3: abrir o formulário de edição
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

        binding.editPesquisa.doAfterTextChanged {texto ->
            viewModel.pesquisar(texto.toString())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null   // obrigatório: evita fuga de memória
    }
}