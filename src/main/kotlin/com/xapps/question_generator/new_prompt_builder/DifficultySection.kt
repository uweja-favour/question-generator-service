package com.xapps.question_generator.new_prompt_builder

import com.xapps.model.Difficulty
import com.xapps.model.QuestionType
import org.apache.xmlbeans.impl.util.Diff
import org.springframework.stereotype.Component

/**
 * Builds the DIFFICULTY ENGINE section of the generation prompt.
 * Extracted from [PromptBuilder] in line with Single Responsibility Principle.
 */
@Component
class DifficultySection {

    fun build(difficulty: Difficulty, questionType: QuestionType): String = """
        Active difficulty: ${difficulty.name}

        Each question MUST strictly follow the rules for this level.

        ${
            when(difficulty) {
                Difficulty.VERY_EASY -> veryEasy(questionType)
                Difficulty.EASY -> easy(questionType)
                Difficulty.MEDIUM -> medium(questionType)
                Difficulty.HARD -> hard(questionType)
                Difficulty.VERY_HARD -> veryHard(questionType)
            }    
        }
    """.trimIndent()

    private fun veryEasy(questionType: QuestionType): String = """
        --------------------------------
        ${Difficulty.VERY_EASY.name} (weight is 1.0)
        --------------------------------
        - Answer MUST be directly visible in ONE sentence from the content
        - No rewording, no inference, no combination of ideas
        ${
            when(questionType) {
                QuestionType.MC, QuestionType.MS ->
                    """incorrect options MUST be obviously wrong or unrelated"""
                QuestionType.TF -> 
                    "" // 
                QuestionType.FIB -> 
                    """blank removes a single keyword explicit in that sentence"""
            }
        }
        
        FAIL IF: requires more than one sentence, or any interpretation
        
        DIFFICULTY VALIDATION:
        - ${Difficulty.VERY_EASY.name} → answer traceable to a single sentence
        If rule not satisfied → REGENERATE that question
    """.trimIndent()

    private fun easy(questionType: QuestionType): String = """
        --------------------------------
        ${Difficulty.EASY.name} (weight is 2.0)
        --------------------------------
        - Answer derived from ONE sentence; light rewording allowed
        - No multi-step reasoning
        ${
            when(questionType) {
                QuestionType.MC, QuestionType.MS -> 
                    """distractors plausible but clearly distinguishable"""
                QuestionType.TF -> 
                    ""
                QuestionType.FIB -> 
                    """blank may remove a short phrase (1–3 words)"""
            }
        }
        
        FAIL IF: requires combining ideas or inference beyond surface meaning
        
        DIFFICULTY VALIDATION:
        - ${Difficulty.EASY.name} → answer traceable to a single sentence
        If rule not satisfied → REGENERATE that question
    """.trimIndent()

    private fun medium(questionType: QuestionType): String = """
        --------------------------------
        ${Difficulty.MEDIUM.name} (weight is 3.0)
        --------------------------------
        - MUST combine information from AT LEAST 2 distinct ideas
        - Requires basic inference or understanding relationships
        ${
            when(questionType) {
                QuestionType.MC, QuestionType.TF -> 
                    ""
                QuestionType.MS -> 
                    """at least one distractor must be partially correct but not fully correct"""
                QuestionType.FIB ->
                    """blank may represent a concept implied across sentence fragments"""
            }
        }

        FAIL IF: answer can be copied from a single sentence        
        
        DIFFICULTY VALIDATION:
        - ${Difficulty.MEDIUM.name} → at least 2 linked ideas involved
        If rule not satisfied → REGENERATE that question
    """.trimIndent()

    private fun hard(questionType: QuestionType): String = """
        --------------------------------
        ${Difficulty.HARD.name} (weight is 4.0)
        --------------------------------
        - MUST require multi-step reasoning combining multiple concepts
        - Answer cannot appear explicitly in any single sentence
        ${
            when(questionType) {
                QuestionType.MS -> 
                    """differences between correct and incorrect options must be subtle"""
                QuestionType.MC, QuestionType.TF -> 
                    ""
                QuestionType.FIB -> 
                    """blank represents a derived concept, not a directly copied word"""
            }
        }

        FAIL IF: solvable by direct lookup; only one concept involved        
        
        DIFFICULTY VALIDATION:
        - ${Difficulty.HARD.name} → requires reasoning beyond direct extraction
        If rule not satisfied → REGENERATE that question
    """.trimIndent()

    private fun veryHard(questionType: QuestionType): String = """
        --------------------------------
        ${Difficulty.VERY_HARD.name} (weight is 5.0)
        --------------------------------
        - MUST require synthesis, abstraction, or implicit reasoning
        - MUST combine multiple concepts AND interpret their relationship
        ${
            when(questionType) {
                QuestionType.MS -> 
                    """all options must appear correct at first glance"""
                QuestionType.TF, QuestionType.MC -> 
                    ""
                QuestionType.FIB -> 
                    """blank represents an inferred or abstracted concept"""
            }
        }

        FAIL IF: answer is explicitly stated anywhere; no reasoning chain required        
        
        DIFFICULTY VALIDATION:
        - ${Difficulty.VERY_HARD.name} → requires reasoning beyond direct extraction
        If rule not satisfied → REGENERATE that question
    """.trimIndent()
}