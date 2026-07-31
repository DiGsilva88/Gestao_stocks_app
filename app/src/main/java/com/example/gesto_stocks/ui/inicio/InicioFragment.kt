package com.example.gesto_stocks.ui.inicio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.gesto_stocks.databinding.FragmentInicioBinding
import java.text.NumberFormat
import java.util.Locale

class InicioFragment : Fragment() {

    private var _binding: FragmentInicioBinding? = null
    private val binding get() = _binding!!

    private val viewModel: InicioViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInicioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val moeda = NumberFormat.getCurrencyInstance(Locale("pt", "PT"))

        viewModel.stockTotal.observe(viewLifecycleOwner) {
            binding.txtStockTotal.text = it.toString()
        }

        viewModel.numProdutos.observe(viewLifecycleOwner) {
            binding.txtNumProdutos.text = it.toString()
            binding.txtVazio.visibility = if (it == 0) View.VISIBLE else View.GONE
        }

        viewModel.emAlerta.observe(viewLifecycleOwner) {
            binding.txtEmAlerta.text = it.toString()
        }

        viewModel.valorTotal.observe(viewLifecycleOwner) {
            binding.txtValorTotal.text = moeda.format(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}