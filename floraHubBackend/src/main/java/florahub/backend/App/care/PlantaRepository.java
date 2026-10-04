package florahub.backend.App.care;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlantaRepository extends JpaRepository<Planta, Long> {

    Optional<Planta> findByNomeCientificoIgnoreCase(String nomeCientifico);
}
