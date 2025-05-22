package edu.software.ergoutree.markdownautoclearup;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

/**
 * 背景设置对话框控制器
 */
public class BackgroundSettingsController {
    @FXML
    private TextField backgroundImagePathField;
    
    @FXML
    private Button selectImageButton;
    
    @FXML
    private Slider opacitySlider;
    
    @FXML
    private Label opacityValueLabel;
    
    @FXML
    private CheckBox tileBackgroundCheckBox;
    
    @FXML
    private Button applyButton;
    
    @FXML
    private Button cancelButton;
    
    @FXML
    private Button removeBackgroundButton;
    
    @FXML
    private ColorPicker backgroundColorPicker;
    
    @FXML
    private Rectangle colorPreviewRect;
    
    @FXML
    private CheckBox useColorBackgroundCheckBox;
    
    private Stage dialogStage;
    private BorderPane mainBorderPane;
    private ObjectProperty<Image> selectedImageProperty = new SimpleObjectProperty<>();
    private ObjectProperty<Color> selectedColorProperty = new SimpleObjectProperty<>(Color.WHITE);
    private DoubleProperty opacityProperty = new SimpleDoubleProperty(0.3);
    private BooleanProperty useColorBackgroundProperty = new SimpleBooleanProperty(false);
    private boolean applyClicked = false;
    private boolean removeBackground = false;
    
    /**
     * 初始化控制器
     */
    @FXML
    private void initialize() {
        // 绑定透明度滑块和标签
        opacityProperty.bind(opacitySlider.valueProperty());
        opacityValueLabel.textProperty().bind(
            Bindings.createStringBinding(
                () -> String.format("当前透明度: %d%%", (int)(opacityProperty.get() * 100)),
                opacityProperty
            )
        );
        
        // 设置选择图片按钮事件
        selectImageButton.setOnAction(event -> selectBackgroundImage());
        
        // 设置颜色选择器事件
        backgroundColorPicker.setValue(Color.LIGHTBLUE); // 设置默认颜色
        selectedColorProperty.bind(backgroundColorPicker.valueProperty());
        
        // 更新颜色预览矩形
        backgroundColorPicker.valueProperty().addListener((observable, oldValue, newValue) -> colorPreviewRect.setFill(newValue));
        colorPreviewRect.setFill(backgroundColorPicker.getValue());
        
        // 绑定使用颜色背景复选框
        useColorBackgroundProperty.bind(useColorBackgroundCheckBox.selectedProperty());
        
        // 设置应用按钮事件
        applyButton.setOnAction(event -> {
            applyClicked = true;
            dialogStage.close();
        });
        
        // 设置取消按钮事件
        cancelButton.setOnAction(event -> dialogStage.close());
        
        // 设置移除背景按钮事件
        removeBackgroundButton.setOnAction(event -> {
            removeBackground = true;
            applyClicked = true;
            dialogStage.close();
        });
    }
    
    /**
     * 设置对话框舞台
     * @param dialogStage 对话框舞台
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    /**
     * 设置主界面BorderPane
     * @param mainBorderPane 主界面BorderPane
     */
    public void setMainBorderPane(BorderPane mainBorderPane) {
        this.mainBorderPane = mainBorderPane;
    }
    
    /**
     * 设置当前背景
     * @param backgroundImage 当前背景图片
     * @param backgroundColor 当前背景颜色
     * @param opacity 当前透明度
     * @param tiled 是否平铺
     * @param useColorBackground 是否使用颜色背景
     */
    public void setCurrentBackground(Image backgroundImage, Color backgroundColor, double opacity, boolean tiled, boolean useColorBackground) {
        if (backgroundImage != null) {
            selectedImageProperty.set(backgroundImage);
            backgroundImagePathField.setText("当前已设置背景图片");
        }
        
        if (backgroundColor != null) {
            backgroundColorPicker.setValue(backgroundColor);
            colorPreviewRect.setFill(backgroundColor);
        }
        
        opacitySlider.setValue(opacity);
        tileBackgroundCheckBox.setSelected(tiled);
        useColorBackgroundCheckBox.setSelected(useColorBackground);
    }
    
    /**
     * 选择背景图片
     */
    private void selectBackgroundImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择背景图片");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("图片文件", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("所有文件", "*.*")
        );
        
        File file = fileChooser.showOpenDialog(dialogStage);
        if (file != null) {
            try {
                // 加载图片
                Image image = new Image(file.toURI().toString());
                selectedImageProperty.set(image);
                backgroundImagePathField.setText(file.getAbsolutePath());
            } catch (Exception e) {
                showAlert("错误", "无法加载图片: " + e.getMessage());
            }
        }
    }
    
    /**
     * 用户是否点击了应用按钮
     * @return 是否点击了应用按钮
     */
    public boolean isApplyClicked() {
        return applyClicked;
    }
    
    /**
     * 获取是否确认设置（与 isApplyClicked 相同，为了保持接口一致性）
     * @return 是否确认设置
     */
    public boolean isConfirmed() {
        return applyClicked;
    }
    
    /**
     * 用户是否选择了移除背景
     * @return 是否选择了移除背景
     */
    public boolean isRemoveBackground() {
        return removeBackground;
    }
    
    /**
     * 获取选择的图片
     * @return 选择的图片
     */
    public Image getSelectedImage() {
        return selectedImageProperty.get();
    }
    
    /**
     * 获取选择的颜色
     * @return 选择的颜色
     */
    public Color getSelectedColor() {
        return selectedColorProperty.get();
    }
    
    /**
     * 获取是否使用颜色背景
     * @return 是否使用颜色背景
     */
    public boolean isUseColorBackground() {
        return useColorBackgroundProperty.get();
    }
    
    /**
     * 获取透明度
     * @return 透明度
     */
    public double getOpacity() {
        return opacityProperty.get();
    }
    
    /**
     * 获取是否平铺背景
     * @return 是否平铺背景
     */
    public boolean isTileBackground() {
        return tileBackgroundCheckBox.isSelected();
    }
    
    /**
     * 显示提示对话框
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
}
