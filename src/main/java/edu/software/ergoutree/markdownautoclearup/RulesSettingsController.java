package edu.software.ergoutree.markdownautoclearup;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

/**
 * 规则设置对话框控制器
 */
public class RulesSettingsController {
    @FXML
    private CheckBox chineseEnglishCheckBox;
    
    @FXML
    private CheckBox chineseNumberCheckBox;
    
    @FXML
    private CheckBox englishNumberCheckBox;
    
    @FXML
    private CheckBox chinesePunctuationSpaceCheckBox;
    
    @FXML
    private CheckBox englishPunctuationSpaceCheckBox;
    
    @FXML
    private CheckBox preserveSpecialElementsCheckBox;
    
    @FXML
    private RadioButton noConversionRadio;
    
    @FXML
    private RadioButton fullWidthRadio;
    
    @FXML
    private RadioButton halfWidthRadio;
    
    @FXML
    private TextArea previewTextArea;
    
    @FXML
    private Button applyButton;
    
    @FXML
    private Button cancelButton;
    
    @FXML
    private Button resetButton;
    
    private boolean addSpaceBetweenChineseAndEnglish = true;
    private boolean addSpaceBetweenChineseAndNumber = true;
    private boolean addSpaceBetweenEnglishAndNumber = false;
    private boolean addSpaceAroundChinesePunctuation = false;
    private boolean addSpaceAroundEnglishPunctuation = false;
    private boolean convertToFullWidthPunctuation = false;
    private boolean convertToHalfWidthPunctuation = false;
    private boolean preserveSpecialElements = true; // 默认保留代码块、公式和超链接内容
    
    private boolean confirmed = false;
    
    @FXML
    private void initialize() {
        // 设置初始值
        chineseEnglishCheckBox.setSelected(addSpaceBetweenChineseAndEnglish);
        chineseNumberCheckBox.setSelected(addSpaceBetweenChineseAndNumber);
        englishNumberCheckBox.setSelected(addSpaceBetweenEnglishAndNumber);
        chinesePunctuationSpaceCheckBox.setSelected(addSpaceAroundChinesePunctuation);
        englishPunctuationSpaceCheckBox.setSelected(addSpaceAroundEnglishPunctuation);
        preserveSpecialElementsCheckBox.setSelected(preserveSpecialElements);
        
        // 设置标点转换单选按钮的初始状态
        if (convertToFullWidthPunctuation) {
            fullWidthRadio.setSelected(true);
        } else if (convertToHalfWidthPunctuation) {
            halfWidthRadio.setSelected(true);
        } else {
            noConversionRadio.setSelected(true);
        }
        
        // 添加监听器，实时更新预览
        chineseEnglishCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        chineseNumberCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        englishNumberCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        chinesePunctuationSpaceCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        englishPunctuationSpaceCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        preserveSpecialElementsCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        noConversionRadio.selectedProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        fullWidthRadio.selectedProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        halfWidthRadio.selectedProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        
        // 设置按钮事件
        applyButton.setOnAction(event -> {
            confirmed = true;
            addSpaceBetweenChineseAndEnglish = chineseEnglishCheckBox.isSelected();
            addSpaceBetweenChineseAndNumber = chineseNumberCheckBox.isSelected();
            addSpaceBetweenEnglishAndNumber = englishNumberCheckBox.isSelected();
            addSpaceAroundChinesePunctuation = chinesePunctuationSpaceCheckBox.isSelected();
            addSpaceAroundEnglishPunctuation = englishPunctuationSpaceCheckBox.isSelected();
            preserveSpecialElements = preserveSpecialElementsCheckBox.isSelected();
            convertToFullWidthPunctuation = fullWidthRadio.isSelected();
            convertToHalfWidthPunctuation = halfWidthRadio.isSelected();
            closeDialog();
        });
        
        cancelButton.setOnAction(event -> closeDialog());
        
        resetButton.setOnAction(event -> {
            chineseEnglishCheckBox.setSelected(true);
            chineseNumberCheckBox.setSelected(true);
            englishNumberCheckBox.setSelected(false);
            chinesePunctuationSpaceCheckBox.setSelected(false);
            englishPunctuationSpaceCheckBox.setSelected(false);
            preserveSpecialElementsCheckBox.setSelected(true);
            noConversionRadio.setSelected(true);
            updatePreview();
        });
        
        // 初始化预览
        updatePreview();
    }
    
