// src/main/java/com/game/server/ai/QuestionFactory.java
package com.game.server.ai;

import com.game.server.store.QuestionCacheRepo;
import com.game.server.store.doc.QuestionCacheDoc;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

@Service
public class QuestionFactory {

  private final GeminiClient gemini;
  private final QuestionCacheRepo qRepo;
  private final Random rng = new Random();

  public QuestionFactory(GeminiClient gemini, QuestionCacheRepo qRepo) {
    this.gemini = gemini;
    this.qRepo = qRepo;
  }

  // Short list of DSA topics to rotate for variety
  private static final List<String> TOPICS = List.of(
      "arrays", "strings", "linked lists", "stacks", "queues",
      "hashmaps", "binary search", "two pointers",
      "sorting", "heaps", "graphs bfs", "graphs dfs",
      "binary trees", "dynamic programming"
  );
  private static final List<String> DIFF = List.of("easy", "medium");

  /**
   * Generate a fresh, non-repeating DSA question for Level 1.
   * - Adds topic/difficulty flavor
   * - Avoids last seen questions via Mongo hash de-dup
   * - Time budget: AI 1s, otherwise local fallback
   */
  public CompletableFuture<QuestionItem> nextLevel1Question(String storyContext) {
    String topic = TOPICS.get(rng.nextInt(TOPICS.size()));
    String difficulty = DIFF.get(rng.nextInt(DIFF.size()));

    // Recent questions (just for hinting the LLM to avoid similarity)
    var recent = qRepo.findAll().stream()
        .sorted(Comparator.comparing((QuestionCacheDoc d) -> d.createdAt).reversed())
        .limit(6)
        .map(q -> q.text)
        .toList();

    String context = """
        Now: %s
        Topic: %s
        Difficulty: %s
        Story flavor: %s
        Avoid repeating or being too similar to any of these recent questions:
        - %s
        """.formatted(
            Instant.now(),
            topic,
            difficulty,
            storyContext == null ? "" : storyContext,
            String.join("\n        - ", recent)
        );

    // Try to get a unique AI question within 1s; otherwise fallback pool for the topic
    return generateUniqueWithTimeout(context, topic, 3, 1000, TimeUnit.MILLISECONDS);
  }

  // === Core uniqueness logic ===

  private CompletableFuture<QuestionItem> generateUniqueWithTimeout(
      String ctx, String topic, int attempts, long timeout, TimeUnit unit
  ) {
    // 1) Ask Gemini (with timeout)
    CompletableFuture<QuestionItem> ai =
        withTimeout(gemini.generateMcq(ctx), timeout, unit).exceptionally(ex -> null);

    // 2) If AI is late/null -> fallback immediately
    QuestionItem fallback = fallbackPool(topic);

    // 3) If AI arrives in time, enforce de-dup with Mongo; otherwise use fallback
    return ai.thenCompose(q -> {
      if (q == null) return CompletableFuture.completedFuture(fallback);
      return ensureUniqueOrRegenerate(q, ctx, topic, attempts);
    });
  }

  private CompletableFuture<QuestionItem> ensureUniqueOrRegenerate(
      QuestionItem q, String ctx, String topic, int attempts
  ) {
    String h = hash(q.text);
    boolean exists = qRepo.findTopByTextHashOrderByCreatedAtDesc(h).isPresent();
    if (!exists) {
      // Persist and return
      saveToCache(q, h);
      return CompletableFuture.completedFuture(q);
    }
    if (attempts <= 1) {
      // Give up after N tries — return a deterministic local fallback
      return CompletableFuture.completedFuture(fallbackPool(topic));
    }
    // Ask the model again with stronger “different” instruction
    String newCtx = ctx + "\n\nIMPORTANT: Produce a DIFFERENT question than any previously generated.";
    return generateUniqueWithTimeout(newCtx, topic, attempts - 1, 800, TimeUnit.MILLISECONDS);
  }

  private void saveToCache(QuestionItem q, String textHash) {
    QuestionCacheDoc d = new QuestionCacheDoc();
    d.text = q.text;
    d.options = q.options;
    d.correctIndex = q.correctIndex;
    d.textHash = textHash;
    d.createdAt = Instant.now();
    qRepo.save(d);
  }

  // === Local fallback pool (instant, topic-aware) ===

  private QuestionItem fallbackPool(String topic) {
    switch (topic) {
      case "stacks":
        return new QuestionItem(
            "Which data structure naturally supports undo operations due to LIFO behavior?",
            List.of("Queue", "Stack", "Priority Queue", "Deque"), 1);
      case "binary search":
        return new QuestionItem(
            "What is the time complexity of binary search on a sorted array?",
            List.of("O(n)", "O(log n)", "O(n log n)", "O(1)"), 1);
      case "graphs bfs":
        return new QuestionItem(
            "Breadth-First Search (BFS) typically uses which auxiliary structure?",
            List.of("Stack", "Queue", "Priority Queue", "Set only"), 1);
      case "hashmaps":
        return new QuestionItem(
            "Average-case time for HashMap get(key) with a good hash function is:",
            List.of("O(1)", "O(log n)", "O(n)", "O(n log n)"), 0);
      case "sorting":
        return new QuestionItem(
            "Which sorting algorithm is stable by default?",
            List.of("Quick Sort", "Heap Sort", "Merge Sort", "Selection Sort"), 2);
      case "two pointers":
        return new QuestionItem(
            "Two-pointers is commonly used to find pairs with a target sum in a(n):",
            List.of("Unsorted array", "Sorted array", "Binary tree", "Hash table"), 1);
      case "heaps":
        return new QuestionItem(
            "A binary max-heap supports extract-max in which time complexity?",
            List.of("O(1)", "O(log n)", "O(n)", "O(n log n)"), 1);
      case "graphs dfs":
        return new QuestionItem(
            "Depth-First Search (DFS) is naturally implemented using:",
            List.of("Queue", "Stack/Recursion", "Priority Queue", "Union-Find"), 1);
      case "binary trees":
        return new QuestionItem(
            "Inorder traversal of a Binary Search Tree yields keys in:",
            List.of("Random order", "Descending order", "Ascending order", "Level order"), 2);
      case "dynamic programming":
        return new QuestionItem(
            "Dynamic Programming primarily trades space for:",
            List.of("Readability", "Parallelism", "Time (by avoiding recomputation)", "I/O speed"), 2);
      default:
        return new QuestionItem(
            "Which algorithmic technique explores layer by layer in graphs?",
            List.of("DFS", "BFS", "Dijkstra’s algorithm", "Union-Find"), 1);
    }
  }

  // === Utilities ===

  private static <T> CompletableFuture<T> withTimeout(CompletableFuture<T> src, long t, TimeUnit u) {
    final CompletableFuture<T> timeoutFuture = new CompletableFuture<>();
    ScheduledExecutorService sch = Executors.newSingleThreadScheduledExecutor();
    sch.schedule(() -> timeoutFuture.complete(null), t, u);
    return src.applyToEither(timeoutFuture, v -> v)
              .whenComplete((v, ex) -> sch.shutdown());
  }

  /** Normalize + hash so tiny wording changes don’t defeat de-dup. */
  private String hash(String s) {
    if (s == null) return "0";
    String norm = s.toLowerCase()
                   .replaceAll("\\s+", " ")
                   .replaceAll("[\\p{Punct}&&[^-_]]", "") // keep hyphen/underscore; drop other punctuation
                   .trim();
    return Integer.toHexString(norm.hashCode());
  }
}
