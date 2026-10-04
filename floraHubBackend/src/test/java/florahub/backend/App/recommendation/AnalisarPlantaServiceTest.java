package florahub.backend.App.recommendation;

import florahub.backend.App.care.Planta;
import florahub.backend.App.care.PlantaService;
import florahub.backend.App.plant.PlantIdentification;
import florahub.backend.App.plant.PlantIdentificationService;
import florahub.backend.App.weather.WeatherService;
import florahub.backend.App.weather.WeatherUnavailableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalisarPlantaServiceTest {

    @Mock
    private PlantIdentificationService plantIdentificationService;

    @Mock
    private PlantaService plantaService;

    @Mock
    private WeatherService weatherService;

    @Mock
    private RecomendacaoService recomendacaoService;

    @Mock
    private AnalisePlantaRepository analisePlantaRepository;

    @InjectMocks
    private AnalisarPlantaService analisarPlantaService;

    @Test
    void continuaRetornandoIdentificacaoECuidadosQuandoClimaIndisponivel() {
        String imageUrl = "https://example.com/planta.jpg";

        PlantIdentification identificacao = new PlantIdentification();
        identificacao.setId(1L);
        identificacao.setScientificName("Monstera deliciosa");
        identificacao.setPopularName("Costela-de-adão");

        Planta planta = new Planta();
        planta.setId(10L);

        when(plantIdentificationService.identifyFromImageUrl(imageUrl)).thenReturn(identificacao);
        when(plantaService.obterOuCriar("Monstera deliciosa", "Costela-de-adão", null, null, null))
                .thenReturn(planta);
        when(plantaService.obterCuidados(10L)).thenReturn(null);
        when(weatherService.obterCondicaoAtual(-12.97, -38.50))
                .thenThrow(new WeatherUnavailableException("API de clima indisponível"));
        when(analisePlantaRepository.save(org.mockito.ArgumentMatchers.any(AnalisePlanta.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AnaliseRespostaDTO resposta = analisarPlantaService.analisar(
                new AnalisarPlantaRequest(imageUrl, -12.97, -38.50));

        assertThat(resposta.identificacao()).isNotNull();
        assertThat(resposta.identificacao().nomeCientifico()).isEqualTo("Monstera deliciosa");
        assertThat(resposta.ambiente()).isNull();
        assertThat(resposta.recomendacoes()).isEmpty();
        assertThat(resposta.status()).isNull();
        assertThat(resposta.aviso()).isNotBlank();

        verifyNoInteractions(recomendacaoService);
        verify(analisePlantaRepository).save(org.mockito.ArgumentMatchers.any(AnalisePlanta.class));
    }
}
