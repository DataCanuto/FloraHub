package florahub.backend.App.plant;

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
@Table(name = "plant_identifications")
@Getter
@Setter
@NoArgsConstructor
public class PlantIdentification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String imageUrl;

    private String scientificName;
    private String popularName;
    private String species;
    private String genus;
    private String originContinent;

    @Column(length = 500)
    private String lightRequirement;

    @Column(length = 500)
    private String wateringRequirement;

    private Boolean healthyAppearance;

    @Column(columnDefinition = "TEXT")
    private String careRecommendation;

    private Double confidence;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
