package com.example.gesto_stocks.util

import com.example.gesto_stocks.data.model.Produto
import java.util.Locale

private const val SEP = ";"

private val CABECALHOS = listOf(
    "ID", "Nome", "SKU", "Categoria", "Fornecedor",
    "Preço", "Quantidade", "Stock mínimo", "Valor", "Estado"
)

fun estadoDe(p: Produto): String = when {
    p.quantidade == 0 -> "Esgotado"
    p.quantidade < p.stockMinimo -> "Baixo"
    else -> "OK"
}

// Um fornecedor chamado "Silva, Lda" partiria a linha em duas colunas
private fun escapar(valor: String): String {
    val limpo = valor.replace("\"", "\"\"")
    val precisaAspas = valor.contains(SEP) ||
            valor.contains("\"") || valor.contains("\n")
    return if (precisaAspas) "\"$limpo\"" else limpo
}

private fun moeda(v: Double) = String.format(Locale("pt", "PT"), "%.2f", v)

fun produtosParaCsv(lista: List<Produto>): String {
    val sb = StringBuilder()

    // Marca UTF-8: sem ela o Excel mostra "Valvula" em vez de "Válvula"
    sb.append("\uFEFF")
    sb.append(CABECALHOS.joinToString(SEP)).append("\r\n")

    for (p in lista) {
        sb.append(
            listOf(
                p.id.toString(),
                escapar(p.nome),
                escapar(p.sku),
                escapar(p.categoria),
                escapar(p.fornecedor),
                moeda(p.preco),
                p.quantidade.toString(),
                p.stockMinimo.toString(),
                moeda(p.preco * p.quantidade),
                estadoDe(p)
            ).joinToString(SEP)
        ).append("\r\n")
    }
    return sb.toString()
}

private fun xml(s: String) = s
    .replace("&", "&amp;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")
    .replace("\"", "&quot;")

private fun texto(v: String) =
    "<Cell><Data ss:Type=\"String\">${xml(v)}</Data></Cell>"

private fun numero(v: Number) =
    "<Cell><Data ss:Type=\"Number\">$v</Data></Cell>"

// O Excel exige ponto decimal em células numéricas, daí o Locale.US
private fun decimal(v: Double) =
    "<Cell><Data ss:Type=\"Number\">" +
            String.format(Locale.US, "%.2f", v) + "</Data></Cell>"

fun produtosParaExcel(lista: List<Produto>): String {
    val sb = StringBuilder()

    sb.append(
        """<?xml version="1.0" encoding="UTF-8"?>
<?mso-application progid="Excel.Sheet"?>
<Workbook xmlns="urn:schemas-microsoft-com:office:spreadsheet"
          xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet">
 <Styles>
  <Style ss:ID="cab">
   <Font ss:Bold="1"/>
   <Interior ss:Color="#D9D9D9" ss:Pattern="Solid"/>
  </Style>
 </Styles>
 <Worksheet ss:Name="Stock">
  <Table>
   <Row>"""
    )

    for (c in CABECALHOS) {
        sb.append("<Cell ss:StyleID=\"cab\">")
            .append("<Data ss:Type=\"String\">").append(xml(c))
            .append("</Data></Cell>")
    }
    sb.append("</Row>")

    for (p in lista) {
        sb.append("<Row>")
            .append(numero(p.id))
            .append(texto(p.nome))
            .append(texto(p.sku))
            .append(texto(p.categoria))
            .append(texto(p.fornecedor))
            .append(decimal(p.preco))
            .append(numero(p.quantidade))
            .append(numero(p.stockMinimo))
            .append(decimal(p.preco * p.quantidade))
            .append(texto(estadoDe(p)))
            .append("</Row>")
    }

    sb.append("</Table></Worksheet></Workbook>")
    return sb.toString()
}