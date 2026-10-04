package florahub.backend.App.recommendation;

public record AmbienteDTO(
        Double temperatura,
        Double umidade,
        Boolean chuva,
        Double velocidadeVento,
        Double indiceUV,
        String descricaoClima
) {
}
