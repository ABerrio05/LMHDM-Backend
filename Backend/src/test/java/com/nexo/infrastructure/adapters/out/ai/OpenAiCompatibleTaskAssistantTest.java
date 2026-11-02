package com.nexo.infrastructure.adapters.out.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class OpenAiCompatibleTaskAssistantTest {

    @Test
    void addsTrailingSlashToPreserveConfiguredProviderPrefix() {
        assertThat(OpenAiCompatibleTaskAssistant
                .normalizeBaseUrl("https://generativelanguage.googleapis.com/v1beta/openai"))
                .isEqualTo("https://generativelanguage.googleapis.com/v1beta/openai/");
    }

    @Test
    void preservesConfiguredProviderPrefixWhenCallingChatCompletions() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta/openai/");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        OpenAiCompatibleTaskAssistant assistant = new OpenAiCompatibleTaskAssistant(
                new ObjectMapper(), builder.build(), "test-key", "gemini-test");

        server.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/openai/chat/completions"))
                .andExpect(method(POST))
                .andRespond(withSuccess("""
                        {"choices":[{"message":{"content":"Prioriza la tarea urgente."}}]}
                        """, MediaType.APPLICATION_JSON));

        String answer = assistant.ask("¿Qué hago primero?", List.of("Tarea urgente"));

        assertThat(answer).isEqualTo("Prioriza la tarea urgente.");
        server.verify();
    }
}
