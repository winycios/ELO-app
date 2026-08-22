package com.winyc.elo.backend.model.search

enum class SeloRecomendacao(val rotulo: String, val justificativa: String) {
    MELHOR_ESCOLHA(
        rotulo = "Melhor escolha",
        justificativa = "Melhor equilíbrio entre reputação, proximidade e experiência.",
    ),
    REQUISITADO(
        rotulo = "Requisitado",
        justificativa = "Um dos mais contratados entre os profissionais desta lista.",
    ),
    PERTO_E_POPULAR(
        rotulo = "Perto e popular",
        justificativa = "Bem avaliado e perto de você.",
    ),
    TALENTO_DA_REGIAO(
        rotulo = "Talento da região",
        justificativa = "Poucas contratações ainda, mas com avaliações positivas.",
    ),
}

data class SeloProfissional(
    val selo: SeloRecomendacao,
    val aspectos: List<String> = emptyList(),
)

private const val MIN_COMENTARIOS_PLN = 3
private const val SENTIMENTO_NEUTRO = 0.5
private const val VOLUME_REFERENCIA_AVALIACOES = 50.0
private const val RAIO_REFERENCIA_KM = 50.0

private const val MIN_AVALIACAO_MELHOR_ESCOLHA = 4.0
private const val MIN_SENTIMENTO_MELHOR_ESCOLHA = 0.6
private const val MIN_CONCLUIDOS_REQUISITADO = 10
private const val PERCENTIL_REQUISITADO = 0.75
private const val DISTANCIA_PERTO_KM = 5.0
private const val MIN_AVALIACAO_PERTO_E_POPULAR = 4.0
private const val MAX_AVALIACOES_TALENTO = 10
private const val MIN_SENTIMENTO_TALENTO = 0.7
private const val DISTANCIA_REGIAO_KM = 20.0

private const val ASPECTOS_NA_JUSTIFICATIVA = 2

fun selosDaLista(profissionais: List<ProfissionalBuscaRS>): Map<Long, SeloProfissional> {
    val elegiveis = profissionais.filter { it.disponivel }
    if (elegiveis.isEmpty()) return emptyMap()

    val limiteConcluidos = percentil(elegiveis.map { it.servicosConcluidos }, PERCENTIL_REQUISITADO)
    val medianaAvaliacoes = percentil(elegiveis.map { it.quantidadeAvaliacoes }, 0.5)
    val raio = elegiveis.mapNotNull { it.distanciaKm }.maxOrNull()?.takeIf { it > 0.0 }
        ?: RAIO_REFERENCIA_KM
    val concluidosMax = elegiveis.maxOf { it.servicosConcluidos }.coerceAtLeast(1)

    val melhorEscolha = elegiveis
        .filter { it.podeSerMelhorEscolha() }
        .maxByOrNull { escoreContextual(it, raio, concluidosMax) }
        ?.profissionalId

    return elegiveis.mapNotNull { pro ->
        val selo = when {
            pro.profissionalId == melhorEscolha -> SeloRecomendacao.MELHOR_ESCOLHA

            pro.servicosConcluidos >= MIN_CONCLUIDOS_REQUISITADO &&
                pro.servicosConcluidos > limiteConcluidos -> SeloRecomendacao.REQUISITADO

            pro.distanciaKm != null &&
                pro.distanciaKm <= DISTANCIA_PERTO_KM &&
                (pro.avaliacao ?: 0.0) >= MIN_AVALIACAO_PERTO_E_POPULAR &&
                pro.quantidadeAvaliacoes >= medianaAvaliacoes -> SeloRecomendacao.PERTO_E_POPULAR

            pro.quantidadeAvaliacoes <= MAX_AVALIACOES_TALENTO &&
                pro.sentimento() >= MIN_SENTIMENTO_TALENTO &&
                (pro.distanciaKm == null || pro.distanciaKm <= DISTANCIA_REGIAO_KM) ->
                SeloRecomendacao.TALENTO_DA_REGIAO

            else -> null
        } ?: return@mapNotNull null

        pro.profissionalId to SeloProfissional(selo = selo, aspectos = pro.aspectosDestaque())
    }.toMap()
}

private fun escoreContextual(pro: ProfissionalBuscaRS, raioKm: Double, concluidosMax: Int): Double {
    val nota = (pro.avaliacao ?: 0.0) / 5.0
    val volume = (pro.quantidadeAvaliacoes / VOLUME_REFERENCIA_AVALIACOES).coerceAtMost(1.0)
    val reputacao = 0.50 * nota + 0.20 * volume + 0.30 * pro.sentimento()

    val proximidade = pro.distanciaKm
        ?.let { 1.0 - (it / raioKm).coerceIn(0.0, 1.0) }
        ?: 0.5
    val experiencia = (pro.servicosConcluidos.toDouble() / concluidosMax).coerceIn(0.0, 1.0)

    return 0.45 * reputacao + 0.25 * proximidade + 0.30 * experiencia
}

private fun ProfissionalBuscaRS.podeSerMelhorEscolha(): Boolean =
    (avaliacao ?: 0.0) >= MIN_AVALIACAO_MELHOR_ESCOLHA &&
        temReputacaoConfiavel() &&
        sentimento() >= MIN_SENTIMENTO_MELHOR_ESCOLHA

private fun ProfissionalBuscaRS.sentimento(): Double =
    if (temReputacaoConfiavel()) reputacao?.sentimentoMedio ?: SENTIMENTO_NEUTRO
    else SENTIMENTO_NEUTRO

private fun ProfissionalBuscaRS.temReputacaoConfiavel(): Boolean =
    (reputacao?.comentariosProcessados ?: 0) >= MIN_COMENTARIOS_PLN

private fun ProfissionalBuscaRS.aspectosDestaque(): List<String> =
    if (temReputacaoConfiavel()) {
        reputacao?.pontosFortes.orEmpty().filter { it.isNotBlank() }.take(ASPECTOS_NA_JUSTIFICATIVA)
    } else {
        emptyList()
    }

private fun percentil(valores: List<Int>, fracao: Double): Int {
    if (valores.isEmpty()) return 0
    val ordenados = valores.sorted()
    val indice = ((ordenados.size - 1) * fracao).toInt().coerceIn(0, ordenados.lastIndex)
    return ordenados[indice]
}
