package florahub.backend.App.recommendation;

import florahub.backend.App.care.CuidadosPlanta;
import florahub.backend.App.weather.CondicaoAmbiental;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RegraUV implements RegraRecomendacao {

    private static final double UV_ALTO = 6.0;

    @Override
    public Optional<Recomendacao> avaliar(CuidadosPlanta cuidados, CondicaoAmbiental ambiente) {
        Double indiceUV = ambiente.getIndiceUV();
        if (indiceUV == null) {
            // O provedor de clima atual não fornece índice UV no plano gratuito; regra fica pronta
            // para quando esse dado estiver disponível.
            return Optional.empty();
        }

        String luminosidade = cuidados.getLuminosidadeIdeal();
        boolean prefereSombra = luminosidade != null
                && (luminosidade.toLowerCase().contains("sombra") || luminosidade.toLowerCase().contains("indireta"));

        if (indiceUV >= UV_ALTO && prefereSombra) {
            return Optional.of(new Recomendacao(CategoriaRecomendacao.UV, NivelRecomendacao.ATENCAO,
                    "Índice UV alto (%.1f) e a planta prefere luz indireta/sombra. Evite exposição direta ao sol no período mais intenso."
                            .formatted(indiceUV)));
        }
        return Optional.of(new Recomendacao(CategoriaRecomendacao.UV, NivelRecomendacao.NORMAL,
                "Índice UV atual (%.1f) não representa risco para a planta.".formatted(indiceUV)));
    }
}
