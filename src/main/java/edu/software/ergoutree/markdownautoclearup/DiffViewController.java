package edu.software.ergoutree.markdownautoclearup;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 差异视图控制器，用于显示文本格式化前后的差异
 */
public class DiffViewController {
    @FXML
    private WebView originalWebView;
    
    @FXML
    private WebView formattedWebView;
    
    @FXML
    private Button closeButton;
    
    @FXML
    private Button exportButton;
    
    private String originalText;
    private String formattedText;
    
    /**
     * 初始化控制器
     */
    @FXML
    private void initialize() {
        // 初始化时不需要特殊操作
    }
    
    /**
     * 设置要比较的文本
     * @param originalText 原始文本
     * @param formattedText 格式化后的文本
     */
    public void setTexts(String originalText, String formattedText) {
        this.originalText = originalText;
        this.formattedText = formattedText;
        
        // 高亮显示差异
        highlightDifferences();
    }
    
    /**
     * 高亮显示文本差异
     * 使用HTML和CSS实现更复杂的差异高亮
     */
    private void highlightDifferences() {
        // 将文本分割为行
        String[] originalLines = originalText.split("\\r?\\n");
        String[] formattedLines = formattedText.split("\\r?\\n");
        
        // 构建 HTML 内容
        StringBuilder originalHtml = new StringBuilder();
        StringBuilder formattedHtml = new StringBuilder();
        
        // 添加 CSS 样式
        String cssStyle = "<style>\n" +
                "body { font-family: monospace; white-space: pre-wrap; margin: 10px; }\n" +
                ".line-number { color: #888; margin-right: 10px; user-select: none; }\n" +
                ".added { background-color: #e6ffed; }\n" +  // 添加的内容使用浅绿色
                ".removed { background-color: #ffeef0; }\n" + // 删除的内容使用浅红色
                ".modified { background-color: #e6f0ff; }\n" + // 修改的内容使用浅蓝色
                "</style>\n";
        
        originalHtml.append("<!DOCTYPE html>\n<html><head>\n");
        originalHtml.append(cssStyle);
        originalHtml.append("</head><body>\n");
        
        formattedHtml.append("<!DOCTYPE html>\n<html><head>\n");
        formattedHtml.append(cssStyle);
        formattedHtml.append("</head><body>\n");
        
        // 找出最大行数
        int maxLines = Math.max(originalLines.length, formattedLines.length);
        
        for (int i = 0; i < maxLines; i++) {
            String originalLine = (i < originalLines.length) ? escapeHtml(originalLines[i]) : "";
            String formattedLine = (i < formattedLines.length) ? escapeHtml(formattedLines[i]) : "";
            
            // 比较行内容是否相同
            if (!originalLine.equals(formattedLine)) {
                // 判断差异类型
                String originalClass = "";
                String formattedClass = "";
                
                if (originalLine.isEmpty()) {
                    // 原始行为空，表示添加了新行
                    formattedClass = "added";
                } else if (formattedLine.isEmpty()) {
                    // 格式化后行为空，表示删除了行
                    originalClass = "removed";
                } else {
                    // 对比字符级别的差异
                    originalClass = "modified";
                    formattedClass = "modified";
                    
                    // 这里可以进一步分析字符级别的差异，标记出空格的添加和删除
                    originalLine = highlightSpaceChanges(originalLine, formattedLine, false);
                    formattedLine = highlightSpaceChanges(formattedLine, originalLine, true);
                }
                
                originalHtml.append(String.format("<div><span class=\"line-number\">%4d:</span><span class=\"%s\">%s</span></div>\n", 
                        i + 1, originalClass, originalLine));
                formattedHtml.append(String.format("<div><span class=\"line-number\">%4d:</span><span class=\"%s\">%s</span></div>\n", 
                        i + 1, formattedClass, formattedLine));
            } else {
                // 相同的行
                originalHtml.append(String.format("<div><span class=\"line-number\">%4d:</span>%s</div>\n", i + 1, originalLine));
                formattedHtml.append(String.format("<div><span class=\"line-number\">%4d:</span>%s</div>\n", i + 1, formattedLine));
            }
        }
        
        originalHtml.append("</body></html>");
        formattedHtml.append("</body></html>");
        
        // 更新 WebView 内容
        originalWebView.getEngine().loadContent(originalHtml.toString());
        formattedWebView.getEngine().loadContent(formattedHtml.toString());
    }
    
