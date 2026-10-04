package florahub.backend.App.care;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "cuidados_planta")
@Getter
@Setter
@NoArgsConstructor
public class CuidadosPlanta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "planta_id", nullable = false, unique = true)
    private Long plantaId;

    private String luminosidadeIdeal;
    private String rega;
    private String frequenciaRega;

    private Integer umidadeMin;
    private Integer umidadeMax;

    private Double temperaturaMin;
    private Double temperaturaMax;

    private String tipoSolo;
    private String drenagem;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
