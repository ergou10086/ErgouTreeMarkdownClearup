package edu.software.ergoutree.markdownautoclearup;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Markdown文件处理工具类，用于在中英文之间添加空格
 */
public class MarkdownSpacingProcessor {

    // 匹配中英文交界的正则表达式
    private static final Pattern CHINESE_ENGLISH_BOUNDARY = Pattern.compile(
            "([\\p{script=Han}\\p{InCJK_Symbols_and_Punctuation}])([a-zA-Z])|" +
            "([a-zA-Z])([\\p{script=Han}\\p{InCJK_Symbols_and_Punctuation}])"
    );
    
    // 匹配中文与数字交界的正则表达式
    private static final Pattern CHINESE_NUMBER_BOUNDARY = Pattern.compile(
            "([\\p{script=Han}\\p{InCJK_Symbols_and_Punctuation}])([0-9])|" +
            "([0-9])([\\p{script=Han}\\p{InCJK_Symbols_and_Punctuation}])"
    );
    
    // 匹配英文与数字交界的正则表达式
    private static final Pattern ENGLISH_NUMBER_BOUNDARY = Pattern.compile(
            "([a-zA-Z])([0-9])|" +
            "([0-9])([a-zA-Z])"
    );
    
    // 中文标点符号
    private static final String CHINESE_PUNCTUATION = "，。！？；：“”【】《》……";
    
    // 英文标点符号
    private static final String ENGLISH_PUNCTUATION = ",.!?;:\"\"[]()''\\-_";
    
    // 全角标点到半角标点的映射
    private static final Map<Character, Character> FULL_TO_HALF_PUNCTUATION = new HashMap<>();
    
    // 半角标点到全角标点的映射
    private static final Map<Character, Character> HALF_TO_FULL_PUNCTUATION = new HashMap<>();
    
    // 特殊元素标记
    private static final String CODE_BLOCK_MARKER = "```";
    private static final String MATH_BLOCK_MARKER = "$$";
    private static final String MATH_INLINE_MARKER = "$";
    
    // 超链接和图片模式
    private static final Pattern LINK_PATTERN = Pattern.compile("\\[([^]]*)]\\(([^)]*)\\)");
    private static final Pattern IMAGE_PATTERN = Pattern.compile("!\\[([^]]*)]\\(([^)]*)\\)");
    
    static {
        // 初始化标点映射
        initPunctuationMaps();
    }
    
    /**
     * 初始化标点映射
     */
    private static void initPunctuationMaps() {
        // 全角到半角的映射
        FULL_TO_HALF_PUNCTUATION.put('，', ',');
        FULL_TO_HALF_PUNCTUATION.put('。', '.');
        FULL_TO_HALF_PUNCTUATION.put('！', '!');
        FULL_TO_HALF_PUNCTUATION.put('？', '?');
        FULL_TO_HALF_PUNCTUATION.put('；', ';');
        FULL_TO_HALF_PUNCTUATION.put('：', ':');
        FULL_TO_HALF_PUNCTUATION.put('“', '"');
        FULL_TO_HALF_PUNCTUATION.put('”', '"');
        FULL_TO_HALF_PUNCTUATION.put('【', '[');
        FULL_TO_HALF_PUNCTUATION.put('】', ']');
        FULL_TO_HALF_PUNCTUATION.put('《', '<');
        FULL_TO_HALF_PUNCTUATION.put('》', '>');
        FULL_TO_HALF_PUNCTUATION.put('（', '(');
        FULL_TO_HALF_PUNCTUATION.put('）', ')');
        FULL_TO_HALF_PUNCTUATION.put('…', '.');
        
        // 半角到全角的映射
        HALF_TO_FULL_PUNCTUATION.put(',', '，');
        HALF_TO_FULL_PUNCTUATION.put('.', '。');
        HALF_TO_FULL_PUNCTUATION.put('!', '！');
        HALF_TO_FULL_PUNCTUATION.put('?', '？');
        HALF_TO_FULL_PUNCTUATION.put(';', '；');
        HALF_TO_FULL_PUNCTUATION.put(':', '：');
        HALF_TO_FULL_PUNCTUATION.put('"', '“'); // 注意这里简化处理了，但是实际上开闭引号是不同的
        HALF_TO_FULL_PUNCTUATION.put('[', '【');
        HALF_TO_FULL_PUNCTUATION.put(']', '】');
        HALF_TO_FULL_PUNCTUATION.put('<', '《');
        HALF_TO_FULL_PUNCTUATION.put('>', '》');
        HALF_TO_FULL_PUNCTUATION.put('(', '（');
        HALF_TO_FULL_PUNCTUATION.put(')', '）');
    }

