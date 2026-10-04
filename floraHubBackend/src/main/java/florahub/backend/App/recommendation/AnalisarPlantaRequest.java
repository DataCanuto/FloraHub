package florahub.backend.App.recommendation;

import jakarta.validation.constraints.NotBlank;

public record AnalisarPlantaRequest(
        @NotBlank(message = "imageUrl é obrigatório") String imageUrl,
        Double latitude,
        Double longitude
) {
}
