package florahub.backend.App.recommendation;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/plantas")
public class AnalisarPlantaController {

    private final AnalisarPlantaService service;

    public AnalisarPlantaController(AnalisarPlantaService service) {
        this.service = service;
    }

    @PostMapping("/analisar")
    public AnaliseRespostaDTO analisar(@Valid @RequestBody AnalisarPlantaRequest request) {
        return service.analisar(request);
    }
}
