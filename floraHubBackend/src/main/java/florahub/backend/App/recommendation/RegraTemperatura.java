package florahub.backend.App.recommendation;

import florahub.backend.App.care.CuidadosPlanta;
import florahub.backend.App.weather.CondicaoAmbiental;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RegraTemperatura implements RegraRecomendacao {

    @Override
    public Optional<Recomendacao> avaliar(CuidadosPlanta cuidados, CondicaoAmbiental ambiente) {
        Double atual = ambiente.getTemperaturaAtual();
        Double min = cuidados.getTemperaturaMin();
        Double max = cuidados.getTemperaturaMax();
        if (atual == null || min == null || max == null) {
            return Optional.empty();
        }

        if (atual > max) {
            return Optional.of(new Recomendacao(CategoriaRecomendacao.TEMPERATURA, NivelRecomendacao.ATENCAO,
                    "A temperatura atual (%.1f°C) está acima da faixa ideal (%.0f–%.0f°C). Mantenha a planta em local ventilado e evite sol direto nas horas mais quentes."
                            .formatted(atual, min, max)));
        }
        if (atual < min) {
            return Optional.of(new Recomendacao(CategoriaRecomendacao.TEMPERATURA, NivelRecomendacao.ATENCAO,
                    "A temperatura atual (%.1f°C) está abaixo da faixa ideal (%.0f–%.0f°C). Proteja a planta do frio e evite correntes de ar."
                            .formatted(atual, min, max)));
        }
        return Optional.of(new Recomendacao(CategoriaRecomendacao.TEMPERATURA, NivelRecomendacao.NORMAL,
                "A temperatura atual (%.1f°C) está dentro da faixa ideal para a planta.".formatted(atual)));
    }
}
