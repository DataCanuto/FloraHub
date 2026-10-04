package florahub.backend.App.plant;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PlantIdentificationRepository extends JpaRepository<PlantIdentification, Long> {
}
