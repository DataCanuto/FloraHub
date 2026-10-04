package florahub.backend.App.plant;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;

@Service
public class PlantIdentificationService {

    private static final String SYSTEM_PROMPT = """
            Você é um especialista em botânica. Analise cuidadosamente a foto de planta enviada e identifique a
            planta com o máximo de precisão possível, respondendo em português do Brasil.
            Baseie a identificação exclusivamente em características visíveis na imagem (formato, borda e
            disposição das folhas, caule, flores, frutos). Nunca escolha uma espécie cujas características
            visuais sejam incompatíveis com o que está de fato visível na foto.
            Preencha todos os campos com sua melhor hipótese em vez de deixar em branco, mas reflita seu grau
            real de certeza no campo "confidence": use valores altos (próximos de 1.0) apenas quando as
            características da planta forem claramente reconhecíveis, e valores baixos (0.1 a 0.3) quando a
            imagem for ambígua, estiver com pouca qualidade/enquadramento ruim, ou permitir apenas um palpite
            entre espécies parecidas.
            Avalie a aparência da planta na foto: considere saudável quando não houver sinais visíveis de
            pragas, doenças, folhas murchas, amareladas ou secas.
            Preencha a recomendação de tratamento apenas quando a aparência não for saudável; caso contrário,
            deixe a recomendação vazia.cd floraHubFrontend
            
            """;

    private static final long MAX_IMAGE_BYTES = 10L * 1024 * 1024;
    private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(10);

    private final ChatClient chatClient;
    private final PlantIdentificationRepository repository;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(HTTP_TIMEOUT)
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    public PlantIdentificationService(ChatClient.Builder chatClientBuilder, PlantIdentificationRepository repository) {
        this.chatClient = chatClientBuilder.build();
        this.repository = repository;
    }

    public PlantIdentification identifyFromImageUrl(String imageUrl) {
        Media media = buildMedia(imageUrl);

        PlantIdentificationResult result = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .options(OpenAiChatOptions.builder().temperature(0.2))
                .user(user -> user
                        .text("Identifique a planta na imagem a seguir e avalie sua aparência.")
                        .media(media))
                .call()
                .entity(PlantIdentificationResult.class);

        if (result == null) {
            throw new IllegalStateException("A IA não retornou um resultado para a imagem informada.");
        }

        PlantIdentification entity = new PlantIdentification();
        entity.setImageUrl(imageUrl);
        entity.setScientificName(result.scientificName());
        entity.setPopularName(result.popularName());
        entity.setSpecies(result.species());
        entity.setGenus(result.genus());
        entity.setOriginContinent(result.originContinent());
        entity.setLightRequirement(result.lightRequirement());
        entity.setWateringRequirement(result.wateringRequirement());
        entity.setHealthyAppearance(result.healthyAppearance());
        entity.setCareRecommendation(result.careRecommendation());
        entity.setConfidence(result.confidence());

        return repository.save(entity);
    }

    private Media buildMedia(String imageUrl) {
        if (imageUrl.startsWith("data:")) {
            return buildMediaFromDataUri(imageUrl);
        }
        return buildMediaFromUrl(imageUrl);
    }

    private Media buildMediaFromUrl(String imageUrl) {
        URI uri = parsePublicImageUri(imageUrl);

        HttpResponse<byte[]> response;
        try {
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(HTTP_TIMEOUT)
                    .header("User-Agent", "FloraHub/1.0")
                    .GET()
                    .build();
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (Exception e) {
            throw new IllegalArgumentException("Não foi possível baixar a imagem informada: " + imageUrl, e);
        }

        if (response.statusCode() != 200) {
            throw new IllegalArgumentException(
                    "Não foi possível baixar a imagem informada (HTTP " + response.statusCode() + ").");
        }

        byte[] bytes = response.body();
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("A URL informada não retornou nenhum conteúdo de imagem.");
        }
        if (bytes.length > MAX_IMAGE_BYTES) {
            throw new IllegalArgumentException("A imagem informada excede o tamanho máximo permitido (10 MB).");
        }

        MimeType mimeType = resolveMimeType(response, bytes)
                .orElseThrow(() -> new IllegalArgumentException(
                        "A URL informada não aponta para uma imagem válida: " + imageUrl));

        return Media.builder().mimeType(mimeType).data(bytes).build();
    }

    private URI parsePublicImageUri(String imageUrl) {
        URI uri;
        try {
            uri = new URI(imageUrl);
        } catch (Exception e) {
            throw new IllegalArgumentException("URL de imagem inválida: " + imageUrl, e);
        }

        String scheme = uri.getScheme();
        if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            throw new IllegalArgumentException("URL de imagem inválida: apenas http/https são suportados.");
        }
        if (uri.getHost() == null) {
            throw new IllegalArgumentException("URL de imagem inválida: " + imageUrl);
        }

        try {
            for (InetAddress address : InetAddress.getAllByName(uri.getHost())) {
                if (address.isLoopbackAddress() || address.isAnyLocalAddress()
                        || address.isLinkLocalAddress() || address.isSiteLocalAddress()
                        || address.isMulticastAddress()) {
                    throw new IllegalArgumentException(
                            "URL de imagem não permitida: aponta para um endereço privado/local.");
                }
            }
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Não foi possível resolver o host da URL informada.", e);
        }

        return uri;
    }

    private Optional<MimeType> resolveMimeType(HttpResponse<byte[]> response, byte[] bytes) {
        String contentType = response.headers().firstValue("Content-Type").orElse(null);
        if (contentType != null) {
            String normalized = contentType.split(";")[0].trim().toLowerCase(Locale.ROOT);
            if (normalized.startsWith("image/")) {
                return Optional.of(MimeType.valueOf(normalized));
            }
        }
        return sniffImageMimeType(bytes);
    }

    private Optional<MimeType> sniffImageMimeType(byte[] bytes) {
        if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF) {
            return Optional.of(MimeType.valueOf("image/jpeg"));
        }
        if (bytes.length >= 8 && (bytes[0] & 0xFF) == 0x89 && bytes[1] == 'P' && bytes[2] == 'N' && bytes[3] == 'G') {
            return Optional.of(MimeType.valueOf("image/png"));
        }
        if (bytes.length >= 6 && bytes[0] == 'G' && bytes[1] == 'I' && bytes[2] == 'F') {
            return Optional.of(MimeType.valueOf("image/gif"));
        }
        if (bytes.length >= 12 && bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') {
            return Optional.of(MimeType.valueOf("image/webp"));
        }
        return Optional.empty();
    }

    private Media buildMediaFromDataUri(String dataUri) {
        int comma = dataUri.indexOf(',');
        if (comma < 0 || !dataUri.substring(0, comma).contains(";base64")) {
            throw new IllegalArgumentException("Formato de imagem em base64 inválido.");
        }

        String header = dataUri.substring("data:".length(), comma);
        String mimeType = header.substring(0, header.indexOf(';'));

        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(dataUri.substring(comma + 1));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Conteúdo base64 da imagem inválido.", e);
        }

        return Media.builder().mimeType(MimeType.valueOf(mimeType)).data(bytes).build();
    }
}
