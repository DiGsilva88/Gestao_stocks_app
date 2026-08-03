package com.example.gesto_stocks.util

import android.content.Context
import android.content.res.ColorStateList
import android.net.Uri
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.example.gesto_stocks.R
import java.io.File
import java.util.UUID

/** Copia a imagem escolhida pelo utilizador para o armazenamento interno da app. */
fun guardarImagemProduto(context: Context, uri: Uri): String {
    val pasta = File(context.filesDir, "produtos").apply { mkdirs() }
    val ficheiro = File(pasta, "${UUID.randomUUID()}.jpg")

    context.contentResolver.openInputStream(uri)?.use { entrada ->
        ficheiro.outputStream().use { saida -> entrada.copyTo(saida) }
    }

    return ficheiro.absolutePath
}

/** Mostra a foto do produto, ou um ícone genérico quando não existe nenhuma. */
fun ImageView.mostrarImagemProduto(path: String?) {
    if (path != null && File(path).exists()) {
        imageTintList = null
        scaleType = ImageView.ScaleType.CENTER_CROP
        setImageURI(Uri.fromFile(File(path)))
    } else {
        // O ícone (24dp) fica naturalmente pequeno e centrado na caixa maior;
        // o drawable tem tint preto embutido, por isso é preciso sobrepor a cor
        imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.text_muted))
        scaleType = ImageView.ScaleType.CENTER_INSIDE
        setImageResource(R.drawable.ic_produtos)
    }
}
