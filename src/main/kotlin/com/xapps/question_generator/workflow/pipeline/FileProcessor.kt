package com.xapps.question_generator.workflow.pipeline

import com.xapps.question_generator.infrastructure.object_store.ObjectHandle
import com.xapps.question_generator.infrastructure.object_store.ObjectStore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.springframework.stereotype.Component

@Component
class FileProcessor(
    private val objectStore: ObjectStore
) {

    private val semaphore = Semaphore(4)

    suspend fun fetch(keys: List<String>): List<ObjectHandle> = coroutineScope {
        keys.map { key ->
            semaphore.withPermit {
                async {
                    objectStore.get(key)
                }
            }
        }.awaitAll().mapNotNull { it }
    }
}