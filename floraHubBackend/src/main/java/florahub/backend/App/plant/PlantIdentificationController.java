package florahub.backend.App.plant;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/plants")
public class PlantIdentificationController {

    private final PlantIdentificationService service;
    private final PlantIdentificationRepository repository;

    public PlantIdentificationController(PlantIdentificationService service, PlantIdentificationRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    @PostMapping("/identify")
    public ResponseEntity<PlantIdentification> identify(@Valid @RequestBody IdentifyPlantRequest request) {
        PlantIdentification saved = service.identifyFromImageUrl(request.imageUrl());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public List<PlantIdentification> list() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlantIdentification> get(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
