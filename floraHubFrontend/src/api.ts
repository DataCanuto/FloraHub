export interface IdentificacaoDTO {
  nomePopular: string | null
  nomeCientifico: string | null
  genero: string | null
  especie: string | null
  continenteOrigem: string | null
  aparenciaSaudavel: boolean | null
  recomendacaoTratamento: string | null
  confianca: number | null
}

export interface CuidadosDTO {
  luminosidadeIdeal: string | null
  rega: string | null
  frequenciaRega: string | null
  umidadeMin: number | null
  umidadeMax: number | null
  temperaturaMin: number | null
  temperaturaMax: number | null
  tipoSolo: string | null
  drenagem: string | null
  observacoes: string | null
}

export interface AmbienteDTO {
  temperatura: number | null
  umidade: number | null
  chuva: boolean | null
  velocidadeVento: number | null
  indiceUV: number | null
  descricaoClima: string | null
}

export type NivelRecomendacao = 'NORMAL' | 'ATENCAO' | 'CRITICO'

export interface RecomendacaoDTO {
  categoria: string
  nivel: NivelRecomendacao
  mensagem: string
}

export interface AnaliseResposta {
  analiseId: number
  identificacao: IdentificacaoDTO
  cuidados: CuidadosDTO | null
  ambiente: AmbienteDTO | null
  recomendacoes: RecomendacaoDTO[]
  status: NivelRecomendacao | null
  aviso: string | null
}

export interface AnalisarPlantaPayload {
  imageUrl: string
  latitude?: number
  longitude?: number
}

export async function analisarPlanta(payload: AnalisarPlantaPayload): Promise<AnaliseResposta> {
  const response = await fetch('/api/plantas/analisar', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })

  if (!response.ok) {
    const body = await response.json().catch(() => null)
    const message = body?.error ?? `Falha ao analisar a planta (HTTP ${response.status}).`
    throw new Error(message)
  }

  return response.json()
}
