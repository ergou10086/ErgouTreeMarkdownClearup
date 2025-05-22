package edu.software.ergoutree.markdownautoclearup;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 应用程序控制器，处理用户界面交互
 */
public class HelloController {
    @FXML
    private TextField inputFileTextField;
    
    @FXML
    private TextField outputFileTextField;
    
    @FXML
    private TextArea editTextArea;
    
    @FXML
    private Button processButton;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private ToggleButton editToggleButton;
    
    @FXML
    private Button saveEditButton;
    
    @FXML
    private Button applySpacingButton;
    
    @FXML
    private Label dropHintLabel;
    
    @FXML
    private HBox progressBarContainer;
    
    @FXML
    private ProgressBar progressBar;
    
    @FXML
    private Label progressLabel;
    
    // 空格规则设置
    private boolean addSpaceBetweenChineseAndEnglish = true;
    private boolean addSpaceBetweenChineseAndNumber = true;
    private boolean addSpaceBetweenEnglishAndNumber = false;
    private boolean addSpaceAroundChinesePunctuation = false;
    private boolean addSpaceAroundEnglishPunctuation = false;
    private boolean convertToFullWidthPunctuation = false;
    private boolean convertToHalfWidthPunctuation = false;
    private boolean preserveSpecialElements = true; // 默认保留代码块、公式和超链接内容
    
    // 自定义正则表达式管理器
    private CustomRegexManager customRegexManager = new CustomRegexManager();
    
    @FXML
    private BorderPane mainBorderPane;
    
    private File selectedInputFile;
    private File selectedOutputFile;
    private String originalContent;
    private String processedContent;
    
    // 背景设置相关属性
    private Image backgroundImage;
    private Color backgroundColor = Color.LIGHTBLUE; // 默认背景颜色
    private double backgroundOpacity = 0.3; // 默认透明度
    private boolean tileBackground = true; // 默认平铺背景
    private boolean useColorBackground = false; // 默认不使用颜色背景
    
