package com.example.gesto_stocks.ui.perfil

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.gesto_stocks.data.local.StockifyDatabase
import com.example.gesto_stocks.data.model.Utilizador
import com.example.gesto_stocks.databinding.FragmentEditarPerfilBinding
import com.example.gesto_stocks.util.Sessao
import com.example.gesto_stocks.util.hashPassword
import kotlinx.coroutines.launch

/**
 * Permite alterar o nome e a password do utilizador com sessão iniciada.
 * O nome de utilizador não é editável para não invalidar o login.
 */
class EditarPerfilFragment : Fragment() {

    private var _binding: FragmentEditarPerfilBinding? = null
    private val binding get() = _binding!!

    private var utilizador: Utilizador? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditarPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        carregar()

        binding.btnGuardar.setOnClickListener { guardar() }
        binding.btnFechar.setOnClickListener { findNavController().navigateUp() }
    }

    /** Preenche os campos com os dados atuais do utilizador. */
    private fun carregar() {
        val sessao = Sessao(requireContext())
        val dao = StockifyDatabase.obter(requireContext()).utilizadorDao()

        lifecycleScope.launch {
            val u = dao.buscarPorId(sessao.utilizadorId()) ?: return@launch
            utilizador = u
            binding.editNome.setText(u.nome)
            binding.editEmail.setText(u.email)
        }
    }

    /** Valida os dados e grava as alterações na base de dados. */
    private fun guardar() {
        val u = utilizador ?: return

        val nome = binding.editNome.text.toString().trim()
        val atual = binding.editPasswordAtual.text.toString()
        val nova = binding.editPasswordNova.text.toString()
        val confirmar = binding.editPasswordConfirmar.text.toString()

        if (nome.isEmpty()) {
            binding.editNome.error = "O nome é obrigatório"
            return
        }

        // Se os três campos de password estiverem vazios, só o nome é alterado
        val querMudarPassword = atual.isNotEmpty() || nova.isNotEmpty() ||
                confirmar.isNotEmpty()

        var novoHash = u.passwordHash

        if (querMudarPassword) {
            // A password atual é pedida para impedir alterações por alguém
            // que encontre o telemóvel desbloqueado
            if (hashPassword(atual) != u.passwordHash) {
                binding.editPasswordAtual.error = "Password atual incorreta"
                return
            }
            if (nova.length < 5) {
                binding.editPasswordNova.error = "Mínimo de 5 caracteres"
                return
            }
            if (nova != confirmar) {
                binding.editPasswordConfirmar.error = "As passwords não coincidem"
                return
            }
            novoHash = hashPassword(nova)
        }

        val dao = StockifyDatabase.obter(requireContext()).utilizadorDao()

        lifecycleScope.launch {
            dao.atualizar(u.copy(nome = nome, passwordHash = novoHash))

            val msg = if (querMudarPassword) {
                "Perfil e password atualizados"
            } else {
                "Perfil atualizado"
            }
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}