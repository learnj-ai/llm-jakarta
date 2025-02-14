package learning.jakarta.ai;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.data.message.UserMessage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Consumer;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
public class LangChainService {
    private JavaAgent javaAgent;
    private PersistentChatMemoryStore chatMemoryStore;

    @Inject
    public LangChainService(LangChain4JConfig config, PersistentChatMemoryStore chatMemoryStore, ChatMessageRepository chatMessageRepository) {
        this.chatMemoryStore = chatMemoryStore;

        var chatModel = OpenAiStreamingChatModel.builder()
                .apiKey(config.getApiKey())
                .modelName(config.getModelName())
                .temperature(config.getTemperature())
                .timeout(config.getTimeout())
                .maxTokens(config.getMaxTokens())
                .frequencyPenalty(config.getFrequencyPenalty())
                .logRequests(config.isLogRequests())
                .logResponses(config.isLogResponses())
                .build();

        ChatMemoryProvider chatMemoryProvider = memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .chatMemoryStore(chatMemoryStore)
                .maxMessages(config.getMaxMemorySize())
                .build();

        javaAgent = AiServices
                .builder(JavaAgent.class)
                .streamingChatLanguageModel(chatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .build();
    }

    public void sendMessage(String userId, String message, Consumer<String> consumer) {
        log.info("User {} message: {}", userId, message);

        if (chatMemoryStore.getMessages(userId).isEmpty()) {
            initializeMemoryWithExamples(userId);
        }

        javaAgent.chat(userId, message)
                .onNext(consumer::accept)
                .onComplete((Response<AiMessage> response) -> consumer.accept("[END]"))
                .onError((Throwable throwable) -> {
                    log.error("Error processing message", throwable);
                    consumer.accept("Sorry, I am unable to process your message at this time. Please try again later.");
                }).start();
    }

    public void initializeMemoryWithExamples(String userId) {
        List<ChatMessage> fewShotExamples = List.of(
                new SystemMessage("""
                    Welcome to the Java Concurrency Chatbot! 🚀 I’m here to help you explore the concepts, evolution, and practical applications of concurrency in Java. Whether you’re just starting out or tackling advanced challenges, let’s unravel the complexities of Java concurrency together! 🎉
                """),
                new UserMessage("What is concurrency in Java?"),
                new AiMessage("""
                    Concurrency in Java refers to the ability of a program to execute multiple tasks simultaneously. It enables efficient utilization of CPU resources by dividing work among threads. For example, multiple threads can independently execute tasks, such as processing user requests or performing background computations.
                """),
                new UserMessage("How is concurrency different from parallelism?"),
                new AiMessage("""
                    Concurrency is about managing multiple tasks that can overlap in execution, even if not at the same time. Parallelism, on the other hand, involves executing multiple tasks simultaneously, requiring multiple processing units like CPU cores. Think of concurrency as juggling tasks and parallelism as working on multiple tasks side-by-side.
                """),
                new UserMessage("What are threads in Java?"),
                new AiMessage("""
                    In Java, a thread is the smallest unit of execution within a program. Threads share the program's memory space but maintain their own call stack, program counter, and local variables. They allow Java programs to perform tasks concurrently, like handling multiple user requests in a web server.
                """),
                new UserMessage("What is the difference between platform threads and virtual threads?"),
                new AiMessage("""
                    Platform threads are traditional OS-level threads, managed by the operating system and have significant resource overhead. Virtual threads, introduced with Project Loom, are lightweight, user-mode threads that allow Java programs to handle millions of threads efficiently by reducing resource consumption and context-switching overhead.
                """),
                new UserMessage("How do I create a thread in Java?"),
                new AiMessage("""
                    You can create a thread in Java by:
                    1. Extending the `Thread` class and overriding the `run()` method.
                    2. Implementing the `Runnable` interface and passing it to a `Thread` object.
                    Example using Runnable:
                    ```java
                    Runnable task = () -> System.out.println("Running in a thread!");
                    Thread thread = new Thread(task);
                    thread.start();
                    ```
                """),
                new UserMessage("What is the Executor framework?"),
                new AiMessage("""
                    The Executor framework, introduced in Java 5, provides a high-level API for managing threads. It uses thread pools to efficiently handle concurrent tasks, reducing the overhead of creating and destroying threads. Example:
                    ```java
                    ExecutorService executor = Executors.newFixedThreadPool(5);
                    executor.submit(() -> System.out.println("Task executed!"));
                    executor.shutdown();
                    ```
                """),
                new UserMessage("What is structured concurrency?"),
                new AiMessage("""
                    Structured concurrency is a programming paradigm introduced with Project Loom that ensures threads are executed in a well-defined lifecycle. It organizes tasks in a way that they start and finish together, making concurrent programming more predictable and manageable.
                """),
                new UserMessage("How do virtual threads handle blocking operations?"),
                new AiMessage("""
                    Virtual threads intelligently handle blocking operations by yielding control back to the underlying platform threads during blocking events, such as I/O. This ensures that platform threads remain available for other virtual threads, maximizing resource utilization.
                """),
                new UserMessage("How do I measure the maximum number of threads my system can handle?"),
                new AiMessage("""
                    You can measure the thread limit by incrementally creating threads until the system encounters an error. Here's a simple example:
                    ```java
                    AtomicInteger threadCount = new AtomicInteger(0);
                    try {
                        while (true) {
                            new Thread(() -> threadCount.incrementAndGet()).start();
                        }
                    } catch (OutOfMemoryError e) {
                        System.out.println("Thread limit reached: " + threadCount.get());
                    }
                    ```
                """),
                new UserMessage("What are some common pitfalls of using threads in Java?"),
                new AiMessage("""
                    Common pitfalls include:
                    - Overuse of threads leading to high memory usage and context-switching overhead.
                    - Deadlocks, where threads block each other indefinitely.
                    - Race conditions caused by unsynchronized access to shared resources.
                    - Poor error handling in multithreaded programs.
                """)
        );

        chatMemoryStore.updateMessages(userId, fewShotExamples);
    }
}

//Example
//- What are virtual threads?
//- How does Project Loom improve concurrency in Java?
//- What is the difference between blocking and non-blocking I/O in Java?