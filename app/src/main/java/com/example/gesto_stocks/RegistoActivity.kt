package com.example.gesto_stocks

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gesto_stocks.databinding.ActivityRegistoBinding

class RegistoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegistar.setOnClickListener {
            if (validar()) {

                // Dia 11: gravar com o UtilizadorDao e guardar a sessão
                aviso("Dados válidos. Falta gravar na base de dados.")
            }
        }

        binding.txtIrLogin.setOnClickListener {
            finish()
        }
    }

    private fun validar(): Boolean {
        val nome = binding.editNome.text.toString().trim()
        val email = binding.editEmail.text.toString().trim()
        val password = binding.editPassword.text.toString()
        val confirmar = binding.editConfirmar.text.toString()

        if (nome.isEmpty() || email.isEmpty() || password.isEmpty()) {
            aviso("Preenche todos os campos")
            return false
        }
        if (!email.contains("@") || !email.contains(".")) {
            aviso("Email inválido")
            return false
        }
        if (password.length < 6) {
            aviso("A password precisa de pelo menos 6 caracteres")
            return false
        }
        if (password != confirmar) {
            aviso("As passwords não coincidem")
            return false
        }
        return true
    }

    private fun aviso(texto: String) {
        Toast.makeText(this, texto, Toast.LENGTH_SHORT).show()
    }
}