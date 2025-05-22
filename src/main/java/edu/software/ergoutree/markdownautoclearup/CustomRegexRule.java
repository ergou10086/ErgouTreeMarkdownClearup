package edu.software.ergoutree.markdownautoclearup;

/**
 * 自定义正则表达式规则类
 * 用于存储用户定义的正则表达式规则
 */
public class CustomRegexRule {
    private String name;        // 规则名称
    private String pattern;     // 匹配模式（正则表达式）
    private String replacement; // 替换内容
    private boolean enabled;    // 是否启用

    /**
     * 构造函数
     * @param name 规则名称
     * @param pattern 匹配模式（正则表达式）
     * @param replacement 替换内容
     * @param enabled 是否启用
     */
    public CustomRegexRule(String name, String pattern, String replacement, boolean enabled) {
        this.name = name;
        this.pattern = pattern;
        this.replacement = replacement;
        this.enabled = enabled;
    }

    /**
     * 默认构造函数
     */
    public CustomRegexRule() {
        this("", "", "", true);
    }

    /**
     * 获取规则名称
     * @return 规则名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置规则名称
     * @param name 规则名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取匹配模式
     * @return 匹配模式（正则表达式）
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * 设置匹配模式
     * @param pattern 匹配模式（正则表达式）
     */
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    /**
     * 获取替换内容
     * @return 替换内容
     */
    public String getReplacement() {
        return replacement;
    }

    /**
     * 设置替换内容
     * @param replacement 替换内容
     */
    public void setReplacement(String replacement) {
        this.replacement = replacement;
    }

    /**
     * 是否启用规则
     * @return 是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置是否启用规则
     * @param enabled 是否启用
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return name + " (" + (enabled ? "启用" : "禁用") + ")";
    }
}
