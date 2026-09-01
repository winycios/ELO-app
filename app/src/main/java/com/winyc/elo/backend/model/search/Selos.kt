package com.winyc.elo.backend.model.search

import com.winyc.elo.backend.model.estimativa.ProfissionalServicoRS

private const val MIN_COMENTARIOS_PLN = 3
private const val SENTIMENTO_NEUTRO = 0.5
private const val VOLUME_REFERENCIA_AVALIACOES = 50.0

private const val RAIO_MINIMO_KM = 20.0

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


enum class SeloRecomendacao(
    val rotulo: String,
    val justificativa: String,
    val explicacao: String,
) {
    MELHOR_ESCOLHA(
        rotulo = "Melhor escolha",
        justificativa = "Melhor equilíbrio entre reputação, proximidade e experiência.",
        explicacao = "É o profissional com o melhor equilíbrio entre nota, opiniões de " +
            "clientes, distância e serviços concluídos nesta busca. Só um profissional " +
            "recebe este selo de cada vez, e ele precisa ter no mínimo nota " +
            "${textoNumero(MIN_AVALIACAO_MELHOR_ESCOLHA)} e comentários majoritariamente " +
            "positivos.",
    ),
    REQUISITADO(
        rotulo = "Requisitado",
        justificativa = "Um dos mais contratados entre os profissionais desta lista.",
        explicacao = "Está entre os mais contratados desta busca — acima de 3 em cada 4 " +
            "profissionais da lista — com pelo menos $MIN_CONCLUIDOS_REQUISITADO serviços " +
            "concluídos pelo Elo.",
    ),
    PERTO_E_POPULAR(
        rotulo = "Perto e popular",
        justificativa = "Bem avaliado e perto de você.",
        explicacao = "Está a menos de ${textoNumero(DISTANCIA_PERTO_KM)} km do endereço que " +
            "você usa no app, tem nota ${textoNumero(MIN_AVALIACAO_PERTO_E_POPULAR)} ou mais " +
            "e recebeu mais avaliações que a metade dos profissionais desta busca.",
    ),
    TALENTO_DA_REGIAO(
        rotulo = "Talento da região",
        justificativa = "Poucas avaliações ainda, mas com comentários muito positivos.",
        explicacao = "Ainda tem poucas avaliações (até $MAX_AVALIACOES_TALENTO), mas os " +
            "comentários que recebeu são muito positivos e ele atende a sua região. É um " +
            "profissional em começo de trajetória no Elo.",
    ),
}

enum class DimensaoSelo(val rotulo: String, val peso: Double, val descricao: String) {
    REPUTACAO(
        rotulo = "Reputação",
        peso = 0.45,
        descricao = "Nota média, quantidade de avaliações e o tom dos comentários.",
    ),
    EXPERIENCIA(
        rotulo = "Experiência",
        peso = 0.30,
        descricao = "Serviços já concluídos pelo profissional dentro do Elo.",
    ),
    PROXIMIDADE(
        rotulo = "Proximidade",
        peso = 0.25,
        descricao = "Distância até o endereço que você usa no app.",
    );

    val percentual: Int get() = (peso * 100).toInt()
}

data class SeloProfissional(
    val selo: SeloRecomendacao,
    val aspectos: List<String> = emptyList(),
)

data class SelosDaBusca(
    val porProfissional: Map<Long, SeloProfissional> = emptyMap(),
    val classificados: Set<Long> = emptySet(),
) {
    operator fun get(profissionalId: Long): SeloProfissional? = porProfissional[profissionalId]

    companion object {
        val NENHUM = SelosDaBusca()
    }
}

fun selosDaLista(
    profissionais: List<ProfissionalBuscaRS>,
    anteriores: SelosDaBusca = SelosDaBusca.NENHUM,
): SelosDaBusca {
    val elegiveis = profissionais.filter { it.disponivel }
    if (elegiveis.isEmpty()) return SelosDaBusca.NENHUM

    // Uma busca nova não herda selo de outra: só sobrevive quem ainda está na lista.
    val naLista = profissionais.mapTo(mutableSetOf()) { it.profissionalId }
    val mantidos = anteriores.porProfissional.filterKeys { it in naLista }
    val jaClassificados = anteriores.classificados.intersect(naLista)

    val novos = elegiveis.filter { it.profissionalId !in jaClassificados }
    if (novos.isEmpty()) return SelosDaBusca(mantidos, jaClassificados)

    val contexto = ContextoDaLista(elegiveis)

    val melhorEscolha = mantidos.entries
        .firstOrNull { it.value.selo == SeloRecomendacao.MELHOR_ESCOLHA }?.key
        ?: novos.filter { it.podeSerMelhorEscolha() }
            .maxByOrNull { contexto.escore(it) }
            ?.profissionalId

    val atribuidos = novos.mapNotNull { pro ->
        val selo = contexto.seloDe(pro, ehMelhorEscolha = pro.profissionalId == melhorEscolha)
            ?: return@mapNotNull null
        pro.profissionalId to SeloProfissional(selo = selo, aspectos = pro.aspectosDestaque())
    }

    return SelosDaBusca(
        porProfissional = mantidos + atribuidos,
        classificados = jaClassificados + novos.map { it.profissionalId },
    )
}

private class ContextoDaLista(elegiveis: List<ProfissionalBuscaRS>) {
    private val limiteConcluidos =
        percentil(elegiveis.map { it.servicosConcluidos }, PERCENTIL_REQUISITADO)
    private val medianaAvaliacoes = percentil(elegiveis.map { it.quantidadeAvaliacoes }, 0.5)
    private val raioKm = (elegiveis.mapNotNull { it.distanciaKm }.maxOrNull() ?: 0.0)
        .coerceAtLeast(RAIO_MINIMO_KM)
    private val concluidosMax = elegiveis.maxOf { it.servicosConcluidos }.coerceAtLeast(1)

