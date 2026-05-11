//package com.xapps.question_generator.prompt_builder
//
//import com.xapps.model.QuestionType
//import com.xapps.question_generator.workflow.QuestionSchemaHolder
//import com.xapps.questions.contracts.question_generation.QuestionAllocation
//import org.springframework.stereotype.Component
//
//@Component
//class PromptBuilder(
//    private val contentOptimizer: ContentOptimizer
//) : QuestionSchemaHolder() {
//
//    fun build(
//        content: String,
//        allocation: QuestionAllocation
//    ): String {
//
//        return """
//
//            You are a STRICT QUESTION GENERATION ENGINE.
//
//            You operate in 3 internal phases:
//            (1) PLAN → (2) GENERATE → (3) VALIDATE
//
//            You MUST complete ALL phases before output.
//
//            Any violation = INVALID OUTPUT.
//
//            ================================================
//            INPUT CONFIGURATION
//            ================================================
//
//            Question Type: ${allocation.questionType.name}
//            Difficulty: ${allocation.difficulty.name}
//            Required Count: ${allocation.count}
//
//            ================================================
//            SOURCE CONTENT (ONLY TRUTH SOURCE)
//            ================================================
//
//            You MUST use ONLY this content.
//            You MUST NOT use external knowledge.
//
//            ${contentOptimizer.optimizeContent(content)}
//
//            ================================================
//            SCHEMA CONTRACT (STRICT)
//            ================================================
//
//            ${schemaFor(allocation)}
//
//            ================================================
//            GLOBAL GENERATION INVARIANTS (CRITICAL)
//            ================================================
//
//            - You MUST generate EXACTLY ${allocation.count} questions
//            - No duplicates across questions
//            - Every question must be answerable from content only
//            - No hallucinated facts
//            - No summarization of entire content
//            - Each question must test ONE concept only
//
//            ================================================
//            DIFFICULTY ENGINE (STRICT AND MEASURABLE)
//            ================================================
//
//            Difficulty: ${allocation.difficulty.name}
//
//            Each question MUST strictly follow the rules for this difficulty level.
//
//            --------------------------------
//            VERY_EASY (weight ≈ 1.0)
//            --------------------------------
//
//            Construction Rules:
//            - Answer MUST be directly visible in ONE sentence from the content
//            - No rewording required
//            - No inference required
//            - No combination of ideas
//
//            FIB:
//            - Blank MUST remove a single keyword explicitly present in the sentence
//
//            MS/MC:
//            - Incorrect options MUST be obviously wrong or unrelated
//
//            FAIL IF:
//            - Requires reading more than one sentence
//            - Requires interpretation or reasoning
//
//            --------------------------------
//            EASY (weight ≈ 2.0)
//            --------------------------------
//
//            Construction Rules:
//            - Answer derived from ONE sentence
//            - Light rewording allowed
//            - No multi-step reasoning
//
//            FIB:
//            - Blank may remove a short phrase (1–3 words)
//
//            MS/MC:
//            - Distractors must be plausible but clearly distinguishable
//
//            FAIL IF:
//            - Requires combining multiple ideas
//            - Requires inference beyond surface meaning
//
//            --------------------------------
//            MEDIUM (weight ≈ 3.0)
//            --------------------------------
//
//            Construction Rules:
//            - MUST combine information from AT LEAST 2 distinct ideas in the content
//            - Requires basic inference or understanding relationships
//
//            FIB:
//            - Blank may represent a concept implied across sentence fragments
//
//            MS:
//            - Distractors MUST be plausible and contextually related
//            - At least one distractor must be partially correct but not fully correct
//
//            FAIL IF:
//            - Answer can be copied directly from a single sentence
//            - No reasoning required
//
//            --------------------------------
//            HARD (weight ≈ 4.0)
//            --------------------------------
//
//            Construction Rules:
//            - MUST require multi-step reasoning
//            - MUST combine multiple concepts OR compare them
//            - Answer cannot appear explicitly in one place
//
//            FIB:
//            - Blank must represent a derived concept, not a directly copied word
//
//            MS:
//            - Distractors MUST be highly plausible and closely related
//            - Differences between correct and incorrect options must be subtle
//
//            FAIL IF:
//            - Question is solvable via direct lookup
//            - Only one concept is involved
//
//            --------------------------------
//            VERY_HARD (weight ≈ 5.0)
//            --------------------------------
//
//            Construction Rules:
//            - MUST require synthesis, abstraction, or implicit reasoning
//            - MUST involve:
//              - combining multiple concepts AND
//              - interpreting their relationship
//
//            FIB:
//            - Blank must represent an inferred or abstracted concept
//
//            MS:
//            - Distractors must be nearly indistinguishable without deep understanding
//            - All options must appear correct at first glance
//
//            FAIL IF:
//            - Answer is explicitly stated anywhere in content
//            - No reasoning chain is required
//
//            ================================================
//            DIFFICULTY VALIDATION (MANDATORY)
//            ================================================
//
//            Before output, verify EACH question:
//
//            - VERY_EASY/EASY:
//              → Answer must be traceable to a single sentence
//
//            - MEDIUM:
//              → Must involve at least 2 linked ideas
//
//            - HARD/VERY_HARD:
//              → Must require reasoning beyond direct extraction
//
//            If difficulty rules are not satisfied:
//            → REGENERATE that question
//
//            ================================================
//            QUESTION TYPE CONSTRAINTS
//            ================================================
//
//            -----------------------------
//            FIB (Fill in Blank)
//            -----------------------------
//
//            - MUST contain exactly ONE ___
//            - Must derive from a real sentence in content
//            - Only ONE word/short phrase removed
//
//            acceptableAnswers MUST:
//            - be at least 2 items
//            - be at most 5 items
//            - be interchangeable in SAME sentence
//            - be grammatically valid in blank
//            - NOT change sentence meaning
//
//            INVALID IF:
//            - < 2 answers
//            - no blank
//            - multiple blanks
//
//            -----------------------------
//            MS (Multi-Select)
//            -----------------------------
//
//            - MUST have 4 options minimum
//            - CORRECT_OPTIONS MUST be 2 or 3 items ONLY
//            - MUST contain at least 1 incorrect option
//
//            CORRECT_OPTIONS RULE:
//            - Must be exact subset of OPTIONS
//            - Must match character-for-character
//
//            INVALID IF:
//            - only 1 correct option
//            - all options are correct
//            - any new string introduced
//
//            -----------------------------
//            TF (True/False)
//            -----------------------------
//
//            GLOBAL BALANCE RULE (MANDATORY):
//            - At least 40% of TF questions must be FALSE
//
//            GENERATION RULE:
//            - You MUST create both true and intentionally false statements
//            - False questions must be plausible but incorrect
//
//            INVALID IF:
//            - all answers are True
//
//            ================================================
//            PHASE 1: PLANNING (MANDATORY INTERNAL STEP)
//            ================================================
//
//            Before generating:
//            - Decide distribution of concepts
//            - Ensure difficulty alignment per question
//            - Ensure MS/FIB/T/F constraints are satisfiable
//            - Map each question to a distinct concept
//
//            DO NOT SKIP THIS PHASE
//
//            ================================================
//            PHASE 2: GENERATION
//            ================================================
//
//            Generate each question one-by-one:
//            - fully complete object
//            - fully schema compliant
//            - no placeholders
//
//            ================================================
//            PHASE 3: VALIDATION (HARD GATE)
//            ================================================
//
//            Before output, verify:
//
//            FOR ALL QUESTIONS:
//            - schema match EXACT
//            - no missing fields
//            - no extra fields
//
//            FOR FIB:
//            - exactly 1 blank
//            - ≥2 acceptableAnswers
//
//            FOR MS:
//            - 2–3 correct options ONLY
//            - at least 1 incorrect option
//
//            FOR TF:
//            - not all True
//            - must include False distribution
//
//            FOR DIFFICULTY:
//            - matches cognitive rules above
//
//            IF ANY FAILURE:
//            → regenerate ONLY failed question(s)
//
//            ================================================
//            OUTPUT FORMAT (ABSOLUTE)
//            ================================================
//
//            Return ONLY:
//
//            {
//              "$QUESTIONS": [
//                ${fixedSlots(allocation.count)}
//              ]
//            }
//
//            ================================================
//            FINAL RULE
//            ================================================
//
//            Do not explain.
//            Do not summarize.
//            Do not output anything outside JSON.
//            Only return valid structured output.
//        """.trimIndent()
//    }
//
//    private fun fixedSlots(count: Int): String =
//        (1..count).joinToString(",\n") {
//            """{ "__SLOT__": $it }"""
//        }
//
//    private fun schemaFor(allocation: QuestionAllocation): String =
//        when (allocation.questionType) {
//
//            QuestionType.MC -> """
//            ALLOWED SCHEMA:
//            {
//              "$TEXT": "string",
//              "$TOPIC": "string",
//              "$DIFFICULTY": "${allocation.difficulty.name}",
//              "$EXPLANATION": "string",
//              "$QUESTION_TYPE": "${QuestionType.MC.name}",
//              "$OPTIONS": [
//                "string",
//                "string",
//                "string",
//                "string"
//              ],
//              "$CORRECT_OPTION_TEXT": "string"
//            }
//
//            RULES:
//            - $OPTIONS MUST be an array of strings
//            - $CORRECT_OPTION_TEXT MUST be EXACTLY one value from $OPTIONS
//        """.trimIndent()
//
//            QuestionType.MS -> """
//            ALLOWED SCHEMA:
//            {
//              "$TEXT": "string",
//              "$TOPIC": "string",
//              "$DIFFICULTY": "${allocation.difficulty.name}",
//              "$EXPLANATION": "string",
//              "$QUESTION_TYPE": "${QuestionType.MS.name}",
//              "$OPTIONS": [
//                "string",
//                "string",
//                "string",
//                "string"
//              ],
//              "$CORRECT_OPTIONS": [
//                "string"
//              ]
//            }
//
//            RULES:
//            - $OPTIONS MUST be an array of strings
//            - Each value in correctOptions MUST be copied EXACTLY from the options array
//            - Do NOT rephrase, summarize, or modify option text
//            - The strings must match character-for-character
//            - If a correct answer is not present in options, you MUST add it to options instead of creating a new string
//            - If you cannot find an exact match in options, you MUST regenerate the question
//            - NEVER introduce a new string in correctOptions that does not exist in options
//            - $CORRECT_OPTIONS MUST contain at least one value
//        """.trimIndent()
//
//            QuestionType.TF -> """
//            ALLOWED SCHEMA:
//            {
//              "$TEXT": "string",
//              "$TOPIC": "string",
//              "$DIFFICULTY": "${allocation.difficulty.name}",
//              "$EXPLANATION": "string",
//              "$QUESTION_TYPE": "${QuestionType.TF.name}",
//              "$OPTIONS": [
//                "True",
//                "False"
//              ],
//              "$CORRECT_OPTION_TEXT": "True"
//            }
//
//            RULES:
//            - $OPTIONS MUST be exactly ["True", "False"]
//            - $CORRECT_OPTION_TEXT MUST be either "True" or "False"
//        """.trimIndent()
//
//            QuestionType.FIB -> """
//            ALLOWED SCHEMA:
//            {
//              "$TEXT": "string containing exactly one ___ placeholder",
//              "$TOPIC": "string",
//              "$DIFFICULTY": "${allocation.difficulty.name}",
//              "$EXPLANATION": "string",
//              "$QUESTION_TYPE": "${QuestionType.FIB.name}",
//              "$ACCEPTABLE_ANSWERS": [
//                "string"
//              ]
//            }
//
//            RULES:
//            - The text MUST contain exactly ONE occurrence of "___"
//            - The blank MUST represent a missing word or short phrase from the sentence
//            - Each value in acceptableAnswers MUST correctly and grammatically replace the blank
//            - acceptableAnswers MUST contain ONLY valid answers for the blank
//            - acceptableAnswers MUST NOT contain duplicates
//            - acceptableAnswers MUST NOT contain full sentences
//            - Each answer MUST be concise (1–5 words max)
//            - Do NOT include punctuation in answers unless required
//            - All answers MUST be semantically equivalent (no unrelated alternatives)
//            - When any acceptableAnswer replaces "___" in the text, the sentence MUST be correct and complete
//            - Do NOT rephrase the sentence differently for different answers
//            - Do NOT generate answers that change the meaning of the sentence
//            - Do NOT include explanations inside acceptableAnswers
//            - Do NOT include multiple blanks
//            - All acceptableAnswers MUST be interchangeable in the same sentence without modifying any other word
//        """.trimIndent()
//        }
//}