package com.xapps.question_generator.note_summary_workflow.ai_text_cleaner

/**
 * Utility responsible for removing formatting artifacts commonly produced by AI models.
 *
 * Supported formatting sources include:
 * - ChatGPT
 * - Claude
 * - Gemini
 * - Copilot
 * - DeepSeek
 * - Markdown-based LLM responses
 *
 * The cleaner removes presentation syntax while preserving readable textual content.
 *
 * Example:
 *
 * Input:
 * ```text
 * ## Hello
 *
 * **Important**
 *
 * [OpenAI](https://openai.com)
 * ```
 *
 * Output:
 * ```text
 * Hello
 *
 * Important
 *
 * OpenAI
 * ```
 */
object AiTextCleaner {

    /**
     * Cleans AI-generated text by removing markdown, HTML,
     * and formatting syntax while preserving readable content.
     *
     * This function sequentially applies all available cleanup operations.
     *
     * Removed formatting includes:
     * - Markdown headers
     * - Bold / italic markers
     * - Code blocks
     * - Inline code formatting
     * - Block quotes
     * - Bullet list markers
     * - Numbered list markers
     * - Markdown links
     * - HTML tags
     * - Horizontal separators
     * - Markdown table syntax
     * - Excessive whitespace
     *
     * Example:
     *
     * Input:
     * ```text
     * ## Welcome
     *
     * **Hello World**
     *
     * > Quote
     * ```
     *
     * Output:
     * ```text
     * Welcome
     *
     * Hello World
     *
     * Quote
     * ```
     *
     * @param rawText Raw AI-generated content that may contain formatting syntax.
     *
     * @return Plain readable text without formatting artifacts.
     */
    fun clean(rawText: String): String {
        return rawText
//            .removeCodeBlocks()
            .removeInlineCode()
            .removeMarkdownHeaders()
            .removeBoldItalicFormatting()
            .removeBlockQuotes()
//            .removeBulletFormatting()
//            .removeNumberedListFormatting()
            .removeMarkdownLinks()
            .removeHtmlTags()
//            .removeHorizontalRules()
//            .removeTables()
            .normalizeWhitespace()
            .trim()
    }

    /**
     * Removes fenced markdown code blocks.
     *
     * Supported formats:
     * - ```kotlin ... ```
     * - ```json ... ```
     * - ``` ... ```
     *
     * The entire code block including its contents is removed.
     *
     * Example:
     *
     * Input:
     * ```text
     * Before
     *
     * ```kotlin
     * val x = 10
     * ```
     *
     * After
     * ```
     *
     * Output:
     * ```text
     * Before
     *
     * After
     * ```
     *
     * @return Text without fenced code blocks.
     */
    private fun String.removeCodeBlocks(): String {
        return replace(Regex("```[\\s\\S]*?```"), "")
    }

    /**
     * Removes inline markdown code markers while preserving the inner content.
     *
     * Example:
     *
     * Input:
     * ```text
     * Use `println()` to print values.
     * ```
     *
     * Output:
     * ```text
     * Use println() to print values.
     * ```
     *
     * @return Text without inline backtick formatting.
     */
    private fun String.removeInlineCode(): String {
        return replace(Regex("`([^`]*)`"), "$1")
    }

    /**
     * Removes markdown header prefixes.
     *
     * Supported headers:
     * - # Header
     * - ## Header
     * - ### Header
     * - #### Header
     * - ##### Header
     * - ###### Header
     *
     * Example:
     *
     * Input:
     * ```text
     * ## Important Section
     * ```
     *
     * Output:
     * ```text
     * Important Section
     * ```
     *
     * @return Text without markdown header symbols.
     */
    private fun String.removeMarkdownHeaders(): String {
        return replace(Regex("(?m)^#{1,6}\\s*"), "")
    }

    /**
     * Removes markdown emphasis formatting while preserving the content.
     *
     * Supported formatting:
     * - Bold: **text**
     * - Italic: *text*
     * - Combined: ***text***
     * - Underscore variants
     * - Strikethrough: ~~text~~
     *
     * Example:
     *
     * Input:
     * ```text
     * **Important**
     * ```
     *
     * Output:
     * ```text
     * Important
     * ```
     *
     * @return Text without emphasis formatting markers.
     */
    private fun String.removeBoldItalicFormatting(): String {
        return this
            .replace(Regex("""\*\*\*(.*?)\*\*\*"""), "$1")
            .replace(Regex("""___(.*?)___"""), "$1")
            .replace(Regex("""\*\*(.*?)\*\*"""), "$1")
            .replace(Regex("""__(.*?)__"""), "$1")
            .replace(Regex("""\*(.*?)\*"""), "$1")
            .replace(Regex("""_(.*?)_"""), "$1")
            .replace(Regex("""~~(.*?)~~"""), "$1")
    }

