package florahub.backend.App.weather;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class WeatherService {

    private static final List<Integer> CODIGOS_CHUVA_MIN = List.of(200, 300, 500, 600, 700);
    private static final List<Integer> CODIGOS_CHUVA_MAX = List.of(299, 399, 599, 699, 799);

    private final RestClient restClient;
    private final CondicaoAmbientalRepository repository;
    private final String apiKey;
    private final long cacheMinutes;

    public WeatherService(CondicaoAmbientalRepository repository,
                           @Value("${openweather.api.key:}") String apiKey,
                           @Value("${openweather.api.url}") String baseUrl,
                           @Value("${openweather.cache.minutes:30}") long cacheMinutes) {
        this.repository = repository;
        this.apiKey = apiKey;
        this.cacheMinutes = cacheMinutes;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    public CondicaoAmbiental obterCondicaoAtual(double latitude, double longitude) {
        double lat = arredondar(latitude);
        double lon = arredondar(longitude);

        Instant desde = Instant.now().minus(Duration.ofMinutes(cacheMinutes));
        return repository.findTopByLatitudeAndLongitudeAndConsultadoEmAfterOrderByConsultadoEmDesc(lat, lon, desde)
                .orElseGet(() -> consultarEArmazenar(lat, lon));
    }

    private CondicaoAmbiental consultarEArmazenar(double lat, double lon) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new WeatherUnavailableException("OPENWEATHER_API_KEY não configurada.");
        }

        OpenWeatherResponse response;
        try {
            response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/weather")
                            .queryParam("lat", lat)
                            .queryParam("lon", lon)
                            .queryParam("appid", apiKey)
                            .queryParam("units", "metric")
                            .queryParam("lang", "pt_br")
                            .build())
                    .retrieve()
                    .body(OpenWeatherResponse.class);
        } catch (Exception e) {
            throw new WeatherUnavailableException("Falha ao consultar a API de clima: " + e.getMessage(), e);
        }

        if (response == null || response.main() == null) {
            throw new WeatherUnavailableException("Resposta inválida da API de clima.");
        }

        CondicaoAmbiental condicao = new CondicaoAmbiental();
        condicao.setLatitude(lat);
        condicao.setLongitude(lon);
        condicao.setTemperaturaAtual(response.main().temp());
        condicao.setUmidadeAtual(response.main().humidity());
        condicao.setVelocidadeVento(response.wind() != null ? response.wind().speed() : null);
        condicao.setPrecipitacaoMm(response.rain() != null ? response.rain().oneHour() : null);

        Integer codigoClima = response.weather() != null && !response.weather().isEmpty()
                ? response.weather().get(0).id() : null;
        condicao.setDescricaoClima(response.weather() != null && !response.weather().isEmpty()
                ? response.weather().get(0).description() : null);
        condicao.setChovendoAgora(isCondicaoDeChuva(codigoClima));
        condicao.setConsultadoEm(Instant.now());

        return repository.save(condicao);
    }

    private boolean isCondicaoDeChuva(Integer codigoClima) {
        if (codigoClima == null) {
            return false;
        }
        for (int i = 0; i < CODIGOS_CHUVA_MIN.size(); i++) {
            if (codigoClima >= CODIGOS_CHUVA_MIN.get(i) && codigoClima <= CODIGOS_CHUVA_MAX.get(i)) {
                return true;
            }
        }
        return false;
    }

    private double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
