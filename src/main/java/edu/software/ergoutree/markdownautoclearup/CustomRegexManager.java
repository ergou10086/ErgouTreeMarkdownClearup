package edu.software.ergoutree.markdownautoclearup;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 自定义正则表达式规则管理器
 * 用于管理用户定义的正则表达式规则
 */
public class CustomRegexManager {
    private List<CustomRegexRule> rules;
    private static final String CONFIG_FILE_NAME = "custom_regex_rules.dat";
    private static final String CONFIG_DIR = System.getProperty("user.home") + File.separator + ".markdown_auto_clearup";

    /**
     * 构造函数
     */
    public CustomRegexManager() {
        rules = new ArrayList<>();
        loadRules();
    }

    /**
     * 添加规则
     * @param rule 规则
     * @return 是否添加成功
     */
    public boolean addRule(CustomRegexRule rule) {
        // 验证正则表达式是否有效
        if (!isValidRegex(rule.getPattern())) {
            return false;
        }
        rules.add(rule);
        saveRules();
        return true;
    }

    /**
     * 编辑规则
     * @param index 规则索引
     * @param rule 新规则
     * @return 是否编辑成功
     */
    public boolean editRule(int index, CustomRegexRule rule) {
        if (index < 0 || index >= rules.size()) {
            return false;
        }
        // 验证正则表达式是否有效
        if (!isValidRegex(rule.getPattern())) {
            return false;
        }
        rules.set(index, rule);
        saveRules();
        return true;
    }

    /**
     * 删除规则
     * @param index 规则索引
     * @return 是否删除成功
     */
    public boolean deleteRule(int index) {
        if (index < 0 || index >= rules.size()) {
            return false;
        }
        rules.remove(index);
        saveRules();
        return true;
    }

    /**
     * 获取所有规则
     * @return 规则列表
     */
    public List<CustomRegexRule> getRules() {
        return new ArrayList<>(rules);
    }

    /**
     * 应用所有启用的规则
     * @param text 要处理的文本
     * @return 处理后的文本
     */
    public String applyRules(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String result = text;
        for (CustomRegexRule rule : rules) {
            if (rule.isEnabled()) {
                try {
                    Pattern pattern = Pattern.compile(rule.getPattern());
                    Matcher matcher = pattern.matcher(result);
                    result = matcher.replaceAll(rule.getReplacement());
                } catch (Exception e) {
                    // 忽略无效的正则表达式
                    System.err.println("应用规则时出错: " + e.getMessage());
                }
            }
        }
        return result;
    }

    /**
     * 验证正则表达式是否有效
     * @param regex 正则表达式
     * @return 是否有效
     */
    private boolean isValidRegex(String regex) {
        try {
            Pattern.compile(regex);
            return true;
        } catch (PatternSyntaxException e) {
            return false;
        }
    }

    /**
     * 保存规则到文件
     */
    private void saveRules() {
        try {
            // 确保目录存在
            Path configDir = Paths.get(CONFIG_DIR);
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }

            Path configFile = Paths.get(CONFIG_DIR, CONFIG_FILE_NAME);
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(configFile.toFile()))) {
                oos.writeObject(rules);
            }
        } catch (IOException e) {
            System.err.println("保存规则失败: " + e.getMessage());
        }
    }

    /**
     * 从文件加载规则
     */
    @SuppressWarnings("unchecked")
    private void loadRules() {
        Path configFile = Paths.get(CONFIG_DIR, CONFIG_FILE_NAME);
        if (Files.exists(configFile)) {
            try (ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(configFile.toFile()))) {
                rules = (List<CustomRegexRule>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("加载规则失败: " + e.getMessage());
                // 加载失败时使用默认规则
                initDefaultRules();
            }
        } else {
            // 文件不存在时使用默认规则
            initDefaultRules();
        }
    }

    /**
     * 初始化默认规则
     */
    private void initDefaultRules() {
        rules = new ArrayList<>();
        // 添加一些默认规则
        rules.add(new CustomRegexRule("C++语言", "C\\+\\+语言", "C++ 语言", true));
        rules.add(new CustomRegexRule("Java语言", "Java语言", "Java 语言", true));
        rules.add(new CustomRegexRule("Python语言", "Python语言", "Python 语言", true));
    }
}
