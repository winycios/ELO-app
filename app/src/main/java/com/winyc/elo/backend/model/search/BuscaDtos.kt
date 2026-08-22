package com.winyc.elo.backend.model.search

/** Página de resultados da busca de profissionais (`GET /busca/profissionais`). */
data class BuscaProfissionalRS(
    val profissionais: List<ProfissionalBuscaRS> = emptyList(),
    val total: Long = 0,
    val pagina: Int = 0,
    val tamanho: Int = 0,
)

/**
 * Profissional retornado pela busca. Vários campos são nulos quando o Elastic
 * ainda não indexou aquele dado (ex.: profissional sem avaliação recebe
 * [avaliacao] == null), então a UI precisa tratar cada ausência.
 */
data class ProfissionalBuscaRS(
    val profissionalId: Long,
    val nome: String = "",
    val fotoPerfil: String? = null,
    val avaliacao: Double? = null,
    val quantidadeAvaliacoes: Int = 0,
    val servicosConcluidos: Int = 0,
    val disponivel: Boolean = false,
    val distanciaKm: Double? = null,
    val precoInicial: Double? = null,
    val cidade: String? = null,
    val estado: String? = null,
    val bairro: String? = null,
    val servicos: List<ServicoBuscaRS> = emptyList(),
    val reputacao: ReputacaoBuscaRS? = null,
)

data class ReputacaoBuscaRS(
    val comentariosProcessados: Int? = null,
    val percentualPositivo: Double? = null,
    val sentimentoMedio: Double? = null,
    val pontosFortes: List<String> = emptyList(),
    val pontosFracos: List<String> = emptyList(),
    val resumo: String? = null,
)

data class ServicoBuscaRS(
    val servicoId: Long,
    val categoriaGeralId: Long? = null,
    val categoriaGeral: String? = null,
    val categoriaEspecificaId: Long? = null,
    val categoriaEspecifica: String? = null,
    val descricao: String? = null,
    val preco: Double? = null,
    val tipoExecucao: String? = null,
)

/**
 * Escolhe, entre os serviços do profissional, aquele que motivou o clique do
 * cliente — o da [categoriaId] buscada quando existir, senão o primeiro. É esse
 * `servicoId` que segue para a tela de perfil ("serviço de interesse").
 */
fun ProfissionalBuscaRS.servicoDeInteresse(categoriaId: Long?): Long? {
    val doContexto = categoriaId?.let { alvo ->
        servicos.firstOrNull { it.categoriaGeralId == alvo }
    }
    return (doContexto ?: servicos.firstOrNull())?.servicoId
}

/** Ordenações aceitas pelo endpoint. Os nomes batem com o enum do backend. */
enum class OrdenacaoBusca(val rotulo: String) {
    RECOMENDADOS("Recomendados"),
    AVALIACAO("Avaliação"),
    DISTANCIA("Distância"),
}
