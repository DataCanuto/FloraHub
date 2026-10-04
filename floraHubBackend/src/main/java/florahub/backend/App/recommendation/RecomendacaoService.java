package florahub.backend.App.recommendation;

import florahub.backend.App.care.CuidadosPlanta;
import florahub.backend.App.weather.CondicaoAmbiental;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class RecomendacaoService {

    private final List<RegraRecomendacao> regras;

    public RecomendacaoService(List<RegraRecomendacao> regras) {
        this.regras = regras;
    }

    public List<Recomendacao> gerar(CuidadosPlanta cuidados, CondicaoAmbiental ambiente) {
        return regras.stream()
                .map(regra -> regra.avaliar(cuidados, ambiente))
                .flatMap(Optional::stream)
                .toList();
    }

    public NivelRecomendacao calcularStatusGeral(List<Recomendacao> recomendacoes) {
        return recomendacoes.stream()
                .map(Recomendacao::getNivel)
                .max(Comparator.comparingInt(this::gravidade))
                .orElse(NivelRecomendacao.NORMAL);
    }

    private int gravidade(NivelRecomendacao nivel) {
        return switch (nivel) {
            case NORMAL -> 0;
            case ATENCAO -> 1;
            case CRITICO -> 2;
        };
    }
}