    /**
     * 初始化控制器
     */
    @FXML
    private void initialize() {
        // 监听输入和输出文件的变化，只有当两者都选择后才启用处理按钮
        inputFileTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateProcessButtonState();
        });
        
        outputFileTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateProcessButtonState();
        });
        
        // 初始时禁用编辑区
        editTextArea.setEditable(false);
        
        // 监听编辑区内容变化，当有内容时启用空格应用按钮
        editTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            applySpacingButton.setDisable(newValue == null || newValue.isEmpty());
        });
        
        // 设置拖放文件功能
        setupDragAndDrop();
    }
    
    /**
     * 设置拖放文件功能
     */
    private void setupDragAndDrop() {
        // 拖动进入事件
        editTextArea.setOnDragEntered(event -> {
            if (event.getDragboard().hasFiles()) {
                dropHintLabel.setVisible(true);
                event.consume();
            }
        });
        
        // 拖动离开事件
        editTextArea.setOnDragExited(event -> {
            dropHintLabel.setVisible(false);
            event.consume();
        });
        
        // 拖动悬停事件
        editTextArea.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(javafx.scene.input.TransferMode.COPY);
            }
            event.consume();
        });
        
        // 拖动放下事件
        editTextArea.setOnDragDropped(event -> {
            boolean success = false;
            if (event.getDragboard().hasFiles()) {
                // 获取拖放的文件
                File droppedFile = event.getDragboard().getFiles().get(0);
                
                // 检查是否为Markdown文件
                String extension = MarkdownSpacingProcessor.getFileExtension(droppedFile).toLowerCase();
                if (extension.equals("md") || extension.equals("markdown")) {
                    handleDroppedFile(droppedFile);
                    success = true;
                } else {
                    showAlert("错误", "请拖放 Markdown 文件(.md 或 .markdown)");
                }
            }
            
            event.setDropCompleted(success);
            dropHintLabel.setVisible(false);
            event.consume();
        });
    }
    
    /**
     * 处理拖放的文件
     * @param file 拖放的文件
     */
    private void handleDroppedFile(File file) {
        if (file != null && file.exists()) {
            // 更新文件路径显示
            selectedInputFile = file;
            inputFileTextField.setText(file.getAbsolutePath());
            
            // 加载文件内容
            loadAndPreviewFile(file);
            
            statusLabel.setText("文件已拖入: " + file.getName());
        }
    }
    
    /**
     * 更新处理按钮的状态
     */
    private void updateProcessButtonState() {
        boolean inputSelected = !inputFileTextField.getText().isEmpty();
        boolean outputSelected = !outputFileTextField.getText().isEmpty();
        processButton.setDisable(!(inputSelected && outputSelected));
    }
    
    /**
     * 选择输入文件按钮点击事件处理
     */
    @FXML
    protected void onSelectInputFileClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择Markdown文件");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Markdown文件", "*.md", "*.markdown"),
                new FileChooser.ExtensionFilter("所有文件", "*.*")
        );
        
        // 获取舞台（窗口）
        Stage stage = (Stage) inputFileTextField.getScene().getWindow();
        selectedInputFile = fileChooser.showOpenDialog(stage);
        
        if (selectedInputFile != null) {
            inputFileTextField.setText(selectedInputFile.getAbsolutePath());
            loadAndPreviewFile(selectedInputFile);
        }
    }
    
    /**
     * 选择输出文件按钮点击事件处理
     */
    @FXML
    protected void onSelectOutputFileClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择保存位置");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Markdown文件", "*.md", "*.markdown"),
                new FileChooser.ExtensionFilter("所有文件", "*.*")
        );
        
        // 如果已经选择了输入文件，设置默认的输出文件名
        if (selectedInputFile != null) {
            String fileName = selectedInputFile.getName();
            String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
            String extension = fileName.substring(fileName.lastIndexOf('.'));
            fileChooser.setInitialFileName(baseName + "_spaced" + extension);
            fileChooser.setInitialDirectory(selectedInputFile.getParentFile());
        }
        
        // 获取舞台（窗口）
        Stage stage = (Stage) outputFileTextField.getScene().getWindow();
        selectedOutputFile = fileChooser.showSaveDialog(stage);
        
        if (selectedOutputFile != null) {
            outputFileTextField.setText(selectedOutputFile.getAbsolutePath());
        }
    }
    
    /**
     * 处理并导出按钮点击事件处理
     */
    @FXML
    protected void onProcessButtonClick() {
        if (selectedInputFile == null || selectedOutputFile == null) {
            showAlert("错误", "请先选择输入和输出文件");
            return;
        }
        
        // 显示进度条
        showProgressBar(true);
        progressBar.setProgress(0);
        progressLabel.setText("0%");
        
        // 禁用处理按钮，防止重复点击
        processButton.setDisable(true);
        
        // 创建后台任务处理文件
        Thread processingThread = new Thread(() -> {
            try {
                // 使用当前编辑区域的内容而不是原始文件内容
                String contentToProcess = editTextArea.getText();
                final int totalSteps = countProcessingSteps();
                int currentStep = 0;
                
                // 更新进度 - 开始处理
                updateProgress(currentStep++, totalSteps);
                
                // 根据规则设置处理内容，添加空格
                String processedText = contentToProcess;
                
                if (preserveSpecialElements) {
                    // 保留特殊元素（代码块、公式、超链接等）
                    processedText = MarkdownSpacingProcessor.processText(
                        contentToProcess,
                        addSpaceBetweenChineseAndEnglish,
                        addSpaceBetweenChineseAndNumber,
                        addSpaceBetweenEnglishAndNumber,
                        addSpaceAroundChinesePunctuation,
                        addSpaceAroundEnglishPunctuation,
                        convertToFullWidthPunctuation,
                        convertToHalfWidthPunctuation,
                        customRegexManager
                    );
                    
                    // 更新进度 - 处理完成
                    updateProgress(totalSteps - 1, totalSteps);
                } else {
                    // 不保留特殊元素，分步处理整个文本
                    
                    // 处理中英文之间的空格
                    if (addSpaceBetweenChineseAndEnglish) {
                        processedText = MarkdownSpacingProcessor.addSpaceBetweenChineseAndEnglish(processedText);
                        updateProgress(currentStep++, totalSteps);
                    }
                    
                    // 处理中文和数字之间的空格
                    if (addSpaceBetweenChineseAndNumber) {
                        processedText = MarkdownSpacingProcessor.addSpaceBetweenChineseAndNumber(processedText);
                        updateProgress(currentStep++, totalSteps);
                    }
                    
                    // 处理英文和数字之间的空格
                    if (addSpaceBetweenEnglishAndNumber) {
                        processedText = MarkdownSpacingProcessor.addSpaceBetweenEnglishAndNumber(processedText);
                        updateProgress(currentStep++, totalSteps);
                    }
                    
                    // 处理中文标点前后的空格
                    if (addSpaceAroundChinesePunctuation) {
                        processedText = MarkdownSpacingProcessor.addSpaceAroundChinesePunctuation(processedText);
                        updateProgress(currentStep++, totalSteps);
                    }
                    
                    // 处理英文标点前后的空格
                    if (addSpaceAroundEnglishPunctuation) {
                        processedText = MarkdownSpacingProcessor.addSpaceAroundEnglishPunctuation(processedText);
                        updateProgress(currentStep++, totalSteps);
                    }
                    
                    // 处理标点转换
                    if (convertToFullWidthPunctuation) {
                        processedText = MarkdownSpacingProcessor.convertToFullWidthPunctuation(processedText);
                        updateProgress(currentStep++, totalSteps);
                    } else if (convertToHalfWidthPunctuation) {
                        processedText = MarkdownSpacingProcessor.convertToHalfWidthPunctuation(processedText);
                        updateProgress(currentStep++, totalSteps);
                    }
                    
                    // 应用自定义正则表达式规则
                    processedText = customRegexManager.applyRules(processedText);
                    updateProgress(currentStep++, totalSteps);
                }
                
                // 保存到输出文件
                final String finalProcessedText = processedText;
                Files.writeString(selectedOutputFile.toPath(), finalProcessedText, StandardCharsets.UTF_8);
                
                // 更新进度 - 完成
                updateProgress(totalSteps, totalSteps);
                
                // 更新UI（必须在JavaFX应用程序线程中执行）
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("处理成功！文件已保存到: " + selectedOutputFile.getAbsolutePath());
                    showAlert("成功", "文件处理成功！\n已保存到: " + selectedOutputFile.getAbsolutePath());
                    // 隐藏进度条
                    showProgressBar(false);
                    // 重新启用处理按钮
                    processButton.setDisable(false);
                });
            } catch (Exception e) {
                // 更新UI（必须在JavaFX应用程序线程中执行）
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("处理出错: " + e.getMessage());
                    showAlert("错误", "处理出错: " + e.getMessage());
                    // 隐藏进度条
                    showProgressBar(false);
                    // 重新启用处理按钮
                    processButton.setDisable(false);
                });
                e.printStackTrace();
            }
        });
        
        // 启动处理线程
        processingThread.setDaemon(true);
        processingThread.start();
    }
    
    /**
     * 加载并预览文件内容
     * @param file 要加载的文件
     */
    private void loadAndPreviewFile(File file) {
        try {
            // 读取原始内容
            originalContent = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            
            // 根据规则设置处理内容，添加空格
            if (preserveSpecialElements) {
                // 保留特殊元素（代码块、公式、超链接等）
                processedContent = MarkdownSpacingProcessor.processText(
                    originalContent,
                    addSpaceBetweenChineseAndEnglish,
                    addSpaceBetweenChineseAndNumber,
                    addSpaceBetweenEnglishAndNumber,
                    addSpaceAroundChinesePunctuation,
                    addSpaceAroundEnglishPunctuation,
                    convertToFullWidthPunctuation,
                    convertToHalfWidthPunctuation,
                    customRegexManager
                );
            } else {
                // 不保留特殊元素，直接处理整个文本
                processedContent = originalContent;
                
                // 处理中英文之间的空格
                if (addSpaceBetweenChineseAndEnglish) {
                    processedContent = MarkdownSpacingProcessor.addSpaceBetweenChineseAndEnglish(processedContent);
                }
                
                // 处理中文和数字之间的空格
                if (addSpaceBetweenChineseAndNumber) {
                    processedContent = MarkdownSpacingProcessor.addSpaceBetweenChineseAndNumber(processedContent);
                }
                
                // 处理英文和数字之间的空格
                if (addSpaceBetweenEnglishAndNumber) {
                    processedContent = MarkdownSpacingProcessor.addSpaceBetweenEnglishAndNumber(processedContent);
                }
                
                // 处理中文标点前后的空格
                if (addSpaceAroundChinesePunctuation) {
                    processedContent = MarkdownSpacingProcessor.addSpaceAroundChinesePunctuation(processedContent);
                }
                
                // 处理英文标点前后的空格
                if (addSpaceAroundEnglishPunctuation) {
                    processedContent = MarkdownSpacingProcessor.addSpaceAroundEnglishPunctuation(processedContent);
                }
                
                // 处理标点转换
                if (convertToFullWidthPunctuation) {
                    processedContent = MarkdownSpacingProcessor.convertToFullWidthPunctuation(processedContent);
                } else if (convertToHalfWidthPunctuation) {
                    processedContent = MarkdownSpacingProcessor.convertToHalfWidthPunctuation(processedContent);
                }
                
                // 应用自定义正则表达式规则
                processedContent = customRegexManager.applyRules(processedContent);
            }
            
            // 在编辑区域显示原始内容
            editTextArea.setText(originalContent);
            
            // 启用空格应用按钮
            applySpacingButton.setDisable(false);
            
            // 更新预览
            updatePreview();
            
            statusLabel.setText("文件已加载，可以处理或编辑");
        } catch (IOException e) {
            editTextArea.setText("无法加载文件: " + e.getMessage());
            statusLabel.setText("加载文件失败");
            e.printStackTrace();
        }
    }
    
    /**
     * 编辑模式切换按钮点击事件
     */
    @FXML
    protected void onEditToggleClick() {
        boolean isEditMode = editToggleButton.isSelected();
        editTextArea.setEditable(isEditMode);
        saveEditButton.setDisable(!isEditMode);
        
        if (isEditMode) {
            editToggleButton.setText("编辑中");
            
            // 如果没有打开文件，显示新建文件提示
            if (selectedInputFile == null) {
                statusLabel.setText("已启用编辑模式，可以直接输入内容并保存为新文件");
                // 清空编辑区，准备新建文件
                if (editTextArea.getText().isEmpty()) {
                    editTextArea.setText("");
                    editTextArea.setPromptText("请输入Markdown内容，然后点击保存编辑按钮保存为新文件");
                }
            } else {
                statusLabel.setText("已启用编辑模式，可以直接编辑文件内容");
            }
        } else {
            editToggleButton.setText("启用编辑");
            statusLabel.setText("已禁用编辑模式");
        }
    }
    
    /**
     * 保存编辑按钮点击事件
     */
    @FXML
    protected void onSaveEditClick() {
        if (!editTextArea.isEditable()) {
            showAlert("错误", "请先启用编辑模式");
            return;
        }
        
        String currentContent = editTextArea.getText();
        if (currentContent.isEmpty()) {
            showAlert("错误", "编辑内容不能为空");
            return;
        }
        
        // 如果是编辑已有文件，提示是否覆盖原文件
        if (selectedInputFile != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("确认保存");
            confirmAlert.setHeaderText("确认保存更改");
            confirmAlert.setContentText("是否保存更改到原文件？\n选择\"否\"将另存为新文件。");
            
            confirmAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
            confirmAlert.showAndWait().ifPresent(buttonType -> {
                if (buttonType == ButtonType.YES) {
                    // 保存到原文件
                    saveToFile(selectedInputFile, currentContent);
                    originalContent = currentContent; // 更新原始内容
                } else if (buttonType == ButtonType.NO) {
                    // 另存为新文件
                    saveAsNewFile(currentContent);
                }
            });
        } else {
            // 新建文件，直接保存
            saveAsNewFile(currentContent);
        }
    }
    
    /**
     * 另存为新文件
     * @param content 要保存的内容
     */
    private void saveAsNewFile(String content) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存为新文件");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Markdown文件", "*.md", "*.markdown"),
                new FileChooser.ExtensionFilter("所有文件", "*.*")
        );
        
        // 获取舞台（窗口）
        Stage stage = (Stage) saveEditButton.getScene().getWindow();
        File newFile = fileChooser.showSaveDialog(stage);
        
        if (newFile != null) {
            saveToFile(newFile, content);
            selectedInputFile = newFile;
            inputFileTextField.setText(newFile.getAbsolutePath());
            originalContent = content; // 更新原始内容
        }
    }
    
    /**
     * 保存内容到文件
     * @param file 目标文件
     * @param content 要保存的内容
     */
    private void saveToFile(File file, String content) {
        try {
            Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
            statusLabel.setText("文件已保存到: " + file.getAbsolutePath());
            showAlert("成功", "文件已成功保存到\n" + file.getAbsolutePath());
        } catch (IOException e) {
            statusLabel.setText("保存失败: " + e.getMessage());
            showAlert("错误", "保存文件失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 应用空格规则按钮点击事件
     */
    @FXML
    protected void onApplySpacingClick() {
        String currentText = editTextArea.getText();
        if (currentText == null || currentText.isEmpty()) {
            showAlert("错误", "没有可处理的文本");
            return;
        }
        
        // 应用空格规则
        String processedText;
        if (preserveSpecialElements) {
            // 保留特殊元素（代码块、公式、超链接等）
            processedText = MarkdownSpacingProcessor.processText(
                currentText,
                addSpaceBetweenChineseAndEnglish,
                addSpaceBetweenChineseAndNumber,
                addSpaceBetweenEnglishAndNumber,
                addSpaceAroundChinesePunctuation,
                addSpaceAroundEnglishPunctuation,
                convertToFullWidthPunctuation,
                convertToHalfWidthPunctuation,
                customRegexManager
            );
        } else {
            // 不保留特殊元素，直接处理整个文本
            processedText = currentText;
            
            // 处理中英文之间的空格
            if (addSpaceBetweenChineseAndEnglish) {
                processedText = MarkdownSpacingProcessor.addSpaceBetweenChineseAndEnglish(processedText);
            }
            
            // 处理中文和数字之间的空格
            if (addSpaceBetweenChineseAndNumber) {
                processedText = MarkdownSpacingProcessor.addSpaceBetweenChineseAndNumber(processedText);
            }
            
            // 处理英文和数字之间的空格
            if (addSpaceBetweenEnglishAndNumber) {
                processedText = MarkdownSpacingProcessor.addSpaceBetweenEnglishAndNumber(processedText);
            }
            
            // 处理中文标点前后的空格
            if (addSpaceAroundChinesePunctuation) {
                processedText = MarkdownSpacingProcessor.addSpaceAroundChinesePunctuation(processedText);
            }
            
            // 处理英文标点前后的空格
            if (addSpaceAroundEnglishPunctuation) {
                processedText = MarkdownSpacingProcessor.addSpaceAroundEnglishPunctuation(processedText);
            }
            
            // 处理标点转换
            if (convertToFullWidthPunctuation) {
                processedText = MarkdownSpacingProcessor.convertToFullWidthPunctuation(processedText);
            } else if (convertToHalfWidthPunctuation) {
                processedText = MarkdownSpacingProcessor.convertToHalfWidthPunctuation(processedText);
            }
            
            // 应用自定义正则表达式规则
            processedText = customRegexManager.applyRules(processedText);
        }
        
        editTextArea.setText(processedText);
        statusLabel.setText("已应用空格规则，可以继续编辑或保存");
    }
    
    /**
     * 打开空格规则设置对话框
     */
    @FXML
    protected void onRulesSettingsClick() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("rules-settings.fxml"));
            VBox page = (VBox) loader.load();
            
            // 创建对话框
            Stage dialogStage = new Stage();
            dialogStage.setTitle("空格规则设置");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(mainBorderPane.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            
            // 获取控制器
            RulesSettingsController controller = loader.getController();
            
            // 设置当前规则
            controller.setCurrentRules(
                addSpaceBetweenChineseAndEnglish,
                addSpaceBetweenChineseAndNumber,
                addSpaceBetweenEnglishAndNumber,
                addSpaceAroundChinesePunctuation,
                addSpaceAroundEnglishPunctuation,
                convertToFullWidthPunctuation,
                convertToHalfWidthPunctuation
            );
            
            // 显示对话框并等待关闭
            dialogStage.showAndWait();
            
            // 如果用户确认了设置，更新规则
            if (controller.isConfirmed()) {
                addSpaceBetweenChineseAndEnglish = controller.isAddSpaceBetweenChineseAndEnglish();
                addSpaceBetweenChineseAndNumber = controller.isAddSpaceBetweenChineseAndNumber();
                addSpaceBetweenEnglishAndNumber = controller.isAddSpaceBetweenEnglishAndNumber();
                addSpaceAroundChinesePunctuation = controller.isAddSpaceAroundChinesePunctuation();
                addSpaceAroundEnglishPunctuation = controller.isAddSpaceAroundEnglishPunctuation();
                convertToFullWidthPunctuation = controller.isConvertToFullWidthPunctuation();
                convertToHalfWidthPunctuation = controller.isConvertToHalfWidthPunctuation();
                preserveSpecialElements = controller.isPreserveSpecialElements();
                
                // 如果有原始内容，重新处理并更新预览
                if (originalContent != null && !originalContent.isEmpty()) {
                    processedContent = MarkdownSpacingProcessor.processText(
                        originalContent,
                        addSpaceBetweenChineseAndEnglish,
                        addSpaceBetweenChineseAndNumber,
                        addSpaceBetweenEnglishAndNumber,
                        addSpaceAroundChinesePunctuation,
                        addSpaceAroundEnglishPunctuation,
                        convertToFullWidthPunctuation,
                        convertToHalfWidthPunctuation,
                        customRegexManager
                    );
                    statusLabel.setText("规则设置已更新，点击\"应用空格规则\"按钮查看效果");
                } else {
                    statusLabel.setText("规则设置已更新");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("错误", "无法打开空格规则设置对话框: " + e.getMessage());
        }
    }
    
    /**
     * 打开自定义正则表达式设置对话框
     */
    @FXML
    protected void onCustomRegexSettingsClick() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("custom-regex-settings.fxml"));
            VBox page = (VBox) loader.load();
            
            // 创建对话框
            Stage dialogStage = new Stage();
            dialogStage.setTitle("自定义正则表达式设置");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(mainBorderPane.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            
            // 获取控制器
            CustomRegexSettingsController controller = loader.getController();
            
            // 设置当前的自定义正则表达式管理器
            controller.setCustomRegexManager(customRegexManager);
            
            // 显示对话框并等待关闭
            dialogStage.showAndWait();
            
            // 如果用户确认了设置，更新自定义正则表达式管理器
            if (controller.isConfirmed()) {
                // 自定义正则表达式管理器已经在控制器中更新，无需额外操作
                statusLabel.setText("自定义正则表达式规则已更新");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("错误", "无法打开自定义正则表达式设置对话框: " + e.getMessage());
        }
    }
    
    /**
     * 更新预览
     * 将处理后的内容显示在编辑区域
     */
    private void updatePreview() {
        if (originalContent != null && !originalContent.isEmpty()) {
            if (processedContent != null && !processedContent.isEmpty()) {
                // 如果已经有处理后的内容，显示处理后的内容
                editTextArea.setText(processedContent);
            } else {
                // 如果没有处理后的内容，显示原始内容
                editTextArea.setText(originalContent);
            }
        }
    }
    
    /**
     * 新建文件按钮点击事件
     */
    @FXML
    protected void onNewFileClick() {
        // 如果当前有未保存的编辑内容，则提示用户
        if (editTextArea.isEditable() && !editTextArea.getText().isEmpty() && 
            (selectedInputFile == null || !editTextArea.getText().equals(originalContent))) {
            
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("确认新建");
            confirmAlert.setHeaderText("当前有未保存的内容");
            confirmAlert.setContentText("是否放弃当前编辑内容并创建新文件？");
            
            confirmAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            confirmAlert.showAndWait().ifPresent(buttonType -> {
                if (buttonType == ButtonType.YES) {
                    createNewFile();
                }
            });
        } else {
            createNewFile();
        }
    }
    
    /**
     * 创建新文件
     */
    private void createNewFile() {
        // 清空文件路径和内容
        selectedInputFile = null;
        selectedOutputFile = null;
        inputFileTextField.setText("");
        outputFileTextField.setText("");
        originalContent = "";
        processedContent = "";
        
        // 清空编辑区并启用编辑
        editTextArea.setText("");
        editTextArea.setPromptText("请输入Markdown内容，然后点击保存编辑按钮保存为新文件");
        editToggleButton.setSelected(true);
        editTextArea.setEditable(true);
        saveEditButton.setDisable(false);
        editToggleButton.setText("编辑中");
        
        // 禁用处理按钮
        processButton.setDisable(true);
        
        statusLabel.setText("已创建新文件，请输入内容后保存");
    }
    

    
    /**
     * 更换背景按钮点击事件
     */
    @FXML
    protected void onChangeBackgroundClick() {
        try {
            // 加载背景设置对话框FXML
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("background-settings.fxml"));
            VBox page = (VBox) loader.load();
            
            // 创建对话框
            Stage dialogStage = new Stage();
            dialogStage.setTitle("背景设置");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(mainBorderPane.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            
            // 获取控制器
            BackgroundSettingsController controller = loader.getController();
            
            // 设置当前背景
            controller.setCurrentBackground(backgroundImage, backgroundColor, backgroundOpacity, tileBackground, useColorBackground);
            
            // 显示对话框并等待关闭
            dialogStage.showAndWait();
            
            // 如果用户确认了设置，更新背景
            if (controller.isConfirmed()) {
                backgroundImage = controller.getSelectedImage();
                backgroundColor = controller.getSelectedColor();
                backgroundOpacity = controller.getOpacity();
                tileBackground = controller.isTileBackground();
                useColorBackground = controller.isUseColorBackground();
                
                updateBackground();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("错误", "无法打开背景设置对话框: " + e.getMessage());
        }
    }
    
    /**
     * 更新背景
     */
    private void updateBackground() {
        // 清除现有背景
        mainBorderPane.setBackground(null);
        
        if (useColorBackground && backgroundColor != null) {
            // 使用颜色背景
            BackgroundFill backgroundFill = new BackgroundFill(
                    Color.color(
                            backgroundColor.getRed(),
                            backgroundColor.getGreen(),
                            backgroundColor.getBlue(),
                            backgroundOpacity
                    ),
                    null, null);
            mainBorderPane.setBackground(new Background(backgroundFill));
        } else if (backgroundImage != null) {
            // 使用图片背景
            BackgroundSize backgroundSize;
            if (tileBackground) {
                // 平铺背景
                backgroundSize = new BackgroundSize(
                        BackgroundSize.AUTO,
                        BackgroundSize.AUTO,
                        true,
                        true,
                        false,
                        false);
            } else {
                // 缩放背景
                backgroundSize = new BackgroundSize(
                        1.0,
                        1.0,
                        true,
                        true,
                        false,
                        false);
            }
            
            BackgroundImage backgroundImg = new BackgroundImage(
                    backgroundImage,
                    BackgroundRepeat.REPEAT,
                    BackgroundRepeat.REPEAT,
                    BackgroundPosition.CENTER,
                    backgroundSize);
            
            mainBorderPane.setBackground(new Background(backgroundImg));
            mainBorderPane.getBackground().getFills().forEach(fill -> {
                System.out.println("Fill: " + fill);
            });
        }
    }
    
    /**
     * 移除背景
     */
    private void removeBackground() {
        backgroundImage = null;
        useColorBackground = false;
        mainBorderPane.setBackground(null);
    }
    
    /**
     * 差异报告按钮点击事件
     * 显示原始内容和处理后内容的差异
     */
    @FXML
    protected void onDiffButtonClick() {
        System.out.println("差异报告按钮被点击");
        
        if (originalContent == null || originalContent.isEmpty()) {
            System.out.println("原始内容为空");
            showAlert("错误", "没有原始内容可比较");
            return;
        }
        
        System.out.println("原始内容长度: " + originalContent.length());
        
        // 获取当前编辑区域的内容
        String currentContent = editTextArea.getText();
        
        try {
            System.out.println("开始加载差异视图 FXML");
            // 创建差异对话框
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("diff-view.fxml"));
            System.out.println("加载资源路径: " + getClass().getResource("diff-view.fxml"));
            BorderPane page = (BorderPane) loader.load();
            System.out.println("成功加载 FXML");
            
            // 创建对话框
            Stage dialogStage = new Stage();
            dialogStage.setTitle("文本差异报告");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(mainBorderPane.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            
            // 获取控制器
            DiffViewController controller = loader.getController();
            
            // 设置原始内容和当前内容
            controller.setTexts(originalContent, currentContent);
            
            // 显示对话框
            dialogStage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("错误", "无法打开差异报告对话框: " + e.getMessage());
        }
    }
    
    /**
     * 显示警告对话框
     * @param title 标题
     * @param message 消息内容
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * 显示或隐藏进度条
     * @param show 是否显示
     */
    private void showProgressBar(boolean show) {
        javafx.application.Platform.runLater(() -> {
            progressBarContainer.setVisible(show);
            progressBarContainer.setManaged(show);
        });
    }
    
    /**
     * 更新进度条
     * @param current 当前步骤
     * @param total 总步骤数
     */
    private void updateProgress(int current, int total) {
        double progress = (double) current / total;
        javafx.application.Platform.runLater(() -> {
            progressBar.setProgress(progress);
            progressLabel.setText(String.format("%.0f%%", progress * 100));
        });
    }
    
    /**
     * 计算处理步骤总数
     * @return 处理步骤总数
     */
    private int countProcessingSteps() {
        if (preserveSpecialElements) {
            return 2; // 开始和结束
        } else {
            int steps = 2; // 开始和保存步骤
            if (addSpaceBetweenChineseAndEnglish) steps++;
            if (addSpaceBetweenChineseAndNumber) steps++;
            if (addSpaceBetweenEnglishAndNumber) steps++;
            if (addSpaceAroundChinesePunctuation) steps++;
            if (addSpaceAroundEnglishPunctuation) steps++;
            if (convertToFullWidthPunctuation || convertToHalfWidthPunctuation) steps++;
            steps++; // 自定义规则处理
            return steps;
        }
    }
}
