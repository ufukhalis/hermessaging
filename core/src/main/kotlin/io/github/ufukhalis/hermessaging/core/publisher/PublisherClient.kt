package io.github.ufukhalis.hermessaging.core.publisher

import io.github.ufukhalis.hermessaging.core.model.MessageRequest
import io.github.ufukhalis.hermessaging.core.model.MessageResult
import java.util.concurrent.CompletableFuture

fun interface PublisherClient<K, V, R> {
    fun publish(messageRequest: MessageRequest<K, V>): CompletableFuture<MessageResult<R>>
}
