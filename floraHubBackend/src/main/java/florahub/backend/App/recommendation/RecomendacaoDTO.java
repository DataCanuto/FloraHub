package florahub.backend.App.recommendation;

public record RecomendacaoDTO(
        CategoriaRecomendacao categoria,
        NivelRecomendacao nivel,
        String mensagem
) {
    public static RecomendacaoDTO from(Recomendacao recomendacao) {
        return new RecomendacaoDTO(recomendacao.getCategoria(), recomendacao.getNivel(), recomendacao.getMensagem());
    }
}
