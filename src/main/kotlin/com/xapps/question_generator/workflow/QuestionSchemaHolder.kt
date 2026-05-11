package com.xapps.question_generator.workflow

open class QuestionSchemaHolder {
    companion object {
        const val TEXT = "text"

        // list of option texts
        const val OPTIONS = "options"

        const val ACCEPTABLE_ANSWERS = "acceptableAnswers"

        // correct option text
        const val CORRECT_OPTION_TEXT = "correctOption"

        const val CORRECT_OPTIONS = "correctOptions"
        const val EXPLANATION = "explanation"
        const val TOPIC = "topic"
        const val QUESTION_TYPE = "questionType"
        const val DIFFICULTY = "difficulty"
        const val QUESTIONS = "questions"
    }
}