package com.newAi302.app.utils

import android.graphics.Color
import android.text.style.URLSpan
import android.util.Log
import io.noties.markwon.linkify.LinkifyPlugin
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.random.Random

/**
 * author :
 * e-mail :
 * time   : 2025/5/23
 * desc   :
 * version: 1.0
 */
object StringObjectUtils {
    /**
     * 从包含HTML代码块的字符串中提取HTML内容
     * @param source 原始字符串（格式示例："前缀：```html...```后缀"）
     * @return 提取到的HTML内容（无匹配时返回空字符串）
     */
    fun extractHtmlFromMarkdown(source: String): String {
        // 定义Markdown代码块的起始和结束标记
        var startTag = "```html"
        if (source.contains("html")){
            startTag = "```html"
        }else if (source.contains("python")){
            startTag = "```python"
        }else if (source.contains("java"))
            startTag = "```java"
        else {
            startTag = "```"
        }
        val endTag = "```"

        // 查找起始标记的位置（从0开始搜索）
        val startIndex = source.indexOf(startTag)
        if (startIndex == -1) {
            return "" // 无起始标记，直接返回空
        }

        // 计算HTML内容的起始位置（起始标记结束后的位置）
        val htmlStart = startIndex + startTag.length

        // 查找结束标记的位置（从htmlStart之后开始搜索）
        val endIndex = source.indexOf(endTag, startIndex = htmlStart)
        if (endIndex == -1) {
            return "" // 无结束标记，返回空
        }

        // 截取HTML内容（注意endIndex是结束标记的起始位置，因此结束位置是endIndex）
        return source.substring(htmlStart, endIndex)
    }

    fun extractHtmlFromMarkdownCode1(source: String): String {
        // 提取所有可能的代码块
        val codeBlockRegex = Regex("```(\\w+)?\\s*([\\s\\S]*?)\\s*```")
        val matches = codeBlockRegex.findAll(source)

        // 查找第一个匹配的代码块（优先按语言类型）
        for (match in matches) {
            val language = match.groupValues[1]?.lowercase() ?: ""
            val codeContent = match.groupValues[2]

            // 如果找到了目标语言的代码块，返回其内容
            if (language == "python" || language == "html" || language == "java") {
                return codeContent
            }

            // 如果没有指定语言，但代码块存在，也返回
            if (language.isEmpty() && codeContent.isNotEmpty()) {
                return codeContent
            }
        }

        return "" // 没有找到任何代码块
    }

    fun extractHtmlFromMarkdownCode(source: String): String {
        // 定义Markdown代码块的正则表达式，包含开始和结束标记
        val codeBlockRegex = Regex("```(\\w+)?[\\s\\S]*?```")

        // 查找第一个匹配的代码块
        val matchResult = codeBlockRegex.find(source)

        // 如果找到匹配项，则返回完整的代码块（包括开始和结束标记）
        return matchResult?.value ?: ""
    }
    fun extractCodeFromMarkdown(source: String): String {
        // 正则说明：
        // 1. ```(\\w+)? → 匹配开头的```，可选的语言标识（如html/python，不限制具体语言）
        // 2. \\s* → 匹配语言标识后的空白（固定包含\n，也兼容可能的空格）
        // 3. ([\\s\\S]*?) → 捕获组2：非贪婪匹配所有内容（从\n开始到结尾```前，含\n）
        // 4. \\s*``` → 匹配结尾的```（兼容前后可能的空格）
        val codeBlockRegex = Regex("```(\\w+)?\\s*([\\s\\S]*?)\\s*```")

        // 查找第一个匹配的代码块
        val matchResult = codeBlockRegex.find(source)

        // 提取捕获组2的内容（即\n开头的目标字符串，不含```和语言标识）
        return matchResult?.groupValues?.getOrNull(2) ?: ""
    }

    fun extractPythonCodeFromMarkdown(markdown: String): String {
        // 匹配 ```python 开头、``` 结尾的代码块，支持多行
        val codeBlockRegex = Regex(
            "```\\s*([\\s\\S]*?)\\s*```",
            RegexOption.DOT_MATCHES_ALL
        )
        // 提取所有匹配的代码块内容
        val codeBlocks = codeBlockRegex.findAll(markdown)
            .map { it.groupValues[1].trim() } // 提取捕获组并去除首尾空格
            .filter { it.isNotEmpty() } // 过滤空代码块
            .toList()
        // 用换行连接所有代码块
        return codeBlocks.joinToString("\n\n")
    }

