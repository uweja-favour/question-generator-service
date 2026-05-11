package com.xapps.question_generator.prompt_builder

/**
 * Builds AI prompts that optimize large notes by removing unnecessary content
 * while preserving all essential information for question generation.
 */
interface NoteOptimizerPromptBuilder {

    /**
     * Constructs an AI prompt that instructs the model to optimize the given note.
     *
     * @param note The original note, potentially very large (~20k tokens)
     * @param targetTokenCount Approximate token count for the optimized note (e.g., 1500–2000)
     * @return A fully formed AI prompt ready for model input
     */
    fun buildOptimizationPrompt(note: String): String
}