    /**
     * 高亮显示空格的变化
     * @param source 源文本
     * @param target 目标文本（用于对比）
     * @param isFormatted 是否是格式化后的文本
     * @return 带有高亮标记的HTML文本
     */
    private String highlightSpaceChanges(String source, String target, boolean isFormatted) {
        // 对中英文之间的空格进行高亮
        Pattern pattern = Pattern.compile("([\\u4e00-\\u9fa5])\\s+([a-zA-Z0-9])|([a-zA-Z0-9])\\s+([\\u4e00-\\u9fa5])");
        Matcher matcher = pattern.matcher(source);
        
        StringBuilder result = new StringBuilder(source);
        int offset = 0;
        
        while (matcher.find()) {
            int start = matcher.start() + offset;
            int end = matcher.end() + offset;
            
            // 检查目标文本中是否有这个空格
            String group = matcher.group();
            String withoutSpace = group.replaceAll("\\s+", "");
            
            if (!target.contains(group) && target.contains(withoutSpace)) {
                // 目标文本中没有这个空格，说明空格被添加或删除
                String replacement;
                if (isFormatted) {
                    // 在格式化后的文本中，空格被添加
                    replacement = "<span class=\"added\">"+group+"</span>";
                } else {
                    // 在原始文本中，空格将被删除
                    replacement = "<span class=\"removed\">"+group+"</span>";
                }
                
                result.replace(start, end, replacement);
                offset += replacement.length() - (end - start);
            }
        }
        
        return result.toString();
    }
    
    /**
     * 转义HTML特殊字符
     * @param text 要转义的文本
     * @return 转义后的文本
     */
    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
    
    /**
     * 关闭按钮点击事件
     */
    @FXML
    protected void onCloseButtonClick() {
        // 获取当前窗口并关闭
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
    
    /**
     * 导出差异报告按钮点击事件
     */
    @FXML
    protected void onExportButtonClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存差异报告");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("文本文件", "*.txt"),
                new FileChooser.ExtensionFilter("所有文件", "*.*")
        );
        fileChooser.setInitialFileName("diff_report.txt");
        
        // 获取舞台（窗口）
        Stage stage = (Stage) exportButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            try {
                // 生成差异报告
                String report = generateDiffReport();
                
                // 保存到文件
                Files.writeString(file.toPath(), report, StandardCharsets.UTF_8);
                
                // 显示成功消息
                showAlert("成功", "差异报告已保存到: " + file.getAbsolutePath());
            } catch (IOException e) {
                showAlert("错误", "保存差异报告失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 生成差异报告
     * @return 差异报告文本
     */
    private String generateDiffReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== Markdown 格式化差异报告 ===\n\n");
        
        // 将文本分割为行
        String[] originalLines = originalText.split("\\r?\\n");
        String[] formattedLines = formattedText.split("\\r?\\n");
        
        // 找出最大行数
        int maxLines = Math.max(originalLines.length, formattedLines.length);
        
        // 统计差异数量
        int diffCount = 0;
        int addedSpaces = 0;
        int removedSpaces = 0;
        int modifiedChars = 0;
        
        // 逐行比较
        for (int i = 0; i < maxLines; i++) {
            String originalLine = (i < originalLines.length) ? originalLines[i] : "";
            String formattedLine = (i < formattedLines.length) ? formattedLines[i] : "";
            
            // 比较行内容是否相同
            if (!originalLine.equals(formattedLine)) {
                diffCount++;
                report.append(String.format("行 %d:\n", i + 1));
                report.append(String.format("- %s\n", originalLine));
                report.append(String.format("+ %s\n\n", formattedLine));
                
                // 分析空格变化
                int originalSpaces = countSpaces(originalLine);
                int formattedSpaces = countSpaces(formattedLine);
                
                if (formattedSpaces > originalSpaces) {
                    addedSpaces += (formattedSpaces - originalSpaces);
                } else if (originalSpaces > formattedSpaces) {
                    removedSpaces += (originalSpaces - formattedSpaces);
                }
                
                // 统计其他字符变化
                int minLength = Math.min(originalLine.length(), formattedLine.length());
                for (int j = 0; j < minLength; j++) {
                    if (originalLine.charAt(j) != formattedLine.charAt(j) && 
                        !Character.isWhitespace(originalLine.charAt(j)) && 
                        !Character.isWhitespace(formattedLine.charAt(j))) {
                        modifiedChars++;
                    }
                }
            }
        }
        
        // 添加统计信息
        report.append(String.format("\n总行数: %d, 差异行数: %d\n", maxLines, diffCount));
        report.append(String.format("添加的空格: %d, 删除的空格: %d, 修改的其他字符: %d\n", 
                addedSpaces, removedSpaces, modifiedChars));
        
        return report.toString();
    }
    
    /**
     * 计算字符串中的空格数量
     * @param text 要计算的文本
     * @return 空格数量
     */
    private int countSpaces(String text) {
        int count = 0;
        for (char c : text.toCharArray()) {
            if (Character.isWhitespace(c)) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * 显示警告对话框
     * @param title 标题
     * @param message 消息内容
     */
    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