    /**
     * 识别代码字符串所属的编程语言
     * @param code 待识别的代码字符串
     * @return 识别出的语言名称（如"Java"、"Kotlin"等），无法识别则返回"Unknown"
     */
    fun detectProgrammingLanguage(code: String): String {
        // 预处理：去除空白字符，便于模式匹配
        val trimmedCode = code.trim()
        if (trimmedCode.isEmpty()) {
            return ".txt"
        }

        // 1. 检测HTML（特征：包含<html>、<body>等标签，或大量<>标签）
        if (Pattern.compile("<(html|body|div|p|span|head|title|script|style)", Pattern.CASE_INSENSITIVE).matcher(trimmedCode).find()
            || trimmedCode.startsWith("<!DOCTYPE html", ignoreCase = true)
            || Pattern.compile("<[^>]+>").matcher(trimmedCode).find() // 存在任意标签
        ) {
            return ".html"
        }

        // 2. 检测CSS（特征：包含选择器和样式声明，如body { ... } 或 .class { ... }）
        if (Pattern.compile("[a-zA-Z0-9#.]+\\s*\\{[^}]+\\}").matcher(trimmedCode).find()
            || Pattern.compile("(color|font-size|margin|padding|border)\\s*:").matcher(trimmedCode).find()
        ) {
            return ".css"
        }

        // 3. 检测Python（特征：存在def/class/import关键字，无分号结尾，使用缩进来分隔代码块）
        if (Pattern.compile("\\b(def|class|import|from|print|if|else|elif|for|while|return)\\b").matcher(trimmedCode).find()
            && !Pattern.compile(";\\s*$", Pattern.MULTILINE).matcher(trimmedCode).find() // 行尾无分号
            && !Pattern.compile("\\{\\s*\\}").matcher(trimmedCode).find() // 无空大括号（排除其他语言）
        ) {
            return ".python"
        }

        // 4. 检测Kotlin（特征：存在fun/var/val关键字，可能有class但无public class）
        if (Pattern.compile("\\b(fun|var|val|object|data class|override)\\b").matcher(trimmedCode).find()
            || Pattern.compile(":\\s*[A-Za-z]+\\s*=").matcher(trimmedCode).find() // 变量声明带类型（如var x: Int = 0）
        ) {
            return ".kotlin"
        }

        // 5. 检测Java（特征：存在public class、extends、implements等）
        if (Pattern.compile("\\b(public class|extends|implements|void|int|String|new)\\b").matcher(trimmedCode).find()
            || Pattern.compile("class\\s+[A-Z]").matcher(trimmedCode).find() // 类名大写开头（Java规范）
        ) {
            return ".java"
        }

        // 可扩展其他语言（如JavaScript、C++等）
        return ".txt"
    }

    /**
     * 生成随机英文文件名
     * @param minLength 文件名主体最小长度（不含扩展名）
     * @param maxLength 文件名主体最大长度（不含扩展名）
     * @return 随机生成的文件名（如：xY7pK2d.txt）
     */
    fun generateRandomFilename(
        minLength: Int = 6,
        maxLength: Int = 12
    ): String {
        // 定义可能的文件扩展名（英文常见格式）
        val extensions = listOf("txt", "doc", "pdf", "jpg", "png", "csv", "json", "xml", "html", "css", "js")

        // 生成随机长度的文件名主体（在min和max之间）
        val length = Random.nextInt(minLength, maxLength + 1)

        // 生成文件名主体：包含大小写字母和数字
        val filenameBuilder = StringBuilder()
        for (i in 0 until length) {
            // 随机选择字符类型：0-25=大写字母，26-51=小写字母，52-61=数字
            val charType = Random.nextInt(62)
            val char = when {
                charType < 26 -> 'A' + charType // 大写字母
                charType < 52 -> 'a' + (charType - 26) // 小写字母
                else -> '0' + (charType - 52) // 数字
            }
            filenameBuilder.append(char)
        }

        // 随机选择一个扩展名
        val extension = extensions.random()

        //return "${filenameBuilder}.${extension}"
        return "${filenameBuilder}"
    }


