package florahub.backend.App.recommendation;

import florahub.backend.App.care.CuidadosPlanta;
import florahub.backend.App.weather.CondicaoAmbiental;

import java.util.Optional;

public interface RegraRecomendacao {

    Optional<Recomendacao> avaliar(CuidadosPlanta cuidados, CondicaoAmbiental ambiente);
}
