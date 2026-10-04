package florahub.backend.App.recommendation;

import florahub.backend.App.care.CuidadosPlanta;
import florahub.backend.App.weather.CondicaoAmbiental;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RegraVento implements RegraRecomendacao {

    private static final double VENTO_FORTE_MS = 11.0;

    @Override
    public Optional<Recomendacao> avaliar(CuidadosPlanta cuidados, CondicaoAmbiental ambiente) {
        Double velocidade = ambiente.getVelocidadeVento();
        if (velocidade == null) {
            return Optional.empty();
        }

        if (velocidade >= VENTO_FORTE_MS) {
            return Optional.of(new Recomendacao(CategoriaRecomendacao.VENTO, NivelRecomendacao.ATENCAO,
                    "Vento forte (%.1f m/s). Se a planta estiver ao ar livre, proteja-a ou a leve para um local abrigado."
                            .formatted(velocidade)));
        }
        return Optional.of(new Recomendacao(CategoriaRecomendacao.VENTO, NivelRecomendacao.NORMAL,
                "Vento dentro de níveis normais (%.1f m/s).".formatted(velocidade)));
    }
}
