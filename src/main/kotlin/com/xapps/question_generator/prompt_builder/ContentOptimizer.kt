package com.xapps.question_generator.prompt_builder

import org.springframework.stereotype.Component

interface ContentOptimizer {
    fun optimizeContent(content: String): String
}

@Component
class DefaultContentOptimizer : ContentOptimizer {

    override fun optimizeContent(content: String): String {
        val seen = LinkedHashSet<String>()
        return content
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .filter { seen.add(it) }   // preserves order, removes exact-duplicate lines only
            .joinToString("\n")
    }
}