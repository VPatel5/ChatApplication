package me.vpatel.server;

import io.github.sashirestela.openai.SimpleOpenAIGeminiGoogle;
import io.github.sashirestela.openai.domain.chat.ChatMessage;
import io.github.sashirestela.openai.domain.chat.ChatRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class OpenAIClientProvider {

    private static final SimpleOpenAIGeminiGoogle INSTANCE = SimpleOpenAIGeminiGoogle.builder()
            .apiKey(ConvoServer.GEMINI_KEY.get())
            .build();

    private OpenAIClientProvider() {}

    public static SimpleOpenAIGeminiGoogle getClient() {
        return INSTANCE;
    }

    public static CompletableFuture<String> askGemini(List<String> messageHistory, String userInput) {
        if (userInput == null || userInput.trim().isEmpty()) {
            throw new IllegalArgumentException("User input cannot be null or empty.");
        }

        List<ChatMessage> chatMessages = new ArrayList<>();
        for (String rawMessage : messageHistory) {
            String[] parts = rawMessage.split(":", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid message format. Expected 'role: message'");
            }

            String role = parts[0].trim().toLowerCase();
            String content = parts[1].trim();

            ChatMessage message;
            if (role.equals("ai")) {
                message = ChatMessage.AssistantMessage.of(content);
            } else {
                message = ChatMessage.UserMessage.of(content);
            }
            chatMessages.add(message);
        }

        chatMessages.add(ChatMessage.UserMessage.of(userInput));

        return INSTANCE.chatCompletions()
                .create(
                        ChatRequest.builder()
                                .model("gemini-2.0-flash")
                                .messages(chatMessages)
                                .build()
                )
                .thenApply(resp -> resp.getChoices().get(0).getMessage().getContent())
                .exceptionally(ex -> {
                    System.err.println("Error calling Gemini API: " + ex.getMessage());
                    ex.printStackTrace();
                    return "Sorry, I couldn't process your request.";
                });
    }
}