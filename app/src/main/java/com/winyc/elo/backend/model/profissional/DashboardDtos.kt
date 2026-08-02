package com.winyc.elo.backend.model.profissional

data class ProfissionalDashboardRS(
    val profissionalId: Long? = null,
    val nome: String? = null,
    val modoProfissionalAtivo: Boolean? = null,
    val novosOrcamentos: Long = 0,
    val servicosHoje: Long = 0,
    val ganhosMes: GanhosMesRS? = null,
    val concluidosMes: ConcluidosMesRS? = null,
    val avaliacao: AvaliacaoResumoRS? = null,
    val taxaRespostaPercentual: Double? = null,
    val ganhosSemana: GanhosSemanaRS? = null,
    val servicosMaisFeitos: List<ServicoMaisFeitoRS> = emptyList(),
)

data class GanhosMesRS(
    val valor: Double? = null,
    val valorMesAnterior: Double? = null,
    val variacaoPercentual: Double? = null,
)

data class ConcluidosMesRS(
    val quantidade: Long = 0,
    val quantidadeMesAnterior: Long = 0,
    val variacaoPercentual: Double? = null,
)

data class AvaliacaoResumoRS(
    val media: Double? = null,
    val quantidade: Int = 0,
    val notaMaxima: Int = 5,
)

data class GanhosSemanaRS(
    val valor: Double? = null,
    val valorSemanaAnterior: Double? = null,
    val variacaoPercentual: Double? = null,
    val dias: List<GanhoDiaRS> = emptyList(),
)

data class GanhoDiaRS(
    val data: String? = null,
    val diaSemana: String? = null,
    val valor: Double? = null,
)

data class ServicoMaisFeitoRS(
    val categoriaEspecificaId: Long? = null,
    val nome: String? = null,
    val quantidade: Long = 0,
    val percentualDoMaisFeito: Double? = null,
)