    /**
     * 处理Markdown文件，根据规则添加空格
     * @param inputFile 输入文件路径
     * @param outputFile 输出文件路径
     * @param addSpaceBetweenChineseAndEnglish 是否在中英文之间添加空格
     * @param addSpaceBetweenChineseAndNumber 是否在中文和数字之间添加空格
     * @param addSpaceBetweenEnglishAndNumber 是否在英文和数字之间添加空格
     * @param addSpaceAroundChinesePunctuation 是否在中文标点前后添加空格
     * @param addSpaceAroundEnglishPunctuation 是否在英文标点前后添加空格
     * @param convertToFullWidthPunctuation 是否将标点转换为全角
     * @param convertToHalfWidthPunctuation 是否将标点转换为半角
     * @return 处理是否成功
     */
    public static boolean processMarkdownFile(Path inputFile, Path outputFile, 
                                            boolean addSpaceBetweenChineseAndEnglish,
                                            boolean addSpaceBetweenChineseAndNumber,
                                            boolean addSpaceBetweenEnglishAndNumber,
                                            boolean addSpaceAroundChinesePunctuation,
                                            boolean addSpaceAroundEnglishPunctuation,
                                            boolean convertToFullWidthPunctuation,
                                            boolean convertToHalfWidthPunctuation) {
        try {
            String content = Files.readString(inputFile, StandardCharsets.UTF_8);
            String processedContent = processText(content, 
                                                addSpaceBetweenChineseAndEnglish,
                                                addSpaceBetweenChineseAndNumber,
                                                addSpaceBetweenEnglishAndNumber,
                                                addSpaceAroundChinesePunctuation,
                                                addSpaceAroundEnglishPunctuation,
                                                convertToFullWidthPunctuation,
                                                convertToHalfWidthPunctuation);
            Files.writeString(outputFile, processedContent, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 处理Markdown文件，使用默认规则（兼容旧版本）
     * @param inputFile 输入文件路径
     * @param outputFile 输出文件路径
     * @return 处理是否成功
     */
    public static boolean processMarkdownFile(Path inputFile, Path outputFile) {
        // 使用默认规则：中英文之间和中文数字之间添加空格
        return processMarkdownFile(inputFile, outputFile, true, true, false, false, false, false, false);
    }
    
    /**
     * 处理Markdown文件，兼容之前的接口
     * @param inputFile 输入文件路径
     * @param outputFile 输出文件路径
     * @param addSpaceBetweenChineseAndEnglish 是否在中英文之间添加空格
     * @param addSpaceBetweenChineseAndNumber 是否在中文和数字之间添加空格
     * @param addSpaceBetweenEnglishAndNumber 是否在英文和数字之间添加空格
     * @return 处理是否成功
     */
    public static boolean processMarkdownFile(Path inputFile, Path outputFile, 
                                            boolean addSpaceBetweenChineseAndEnglish,
                                            boolean addSpaceBetweenChineseAndNumber,
                                            boolean addSpaceBetweenEnglishAndNumber) {
        // 调用新的接口，但不启用新增的标点相关功能
        return processMarkdownFile(inputFile, outputFile, 
                                  addSpaceBetweenChineseAndEnglish,
                                  addSpaceBetweenChineseAndNumber,
                                  addSpaceBetweenEnglishAndNumber,
                                  false, false, false, false);
    }

    /**
     * 根据规则处理文本，添加空格和处理标点
     * @param text 原始文本
     * @param addSpaceBetweenChineseAndEnglish 是否在中英文之间添加空格
     * @param addSpaceBetweenChineseAndNumber 是否在中文和数字之间添加空格
     * @param addSpaceBetweenEnglishAndNumber 是否在英文和数字之间添加空格
     * @param addSpaceAroundChinesePunctuation 是否在中文标点前后添加空格
     * @param addSpaceAroundEnglishPunctuation 是否在英文标点前后添加空格
     * @param convertToFullWidthPunctuation 是否将标点转换为全角
     * @param convertToHalfWidthPunctuation 是否将标点转换为半角
     * @param customRegexManager 自定义正则表达式管理器，可以为空
     * @return 处理后的文本
     */
    public static String processText(String text, 
                                   boolean addSpaceBetweenChineseAndEnglish,
                                   boolean addSpaceBetweenChineseAndNumber,
                                   boolean addSpaceBetweenEnglishAndNumber,
                                   boolean addSpaceAroundChinesePunctuation,
                                   boolean addSpaceAroundEnglishPunctuation,
                                   boolean convertToFullWidthPunctuation,
                                   boolean convertToHalfWidthPunctuation,
                                   CustomRegexManager customRegexManager) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        // 保存特殊元素（代码块、公式、超链接等）
        Map<String, String> specialElements = new HashMap<>();
        int placeholderCount = 0;
        
        // 先处理代码块
        text = preserveCodeBlocks(text, specialElements, placeholderCount);
        placeholderCount = specialElements.size();
        
        // 处理数学公式
        text = preserveMathFormulas(text, specialElements, placeholderCount);
        placeholderCount = specialElements.size();
        
        // 处理超链接和图片
        text = preserveLinksAndImages(text, specialElements, placeholderCount);
        
        // 处理中英文之间的空格
        if (addSpaceBetweenChineseAndEnglish) {
            text = addSpaceBetweenChineseAndEnglish(text);
        }
        
        // 处理中文和数字之间的空格
        if (addSpaceBetweenChineseAndNumber) {
            text = addSpaceBetweenChineseAndNumber(text);
        }
        
        // 处理英文和数字之间的空格
        if (addSpaceBetweenEnglishAndNumber) {
            text = addSpaceBetweenEnglishAndNumber(text);
        }
        
        // 处理中文标点前后的空格
        if (addSpaceAroundChinesePunctuation) {
            text = addSpaceAroundChinesePunctuation(text);
        }
        
        // 处理英文标点前后的空格
        if (addSpaceAroundEnglishPunctuation) {
            text = addSpaceAroundEnglishPunctuation(text);
        }
        
        // 处理标点转换
        if (convertToFullWidthPunctuation) {
            text = convertToFullWidthPunctuation(text);
        } else if (convertToHalfWidthPunctuation) {
            text = convertToHalfWidthPunctuation(text);
        }
        
        // 应用自定义正则表达式规则
        if (customRegexManager != null) {
            text = customRegexManager.applyRules(text);
        }
        
        // 恢复特殊元素
        for (Map.Entry<String, String> entry : specialElements.entrySet()) {
            text = text.replace(entry.getKey(), entry.getValue());
        }
        
        return text;
    }
    
    /**
     * 兼容旧版本的processText方法
     * @param text 原始文本
     * @param addSpaceBetweenChineseAndEnglish 是否在中英文之间添加空格
     * @param addSpaceBetweenChineseAndNumber 是否在中文和数字之间添加空格
     * @param addSpaceBetweenEnglishAndNumber 是否在英文和数字之间添加空格
     * @return 处理后的文本
     */
    public static String processText(String text, 
                                   boolean addSpaceBetweenChineseAndEnglish,
                                   boolean addSpaceBetweenChineseAndNumber,
                                   boolean addSpaceBetweenEnglishAndNumber) {
        return processText(text, 
                          addSpaceBetweenChineseAndEnglish,
                          addSpaceBetweenChineseAndNumber,
                          addSpaceBetweenEnglishAndNumber,
                          false, false, false, false,
                          null);
    }
    
    /**
     * 兼容中间版本的processText方法
     * @param text 原始文本
     * @param addSpaceBetweenChineseAndEnglish 是否在中英文之间添加空格
     * @param addSpaceBetweenChineseAndNumber 是否在中文和数字之间添加空格
     * @param addSpaceBetweenEnglishAndNumber 是否在英文和数字之间添加空格
     * @param addSpaceAroundChinesePunctuation 是否在中文标点前后添加空格
     * @param addSpaceAroundEnglishPunctuation 是否在英文标点前后添加空格
     * @param convertToFullWidthPunctuation 是否将标点转换为全角
     * @param convertToHalfWidthPunctuation 是否将标点转换为半角
     * @return 处理后的文本
     */
    public static String processText(String text, 
                                   boolean addSpaceBetweenChineseAndEnglish,
                                   boolean addSpaceBetweenChineseAndNumber,
                                   boolean addSpaceBetweenEnglishAndNumber,
                                   boolean addSpaceAroundChinesePunctuation,
                                   boolean addSpaceAroundEnglishPunctuation,
                                   boolean convertToFullWidthPunctuation,
                                   boolean convertToHalfWidthPunctuation) {
        return processText(text, 
                          addSpaceBetweenChineseAndEnglish,
                          addSpaceBetweenChineseAndNumber,
                          addSpaceBetweenEnglishAndNumber,
                          addSpaceAroundChinesePunctuation,
                          addSpaceAroundEnglishPunctuation,
                          convertToFullWidthPunctuation,
                          convertToHalfWidthPunctuation,
                          null);
    }
    
    /**
     * 在中英文之间添加空格
     * @param text 原始文本
     * @return 处理后的文本
     */
    public static String addSpaceBetweenChineseAndEnglish(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        // 使用正则表达式在中英文交界处添加空格
        Matcher matcher = CHINESE_ENGLISH_BOUNDARY.matcher(text);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String replacement;
            if (matcher.group(1) != null && matcher.group(2) != null) {
                // 中文后面跟英文
                replacement = matcher.group(1) + " " + matcher.group(2);
            } else {
                // 英文后面跟中文
                replacement = matcher.group(3) + " " + matcher.group(4);
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }
    
    /**
     * 在中文和数字之间添加空格
     * @param text 原始文本
     * @return 处理后的文本
     */
    public static String addSpaceBetweenChineseAndNumber(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        // 使用正则表达式在中文和数字交界处添加空格
        Matcher matcher = CHINESE_NUMBER_BOUNDARY.matcher(text);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String replacement;
            if (matcher.group(1) != null && matcher.group(2) != null) {
                // 中文后面跟数字
                replacement = matcher.group(1) + " " + matcher.group(2);
            } else {
                // 数字后面跟中文
                replacement = matcher.group(3) + " " + matcher.group(4);
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }
    
    /**
     * 在英文和数字之间添加空格
     * @param text 原始文本
     * @return 处理后的文本
     */
    public static String addSpaceBetweenEnglishAndNumber(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        // 使用正则表达式在英文和数字交界处添加空格
        Matcher matcher = ENGLISH_NUMBER_BOUNDARY.matcher(text);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String replacement;
            if (matcher.group(1) != null && matcher.group(2) != null) {
                // 英文后面跟数字
                replacement = matcher.group(1) + " " + matcher.group(2);
            } else {
                // 数字后面跟英文
                replacement = matcher.group(3) + " " + matcher.group(4);
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * 在中文标点前后添加空格
     * @param text 原始文本
     * @return 处理后的文本
     */
    public static String addSpaceAroundChinesePunctuation(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (CHINESE_PUNCTUATION.indexOf(c) >= 0) {
                // 如果前面不是空格，则添加空格
                if (i > 0 && text.charAt(i-1) != ' ') {
                    result.append(' ');
                }
                result.append(c);
                // 如果后面不是空格，则添加空格
                if (i < text.length() - 1 && text.charAt(i+1) != ' ') {
                    result.append(' ');
                }
            } else {
                result.append(c);
            }
        }
        
        return result.toString();
    }
    
    /**
     * 在英文标点前后添加空格
     * @param text 原始文本
     * @return 处理后的文本
     */
    public static String addSpaceAroundEnglishPunctuation(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (ENGLISH_PUNCTUATION.indexOf(c) >= 0) {
                // 如果前面不是空格，则添加空格
                if (i > 0 && text.charAt(i-1) != ' ') {
                    result.append(' ');
                }
                result.append(c);
                // 如果后面不是空格，则添加空格
                if (i < text.length() - 1 && text.charAt(i+1) != ' ') {
                    result.append(' ');
                }
            } else {
                result.append(c);
            }
        }
        
        return result.toString();
    }
    
    /**
     * 将标点转换为全角
     * @param text 原始文本
     * @return 处理后的文本
     */
    public static String convertToFullWidthPunctuation(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (HALF_TO_FULL_PUNCTUATION.containsKey(c)) {
                result.append(HALF_TO_FULL_PUNCTUATION.get(c));
            } else {
                result.append(c);
            }
        }
        
        return result.toString();
    }
    
    /**
     * 将标点转换为半角
     * @param text 原始文本
     * @return 处理后的文本
     */
    public static String convertToHalfWidthPunctuation(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (FULL_TO_HALF_PUNCTUATION.containsKey(c)) {
                result.append(FULL_TO_HALF_PUNCTUATION.get(c));
            } else {
                result.append(c);
            }
        }
        
        return result.toString();
    }
    
    /**
     * 保留代码块内容，不进行空格处理
     * @param text 原始文本
     * @param specialElements 特殊元素映射
     * @param startIndex 占位符起始索引
     * @return 处理后的文本
     */
    private static String preserveCodeBlocks(String text, Map<String, String> specialElements, int startIndex) {
        StringBuilder result = new StringBuilder();
        int index = 0;
        int placeholderCount = startIndex;
        
        // 分割文本，处理代码块
        while (index < text.length()) {
            int codeBlockStart = text.indexOf(CODE_BLOCK_MARKER, index);
            if (codeBlockStart == -1) {
                // 没有更多代码块，添加剩余文本
                result.append(text.substring(index));
                break;
            }
            
            // 添加代码块前的文本
            result.append(text.substring(index, codeBlockStart));
            
            // 查找代码块结束位置
            int codeBlockEnd = text.indexOf(CODE_BLOCK_MARKER, codeBlockStart + CODE_BLOCK_MARKER.length());
            if (codeBlockEnd == -1) {
                // 没有找到结束标记，添加剩余文本
                result.append(text.substring(codeBlockStart));
                break;
            }
            
            // 提取完整的代码块（包括标记）
            String codeBlock = text.substring(codeBlockStart, codeBlockEnd + CODE_BLOCK_MARKER.length());
            
            // 创建占位符并保存映射
            String placeholder = "__CODE_BLOCK_" + placeholderCount + "__";
            specialElements.put(placeholder, codeBlock);
            result.append(placeholder);
            
            // 更新索引和计数器
            index = codeBlockEnd + CODE_BLOCK_MARKER.length();
            placeholderCount++;
        }
        
        return result.toString();
    }
    
    /**
     * 保留数学公式内容，不进行空格处理
     * @param text 原始文本
     * @param specialElements 特殊元素映射
     * @param startIndex 占位符起始索引
     * @return 处理后的文本
     */
    private static String preserveMathFormulas(String text, Map<String, String> specialElements, int startIndex) {
        // 先处理块级公式（$$...$$）
        StringBuilder result = new StringBuilder();
        int index = 0;
        int placeholderCount = startIndex;
        
        // 分割文本，处理块级公式
        while (index < text.length()) {
            int mathBlockStart = text.indexOf(MATH_BLOCK_MARKER, index);
            if (mathBlockStart == -1) {
                // 没有更多块级公式，添加剩余文本
                result.append(text.substring(index));
                break;
            }
            
            // 添加公式前的文本
            result.append(text.substring(index, mathBlockStart));
            
            // 查找公式结束位置
            int mathBlockEnd = text.indexOf(MATH_BLOCK_MARKER, mathBlockStart + MATH_BLOCK_MARKER.length());
            if (mathBlockEnd == -1) {
                // 没有找到结束标记，添加剩余文本
                result.append(text.substring(mathBlockStart));
                break;
            }
            
            // 提取完整的公式（包括标记）
            String mathBlock = text.substring(mathBlockStart, mathBlockEnd + MATH_BLOCK_MARKER.length());
            
            // 创建占位符并保存映射
            String placeholder = "__MATH_BLOCK_" + placeholderCount + "__";
            specialElements.put(placeholder, mathBlock);
            result.append(placeholder);
            
            // 更新索引和计数器
            index = mathBlockEnd + MATH_BLOCK_MARKER.length();
            placeholderCount++;
        }
        
        // 然后处理行内公式（$...$）
        text = result.toString();
        result = new StringBuilder();
        index = 0;
        
        // 分割文本，处理行内公式
        while (index < text.length()) {
            int mathInlineStart = text.indexOf(MATH_INLINE_MARKER, index);
            if (mathInlineStart == -1) {
                // 没有更多行内公式，添加剩余文本
                result.append(text.substring(index));
                break;
            }
            
            // 检查是否为块级公式的开始（$$）
            if (mathInlineStart + 1 < text.length() && text.charAt(mathInlineStart + 1) == '$') {
                // 这是块级公式的开始，跳过
                result.append(text.substring(index, mathInlineStart + 2));
                index = mathInlineStart + 2;
                continue;
            }
            
            // 添加公式前的文本
            result.append(text.substring(index, mathInlineStart));
            
            // 查找公式结束位置
            int mathInlineEnd = text.indexOf(MATH_INLINE_MARKER, mathInlineStart + MATH_INLINE_MARKER.length());
            if (mathInlineEnd == -1) {
                // 没有找到结束标记，添加剩余文本
                result.append(text.substring(mathInlineStart));
                break;
            }
            
            // 提取完整的公式（包括标记）
            String mathInline = text.substring(mathInlineStart, mathInlineEnd + MATH_INLINE_MARKER.length());
            
            // 创建占位符并保存映射
            String placeholder = "__MATH_INLINE_" + placeholderCount + "__";
            specialElements.put(placeholder, mathInline);
            result.append(placeholder);
            
            // 更新索引和计数器
            index = mathInlineEnd + MATH_INLINE_MARKER.length();
            placeholderCount++;
        }
        
        return result.toString();
    }
    
    /**
     * 保留超链接和图片内容，不进行空格处理
     * @param text 原始文本
     * @param specialElements 特殊元素映射
     * @param startIndex 占位符起始索引
     * @return 处理后的文本
     */
    private static String preserveLinksAndImages(String text, Map<String, String> specialElements, int startIndex) {
        // 先处理图片（![...](...))
        int placeholderCount = startIndex;
        StringBuilder result = new StringBuilder(text);
        
        // 处理图片
        Matcher imageMatcher = IMAGE_PATTERN.matcher(text);
        while (imageMatcher.find()) {
            String imageTag = imageMatcher.group(0);
            String placeholder = "__IMAGE_" + placeholderCount + "__";
            specialElements.put(placeholder, imageTag);
            placeholderCount++;
            
            // 替换文本中的图片标签
            int startPos = result.indexOf(imageTag);
            if (startPos != -1) {
                result.replace(startPos, startPos + imageTag.length(), placeholder);
            }
        }
        
        // 处理超链接（[...](...))
        text = result.toString();
        result = new StringBuilder(text);
        
        Matcher linkMatcher = LINK_PATTERN.matcher(text);
        while (linkMatcher.find()) {
            String linkTag = linkMatcher.group(0);
            String placeholder = "__LINK_" + placeholderCount + "__";
            specialElements.put(placeholder, linkTag);
            placeholderCount++;
            
            // 替换文本中的超链接标签
            int startPos = result.indexOf(linkTag);
            if (startPos != -1) {
                result.replace(startPos, startPos + linkTag.length(), placeholder);
            }
        }
        
        return result.toString();
    }
    
    /**
     * 获取文件扩展名
     * @param file 文件
     * @return 文件扩展名（不包含点）
     */
    public static String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // 没有扩展名
        }
        return name.substring(lastIndexOf + 1);
    }
}
