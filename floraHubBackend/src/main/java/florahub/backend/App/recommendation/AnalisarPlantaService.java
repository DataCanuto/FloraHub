package florahub.backend.App.recommendation;

import florahub.backend.App.care.CuidadosPlanta;
import florahub.backend.App.care.Planta;
import florahub.backend.App.care.PlantaService;
import florahub.backend.App.plant.PlantIdentification;
import florahub.backend.App.plant.PlantIdentificationService;
import florahub.backend.App.weather.CondicaoAmbiental;
import florahub.backend.App.weather.WeatherService;
import florahub.backend.App.weather.WeatherUnavailableException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AnalisarPlantaService {

    private final PlantIdentificationService plantIdentificationService;
    private final PlantaService plantaService;
    private final WeatherService weatherService;
    private final RecomendacaoService recomendacaoService;
    private final AnalisePlantaRepository analisePlantaRepository;

    public AnalisarPlantaService(PlantIdentificationService plantIdentificationService, PlantaService plantaService,
                                  WeatherService weatherService, RecomendacaoService recomendacaoService,
                                  AnalisePlantaRepository analisePlantaRepository) {
        this.plantIdentificationService = plantIdentificationService;
        this.plantaService = plantaService;
        this.weatherService = weatherService;
        this.recomendacaoService = recomendacaoService;
        this.analisePlantaRepository = analisePlantaRepository;
    }

    @Transactional
    public AnaliseRespostaDTO analisar(AnalisarPlantaRequest request) {
        PlantIdentification identificacao = plantIdentificationService.identifyFromImageUrl(request.imageUrl());

        Planta planta = plantaService.obterOuCriar(identificacao.getScientificName(), identificacao.getPopularName(),
                identificacao.getGenus(), identificacao.getSpecies(), identificacao.getOriginContinent());
        CuidadosPlanta cuidados = plantaService.obterCuidados(planta.getId());

        CondicaoAmbiental ambiente = null;
        String aviso = null;

        if (request.latitude() == null || request.longitude() == null) {
            aviso = "Localização não informada; não foi possível calcular recomendações ambientais.";
        } else {
            try {
                ambiente = weatherService.obterCondicaoAtual(request.latitude(), request.longitude());
            } catch (WeatherUnavailableException e) {
                aviso = "Não foi possível obter dados climáticos no momento; exibindo apenas identificação e cuidados.";
            }
        }

        List<Recomendacao> recomendacoesGeradas = List.of();
        if (ambiente != null) {
            if (cuidados != null) {
                recomendacoesGeradas = recomendacaoService.gerar(cuidados, ambiente);
            } else {
                aviso = "Cuidados da espécie ainda não disponíveis; não foi possível gerar recomendações.";
            }
        }

        AnalisePlanta analise = new AnalisePlanta();
        analise.setPlantaId(planta.getId());
        analise.setIdentificationId(identificacao.getId());
        analise.setCondicaoAmbientalId(ambiente != null ? ambiente.getId() : null);
        analise.setImageUrl(request.imageUrl());
        recomendacoesGeradas.forEach(analise::adicionarRecomendacao);
        analise = analisePlantaRepository.save(analise);

        NivelRecomendacao status = recomendacoesGeradas.isEmpty()
                ? null
                : recomendacaoService.calcularStatusGeral(recomendacoesGeradas);

        return new AnaliseRespostaDTO(
                analise.getId(),
                toIdentificacaoDTO(identificacao),
                toCuidadosDTO(cuidados),
                toAmbienteDTO(ambiente),
                recomendacoesGeradas.stream().map(RecomendacaoDTO::from).toList(),
                status,
                aviso
        );
    }

    private IdentificacaoDTO toIdentificacaoDTO(PlantIdentification identificacao) {
        return new IdentificacaoDTO(
                identificacao.getPopularName(),
                identificacao.getScientificName(),
                identificacao.getGenus(),
                identificacao.getSpecies(),
                identificacao.getOriginContinent(),
                identificacao.getHealthyAppearance(),
                identificacao.getCareRecommendation(),
                identificacao.getConfidence()
        );
    }

    private CuidadosDTO toCuidadosDTO(CuidadosPlanta cuidados) {
        if (cuidados == null) {
            return null;
        }
        return new CuidadosDTO(
                cuidados.getLuminosidadeIdeal(),
                cuidados.getRega(),
                cuidados.getFrequenciaRega(),
                cuidados.getUmidadeMin(),
                cuidados.getUmidadeMax(),
                cuidados.getTemperaturaMin(),
                cuidados.getTemperaturaMax(),
                cuidados.getTipoSolo(),
                cuidados.getDrenagem(),
                cuidados.getObservacoes()
        );
    }

    private AmbienteDTO toAmbienteDTO(CondicaoAmbiental ambiente) {
        if (ambiente == null) {
            return null;
        }
        return new AmbienteDTO(
                ambiente.getTemperaturaAtual(),
                ambiente.getUmidadeAtual(),
                ambiente.getChovendoAgora(),
                ambiente.getVelocidadeVento(),
                ambiente.getIndiceUV(),
                ambiente.getDescricaoClima()
        );
    }
}
