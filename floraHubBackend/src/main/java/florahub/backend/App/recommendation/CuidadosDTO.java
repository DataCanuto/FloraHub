package florahub.backend.App.recommendation;

public record CuidadosDTO(
        String luminosidadeIdeal,
        String rega,
        String frequenciaRega,
        Integer umidadeMin,
        Integer umidadeMax,
        Double temperaturaMin,
        Double temperaturaMax,
        String tipoSolo,
        String drenagem,
        String observacoes
) {
}
