package com.xapps.question_generator.gpt_service

import com.knuddels.jtokkit.Encodings
import com.knuddels.jtokkit.api.Encoding
import com.knuddels.jtokkit.api.EncodingRegistry
import com.knuddels.jtokkit.api.EncodingType
import org.springframework.stereotype.Component

@Component
class LocalTokenCounter {
    private val registry: EncodingRegistry = Encodings.newDefaultEncodingRegistry()

    /**
     * Map model names to their encoding type.
     */
    private fun encodingForModel(modelName: String): Encoding {
        val encodingType = when {
            modelName.startsWith("gpt-4o") -> EncodingType.CL100K_BASE
            modelName.startsWith("gpt-4") -> EncodingType.CL100K_BASE
            modelName.startsWith("gpt-3.5") -> EncodingType.CL100K_BASE
            modelName.startsWith("text-embedding-ada-002") -> EncodingType.CL100K_BASE
            modelName.startsWith("davinci") -> EncodingType.P50K_BASE
            modelName.startsWith("curie") -> EncodingType.P50K_BASE
            modelName.startsWith("babbage") -> EncodingType.P50K_BASE
            modelName.startsWith("ada") -> EncodingType.P50K_BASE
            modelName.startsWith("code-davinci") -> EncodingType.P50K_BASE
            modelName.startsWith("code-cushman") -> EncodingType.P50K_BASE
            else -> EncodingType.CL100K_BASE // fallback to safest default
        }
        return registry.getEncoding(encodingType)
    }

    /**
     * Count tokens for a given text with respect to a model.
     */
    fun countTokens(text: String, modelName: String): Int {
        val encoding = encodingForModel(modelName)
        return encoding.countTokens(text)
    }

    fun countTokensWithUniversalEncoding(text: String): Int {
        val encoding = Encodings.newDefaultEncodingRegistry().getEncoding(EncodingType.CL100K_BASE)
        return encoding.countTokens(text)
    }
}