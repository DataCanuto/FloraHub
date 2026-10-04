package florahub.backend.App.recommendation;

import florahub.backend.App.care.CuidadosPlanta;
import florahub.backend.App.weather.CondicaoAmbiental;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RegraRega implements RegraRecomendacao {

    private static final double UMIDADE_BAIXA_LIMITE = 40.0;

    @Override
    public Optional<Recomendacao> avaliar(CuidadosPlanta cuidados, CondicaoAmbiental ambiente) {
        String rega = cuidados.getRega();
        if (rega == null || rega.isBlank()) {
            return Optional.empty();
        }

        boolean chovendo = Boolean.TRUE.equals(ambiente.getChovendoAgora());
        if (chovendo) {
            return Optional.of(new Recomendacao(CategoriaRecomendacao.REGA, NivelRecomendacao.NORMAL,
                    "Há chuva no momento, então não é necessário regar hoje."));
        }

        boolean regaAlta = rega.toLowerCase().contains("alta");
        Double umidadeAtual = ambiente.getUmidadeAtual();
        if (regaAlta && umidadeAtual != null && umidadeAtual < UMIDADE_BAIXA_LIMITE) {
            return Optional.of(new Recomendacao(CategoriaRecomendacao.REGA, NivelRecomendacao.ATENCAO,
                    "A planta necessita de rega %s e a umidade do ar está baixa (%.0f%%). Aumente a atenção à hidratação."
                            .formatted(rega, umidadeAtual)));
        }

        return Optional.of(new Recomendacao(CategoriaRecomendacao.REGA, NivelRecomendacao.NORMAL,
                "Sem chuva no momento; mantenha a rotina de rega %s recomendada para a espécie.".formatted(rega)));
    }
}
