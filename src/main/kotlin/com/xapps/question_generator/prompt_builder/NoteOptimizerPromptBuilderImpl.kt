package com.xapps.question_generator.prompt_builder

import org.springframework.stereotype.Service

@Service
class NoteOptimizerPromptBuilderImpl(
    private val contentOptimizer: ContentOptimizer
) : NoteOptimizerPromptBuilder {

    override fun buildOptimizationPrompt(note: String): String {
        val optimizedNote = contentOptimizer.optimizeContent(note)

        return buildString {
            appendLine("You are an AI specialized in optimizing educational notes for AI question generation.")
            appendLine()
            appendLine("TASK: Compress the note while preserving all content needed to generate meaningful exam questions.")
            appendLine()
            appendLine("RULES:")
            appendLine("1. TARGET LENGTH: Reduce to 40-50% of original length")
            appendLine("2. PRESERVE:")
            appendLine("   - All definitions, concepts, and terminology")
            appendLine("   - Key examples and scenarios (these become question material)")
            appendLine("   - Technical details: algorithms, protocols, steps, phases")
            appendLine("   - Comparisons and contrasts between concepts")
            appendLine("   - Numbered/named items (e.g., 'Three types:', 'Phase 1:', 'ACID properties')")
            appendLine("3. REMOVE:")
            appendLine("   - Redundant explanations of the same concept")
            appendLine("   - Conversational filler ('As mentioned earlier', 'In summary', 'It is important to note')")
            appendLine("   - Meta-commentary about the document itself")
            appendLine("   - Duplicate information across pages")
            appendLine("4. FORMAT:")
            appendLine("   - Write in clear, dense prose using complete sentences")
            appendLine("   - Use concise paragraphs (2-4 sentences each)")
            appendLine("   - Preserve logical section breaks with blank lines")
            appendLine("   - NO bullet points, NO markdown, NO added commentary")
            appendLine("5. OUTPUT:")
            appendLine("   - Return ONLY the optimized note content")
            appendLine("   - Start directly with the content (no preamble like 'Here is...')")
            appendLine()
            appendLine("ORIGINAL NOTE:")
            appendLine(optimizedNote)
        }
    }
}
