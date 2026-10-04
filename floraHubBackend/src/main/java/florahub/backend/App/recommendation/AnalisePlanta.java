package florahub.backend.App.recommendation;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "analises_planta")
@Getter
@Setter
@NoArgsConstructor
public class AnalisePlanta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "planta_id", nullable = false)
    private Long plantaId;

    @Column(name = "identification_id", nullable = false)
    private Long identificationId;

    @Column(name = "condicao_ambiental_id")
    private Long condicaoAmbientalId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String imageUrl;

    @OneToMany(mappedBy = "analise", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Recomendacao> recomendacoes = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public void adicionarRecomendacao(Recomendacao recomendacao) {
        recomendacao.setAnalise(this);
        recomendacoes.add(recomendacao);
    }
}