    /**
     * 更新预览文本
     */
    private void updatePreview() {
        String originalText = "示例文本：我们使用Java8编程language来实现这个功能。这是中文标点，这是英文标点,.!?\n```java\npublic class Example { }\n```\n$E=mc^2$\n[Markdown链接](https://example.com)";
        
        // 获取标点转换设置
        boolean convertToFullWidth = fullWidthRadio.isSelected();
        boolean convertToHalfWidth = halfWidthRadio.isSelected();
        
        // 根据选项处理文本
        String processedText;
        if (preserveSpecialElementsCheckBox.isSelected()) {
            // 使用带特殊元素处理的方法
            processedText = MarkdownSpacingProcessor.processText(
                originalText,
                chineseEnglishCheckBox.isSelected(),
                chineseNumberCheckBox.isSelected(),
                englishNumberCheckBox.isSelected(),
                chinesePunctuationSpaceCheckBox.isSelected(),
                englishPunctuationSpaceCheckBox.isSelected(),
                convertToFullWidth,
                convertToHalfWidth
            );
        } else {
            // 不保留特殊元素，直接在整个文本上应用规则
            processedText = originalText;
            
            // 处理中英文之间的空格
            if (chineseEnglishCheckBox.isSelected()) {
                processedText = MarkdownSpacingProcessor.addSpaceBetweenChineseAndEnglish(processedText);
            }
            
            // 处理中文和数字之间的空格
            if (chineseNumberCheckBox.isSelected()) {
                processedText = MarkdownSpacingProcessor.addSpaceBetweenChineseAndNumber(processedText);
            }
            
            // 处理英文和数字之间的空格
            if (englishNumberCheckBox.isSelected()) {
                processedText = MarkdownSpacingProcessor.addSpaceBetweenEnglishAndNumber(processedText);
            }
            
            // 处理中文标点前后的空格
            if (chinesePunctuationSpaceCheckBox.isSelected()) {
                processedText = MarkdownSpacingProcessor.addSpaceAroundChinesePunctuation(processedText);
            }
            
            // 处理英文标点前后的空格
            if (englishPunctuationSpaceCheckBox.isSelected()) {
                processedText = MarkdownSpacingProcessor.addSpaceAroundEnglishPunctuation(processedText);
            }
            
            // 处理标点转换
            if (convertToFullWidth) {
                processedText = MarkdownSpacingProcessor.convertToFullWidthPunctuation(processedText);
            } else if (convertToHalfWidth) {
                processedText = MarkdownSpacingProcessor.convertToHalfWidthPunctuation(processedText);
            }
        }
        
        previewTextArea.setText(processedText);
    }
    
    /**
     * 关闭对话框
     */
    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    /**
     * 设置当前规则
     */
    public void setCurrentRules(boolean addSpaceBetweenChineseAndEnglish,
                               boolean addSpaceBetweenChineseAndNumber,
                               boolean addSpaceBetweenEnglishAndNumber,
                               boolean addSpaceAroundChinesePunctuation,
                               boolean addSpaceAroundEnglishPunctuation,
                               boolean convertToFullWidthPunctuation,
                               boolean convertToHalfWidthPunctuation) {
        this.addSpaceBetweenChineseAndEnglish = addSpaceBetweenChineseAndEnglish;
        this.addSpaceBetweenChineseAndNumber = addSpaceBetweenChineseAndNumber;
        this.addSpaceBetweenEnglishAndNumber = addSpaceBetweenEnglishAndNumber;
        this.addSpaceAroundChinesePunctuation = addSpaceAroundChinesePunctuation;
        this.addSpaceAroundEnglishPunctuation = addSpaceAroundEnglishPunctuation;
        this.convertToFullWidthPunctuation = convertToFullWidthPunctuation;
        this.convertToHalfWidthPunctuation = convertToHalfWidthPunctuation;
        
        // 如果已经初始化了UI组件，则更新UI
        if (chineseEnglishCheckBox != null) {
            chineseEnglishCheckBox.setSelected(addSpaceBetweenChineseAndEnglish);
            chineseNumberCheckBox.setSelected(addSpaceBetweenChineseAndNumber);
            englishNumberCheckBox.setSelected(addSpaceBetweenEnglishAndNumber);
            chinesePunctuationSpaceCheckBox.setSelected(addSpaceAroundChinesePunctuation);
            englishPunctuationSpaceCheckBox.setSelected(addSpaceAroundEnglishPunctuation);
            
            // 设置标点转换单选按钮
            if (convertToFullWidthPunctuation) {
                fullWidthRadio.setSelected(true);
            } else if (convertToHalfWidthPunctuation) {
                halfWidthRadio.setSelected(true);
            } else {
                noConversionRadio.setSelected(true);
            }
            
            updatePreview();
        }
    }
    
    /**
     * 兼容旧版本的setCurrentRules方法
     */
    public void setCurrentRules(boolean addSpaceBetweenChineseAndEnglish,
                               boolean addSpaceBetweenChineseAndNumber,
                               boolean addSpaceBetweenEnglishAndNumber) {
        setCurrentRules(addSpaceBetweenChineseAndEnglish,
                       addSpaceBetweenChineseAndNumber,
                       addSpaceBetweenEnglishAndNumber,
                       false, false, false, false);
    }
    
    /**
     * 获取是否确认
     */
    public boolean isConfirmed() {
        return confirmed;
    }
    
    /**
     * 获取是否在中英文之间添加空格
     */
    public boolean isAddSpaceBetweenChineseAndEnglish() {
        return addSpaceBetweenChineseAndEnglish;
    }
    
    /**
     * 获取是否在中文和数字之间添加空格
     */
    public boolean isAddSpaceBetweenChineseAndNumber() {
        return addSpaceBetweenChineseAndNumber;
    }
    
    /**
     * 获取是否在英文和数字之间添加空格
     */
    public boolean isAddSpaceBetweenEnglishAndNumber() {
        return addSpaceBetweenEnglishAndNumber;
    }
    
    /**
     * 获取是否在中文标点前后添加空格
     */
    public boolean isAddSpaceAroundChinesePunctuation() {
        return addSpaceAroundChinesePunctuation;
    }
    
    /**
     * 获取是否在英文标点前后添加空格
     */
    public boolean isAddSpaceAroundEnglishPunctuation() {
        return addSpaceAroundEnglishPunctuation;
    }
    
    /**
     * 获取是否将标点转换为全角
     */
    public boolean isConvertToFullWidthPunctuation() {
        return convertToFullWidthPunctuation;
    }
    
    /**
     * 获取是否将标点转换为半角
     */
    public boolean isConvertToHalfWidthPunctuation() {
        return convertToHalfWidthPunctuation;
    }
    
    /**
     * 获取是否保留特殊元素的设置
     * @return 是否保留特殊元素
     */
    public boolean isPreserveSpecialElements() {
        return preserveSpecialElements;
    }
}
