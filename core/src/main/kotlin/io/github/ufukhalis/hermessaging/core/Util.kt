package io.github.ufukhalis.hermessaging.core

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

}
