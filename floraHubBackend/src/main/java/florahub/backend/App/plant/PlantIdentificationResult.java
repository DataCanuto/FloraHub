package florahub.backend.App.plant;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record PlantIdentificationResult(

        @JsonPropertyDescription("Nome científico da planta (binômio latino), ex: Epipremnum aureum")
        String scientificName,

        @JsonPropertyDescription("Nome popular mais comum da planta, em português do Brasil")
        String popularName,

        @JsonPropertyDescription("Gênero botânico da planta")
        String genus,

        @JsonPropertyDescription("Espécie botânica da planta")
        String species,

        @JsonPropertyDescription("Continente de origem da planta")
        String originContinent,

        @JsonPropertyDescription("Necessidade de luminosidade da planta, ex: luz indireta, sol pleno, sombra parcial")
        String lightRequirement,

        @JsonPropertyDescription("Frequência e forma de rega recomendada")
        String wateringRequirement,

        @JsonPropertyDescription("true se a planta aparenta estar saudável na foto (sem sinais de pragas, doenças ou folhas murchas/amareladas/secas), false caso contrário")
        boolean healthyAppearance,

        @JsonPropertyDescription("Recomendação de tratamento caso a planta não pareça saudável; deixe vazio se a planta estiver saudável")
        String careRecommendation,

        @JsonPropertyDescription("Confiança da identificação da espécie, de 0.0 a 1.0")
        Double confidence
) {
}
