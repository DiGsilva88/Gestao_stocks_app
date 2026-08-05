package com.example.gesto_stocks

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gesto_stocks.data.local.StockifyDatabase
import com.example.gesto_stocks.data.model.Utilizador
import com.example.gesto_stocks.databinding.ActivityRegistoBinding
import com.example.gesto_stocks.util.Sessao
import com.example.gesto_stocks.util.hashPassword
import kotlinx.coroutines.launch

class RegistoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dao = StockifyDatabase.obter(this).utilizadorDao()
        val sessao = Sessao(this)

        binding.btnRegistar.setOnClickListener {
            if (!validar()) return@setOnClickListener

            val nome = binding.editNome.text.toString().trim()
            val email = binding.editEmail.text.toString().trim().lowercase()
            val password = binding.editPassword.text.toString()

            lifecycleScope.launch {
                if (dao.buscarPorEmail(email) != null) {
                    erro(binding.editEmail, "Já existe uma conta com este email")
                    return@launch
                }

                val id = dao.inserir(
                    Utilizador(
                        nome = nome,
                        email = email,
                        passwordHash = hashPassword(password)
                    )
                )

                sessao.guardarUtilizador(id.toInt())
                startActivity(Intent(this@RegistoActivity, MainActivity::class.java))
                finishAffinity()
            }
        }

        binding.txtIrLogin.setOnClickListener { finish() }
    }

    private fun validar(): Boolean {
        val nome = binding.editNome.text.toString().trim()
        val email = binding.editEmail.text.toString().trim()
        val password = binding.editPassword.text.toString()
        val confirmar = binding.editConfirmar.text.toString()

        // Cada erro é marcado no campo que o causou, para que o utilizador
        // (e o leitor de ecrã) saiba exatamente o que corrigir
        if (nome.isEmpty()) return erro(binding.editNome, "Indica o teu nome")
        if (email.isEmpty()) return erro(binding.editEmail, "Indica o teu email")
        if (!email.contains("@") || !email.contains(".")) {
            return erro(binding.editEmail, "Email inválido")
        }
        if (password.length < 6) {
            return erro(binding.editPassword, "Precisa de pelo menos 6 caracteres")
        }
        if (password != confirmar) {
            return erro(binding.editConfirmar, "As passwords não coincidem")
        }
        return true
    }

    /** Marca o erro no campo e devolve false para travar a validação. */
    private fun erro(campo: EditText, texto: String): Boolean {
        campo.error = texto
        campo.requestFocus()
        return false
    }
}