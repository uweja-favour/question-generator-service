package com.xapps.question_generator.job.progress

import com.xapps.dto.IdHolder
import com.xapps.dto.SseJobUpdateDto
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Service

@Service
class SseJobUpdateEventFactory(
    private val json: Json
) : JobUpdateEventFactory {

    override fun create(snapshot: JobSnapshot): ServerSentEvent<IdHolder> {
        val dto = SseJobUpdateDto(
            jobId = snapshot.jobId,
            status = snapshot.status
        )

        val payload = json.encodeToString(dto)

        return ServerSentEvent.builder(IdHolder(payload))
            .id(snapshot.jobId.value)
            .event("job-update")
            .data(IdHolder(payload))
            .build()
    }
}