    /**
     * Removes markdown block quote prefixes.
     *
     * Example:
     *
     * Input:
     * ```text
     * > This is a quote
     * ```
     *
     * Output:
     * ```text
     * This is a quote
     * ```
     *
     * @return Text without block quote markers.
     */
    private fun String.removeBlockQuotes(): String {
        return replace(Regex("(?m)^>\\s?"), "")
    }

    /**
     * Removes unordered list markers.
     *
     * Supported markers:
     * - -
     * - *
     * - +
     *
     * Example:
     *
     * Input:
     * ```text
     * - Apple
     * - Banana
     * ```
     *
     * Output:
     * ```text
     * Apple
     * Banana
     * ```
     *
     * @return Text without unordered list prefixes.
     */
    private fun String.removeBulletFormatting(): String {
        return replace(Regex("(?m)^\\s*[-*+]\\s+"), "")
    }

    /**
     * Removes numbered list prefixes.
     *
     * Example:
     *
     * Input:
     * ```text
     * 1. First item
     * 2. Second item
     * ```
     *
     * Output:
     * ```text
     * First item
     * Second item
     * ```
     *
     * @return Text without numbered list prefixes.
     */
    private fun String.removeNumberedListFormatting(): String {
        return replace(Regex("(?m)^\\s*\\d+\\.\\s+"), "")
    }

    /**
     * Removes markdown link syntax while preserving visible text.
     *
     * Example:
     *
     * Input:
     * ```text
     * [OpenAI](https://openai.com)
     * ```
     *
     * Output:
     * ```text
     * OpenAI
     * ```
     *
     * @return Text without markdown link formatting.
     */
    private fun String.removeMarkdownLinks(): String {
        return replace(
            Regex("""\[(.*?)]\((.*?)\)"""),
            "$1"
        )
    }

    /**
     * Removes HTML and XML tags.
     *
     * Example:
     *
     * Input:
     * ```text
     * <b>Hello</b>
     * ```
     *
     * Output:
     * ```text
     * Hello
     * ```
     *
     * @return Text without HTML or XML tags.
     */
    private fun String.removeHtmlTags(): String {
        return replace(Regex("<[^>]+>"), "")
    }

    /**
     * Removes markdown horizontal separators.
     *
     * Supported formats:
     * - ---
     * - ***
     * - ___
     *
     * Example:
     *
     * Input:
     * ```text
     * ---
     * ```
     *
     * Output:
     * ```text
     * ""
     * ```
     *
     * @return Text without horizontal separator lines.
     */
    private fun String.removeHorizontalRules(): String {
        return replace(
            Regex("(?m)^\\s*([-*_]){3,}\\s*$"),
            ""
        )
    }

    /**
     * Removes markdown table formatting while preserving table content.
     *
     * Table separators are removed and pipe characters are converted to spaces.
     *
     * Example:
     *
     * Input:
     * ```text
     * | Name | Age |
     * |------|-----|
     * | John | 25  |
     * ```
     *
     * Output:
     * ```text
     * Name Age
     * John 25
     * ```
     *
     * @return Text without markdown table syntax.
     */
    private fun String.removeTables(): String {
        return lines()
            .filterNot { line ->
                line.matches(Regex("""^\s*\|?(\s*:?-+:?\s*\|)+\s*$"""))
            }
            .joinToString("\n") { line ->
                line.replace("|", " ")
            }
    }

    /**
     * Normalizes whitespace for cleaner readable output.
     *
     * Applied normalization:
     * - Converts multiple spaces into a single space
     * - Converts excessive blank lines into a maximum of two
     *
     * Example:
     *
     * Input:
     * ```text
     * Hello      World
     *
     *
     *
     * Next
     * ```
     *
     * Output:
     * ```text
     * Hello World
     *
     * Next
     * ```
     *
     * @return Text with normalized whitespace.
     */
    private fun String.normalizeWhitespace(): String {
        return this
            .replace(Regex("[ \t]+"), " ")
            .replace(Regex("\n{3,}"), "\n\n")
    }
}