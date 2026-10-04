package florahub.backend.App.care;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CuidadosPlantaRepository extends JpaRepository<CuidadosPlanta, Long> {

    Optional<CuidadosPlanta> findByPlantaId(Long plantaId);
}