    /**
     * 从字符串中提取```html代码块内容
     * @param source 原始字符串（格式示例："前缀：```html...```后缀"）
     * @return 提取到的HTML内容（无匹配时返回空字符串）
     */
    fun extractHtml(source: String): String {
        // 定义代码块的起始和结束标记
        var startMarker = "```html"
        if (source.contains("html")){
            startMarker = "```html"
        }else{
            startMarker = "```"
        }
        val endMarker = "```"

        // 查找起始标记的位置（注意：起始标记可能包含换行，这里假设紧接内容）
        val startIndex = source.indexOf(startMarker)
        if (startIndex == -1) return ""  // 无起始标记

        // 计算HTML内容的起始位置（跳过起始标记）
        val contentStart = startIndex + startMarker.length

        // 查找结束标记的位置（从内容起始位置后开始搜索）
        val endIndex = source.indexOf(endMarker, startIndex = contentStart)
        if (endIndex == -1) return ""  // 无结束标记

        // 截取并返回HTML内容（注意：endIndex是结束标记的起始位置）
        return "$startMarker${source.substring(contentStart, endIndex)}$endMarker"
    }

    /**
     * 自动识别并转换公式格式
     * 将 (公式) 转换为 $$公式$$
     */
    fun processFormulas1(text: String): String {
        // 使用正则表达式匹配所有被括号包裹的公式
        val regex = """\(([^()]+)\)""".toRegex()

        // 替换匹配的公式
        return regex.replace(text) { matchResult ->
            val formula = matchResult.groupValues[1]
            // 转换为块级公式格式
            "\n\n$$$formula$$\n\n"
        }
    }

    fun convertLatexFormat(text: String): String {
        // 将 \(...\) 转换为 $...$
        return text.replace(Regex("\\\\\\((.*?)\\\\\\)|\\\\\\[(.*?)\\\\\\]|\\\\[(.*?)\\\\]|\\\\[\\\\s*([^\\\\]](.*?)\\\\s*\\\\]|\\\$(.*?)\\\$")) { match ->
            // 获取第一个捕获组（\(...\)）或第二个捕获组（\[...\]）的内容
            val formula = match.groupValues[0].takeIf { it.isNotEmpty() }
                ?: match.groupValues[1].takeIf { it.isNotEmpty() }
                ?: match.groupValues[2].takeIf { it.isNotEmpty() }
                ?: match.groupValues[3].takeIf { it.isNotEmpty() }
                ?: match.groupValues[4]
            if (formula.contains("\n")) {
                "$$${formula}$$"  // 包含换行符的转换为块级公式
            }else if (match.groupValues[4].isNotEmpty()){
                "$${formula}$"
            }
            else {
                "$$${formula}$$"  // 行内公式
            }
        }
    }

    fun convertLatexFormat1(text: String): String {
        // 关键1：添加 RegexOption.DOT_MATCHES_ALL 支持 . 匹配换行符（解决多行公式问题）
        val regex = Regex(
            "\\\\\\((.*?)\\\\\\)|" +  // 1. 匹配 \(...\) → 捕获组1：纯公式内容
                    "\\\\\\[(.*?)\\\\\\]|" +  // 2. 匹配 \[...\] → 捕获组2：纯公式内容（支持多行）
                    "\\\\[(.*?)\\\\]|" +      // 3. 原有 \[...\] 变体 → 捕获组3：纯公式内容
                    "\\\\[\\\\s*([^\\\\])(.*?)\\\\s*\\\\]|" +  // 4. 原有空格变体 → 捕获组4+5：纯公式内容
                    "\\\$(.*?)\\\$|" +        // 5. 匹配 $...$ → 捕获组6：纯公式内容
                    "\\[(.*?)\\]|" +          // 6. 匹配 [...] → 捕获组7：纯公式内容
                    "%-+(.*?)%-+"  +           // 7. 匹配 %---...%--- → 捕获组8：纯公式内容
                    "\\\\(.*?)\\\\+"             // 8. 匹配 \...\ → 捕获组8：纯公式内容
            , RegexOption.DOT_MATCHES_ALL // 允许 . 匹配换行符，支持多行公式
        )

        return text.replace(regex) { match ->
            // 安全提取公式：先判断捕获组是否存在，再取值（避免越界）
            val formula = when {
                // 按捕获组编号顺序提取，优先非空值
                match.groupValues.size > 1 && match.groupValues[1].isNotEmpty() -> match.groupValues[1]  // 规则1：\(...\)
                match.groupValues.size > 2 && match.groupValues[2].isNotEmpty() -> match.groupValues[2]  // 规则2：\[...\]
                match.groupValues.size > 3 && match.groupValues[3].isNotEmpty() -> match.groupValues[3]  // 规则3：\[...\]变体
                // 规则4：空格变体（拼接捕获组4和5）
                match.groupValues.size > 5 && (match.groupValues[4].isNotEmpty() || match.groupValues[5].isNotEmpty()) ->
                    match.groupValues[4] + match.groupValues[5]
                match.groupValues.size > 6 && match.groupValues[6].isNotEmpty() -> match.groupValues[6]  // 规则5：$...$
                match.groupValues.size > 7 && match.groupValues[7].isNotEmpty() -> match.groupValues[7]  // 规则6：[...]
                match.groupValues.size > 8 && match.groupValues[8].isNotEmpty() -> match.groupValues[8]  // 规则7：\...\
                match.groupValues.size > 9 && match.groupValues[9].isNotEmpty() -> match.groupValues[9]  // 规则8：%---...%---
                else -> ""  // 无匹配时返回空（避免空指针）
            }.trim()  // 统一去除首尾空格

            // 原有替换逻辑
            if (formula.contains("\n")) {
                "$$${formula}$$"
            } else if (match.groupValues.size > 4 && match.groupValues[4].isNotEmpty()) {
                "$${formula}$"
            } else {
                "$$${formula}$$"
            }
        }
    }

