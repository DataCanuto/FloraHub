package florahub.backend.App.recommendation;

import java.util.List;

public record AnaliseRespostaDTO(
        Long analiseId,
        IdentificacaoDTO identificacao,
        CuidadosDTO cuidados,
        AmbienteDTO ambiente,
        List<RecomendacaoDTO> recomendacoes,
        NivelRecomendacao status,
        String aviso
) {
}
