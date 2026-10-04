import { useRef, useState } from 'preact/hooks'
import './app.css'
import { analisarPlanta, type AnaliseResposta } from './api'
import { fileToResizedDataUrl } from './imageInput'

type Screen = 'upload' | 'loading' | 'result' | 'error'
type UploadMode = 'url' | 'galeria' | 'camera'
type Tab = 'sobre' | 'cuidados' | 'clima'
type LocationStatus = 'idle' | 'requesting' | 'granted' | 'denied'

export function App() {
  const [screen, setScreen] = useState<Screen>('upload')
  const [mode, setMode] = useState<UploadMode>('url')
  const [imageUrl, setImageUrl] = useState('')
  const [previewSrc, setPreviewSrc] = useState<string | null>(null)
  const [coords, setCoords] = useState<{ latitude: number; longitude: number } | null>(null)
  const [locationStatus, setLocationStatus] = useState<LocationStatus>('idle')
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [result, setResult] = useState<AnaliseResposta | null>(null)
  const [analyzedImage, setAnalyzedImage] = useState<string | null>(null)
  const [activeTab, setActiveTab] = useState<Tab>('sobre')

  const galeriaInputRef = useRef<HTMLInputElement>(null)
  const cameraInputRef = useRef<HTMLInputElement>(null)

  const displaySrc = mode === 'url' ? (imageUrl.trim() || null) : previewSrc
  const canAnalyze = mode === 'url' ? imageUrl.trim().length > 0 : previewSrc !== null

  async function onFileSelected(e: Event, selectedMode: UploadMode) {
    const input = e.currentTarget as HTMLInputElement
    const file = input.files?.[0]
    if (!file) return
    try {
      const dataUrl = await fileToResizedDataUrl(file)
      setPreviewSrc(dataUrl)
      setMode(selectedMode)
    } catch (err) {
      setErrorMessage(err instanceof Error ? err.message : 'Não foi possível carregar a imagem.')
      setScreen('error')
    } finally {
      input.value = ''
    }
  }

  function requestLocation() {
    if (!navigator.geolocation) {
      setLocationStatus('denied')
      return
    }
    setLocationStatus('requesting')
    navigator.geolocation.getCurrentPosition(
      (position) => {
        setCoords({ latitude: position.coords.latitude, longitude: position.coords.longitude })
        setLocationStatus('granted')
      },
      () => setLocationStatus('denied'),
      { timeout: 10000 },
    )
  }

  async function handleAnalyze() {
    const payloadImage = mode === 'url' ? imageUrl.trim() : previewSrc
    if (!payloadImage) return

    setScreen('loading')
    setErrorMessage(null)
    try {
      const resposta = await analisarPlanta({
        imageUrl: payloadImage,
        latitude: coords?.latitude,
        longitude: coords?.longitude,
      })
      setResult(resposta)
      setAnalyzedImage(payloadImage)
      setActiveTab('sobre')
      setScreen('result')
    } catch (err) {
      setErrorMessage(err instanceof Error ? err.message : 'Erro desconhecido ao analisar a planta.')
      setScreen('error')
    }
  }

  function resetToUpload(keepImage: boolean) {
    setScreen('upload')
    setResult(null)
    setAnalyzedImage(null)
    if (!keepImage) {
      setImageUrl('')
      setPreviewSrc(null)
      setMode('url')
    }
  }

  return (
    <div class="app-shell">
      <header class="topbar">
        <span class="brand">🌿 FLORA HUB</span>
      </header>

      {screen === 'upload' && (
        <main class="screen">
          <h1>Identificar planta</h1>
          <p class="subtitle">
            Envie uma foto para descobrir a espécie, os cuidados ideais e receber recomendações
            com base no clima atual.
          </p>

          <div class="mode-tabs">
            <button type="button" class={mode === 'url' ? 'active' : ''} onClick={() => setMode('url')}>
              🔗 URL
            </button>
            <button type="button" class={mode === 'galeria' ? 'active' : ''} onClick={() => galeriaInputRef.current?.click()}>
              🖼️ Galeria
            </button>
            <button type="button" class={mode === 'camera' ? 'active' : ''} onClick={() => cameraInputRef.current?.click()}>
              📷 Câmera
            </button>
          </div>

          {mode === 'url' && (
            <input
              type="text"
              class="url-input"
              placeholder="Cole a URL de uma imagem..."
              value={imageUrl}
              onInput={(e) => setImageUrl((e.currentTarget as HTMLInputElement).value)}
            />
          )}

          <input
            ref={galeriaInputRef}
            type="file"
            accept="image/*"
            hidden
            onChange={(e) => onFileSelected(e, 'galeria')}
          />
          <input
            ref={cameraInputRef}
            type="file"
            accept="image/*"
            capture="environment"
            hidden
            onChange={(e) => onFileSelected(e, 'camera')}
          />

          <div class="preview-box">
            {displaySrc ? (
              <img src={displaySrc} alt="Pré-visualização da planta" />
            ) : (
              <div class="preview-placeholder">
                <span class="preview-icon">📷</span>
                <span>Tire uma foto ou faça upload</span>
              </div>
            )}
          </div>

          <button type="button" class="location-btn" onClick={requestLocation} disabled={locationStatus === 'requesting'}>
            {locationStatus === 'granted' && '📍 Localização obtida'}
            {locationStatus === 'denied' && '📍 Indisponível — tentar novamente'}
            {locationStatus === 'requesting' && '📍 Obtendo localização...'}
            {locationStatus === 'idle' && '📍 Usar minha localização (opcional)'}
          </button>
          <p class="hint">Com a localização, a análise também traz recomendações baseadas no clima atual.</p>

          <button type="button" class="primary-btn" disabled={!canAnalyze} onClick={handleAnalyze}>
            ANALISAR
          </button>
        </main>
      )}

      {screen === 'loading' && (
        <main class="screen center">
          <div class="spinner" />
          <p>Analisando planta...</p>
        </main>
      )}

      {screen === 'error' && (
        <main class="screen center">
          <span class="state-icon">🌿❓</span>
          <h2>Não foi possível identificar a planta</h2>
          <p class="subtitle">{errorMessage}</p>
          <button type="button" class="primary-btn" onClick={() => resetToUpload(true)}>
            Tentar novamente
          </button>
          <button type="button" class="secondary-btn" onClick={() => resetToUpload(false)}>
            Escolher outra imagem
          </button>
        </main>
      )}

      {screen === 'result' && result && (
        <main class="screen">
          <button type="button" class="back-btn" onClick={() => resetToUpload(false)}>
            ← Nova identificação
          </button>

          <div class="result-card">
            {analyzedImage && <img class="result-image" src={analyzedImage} alt={result.identificacao.nomePopular ?? 'Planta'} />}

            <h2>{result.identificacao.nomePopular ?? 'Planta não identificada'}</h2>
            <p class="scientific-name">{result.identificacao.nomeCientifico}</p>

            <div class="chips">
              {result.identificacao.aparenciaSaudavel === true && <span class="chip chip-ok">Saudável</span>}
              {result.identificacao.aparenciaSaudavel === false && <span class="chip chip-warn">Atenção necessária</span>}
              {result.identificacao.confianca != null && (
                <span class="chip chip-muted">Confiança: {(result.identificacao.confianca * 100).toFixed(0)}%</span>
              )}
            </div>

            {result.identificacao.aparenciaSaudavel === false && result.identificacao.recomendacaoTratamento && (
              <div class="alert-box">🩹 {result.identificacao.recomendacaoTratamento}</div>
            )}

            <div class="tabs">
              <button type="button" class={activeTab === 'sobre' ? 'active' : ''} onClick={() => setActiveTab('sobre')}>
                Sobre
              </button>
              <button type="button" class={activeTab === 'cuidados' ? 'active' : ''} onClick={() => setActiveTab('cuidados')}>
                Cuidados
              </button>
              <button type="button" class={activeTab === 'clima' ? 'active' : ''} onClick={() => setActiveTab('clima')}>
                Clima
              </button>
            </div>

            <div class="tab-content">
              {activeTab === 'sobre' && (
                <ul class="info-list">
                  <li><span>Gênero</span><span>{result.identificacao.genero ?? '—'}</span></li>
                  <li><span>Espécie</span><span>{result.identificacao.especie ?? '—'}</span></li>
                  <li><span>Origem</span><span>{result.identificacao.continenteOrigem ?? '—'}</span></li>
                </ul>
              )}

              {activeTab === 'cuidados' && (
                result.cuidados ? (
                  <>
                    <ul class="info-list">
                      <li><span>☀️ Luminosidade</span><span>{result.cuidados.luminosidadeIdeal ?? '—'}</span></li>
                      <li>
                        <span>💧 Rega</span>
                        <span>
                          {result.cuidados.rega ?? '—'}
                          {result.cuidados.frequenciaRega ? ` (${result.cuidados.frequenciaRega})` : ''}
                        </span>
                      </li>
                      <li><span>🌡️ Temperatura</span><span>{result.cuidados.temperaturaMin ?? '—'}–{result.cuidados.temperaturaMax ?? '—'} °C</span></li>
                      <li><span>💦 Umidade</span><span>{result.cuidados.umidadeMin ?? '—'}–{result.cuidados.umidadeMax ?? '—'}%</span></li>
                      <li><span>🌱 Solo</span><span>{result.cuidados.tipoSolo ?? '—'}</span></li>
                      <li><span>🪴 Drenagem</span><span>{result.cuidados.drenagem ?? '—'}</span></li>
                    </ul>
                    {result.cuidados.observacoes && <p class="observacoes">{result.cuidados.observacoes}</p>}
                  </>
                ) : (
                  <p class="empty-state">Cuidados ainda não disponíveis para esta espécie.</p>
                )
              )}

              {activeTab === 'clima' && (
                result.ambiente ? (
                  <>
                    <ul class="info-list">
                      <li><span>Temperatura atual</span><span>{result.ambiente.temperatura}°C</span></li>
                      <li><span>Umidade atual</span><span>{result.ambiente.umidade}%</span></li>
                      <li><span>Vento</span><span>{result.ambiente.velocidadeVento} m/s</span></li>
                      <li><span>Condição</span><span>{result.ambiente.descricaoClima ?? '—'}</span></li>
                    </ul>
                    <div class="recomendacoes">
                      {result.recomendacoes.map((rec, index) => (
                        <div key={index} class={`rec rec-${rec.nivel.toLowerCase()}`}>
                          <strong>{rec.categoria}</strong>
                          <p>{rec.mensagem}</p>
                        </div>
                      ))}
                    </div>
                  </>
                ) : (
                  <p class="empty-state">{result.aviso ?? 'Recomendações climáticas não disponíveis.'}</p>
                )
              )}
            </div>

            <button type="button" class="primary-btn" onClick={() => resetToUpload(false)}>
              Nova identificação
            </button>
          </div>
        </main>
      )}
    </div>
  )
}
