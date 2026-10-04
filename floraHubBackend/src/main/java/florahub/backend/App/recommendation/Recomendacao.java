package florahub.backend.App.recommendation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "analise_recomendacoes")
@Getter
@Setter
@NoArgsConstructor
public class Recomendacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "analise_id", nullable = false)
    private AnalisePlanta analise;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaRecomendacao categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NivelRecomendacao nivel;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensagem;

    public Recomendacao(CategoriaRecomendacao categoria, NivelRecomendacao nivel, String mensagem) {
        this.categoria = categoria;
        this.nivel = nivel;
        this.mensagem = mensagem;
    }
}
