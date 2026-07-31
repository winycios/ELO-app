package com.winyc.elo.telas.componentes

import com.winyc.elo.backend.model.orcamento.OrcamentoDetalheRS
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

/* ---------------------------- Valores e notas ---------------------------- */

internal fun formatarBRL(valor: Double?): String? =
    valor?.let { "R$ %,.2f".format(Locale.forLanguageTag("pt-BR"), it) }

internal fun formatarNota(nota: Double): String =
    if (nota % 1.0 == 0.0) nota.toInt().toString() else "%.1f".format(nota).replace('.', ',')

/* ---------------------------- Datas e horários ---------------------------- */

private fun dataHora(iso: String?): LocalDateTime? =
    iso?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() }

internal fun formatarData(iso: String?): String? = dataHora(iso)?.let {
    "%02d/%02d/%d".format(it.dayOfMonth, it.monthValue, it.year)
}

internal fun formatarHora(iso: String?): String? = dataHora(iso)?.let {
    "%02d:%02d".format(it.hour, it.minute)
}

internal fun formatarDataHora(iso: String?): String? {
    val data = formatarData(iso) ?: return null
    return "$data às ${formatarHora(iso)}"
}

internal fun faixaDeHorario(inicioIso: String?, fimIso: String?): String? {
    val inicio = formatarHora(inicioIso) ?: return null
    val fim = formatarHora(fimIso) ?: return inicio
    return "$inicio – $fim"
}

/** Data + faixa de horário: "22/04/2026 · 08:00 – 12:00". */
internal fun dataComFaixaDeHorario(inicioIso: String?, fimIso: String?): String? {
    val data = formatarData(inicioIso) ?: return null
    val faixa = faixaDeHorario(inicioIso, fimIso) ?: return data
    return "$data · $faixa"
}

/** Hora vinda de um horário disponível (só hora, sem data): "09:00". */
internal fun formatarHoraSlot(horaIso: String): String = runCatching {
    val hora = LocalTime.parse(horaIso)
    "%02d:%02d".format(hora.hour, hora.minute)
}.getOrDefault(horaIso)

/** Junta dia (`2026-04-22`) e horário (`09:00`) no ISO local aceito pela API. */
internal fun montarDataHoraIso(dataIso: String, horaIso: String): String = runCatching {
    LocalDateTime.of(LocalDate.parse(dataIso), LocalTime.parse(horaIso)).toString()
}.getOrDefault("${dataIso}T$horaIso")

/** "Há 2h", "Há 3 dias"… a partir de uma data/hora ISO no passado. */
internal fun tempoRelativo(iso: String?): String? {
    val momento = dataHora(iso) ?: return null
    val minutos = Duration.between(momento, LocalDateTime.now()).toMinutes()
    return when {
        minutos < 1 -> "Agora"
        minutos < 60 -> "Há ${minutos}min"
        minutos < 60 * 24 -> "Há ${minutos / 60}h"
        minutos < 60 * 24 * 7 -> pluralizar(minutos / (60 * 24), "dia", "dias")
        minutos < 60 * 24 * 30 -> pluralizar(minutos / (60 * 24 * 7), "semana", "semanas")
        else -> pluralizar(minutos / (60 * 24 * 30), "mês", "meses")
    }
}

private fun pluralizar(quantidade: Long, singular: String, plural: String): String =
    "Há $quantidade ${if (quantidade == 1L) singular else plural}"

/* ---------------------------- Textos do orçamento ---------------------------- */

internal fun rotuloTipoServico(tipo: String?): String? = when (tipo?.trim()?.lowercase()) {
    null, "" -> null
    "presencial" -> "Presencial"
    "remoto" -> "Remoto"
    else -> tipo.replace('_', ' ').replaceFirstChar { it.uppercaseChar() }
}

internal fun formatarDistancia(km: Double?): String? =
    km?.let { "%.1f km".format(Locale.forLanguageTag("pt-BR"), it) }

/** "Rua X, 10 - compl. · Bairro, Cidade - UF" com as partes que vieram preenchidas. */
internal fun enderecoCompleto(endereco: OrcamentoDetalheRS.EnderecoDetalheRS?): String? {
    if (endereco == null) return null
    val logradouro = listOfNotNull(
        endereco.rua?.takeIf { it.isNotBlank() },
        endereco.numero?.toString(),
        endereco.complemento?.takeIf { it.isNotBlank() },
    ).joinToString(", ")
    val cidade = listOfNotNull(
        endereco.bairro?.takeIf { it.isNotBlank() },
        endereco.cidade?.takeIf { it.isNotBlank() },
    ).joinToString(", ")
    val estado = endereco.estado?.takeIf { it.isNotBlank() }?.let { " - $it" }.orEmpty()
    val texto = listOf(logradouro, cidade + estado).filter { it.isNotBlank() }.joinToString(" · ")
    return texto.takeIf { it.isNotBlank() }
}

/* ---------------------------- Telefone ---------------------------- */

internal fun somenteDigitos(telefone: String?): String = telefone?.filter { it.isDigit() }.orEmpty()

/** (11) 98467-5735 quando vier só com dígitos; caso contrário devolve como está. */
internal fun formatarTelefone(telefone: String): String {
    val digitos = somenteDigitos(telefone)
    return when (digitos.length) {
        11 -> "(${digitos.take(2)}) ${digitos.substring(2, 7)}-${digitos.substring(7)}"
        10 -> "(${digitos.take(2)}) ${digitos.substring(2, 6)}-${digitos.substring(6)}"
        else -> telefone
    }
}

private val LOCALE_BR: Locale = Locale.forLanguageTag("pt-BR")
internal val DIAS_DA_SEMANA = listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom")


/** "Agosto 2026". */

internal fun mesEAno(dia: LocalDate): String {
    val mes = dia.month.getDisplayName(TextStyle.FULL, LOCALE_BR)
        .replaceFirstChar { it.uppercaseChar() }
    return "$mes ${dia.year}"
}

/** "Qui, 6 de agosto". */
internal fun diaPorExtenso(dia: LocalDate): String {
    val diaSemana = DIAS_DA_SEMANA[dia.dayOfWeek.value - 1]
    val mes = dia.month.getDisplayName(TextStyle.FULL, LOCALE_BR).lowercase(LOCALE_BR)
    return "$diaSemana, ${dia.dayOfMonth} de $mes"
}
