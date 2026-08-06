package com.example.gesto_stocks

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gesto_stocks.data.local.StockifyDatabase
import com.example.gesto_stocks.databinding.ActivityLoginBinding
import com.example.gesto_stocks.util.Sessao
import com.example.gesto_stocks.util.passwordCorreta
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dao = StockifyDatabase.obter(this).utilizadorDao()
        val sessao = Sessao(this)

        binding.btnEntrar.setOnClickListener {
            val email = binding.editEmail.text.toString().trim().lowercase()
            val password = binding.editPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                val campo = if (email.isEmpty()) binding.editEmail else binding.editPassword
                erro(campo, getString(R.string.erro_campo_obrigatorio))
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val utilizador = dao.buscarPorEmail(email)

                if (utilizador == null ||
                    !passwordCorreta(password, utilizador.passwordHash)) {
                    erro(binding.editPassword, getString(R.string.erro_credenciais))
                    return@launch
                }

                sessao.guardarUtilizador(utilizador.id)
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            }
        }

        binding.txtIrRegisto.setOnClickListener {
            startActivity(Intent(this, RegistoActivity::class.java))
        }
    }

    /**
     * Marca o erro no próprio campo em vez de um Toast: fica visível enquanto
     * o utilizador corrige e o leitor de ecrã anuncia-o ao focar o campo.
     */
    private fun erro(campo: EditText, texto: String) {
        campo.error = texto
        campo.requestFocus()
    }
}