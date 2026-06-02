package com.xapps.question_generator.job

import com.xapps.question_generator.job.domain.model.QuestionCreationJob
import com.xapps.dto.SseJobUpdateDto

private fun QuestionCreationJob.toSseDto(): SseJobUpdateDto =
    SseJobUpdateDto(
        jobId = id,
        status = status
    )


//@Service
//class JobProgressTracker(
//    private val jobService: QuestionCreationJobService,
//    private val json: Json,
//    redisConnectionFactory: RedisConnectionFactory
//) {
//
//    private val logger = LoggerFactory.getLogger(javaClass)
//
//    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
//
//    private val redisContainer: RedisMessageListenerContainer =
//        RedisMessageListenerContainer().apply {
//            setConnectionFactory(redisConnectionFactory)
//            afterPropertiesSet()
//            start()
//        }
//
//    fun subscribeWith(jobIds: Set<JobId>): Flow<ServerSentEvent<IdHolder>> = channelFlow {
//        val completedJobs = mutableSetOf<JobId>()
//
//        // Polling loop
//        try {
//            while (isActive && completedJobs.size < jobIds.size) {
//                delay(2500) // 2.5 seconds
//
//                emitCurrentState(jobIds) { sseJobUpdateDto ->
//                    logger.info("SSE DATA CLASS = ${sseJobUpdateDto::class}")
//                    logger.info("SSE DATA = $sseJobUpdateDto")
//
//                    // Send safely in channelFlow
//                    trySend(toEvent(sseJobUpdateDto))
//
//                    if (sseJobUpdateDto.status.isFinal()) {
//                        completedJobs.add(sseJobUpdateDto.jobId)
//                    }
//                }
//            }
//
//            // Final flush just in case
//            logger.info("Final Flush just in case...")
//            emitCurrentState(completedJobs) {
//                trySend(toEvent(it))
//            }
//        } finally {
//            logger.info("Polling finished or cancelled. Completed jobs: $completedJobs")
//            close() // close flow explicitly
//        }
//
//        awaitClose {
//            logger.info("Client disconnected, cancelling polling.")
//        }
//    }
//
//    private fun toEvent(data: SseJobUpdateDto): ServerSentEvent<IdHolder> {
//        logger.info("Data is: $data")
//        val dataAsString = json.encodeToString(data)
//        logger.info("Data as String is: $dataAsString")
//
//        return ServerSentEvent.builder(IdHolder(dataAsString))
//            .id(data.jobId.value)
//            .event("job-update")
//            .data(IdHolder(dataAsString))
//            .build()
//    }
//
//    private suspend fun emitCurrentState(
//        jobIds: Set<JobId>,
//        send: suspend (SseJobUpdateDto) -> Unit
//    ) {
//        outcomeOf {
//            jobService
//                .findAllByIds(jobIds.toList())
//                .forEach { job ->
//                    send(job.toSseDto())
//                }
//        }.onFailure {
//            logger.error(
//                "Failed to emit job state for jobs $jobIds.",
//                it.exception
//            )
//        }
//    }
//}
