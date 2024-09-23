package io.github.ufukhalis.hermessaging.core

import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

object Util {

    fun <T> Future<T>.toCompletableFuture(): CompletableFuture<T> {
        val completableFuture = CompletableFuture<T>()
        CompletableFuture.runAsync {
            try {
                completableFuture.complete(this.get(5000, TimeUnit.SECONDS))
            } catch (ex: Exception) {
                completableFuture.completeExceptionally(ex)
            }
        }
        return completableFuture
    }

    fun <T> withRetry(
        retryPolicy: RetryPolicy = RetryPolicy(),
        action: () -> T
    ): T {
        var currentDelay = retryPolicy.initialDelay
        var attempt = 0

        while (retryPolicy.times == null || attempt <= retryPolicy.times) {
            try {
                return action.invoke()
            } catch (ex: Exception) {
                attempt++
            }
            TimeUnit.SECONDS.sleep(currentDelay.seconds)
            currentDelay = Duration.ofSeconds(
                (currentDelay.seconds * retryPolicy.factor).toLong().coerceAtMost(retryPolicy.maxDelay.seconds)
            )
        }
        throw Exception("Max retry limit reached (${retryPolicy.times} attempts)")
    }

    data class RetryPolicy(
        val times: Int? = null,
        val initialDelay: Duration = Duration.ofSeconds(1),
        val maxDelay: Duration = Duration.ofSeconds(5),
        val factor: Double = 2.0,
    )
}
