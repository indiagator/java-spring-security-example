package io.example.configuration;

import java.util.Map;
import java.util.concurrent.Executor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

  private final int corePoolSize = 1;
  private final int maxPoolSize = 100;
  private final int queueCapacity = 1000;
  private final String prefix = "Async-Thread-";

  @Bean
  public Executor taskExecutor() {
    var executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(corePoolSize);
    executor.setMaxPoolSize(maxPoolSize);
    executor.setQueueCapacity(queueCapacity);
    executor.setThreadNamePrefix(prefix);

    // Copy sl4j Mapped Diagnostic Context (MDC) from the main to the child thread.
    executor.setTaskDecorator(
        runnable -> {
          Map<String, String> contextMap = MDC.getCopyOfContextMap();
          return () -> {
            try {
              MDC.setContextMap(contextMap);
              runnable.run();
            } finally {
              MDC.clear();
            }
          };
        });
    executor.initialize();

    // Delegate spring security context to the async threads
    return new DelegatingSecurityContextAsyncTaskExecutor(executor);
  }
}
