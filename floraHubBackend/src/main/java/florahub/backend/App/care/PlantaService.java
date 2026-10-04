package florahub.backend.App.care;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class PlantaService {

    private static final String SYSTEM_PROMPT = """
            Você é um especialista em botânica. Com base apenas no nome científico informado,
            forneça o perfil de cuidados ideais dessa espécie, respondendo em português do Brasil.
            Faça sua melhor estimativa mesmo quando não tiver certeza absoluta.
            """;

    private final ChatClient chatClient;
    private final PlantaRepository plantaRepository;
    private final CuidadosPlantaRepository cuidadosPlantaRepository;

    public PlantaService(ChatClient.Builder chatClientBuilder, PlantaRepository plantaRepository,
                          CuidadosPlantaRepository cuidadosPlantaRepository) {
        this.chatClient = chatClientBuilder.build();
        this.plantaRepository = plantaRepository;
        this.cuidadosPlantaRepository = cuidadosPlantaRepository;
    }

    public Planta obterOuCriar(String nomeCientifico, String nomePopular, String genero, String especie,
                                String continenteOrigem) {
        return plantaRepository.findByNomeCientificoIgnoreCase(nomeCientifico)
                .orElseGet(() -> criarPlantaComCuidados(nomeCientifico, nomePopular, genero, especie, continenteOrigem));
    }

    public CuidadosPlanta obterCuidados(Long plantaId) {
        return cuidadosPlantaRepository.findByPlantaId(plantaId).orElse(null);
    }

    private Planta criarPlantaComCuidados(String nomeCientifico, String nomePopular, String genero, String especie,
                                           String continenteOrigem) {
        Planta planta = new Planta();
        planta.setNomeCientifico(nomeCientifico);
        planta.setNomePopular(nomePopular);
        planta.setGenero(genero);
        planta.setEspecie(especie);
        planta.setContinenteOrigem(continenteOrigem);
        planta = plantaRepository.save(planta);

        PlantCareProfileResult perfil = buscarPerfilDeCuidados(nomeCientifico, nomePopular);
        if (perfil != null) {
            CuidadosPlanta cuidados = new CuidadosPlanta();
            cuidados.setPlantaId(planta.getId());
            cuidados.setLuminosidadeIdeal(perfil.luminosidadeIdeal());
            cuidados.setRega(perfil.rega());
            cuidados.setFrequenciaRega(perfil.frequenciaRega());
            cuidados.setUmidadeMin(perfil.umidadeMin());
            cuidados.setUmidadeMax(perfil.umidadeMax());
            cuidados.setTemperaturaMin(perfil.temperaturaMin());
            cuidados.setTemperaturaMax(perfil.temperaturaMax());
            cuidados.setTipoSolo(perfil.tipoSolo());
            cuidados.setDrenagem(perfil.drenagem());
            cuidados.setObservacoes(perfil.observacoes());
            cuidadosPlantaRepository.save(cuidados);
        }

        return planta;
    }

    private PlantCareProfileResult buscarPerfilDeCuidados(String nomeCientifico, String nomePopular) {
        try {
            return chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user("Espécie: %s (nome popular: %s). Informe o perfil de cuidados ideais."
                            .formatted(nomeCientifico, nomePopular == null ? "desconhecido" : nomePopular))
                    .call()
                    .entity(PlantCareProfileResult.class);
        } catch (Exception e) {
            return null;
        }
    }
}
