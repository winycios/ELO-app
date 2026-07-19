package com.winyc.elo.backend.model.estimativa

/**
 * Detalhes do profissional para estimar um orçamento (`GET /estimativa/...`).
 * A distância só vem preenchida quando o cliente está logado.
 */
data class ProfissionalServicoRS(
    val profissional: ProfissionalDetalhesRS? = null,
    val servicoSelecionado: ServicoOferecidoRS? = null,
    val servicosOferecidos: List<ServicoOferecidoRS> = emptyList(),
    val resumoAvaliacoes: ResumoAvaliacoesRS? = null,
    val ultimasAvaliacoes: List<AvaliacaoRS> = emptyList(),
)

data class ProfissionalDetalhesRS(
    val id: Long,
    val nome: String? = null,
    val fotoPerfil: String? = null,
    val apresentacao: String? = null,
    val especialidades: String? = null,
    val avaliacao: Double? = null,
    val quantidadeAvaliacoes: Int? = null,
    val servicosConcluidos: Int? = null,
    val tempoExperiencia: Int? = null,
    val distanciaKm: Double? = null,
)

data class ServicoOferecidoRS(
    val id: Long,
    val nome: String? = null,
    val valor: Double? = null,
    val descricao: String? = null,
    val pontosPrincipais: String? = null,
    val tipoExecucao: String? = null,
    val tempoExperiencia: Int? = null,
    val categoria: CategoriaEstimativaRS? = null,
    val imagens: List<ImagemRS> = emptyList(),
    val disponibilidades: List<DisponibilidadeRS> = emptyList(),
)

data class CategoriaEstimativaRS(
    val categoriaGeralId: Long? = null,
    val categoriaGeral: String? = null,
    val categoriaEspecificaId: Long? = null,
    val categoriaEspecifica: String? = null,
)

data class ImagemRS(val id: Long? = null, val url: String? = null, val ordem: Int? = null)

data class DisponibilidadeRS(
    val id: Long? = null,
    val diaSemana: Int? = null,
    val horaInicio: String? = null,
    val horaFim: String? = null,
)

data class ResumoAvaliacoesRS(
    val media: Double? = null,
    val quantidade: Int? = null,
    val percentualPositivas: Double? = null,
)

data class AvaliacaoRS(
    val id: Long,
    val avaliadorId: Long? = null,
    val avaliador: String? = null,
    val fotoAvaliador: String? = null,
    val nota: Int? = null,
    val comentario: String? = null,
)

/** Quebra os pontos principais (texto separado por `;` ou `,`) em itens. */
fun ServicoOferecidoRS.pontos(): List<String> =
    pontosPrincipais?.split(';', ',')
        ?.map { it.trim() }
        ?.filter { it.isNotBlank() }
        .orEmpty()
