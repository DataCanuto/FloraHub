package florahub.backend.App.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenWeatherResponse(
        Main main,
        Wind wind,
        List<Weather> weather,
        Rain rain
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Main(Double temp, Double humidity) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Wind(Double speed) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Weather(Integer id, String description) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Rain(@JsonProperty("1h") Double oneHour) {
    }
}
