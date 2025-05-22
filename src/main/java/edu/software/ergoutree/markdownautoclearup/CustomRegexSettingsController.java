package edu.software.ergoutree.markdownautoclearup;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

/**
 * 自定义正则表达式设置控制器
 */
public class CustomRegexSettingsController {
    @FXML
    private ListView<CustomRegexRule> rulesListView;
    
    @FXML
    private TextField nameTextField;
    
    @FXML
    private TextField patternTextField;
    
    @FXML
    private TextField replacementTextField;
    
    @FXML
    private CheckBox enabledCheckBox;
    
    @FXML
    private Button addButton;
    
    @FXML
    private Button editButton;
    
    @FXML
    private Button deleteButton;
    
    @FXML
    private Button applyButton;
    
    @FXML
    private Button cancelButton;
    
    private CustomRegexManager regexManager;
    private ObservableList<CustomRegexRule> rulesList;
    private boolean confirmed = false;
    
    /**
     * 初始化
     */
    @FXML
    private void initialize() {
        regexManager = new CustomRegexManager();
        rulesList = FXCollections.observableArrayList(regexManager.getRules());
        rulesListView.setItems(rulesList);
        
        // 设置列表选择监听器
        rulesListView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    nameTextField.setText(newValue.getName());
                    patternTextField.setText(newValue.getPattern());
                    replacementTextField.setText(newValue.getReplacement());
                    enabledCheckBox.setSelected(newValue.isEnabled());
                } else {
                    clearFields();
                }
            }
        );
        
        // 初始状态下禁用编辑和删除按钮
        editButton.setDisable(true);
        deleteButton.setDisable(true);
        
        // 当选择项变化时，更新按钮状态
        rulesListView.getSelectionModel().selectedIndexProperty().addListener(
            (observable, oldValue, newValue) -> {
                boolean hasSelection = newValue.intValue() >= 0;
                editButton.setDisable(!hasSelection);
                deleteButton.setDisable(!hasSelection);
            }
        );
    }
    
    /**
     * 添加规则按钮点击事件
     */
    @FXML
    protected void onAddButtonClick() {
        String name = nameTextField.getText().trim();
        String pattern = patternTextField.getText().trim();
        String replacement = replacementTextField.getText();
        boolean enabled = enabledCheckBox.isSelected();
        
        if (name.isEmpty() || pattern.isEmpty()) {
            showAlert("错误", "规则名称和匹配模式不能为空");
            return;
        }
        
        CustomRegexRule rule = new CustomRegexRule(name, pattern, replacement, enabled);
        if (regexManager.addRule(rule)) {
            rulesList.add(rule);
            clearFields();
            showAlert("成功", "规则添加成功");
        } else {
            showAlert("错误", "无效的正则表达式");
        }
    }
    
    /**
     * 修改规则按钮点击事件
     */
    @FXML
    protected void onEditButtonClick() {
        int selectedIndex = rulesListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0) {
            showAlert("错误", "请先选择一个规则");
            return;
        }
        
        String name = nameTextField.getText().trim();
        String pattern = patternTextField.getText().trim();
        String replacement = replacementTextField.getText();
        boolean enabled = enabledCheckBox.isSelected();
        
        if (name.isEmpty() || pattern.isEmpty()) {
            showAlert("错误", "规则名称和匹配模式不能为空");
            return;
        }
        
        CustomRegexRule rule = new CustomRegexRule(name, pattern, replacement, enabled);
        if (regexManager.editRule(selectedIndex, rule)) {
            rulesList.set(selectedIndex, rule);
            showAlert("成功", "规则修改成功");
        } else {
            showAlert("错误", "无效的正则表达式");
        }
    }
    
    /**
     * 删除规则按钮点击事件
     */
    @FXML
    protected void onDeleteButtonClick() {
        int selectedIndex = rulesListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0) {
            showAlert("错误", "请先选择一个规则");
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("确认删除");
        confirmAlert.setHeaderText("确定要删除所选规则吗？");
        confirmAlert.setContentText("此操作不可撤销");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (regexManager.deleteRule(selectedIndex)) {
                    rulesList.remove(selectedIndex);
                    clearFields();
                    showAlert("成功", "规则删除成功");
                }
            }
        });
    }
    
    /**
     * 应用按钮点击事件
     */
    @FXML
    protected void onApplyButtonClick() {
        confirmed = true;
        closeDialog();
    }
    
    /**
     * 取消按钮点击事件
     */
    @FXML
    protected void onCancelButtonClick() {
        confirmed = false;
        closeDialog();
    }
    
    /**
     * 清空输入字段
     */
    private void clearFields() {
        nameTextField.clear();
        patternTextField.clear();
        replacementTextField.clear();
        enabledCheckBox.setSelected(true);
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
     * 关闭对话框
     */
    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    /**
     * 获取用户是否确认
     * @return 是否确认
     */
    public boolean isConfirmed() {
        return confirmed;
    }
    
    /**
     * 获取正则表达式管理器
     * @return 正则表达式管理器
     */
    public CustomRegexManager getRegexManager() {
        return regexManager;
    }
    
    /**
     * 设置正则表达式管理器
     * @param manager 要设置的正则表达式管理器
     */
    public void setCustomRegexManager(CustomRegexManager manager) {
        if (manager != null) {
            this.regexManager = manager;
            this.rulesList = FXCollections.observableArrayList(manager.getRules());
            rulesListView.setItems(rulesList);
        }
    }
}
