module edu.software.ergoutree.markdownautoclearup {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires javafx.graphics;
    requires javafx.web;
    requires org.commonmark;

    opens edu.software.ergoutree.markdownautoclearup to javafx.fxml;
    exports edu.software.ergoutree.markdownautoclearup;
}