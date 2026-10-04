package florahub.backend.App.plant;

import jakarta.validation.constraints.NotBlank;

public record IdentifyPlantRequest(
        @NotBlank(message = "imageUrl é obrigatório") String imageUrl
) {
}
