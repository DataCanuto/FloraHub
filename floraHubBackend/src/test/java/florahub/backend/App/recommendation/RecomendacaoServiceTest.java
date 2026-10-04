package florahub.backend.App.recommendation;

import florahub.backend.App.care.CuidadosPlanta;
import florahub.backend.App.weather.CondicaoAmbiental;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RecomendacaoServiceTest {

    private RecomendacaoService service;

    @BeforeEach
    void setUp() {
        service = new RecomendacaoService(List.of(
                new RegraTemperatura(),
                new RegraUmidade(),
                new RegraRega(),
                new RegraVento(),
                new RegraUV()
        ));
    }

    private CuidadosPlanta cuidadosPadrao() {
        CuidadosPlanta cuidados = new CuidadosPlanta();
        cuidados.setTemperaturaMin(18.0);
        cuidados.setTemperaturaMax(30.0);
        cuidados.setUmidadeMin(60);
        cuidados.setUmidadeMax(80);
        cuidados.setRega("moderada");
        cuidados.setLuminosidadeIdeal("luz indireta");
        return cuidados;
    }

    private CondicaoAmbiental ambientePadrao() {
        CondicaoAmbiental ambiente = new CondicaoAmbiental();
        ambiente.setTemperaturaAtual(24.0);
        ambiente.setUmidadeAtual(70.0);
        ambiente.setVelocidadeVento(2.0);
        ambiente.setChovendoAgora(false);
        return ambiente;
    }

    @Test
    void temperaturaAcimaDoIdealGeraAtencao() {
        CuidadosPlanta cuidados = cuidadosPadrao();
        CondicaoAmbiental ambiente = ambientePadrao();
        ambiente.setTemperaturaAtual(34.0);

        List<Recomendacao> recomendacoes = service.gerar(cuidados, ambiente);

        Recomendacao temperatura = recomendacoes.stream()
                .filter(r -> r.getCategoria() == CategoriaRecomendacao.TEMPERATURA)
                .findFirst().orElseThrow();
        assertThat(temperatura.getNivel()).isEqualTo(NivelRecomendacao.ATENCAO);
    }

    @Test
    void temperaturaAbaixoDoIdealGeraAtencao() {
        CuidadosPlanta cuidados = cuidadosPadrao();
        CondicaoAmbiental ambiente = ambientePadrao();
        ambiente.setTemperaturaAtual(12.0);

        List<Recomendacao> recomendacoes = service.gerar(cuidados, ambiente);

        Recomendacao temperatura = recomendacoes.stream()
                .filter(r -> r.getCategoria() == CategoriaRecomendacao.TEMPERATURA)
                .findFirst().orElseThrow();
        assertThat(temperatura.getNivel()).isEqualTo(NivelRecomendacao.ATENCAO);
    }

    @Test
    void umidadeBaixaGeraAtencao() {
        CuidadosPlanta cuidados = cuidadosPadrao();
        CondicaoAmbiental ambiente = ambientePadrao();
        ambiente.setUmidadeAtual(40.0);

        List<Recomendacao> recomendacoes = service.gerar(cuidados, ambiente);

        Recomendacao umidade = recomendacoes.stream()
                .filter(r -> r.getCategoria() == CategoriaRecomendacao.UMIDADE)
                .findFirst().orElseThrow();
        assertThat(umidade.getNivel()).isEqualTo(NivelRecomendacao.ATENCAO);
    }

    @Test
    void chuvaPrevistaComRegaModeradaEvitaRegarHoje() {
        CuidadosPlanta cuidados = cuidadosPadrao();
        CondicaoAmbiental ambiente = ambientePadrao();
        ambiente.setChovendoAgora(true);

        List<Recomendacao> recomendacoes = service.gerar(cuidados, ambiente);

        Recomendacao rega = recomendacoes.stream()
                .filter(r -> r.getCategoria() == CategoriaRecomendacao.REGA)
                .findFirst().orElseThrow();
        assertThat(rega.getNivel()).isEqualTo(NivelRecomendacao.NORMAL);
        assertThat(rega.getMensagem()).containsIgnoringCase("chuva");
    }

    @Test
    void condicoesNormaisGeramStatusNormal() {
        CuidadosPlanta cuidados = cuidadosPadrao();
        CondicaoAmbiental ambiente = ambientePadrao();

        List<Recomendacao> recomendacoes = service.gerar(cuidados, ambiente);
        NivelRecomendacao status = service.calcularStatusGeral(recomendacoes);

        assertThat(status).isEqualTo(NivelRecomendacao.NORMAL);
        assertThat(recomendacoes).isNotEmpty();
        assertThat(recomendacoes).allMatch(r -> r.getNivel() == NivelRecomendacao.NORMAL);
    }
}
