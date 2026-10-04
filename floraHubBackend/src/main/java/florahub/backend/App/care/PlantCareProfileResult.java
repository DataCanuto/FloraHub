package florahub.backend.App.care;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record PlantCareProfileResult(

        @JsonPropertyDescription("Necessidade de luminosidade ideal da espécie, ex: luz indireta intensa, sol pleno, sombra parcial")
        String luminosidadeIdeal,

        @JsonPropertyDescription("Nível de rega ideal da espécie, ex: baixa, moderada, alta")
        String rega,

        @JsonPropertyDescription("Frequência de rega recomendada, ex: a cada 5-7 dias")
        String frequenciaRega,

        @JsonPropertyDescription("Umidade relativa do ar mínima ideal, em porcentagem (0-100)")
        Integer umidadeMin,

        @JsonPropertyDescription("Umidade relativa do ar máxima ideal, em porcentagem (0-100)")
        Integer umidadeMax,

        @JsonPropertyDescription("Temperatura mínima ideal, em graus Celsius")
        Double temperaturaMin,

        @JsonPropertyDescription("Temperatura máxima ideal, em graus Celsius")
        Double temperaturaMax,

        @JsonPropertyDescription("Tipo de solo ideal, ex: bem drenado, rico em matéria orgânica")
        String tipoSolo,

        @JsonPropertyDescription("Nível de drenagem necessário, ex: boa drenagem, drenagem moderada")
        String drenagem,

        @JsonPropertyDescription("Observações adicionais relevantes sobre o cultivo da espécie")
        String observacoes
) {
}
