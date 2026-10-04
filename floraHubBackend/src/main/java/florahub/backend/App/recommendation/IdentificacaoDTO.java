package florahub.backend.App.recommendation;

public record IdentificacaoDTO(
        String nomePopular,
        String nomeCientifico,
        String genero,
        String especie,
        String continenteOrigem,
        Boolean aparenciaSaudavel,
        String recomendacaoTratamento,
        Double confianca
) {
}