    fun convertLatexFormat2(text: String): String {
        // 新增匹配 %-------------------------....%------------------------- 格式
        // 规则：% 开头 + 至少1个连字符(-) + 内容 + % 开头 + 至少1个连字符(-)
        val regex = Regex(
            "\\\\\\((.*?)\\\\\\)|" +  // 匹配 \(...\)
                    "\\\\\\[(.*?)\\\\\\]|" +  // 匹配 \[...\]
                    "\\\\[(.*?)\\\\]|" +      // 原有其他\[...\]变体
                    "\\\\[\\\\s*([^\\\\]](.*?)\\\\s*\\\\]|" +  // 原有空格变体
                    "\\\$(.*?)\\\$|" +        // 匹配 $...$
                    "%-+(.*?)%-+"             // 新增：匹配 %---...%--- 格式（%+连字符包裹内容）
            // 说明：%-+ 表示 % 后接至少1个连字符(-)；(.*?) 匹配中间内容；整体非贪婪匹配
        )

        return text.replace(regex) { match ->
            // 提取公式内容：新增对第7个捕获组（%-+...%-+ 格式）的判断
            val formula = match.groupValues[0].takeIf { it.isNotEmpty() }
                ?: match.groupValues[1].takeIf { it.isNotEmpty() }  // \(...\)
                ?: match.groupValues[2].takeIf { it.isNotEmpty() }  // \[...\]
                ?: match.groupValues[3].takeIf { it.isNotEmpty() }  // 原有\[...\]变体
                ?: match.groupValues[4].takeIf { it.isNotEmpty() }  // 原有空格变体第1组
                ?: match.groupValues[5].takeIf { it.isNotEmpty() }  // 原有空格变体第2组
                ?: match.groupValues[6].takeIf { it.isNotEmpty() }  // $...$
                ?: match.groupValues[7]  // 新增：%-+...%-+ 格式的中间内容

            // 保持原有替换逻辑
            if (formula.contains("\n")) {
                "$$${formula}$$"  // 含换行→块级公式
            } else if (match.groupValues[4].isNotEmpty()) {
                "$${formula}$"
            } else {
                "$$${formula}$$"  // 行内公式
            }
        }
    }

    // 处理 LaTeX 字符串（针对硬编码场景，自动转义反斜杠）
    fun processLatexString(rawString: String): String {
        var processed = rawString
        // 1. 转义反斜杠（将单个 \ 替换为 \\）—— 这步逻辑正确，保留
        processed = processed.replace("\\", "\\\\")
        // 2. 清理行内公式的多余空格（$ 与公式间无空格）
        // 关键修复：替换字符串中的 $ 转义为 \\$，避免被解析为组引用
        processed = processed.replace(
            Regex("\\\\\\$\\s+(.*?)\\s+\\\\\\$"),  // 匹配 \$ 前后带空格的情况（因第一步已转义\，故匹配\\$）
            "\\\$$1\\\\$"  // 替换为 \$公式\$（$1 是捕获组，保留公式内容）
        )
        // 3. 确保块级公式环境闭合（简单校验）
        // 关键修复：替换字符串中的 $1/$2 转义为 \\$1/\\$2，同时添加 DOT_MATCHES_ALL 支持多行匹配
        processed = processed.replace(
            Regex("(\\\\begin\\{equation\\})(.*?)(?!\\\\end\\{equation\\})", RegexOption.DOT_MATCHES_ALL),
            "\\\\$1\\\\$2\\\\end{equation}"  // 补全 \\end{equation}
        )
        return processed
    }


