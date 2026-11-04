package com.nexo.infrastructure.adapters.out.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexo.application.ports.out.TaskAssistantProviderPort;
import com.nexo.domain.model.AssistantActionProposal;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Adaptador para un proveedor de IA con API compatible con OpenAI.
 * La clave se lee exclusivamente desde variables de entorno y nunca llega al frontend.
 */
@Component
@ConditionalOnProperty(prefix = "app.ai", name = "enabled", havingValue = "true")
public class OpenAiCompatibleTaskAssistant implements TaskAssistantProviderPort {
    private static final int MAX_PROVIDER_ATTEMPTS = 3;
    private final RestClient client;
    private final ObjectMapper mapper;
    private final String apiKey;
    private final String model;

    @Autowired
    public OpenAiCompatibleTaskAssistant(
            ObjectMapper mapper,
            @Value("${app.ai.base-url}") String baseUrl,
            @Value("${app.ai.api-key}") String apiKey,
            @Value("${app.ai.model}") String model) {
        this(mapper, buildClient(baseUrl, apiKey), apiKey, model);
    }

    OpenAiCompatibleTaskAssistant(ObjectMapper mapper, RestClient client, String apiKey, String model) {
        this.client = client;
        this.mapper = mapper;
        this.apiKey = apiKey;
        this.model = model;
    }

    private static RestClient buildClient(String baseUrl, String apiKey) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(8));
        requestFactory.setReadTimeout(Duration.ofSeconds(25));
        // RestClient treats a base URL without a trailing slash as a file path.
        // Normalising it preserves provider prefixes such as /v1beta/openai.
        return RestClient.builder().baseUrl(normalizeBaseUrl(baseUrl)).requestFactory(requestFactory)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    static String normalizeBaseUrl(String baseUrl) {
        return baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    }

    @Override
    public boolean isConfigured() { return !apiKey.isBlank(); }

    @Override
    public AssistantResponse ask(String question, List<String> taskContext) {
        if (!isConfigured()) throw new TaskAssistantUnavailableException();
        String context = taskContext.isEmpty() ? "No hay tareas pendientes." : String.join("\n", taskContext);
        Map<String, Object> payload = Map.of(
                "model", model,
                "temperature", 0.3,
                "messages", List.of(
                        Map.of("role", "system", "content", "Eres el asistente de productividad de NEXO. Responde exclusivamente JSON válido, sin markdown: {\"answer\":\"respuesta breve en español\",\"proposedActions\":[{\"type\":\"TASK_CREATE, NOTE_CREATE, TASK_UPDATE, NOTE_UPDATE, TASK_DELETE o NOTE_DELETE\",\"targetId\":123 o null,\"title\":\"texto\",\"description\":\"texto\",\"dueDate\":\"YYYY-MM-DD o vacío\",\"priority\":\"ALTA, MEDIA o BAJA\"}]}. Para editar o eliminar usa obligatoriamente el ID del elemento incluido en el contexto. Propón acciones solo si el usuario pide crear, editar, actualizar, eliminar o borrar. Nunca inventes IDs o fechas."),
                        Map.of("role", "user", "content",
                                "Consulta: " + question + "\n\nTareas actuales:\n" + context)));
        for (int attempt = 1; attempt <= MAX_PROVIDER_ATTEMPTS; attempt++) {
            try {
            // Sin barra inicial: conserva prefijos como /v1 o /v1beta/openai del proveedor configurado.
            String response = client.post().uri("chat/completions").body(payload).retrieve().body(String.class);
            JsonNode root = mapper.readTree(response);
            String content = root.path("choices").path(0).path("message").path("content").asText();
            if (content.isBlank()) throw new IllegalStateException("El proveedor no devolvió una respuesta útil");
            return parseAssistantResponse(content);
            } catch (RestClientResponseException exception) {
                if (!isTransientProviderError(exception) || attempt == MAX_PROVIDER_ATTEMPTS) {
                    throw new TaskAssistantUnavailableException();
                }
                waitBeforeRetry(attempt);
            } catch (Exception exception) {
                throw new TaskAssistantUnavailableException();
            }
        }
        throw new TaskAssistantUnavailableException();
    }

    private AssistantResponse parseAssistantResponse(String content) {
        try {
            String json = content.trim().replaceFirst("^```json\\s*", "").replaceFirst("^```\\s*", "")
                    .replaceFirst("\\s*```$", "");
            JsonNode parsed = mapper.readTree(json);
            String answer = parsed.path("answer").asText().trim();
            if (answer.isBlank()) return new AssistantResponse(content.trim(), List.of());
            List<AssistantActionProposal> actions = new java.util.ArrayList<>();
            for (JsonNode action : parsed.path("proposedActions")) {
                try {
                    actions.add(new AssistantActionProposal(
                            AssistantActionProposal.Type.valueOf(action.path("type").asText().toUpperCase()),
                            action.path("targetId").isIntegralNumber() ? action.path("targetId").asLong() : null,
                            action.path("title").asText(), action.path("description").asText(),
                            action.path("dueDate").asText(), action.path("priority").asText()));
                } catch (IllegalArgumentException ignored) { }
            }
            return new AssistantResponse(answer, actions);
        } catch (Exception ignored) {
            return new AssistantResponse(content.trim(), List.of());
        }
    }

    private boolean isTransientProviderError(RestClientResponseException exception) {
        int status = exception.getStatusCode().value();
        return status == 429 || exception.getStatusCode().is5xxServerError();
    }

    private void waitBeforeRetry(int attempt) {
        try {
            Thread.sleep(attempt * 1_000L);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new TaskAssistantUnavailableException();
        }
    }

    public static class TaskAssistantUnavailableException extends RuntimeException { }
}
