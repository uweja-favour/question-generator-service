
package com.xapps.question_generator.job.progress

import com.xapps.dto.IdHolder
import org.springframework.http.codec.ServerSentEvent

interface JobUpdateEventFactory {
    fun create(snapshot: JobSnapshot): ServerSentEvent<IdHolder>
}
