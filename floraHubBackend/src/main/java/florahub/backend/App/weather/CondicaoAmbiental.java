package florahub.backend.App.weather;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "condicoes_ambientais")
@Getter
@Setter
@NoArgsConstructor
public class CondicaoAmbiental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double latitude;
    private Double longitude;

    private Double temperaturaAtual;
    private Double umidadeAtual;
    private Double precipitacaoMm;

    @Column(name = "indice_uv")
    private Double indiceUV;

    private Double velocidadeVento;
    private String descricaoClima;
    private Boolean chovendoAgora;

    private Instant consultadoEm;
}