    /**
     * 自动识别并转换公式格式
     * 支持两种格式：\(...\) 和 (...)
     */
    fun processFormulas(text: String): String {
        // 处理 \(...\) 格式的公式
        val processed1 = processBackslashFormulas(text)
        // 处理 (...) 格式的公式
        return processParenthesisFormulas(processed1)
    }

    /**
     * 处理 \(...\) 格式的公式
     */
    private fun processBackslashFormulas(text: String): String {
        val regex = """\\\((.*?)\\\)""".toRegex()
        return regex.replace(text) { matchResult ->
            val formula = matchResult.groupValues[1]
            // 转换为行内公式格式
            "\$$formula\$"
        }
    }

    /**
     * 处理 (...) 格式的公式
     */
    private fun processParenthesisFormulas(text: String): String {
        val regex = """\(([^()]+)\)""".toRegex()
        return regex.replace(text) { matchResult ->
            val formula = matchResult.groupValues[1]
            // 转换为行内公式格式
            "\$$formula\$"
        }
    }


    //正则表达式提取字符串中的URL地址
    fun extractAllUrls(text: String): List<String> {
        val regex = Regex("https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]")
        return regex.findAll(text).map { it.value }.toList()
    }

    fun extractUrl(text: String): String? {
        // 通用URL正则表达式（简化版，实际使用可优化）
        val regex = Regex("(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]")
        return regex.find(text)?.value
    }

    // 创建更宽松的URL识别模式
    private val URL_PATTERN = Pattern.compile(
        """
        (?:(?:https?|ftp)://)?                      # 协议部分（可选）
        (?:www\.|[-a-zA-Z0-9@:%._+~#=]{2,256}\.)  # www. 或域名前缀
        [-a-zA-Z0-9@:%._+~#=]{1,256}               # 域名主体
        (?:/[-a-zA-Z0-9@:%_+.~#?&/=]*)?            # 路径部分（可选）
        """.trimIndent(), Pattern.CASE_INSENSITIVE
    )



    /**
     * 仅对 URL 中的中文部分进行合法路径编码，不重复编码已合法的片段
     * @param originalUrl 包含中文的原始 URL
     * @return 编码后的合法 URL
     */
    fun encodeChineseInUrlPath(originalUrl: String): String {
        // 1. 拆分 URL 为 "基础路径" 和 "待编码的中文部分"（最后一个 / 之后的内容）
        val lastSlashIndex = originalUrl.lastIndexOf('/')
        if (lastSlashIndex == -1) {
            return encodeChineseSegment(originalUrl) // 无 / 时直接编码整个字符串
        }

        // 2. 拆分：基础路径（含最后一个 /） + 中文文件名（需编码）
        val basePath = originalUrl.substring(0, lastSlashIndex + 1) // 如 "https://file.302.ai/gpt/%22imgs%22/20250821/"
        val chineseSegment = originalUrl.substring(lastSlashIndex + 1) // 如 "星维互动办公指南.pdf"

        // 3. 仅对中文片段进行 URL 路径编码（用 UTF-8，避免 + 符号）
        val encodedChinese = encodeChineseSegment(chineseSegment)

        // 4. 拼接最终合法 URL
        return basePath + encodedChinese
    }

    /**
     * 对单个字符串片段（如文件名）中的中文进行 URL 路径编码
     * （区别于表单编码，空格转为 %20，而非 +）
     */
    private fun encodeChineseSegment(segment: String): String {
        // 用 UTF-8 编码，然后替换表单编码的 + 为 URL 路径规范的 %20
        return URLEncoder.encode(segment, StandardCharsets.UTF_8.name())
            .replace("+", "%20") // 修复空格编码问题
            .replace("%2F", "/") // 若片段含 /（路径分隔符），需保留（可选，根据实际场景）
    }


