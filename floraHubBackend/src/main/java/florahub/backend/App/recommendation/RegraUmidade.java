package florahub.backend.App.recommendation;

import florahub.backend.App.care.CuidadosPlanta;
import florahub.backend.App.weather.CondicaoAmbiental;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RegraUmidade implements RegraRecomendacao {

    @Override
    public Optional<Recomendacao> avaliar(CuidadosPlanta cuidados, CondicaoAmbiental ambiente) {
        Double atual = ambiente.getUmidadeAtual();
        Integer min = cuidados.getUmidadeMin();
        Integer max = cuidados.getUmidadeMax();
        if (atual == null || min == null || max == null) {
            return Optional.empty();
        }

        if (atual < min) {
            return Optional.of(new Recomendacao(CategoriaRecomendacao.UMIDADE, NivelRecomendacao.ATENCAO,
                    "A umidade do ar atual (%.0f%%) está abaixo do ideal (%d–%d%%). Considere borrifar água nas folhas ou usar um umidificador."
                            .formatted(atual, min, max)));
        }
        if (atual > max) {
            return Optional.of(new Recomendacao(CategoriaRecomendacao.UMIDADE, NivelRecomendacao.ATENCAO,
                    "A umidade do ar atual (%.0f%%) está acima do ideal (%d–%d%%). Garanta boa ventilação para evitar fungos."
                            .formatted(atual, min, max)));
        }
        return Optional.of(new Recomendacao(CategoriaRecomendacao.UMIDADE, NivelRecomendacao.NORMAL,
                "A umidade do ar (%.0f%%) está adequada para a planta.".formatted(atual)));
    }
}
