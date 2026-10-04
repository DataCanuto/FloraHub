package florahub.backend.App.weather;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface CondicaoAmbientalRepository extends JpaRepository<CondicaoAmbiental, Long> {

    Optional<CondicaoAmbiental> findTopByLatitudeAndLongitudeAndConsultadoEmAfterOrderByConsultadoEmDesc(
            Double latitude, Double longitude, Instant desde);
}
