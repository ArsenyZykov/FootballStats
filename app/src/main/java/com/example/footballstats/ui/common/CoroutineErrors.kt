package com.example.footballstats.ui.common

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.IOException


fun CoroutineScope.launchWithErrorHandler(
    onError: (Throwable) -> Unit,
    block: suspend CoroutineScope.() -> Unit
): Job = launch(CoroutineExceptionHandler { _, error -> onError(error) }, block = block)

fun Throwable.toUserMessage(fallback: String): String = when (this) {
    is IOException -> "Не удалось подключиться. Проверьте интернет и повторите."
    else -> fallback
}