    fun extractImageId(path: String): String {
        // 找到最后一个斜杠的位置，获取文件名（包含扩展名）
        val lastSlashIndex = path.lastIndexOf('/')
        val fileName = if (lastSlashIndex != -1) {
            path.substring(lastSlashIndex + 1)
        } else {
            path // 如果没有斜杠，直接使用原字符串
        }

        // 找到最后一个点的位置，获取不带扩展名的部分
        val lastDotIndex = fileName.lastIndexOf('.')
        return if (lastDotIndex != -1) {
            fileName.substring(0, lastDotIndex)
        } else {
            fileName // 如果没有扩展名，直接返回文件名
        }
    }


    fun extractInfoFromString(input: String): String {
        // 提取图片路径的正则表达式：匹配src="..."中的内容
        val imgPattern = Pattern.compile("src=\"(content://.*?)\"")
        val imgMatcher = imgPattern.matcher(input)
        val imagePath = if (imgMatcher.find()) {
            imgMatcher.group(1) // 获取src属性的值
        } else {
            "" // 未找到图片路径
        }

        // 提取<br>后面的文本内容
        val brIndex = input.indexOf("<br>")
        val textContent = if (brIndex != -1 && brIndex < input.length - 4) {
            input.substring(brIndex + 4) // 跳过<br>标签（长度为4）
        } else {
            "" // 未找到<br>标签或后面无内容
        }

        return textContent
    }

    fun extractAllImageUrls(input: String): List<String> {
        // 正则表达式：匹配所有src="content://..."格式的URL
        // 说明：
        // - src=" 匹配src属性开头
        // - (content://.*?) 捕获以content://开头的URL（非贪婪模式，避免匹配到多个URL时出错）
        // - " 匹配属性结束的引号
        val regex = "src=\"(content://.*?)\""
        val pattern = Pattern.compile(regex)
        val matcher: Matcher = pattern.matcher(input)

        val urls = mutableListOf<String>()
        // 循环查找所有匹配的URL
        while (matcher.find()) {
            // group(1)获取第一个捕获组的内容（即src属性中的URL）
            val url = matcher.group(1)
            urls.add(url)
        }
        return urls
    }

    fun extractAllImageUrlsNew(input: String): List<String> {
        // 正则表达式：同时匹配以下两种URL
        // 1. src="content://..." 或 ima="content://..."（支持英文引号"和中文引号“”）
        // 2. src="file://..." 或 ima="file://..."（支持英文引号"和中文引号“”）
        // 说明：
        // - (src|ima)=["“”] 匹配属性名（src或ima）+ 引号（英文"或中文“”）
        // - (content://|file://)/.*? 捕获以content://或file://开头的URL（非贪婪模式）
        // - ["“”] 匹配结束的引号
        val regex = "(src|ima)=[\"“”]((content://|file://)/.*?)[\"“”]"
        val pattern = Pattern.compile(regex)
        val matcher: Matcher = pattern.matcher(input)

        val urls = mutableListOf<String>()
        while (matcher.find()) {
            // group(2) 是捕获到的URL（content://... 或 file://...）
            val url = matcher.group(2)
            urls.add(url)
        }



        val regex1 = "src=\"(content://.*?)\""
        val pattern1 = Pattern.compile(regex1)
        val matcher1: Matcher = pattern1.matcher(input)


        // 循环查找所有匹配的URL
        while (matcher1.find()) {
            // group(1)获取第一个捕获组的内容（即src属性中的URL）
            val url = matcher1.group(1)
            if (!urls.contains(url)){
                urls.add(url)
            }

        }

        return urls
    }




}
// 创建自定义URLSpan，移除下划线
class CustomUrlSpan(url: String) : URLSpan(url) {
    override fun updateDrawState(ds: android.text.TextPaint) {
        super.updateDrawState(ds)
        ds.isUnderlineText = true // 移除下划线
        ds.color = Color.BLUE // 使用默认链接颜色，或自定义颜色
    }



    object UrlValidator {
        // 验证URL格式的正则表达式（支持http/https，域名，路径等）
        private val URL_PATTERN = "^(https?://)?(www\\.)?[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}(:[0-9]{1,5})?(/.*)?$".toRegex()

        // 验证是否为302.ai相关的合法URL
        fun isValid302Url(url: String): Boolean {
            // 1. 验证URL格式
            if (!URL_PATTERN.matches(url)) {
                return false
            }
            // 2. 验证域名是否包含api.302.ai
            //return url.contains("api.302.ai", ignoreCase = true)
            return true
        }
    }






}