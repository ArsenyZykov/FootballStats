package com.example.footballstats.ui.common

import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class CoroutineErrorsTest {
    @Test fun failureIsReportedAndDoesNotCancelSiblingOperation() = runTest {
        val scope = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler))
        try {
            var error: Throwable? = null
            var completed = false
            scope.launchWithErrorHandler(onError = { error = it }) { throw IOException("offline") }
            scope.launchWithErrorHandler(onError = { fail("Sibling must succeed") }) { completed = true }
            advanceUntilIdle()
            assertTrue(error is IOException)
            assertTrue(completed)
        } finally { scope.cancel() }
    }

    @Test fun cancellationIsNotReportedAsFailure() = runTest {
        val scope = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler))
        try {
            var reported = false
            val job = scope.launchWithErrorHandler(onError = { reported = true }) { awaitCancellation() }
            runCurrent()
            job.cancel()
            advanceUntilIdle()
            assertFalse(reported)
        } finally { scope.cancel() }
    }
}