    fun seloDe(pro: ProfissionalBuscaRS, ehMelhorEscolha: Boolean): SeloRecomendacao? = when {
        ehMelhorEscolha -> SeloRecomendacao.MELHOR_ESCOLHA

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
    }

    fun escore(pro: ProfissionalBuscaRS): Double {
        val nota = (pro.avaliacao ?: 0.0) / 5.0
        val volume = (pro.quantidadeAvaliacoes / VOLUME_REFERENCIA_AVALIACOES).coerceAtMost(1.0)
        val reputacao = 0.50 * nota + 0.20 * volume + 0.30 * pro.sentimento()

        // Sem distância (cliente deslogado) a dimensão fica neutra em vez de zerar.
        val proximidade = pro.distanciaKm
            ?.let { 1.0 - (it / raioKm).coerceIn(0.0, 1.0) }
            ?: SENTIMENTO_NEUTRO
        val experiencia = (pro.servicosConcluidos.toDouble() / concluidosMax).coerceIn(0.0, 1.0)

        return DimensaoSelo.REPUTACAO.peso * reputacao +
            DimensaoSelo.PROXIMIDADE.peso * proximidade +
            DimensaoSelo.EXPERIENCIA.peso * experiencia
    }
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

data class DadosDoSelo(
    val avaliacao: Double? = null,
    val quantidadeAvaliacoes: Int = 0,
    val servicosConcluidos: Int = 0,
    val distanciaKm: Double? = null,
    val comentariosProcessados: Int = 0,
    val percentualPositivo: Double? = null,
    val pontosFortes: List<String> = emptyList(),
) {
    /** Só cita o PLN quando há comentários suficientes para ele ser confiável. */
    val temReputacaoConfiavel: Boolean get() = comentariosProcessados >= MIN_COMENTARIOS_PLN

    val aspectos: List<String>
        get() = if (temReputacaoConfiavel) {
            pontosFortes.filter { it.isNotBlank() }.take(ASPECTOS_NA_JUSTIFICATIVA)
        } else {
            emptyList()
        }
}

sealed interface CriterioSelo {
    data class Nota(val valor: Double, val avaliacoes: Int) : CriterioSelo
    data class Opinioes(val percentualPositivo: Double?, val comentarios: Int) : CriterioSelo
    data class Contratacoes(val concluidos: Int) : CriterioSelo
    data class Proximidade(val km: Double) : CriterioSelo
    data class Elogios(val aspectos: List<String>) : CriterioSelo
}

fun criteriosDoSelo(selo: SeloRecomendacao, dados: DadosDoSelo): List<CriterioSelo> {
    val nota = dados.avaliacao
        ?.takeIf { it > 0.0 && dados.quantidadeAvaliacoes > 0 }
        ?.let { CriterioSelo.Nota(it, dados.quantidadeAvaliacoes) }
    val opinioes = dados.takeIf { it.temReputacaoConfiavel }
        ?.let { CriterioSelo.Opinioes(it.percentualPositivo, it.comentariosProcessados) }
    val contratacoes = dados.servicosConcluidos
        .takeIf { it > 0 }
        ?.let { CriterioSelo.Contratacoes(it) }
    val proximidade = dados.distanciaKm?.let { CriterioSelo.Proximidade(it) }
    val elogios = dados.aspectos.takeIf { it.isNotEmpty() }?.let { CriterioSelo.Elogios(it) }

    return when (selo) {
        SeloRecomendacao.MELHOR_ESCOLHA ->
            listOfNotNull(nota, contratacoes, proximidade, opinioes, elogios)

        SeloRecomendacao.REQUISITADO ->
            listOfNotNull(contratacoes, nota, elogios, proximidade)

        SeloRecomendacao.PERTO_E_POPULAR ->
            listOfNotNull(proximidade, nota, elogios)

        SeloRecomendacao.TALENTO_DA_REGIAO ->
            listOfNotNull(opinioes, nota, elogios, proximidade)
    }
}

fun ProfissionalBuscaRS.dadosDoSelo(): DadosDoSelo = DadosDoSelo(
    avaliacao = avaliacao,
    quantidadeAvaliacoes = quantidadeAvaliacoes,
    servicosConcluidos = servicosConcluidos,
    distanciaKm = distanciaKm,
    comentariosProcessados = reputacao?.comentariosProcessados ?: 0,
    percentualPositivo = reputacao?.percentualPositivo,
    pontosFortes = reputacao?.pontosFortes.orEmpty(),
)

fun ProfissionalServicoRS.dadosDoSelo(): DadosDoSelo = DadosDoSelo(
    avaliacao = profissional?.avaliacao ?: resumoAvaliacoes?.media,
    quantidadeAvaliacoes = profissional?.quantidadeAvaliacoes
        ?: resumoAvaliacoes?.quantidade
        ?: 0,
    servicosConcluidos = profissional?.servicosConcluidos ?: 0,
    distanciaKm = profissional?.distanciaKm,
    comentariosProcessados = reputacao?.comentariosProcessados ?: 0,
    percentualPositivo = reputacao?.percentualPositivo ?: resumoAvaliacoes?.percentualPositivas,
    pontosFortes = reputacao?.pontosFortes.orEmpty(),
)

private fun textoNumero(valor: Double): String =
    if (valor % 1.0 == 0.0) valor.toInt().toString() else "%.1f".format(valor).replace('.', ',')
