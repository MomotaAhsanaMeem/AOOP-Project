// src/main/java/com/game/server/ai/GeminiClient.java
package com.game.server.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class GeminiClient {

  private final WebClient http;
  private final String model;
  private final String apiKey;

  public GeminiClient(
      @Value("${app.gemini.apiKey:}") String apiKey,
      @Value("${app.gemini.model:gemini-2.5-flash-lite}") String model
  ) {
    this.apiKey = apiKey;
    this.model = model;

    // Allow slightly larger responses; keep defaults otherwise
    ExchangeStrategies strategies = ExchangeStrategies.builder()
        .codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(512 * 1024))
        .build();

    this.http = WebClient.builder()
        .baseUrl("https://generativelanguage.googleapis.com")
        .exchangeStrategies(strategies)
        .build();
  }

  // ─────────────────────────────────────────────────────────────────────────────
  // MCQ generation (strict JSON). Falls back safely on errors/timeouts.
  // ─────────────────────────────────────────────────────────────────────────────

  /** Generate a DSA MCQ as strict JSON. Falls back to a safe built-in MCQ on any issue. */
  public CompletableFuture<QuestionItem> generateMcq(String storyContext) {
    String prompt =
        "You are a DSA (coding/algorithms) MCQ generator and must avoid repeating recent questions.\n" +
        "Return STRICT JSON ONLY with this exact shape:\n" +
        "{\"text\": string, \"options\": string[], \"correctIndex\": number}\n" +
        "Constraints:\n" +
        "- 3–4 options\n" +
        "- exactly one correct answer\n" +
        "- each option < 80 characters\n" +
        "- no additional prose, code fences, or explanation\n" +
        "Theme (flavor only, do not include story text in options): " +
        (storyContext == null ? "" : storyContext);

    Map<String, Object> part = new HashMap<>();
    part.put("text", prompt);
    List<Map<String, Object>> parts = new ArrayList<>();
    parts.add(part);

    Map<String, Object> content = new HashMap<>();
    content.put("parts", parts);
    List<Map<String, Object>> contents = new ArrayList<>();
    contents.add(content);

    Map<String, Object> generationConfig = new HashMap<>();
    generationConfig.put("temperature", 0.7);
    generationConfig.put("responseMimeType", "application/json");

    Map<String, Object> body = new HashMap<>();
    body.put("contents", contents);
    body.put("generationConfig", generationConfig);

    return http.post()
        .uri("/v1beta/models/{model}:generateContent?key={key}", model, apiKey)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(body)
        .retrieve()
        .bodyToMono(Map.class)
        // Keep a hard time budget so UI doesn't wait forever
        .timeout(Duration.ofSeconds(6))
        .map(this::extractMcqFromResponse)
        .onErrorResume(e -> Mono.just(fallbackMcq()))
        .defaultIfEmpty(fallbackMcq())
        .toFuture();
  }

  // ─────────────────────────────────────────────────────────────────────────────
  // Hints for current question (strategy only; no answers/letters).
  // ─────────────────────────────────────────────────────────────────────────────

  /** Generate one short, targeted hint for the current question + options. */
  public CompletableFuture<String> generateHint(String questionText, List<String> options) {
    String system =
        "You are a coding tutor for DSA multiple-choice questions.\n" +
        "Rules:\n" +
        "- NEVER reveal the final answer or option letter.\n" +
        "- Provide a strategy/approach hint in 1–2 sentences.\n" +
        "- Make it specific to the provided question and options.\n" +
        "- Avoid generic advice and avoid restating the options verbatim.";

    String user = "Question: " + (questionText == null ? "(none)" : questionText) + "\n" +
                  "Options: " + ((options == null || options.isEmpty()) ? "(none)" : String.join(", ", options)) + "\n" +
                  "Give one strong, targeted hint without revealing the final answer/letter.";

    Map<String, Object> part = new HashMap<>();
    part.put("text", system + "\n\n" + user);
    List<Map<String, Object>> parts = new ArrayList<>();
    parts.add(part);

    Map<String, Object> content = new HashMap<>();
    content.put("parts", parts);
    List<Map<String, Object>> contents = new ArrayList<>();
    contents.add(content);

    Map<String, Object> generationConfig = new HashMap<>();
    generationConfig.put("temperature", 0.4);
    generationConfig.put("maxOutputTokens", 64);

    Map<String, Object> body = new HashMap<>();
    body.put("contents", contents);
    body.put("generationConfig", generationConfig);

    return http.post()
        .uri("/v1beta/models/{model}:generateContent?key={key}", model, apiKey)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(body)
        .retrieve()
        .bodyToMono(Map.class)
        .timeout(Duration.ofSeconds(6))
        .map(this::extractTextFromResponse)
        .map(this::cleanHint)
        .map(text -> redactAnswer(text, options)) // extra safety
        .onErrorResume(e -> Mono.just("Focus on the key property that distinguishes the correct choice, not the exact option."))
        .defaultIfEmpty("Focus on the key property that distinguishes the correct choice, not the exact option.")
        .toFuture();
  }

  // ─────────────────────────────────────────────────────────────────────────────
  // Guide chat: human-like small talk OR strategy-only hinting (no answers).
  // Call with mode="chitchat" or mode="hint".
  // ─────────────────────────────────────────────────────────────────────────────

  /**
   * Human-like Guide reply.
   * @param mode "chitchat" (greetings/small talk) or "hint" (question help)
   * @param userText user's message
   * @param questionText current question (optional for hint mode)
   * @param options options list (optional for hint mode)
   */
  public CompletableFuture<String> generateGuideReply(
      String mode,
      String userText,
      String questionText,
      List<String> options
  ) {
    boolean hintMode = "hint".equalsIgnoreCase(mode);

    String system =
        "You are a friendly in-game Guide.\n" +
        "Global rules:\n" +
        "- NEVER reveal the final answer or option letter.\n" +
        "- Keep responses brief (1–2 sentences).\n" +
        "- Be polite and encouraging.\n" +
        (hintMode
            ? "- Provide strategy/approach hints only, tailored to the question/options.\n" +
              "- Do NOT restate options verbatim; prefer properties (e.g., stability, LIFO/FIFO).\n"
            : "- This is chit-chat (greetings/small talk). Reply naturally and briefly.\n");

    String context = (questionText == null ? "" : "Question: " + questionText + "\n") +
                     ((options == null || options.isEmpty()) ? "" : "Options: " + String.join(", ", options) + "\n");

    String user =
        "Mode: " + (hintMode ? "hint" : "chitchat") + "\n" +
        context +
        "User said: " + (userText == null ? "" : userText) + "\n" +
        (hintMode
            ? "Respond with a helpful strategy without giving the final answer/letter."
            : "Respond like a human greeting/small talk. Keep it short.");

    // build request
    Map<String, Object> part = new HashMap<>();
    part.put("text", system + "\n\n" + user);
    List<Map<String, Object>> parts = new ArrayList<>();
    parts.add(part);

    Map<String, Object> content = new HashMap<>();
    content.put("parts", parts);
    List<Map<String, Object>> contents = new ArrayList<>();
    contents.add(content);

    Map<String, Object> gen = new HashMap<>();
    gen.put("temperature", hintMode ? 0.7 : 0.7);
    gen.put("maxOutputTokens", 100);

    Map<String, Object> body = new HashMap<>();
    body.put("contents", contents);
    body.put("generationConfig", gen);

    return http.post()
        .uri("/v1beta/models/{model}:generateContent?key={key}", model, apiKey)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(body)
        .retrieve()
        .bodyToMono(Map.class)
        .timeout(Duration.ofSeconds(6))
        .map(this::extractTextFromResponse)
        .map(this::cleanHint) // trims / removes fences
        .map(text -> hintMode ? redactAnswer(text, options) : text) // scrub answers in hint mode
        .map(text -> truncate(text, 240))
        .onErrorResume(e -> Mono.just(hintMode
            ? "Let’s think about the approach, not the final option sajid ."
            : "Hey! I’m your Guide—how can I help?"))
        .defaultIfEmpty(hintMode
            ? "Let’s think about the approach, not the final option omio."
            : "Hey! I’m your Guide—how can I help?")
        .toFuture();
  }

  // ─────────────────────────────────────────────────────────────────────────────
  // Helpers / Parsers / Fallbacks
  // ─────────────────────────────────────────────────────────────────────────────

  @SuppressWarnings("unchecked")
  private QuestionItem extractMcqFromResponse(Map<?, ?> resp) {
    try {
      List<Map<String, Object>> candidates = (List<Map<String, Object>>) resp.get("candidates");
      if (candidates == null || candidates.isEmpty()) return fallbackMcq();

      Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
      if (content == null) return fallbackMcq();

      List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
      if (parts == null || parts.isEmpty()) return fallbackMcq();

      String jsonStr = String.valueOf(parts.get(0).get("text"));
      if (jsonStr == null || jsonStr.isBlank()) return fallbackMcq();

      var gson = new com.google.gson.Gson();
      Map<String, Object> m = gson.fromJson(jsonStr, Map.class);

      String text = safeString(m.get("text"));
      List<String> options = (List<String>) m.get("options");
      Number ciNum = (Number) m.get("correctIndex");
      int correct = (ciNum == null) ? 0 : ciNum.intValue();

      if (text == null || options == null || options.isEmpty() || correct < 0 || correct >= options.size()) {
        return fallbackMcq();
      }
      return new QuestionItem(text, options, correct);
    } catch (Exception e) {
      return fallbackMcq();
    }
  }

  @SuppressWarnings("unchecked")
  private String extractTextFromResponse(Map<?, ?> resp) {
    try {
      List<Map<String, Object>> candidates = (List<Map<String, Object>>) resp.get("candidates");
      if (candidates == null || candidates.isEmpty()) return "";
      Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
      if (content == null) return "";
      List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
      if (parts == null || parts.isEmpty()) return "";
      String text = String.valueOf(parts.get(0).get("text"));
      return text == null ? "" : text.trim();
    } catch (Exception e) {
      return "";
    }
  }

  private String cleanHint(String s) {
    if (s == null) return "";
    s = s.replaceAll("^```[a-zA-Z0-9]*\\s*", "")
         .replaceAll("```\\s*$", "")
         .trim();
    return s;
  }

  private String truncate(String s, int max) {
    if (s == null) return "";
    return s.length() <= max ? s : s.substring(0, max);
  }

  /**
   * Redact accidental answer leakage:
   * - removes A)/B)/C)/D) style hints
   * - masks any option text if it appears verbatim
   */
  private String redactAnswer(String text, List<String> options) {
    if (text == null) return "";
    String out = text;

    // Strip option-letter patterns (A), B), A., etc.)
    out = out.replaceAll("\\b[ABCD]\\s*[\\).:-]\\s*", "");

    // If the model echoed an option verbatim, mask it
    if (options != null) {
      for (String opt : options) {
        if (opt == null) continue;
        String esc = java.util.regex.Pattern.quote(opt.trim());
        out = out.replaceAll("(?i)\\b" + esc + "\\b", "[option]");
      }
    }

    // Safety: if it still contains “Answer is” or “Correct is”, neutralize it
    out = out.replaceAll("(?i)\\b(answer|correct)\\s+(is|:)\\s*.*$", "Focus on the approach, not the final option.");

    return out.trim();
  }

  private QuestionItem fallbackMcq() {
    return new QuestionItem(
        "What is the time complexity of binary search on a sorted array?",
        List.of("O(n)", "O(log n)", "O(n log n)"),
        1
    );
  }

  private static String safeString(Object o) {
    return (o == null) ? null : String.valueOf(o);
  }
}
