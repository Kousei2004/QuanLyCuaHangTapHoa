package com.example.da.controller;

import com.example.da.model.Users;
import com.example.da.model.MySQLConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.stage.StageStyle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label statusLabel;
    @FXML
    private Button loginButton;
    @FXML
    private Button registerButton;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Vui lòng nhập đầy đủ thông tin.");
            return;
        }
        try (Connection conn = MySQLConnection.getConnection()) {
            String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND status = 'active'";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String roleStr = rs.getString("role");
                Users.Role role = Users.Role.valueOf(roleStr);
                showAlert(Alert.AlertType.INFORMATION, "Đăng nhập thành công!");
                // Chuyển màn hình dựa vào role
                javafx.fxml.FXMLLoader loader;
                javafx.scene.Parent root;
                javafx.stage.Stage stage = (javafx.stage.Stage) loginButton.getScene().getWindow();

                
                if (role == Users.Role.quanly) {
                    loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/da/view/Admin.fxml"));
                    root = loader.load();
                    stage.setScene(new javafx.scene.Scene(root, 1400, 800));
                    stage.setTitle("Hệ thống Quản lý Bán hàng - Admin");
                } else {
                    loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/da/view/SalesTab.fxml"));
                    root = loader.load();
                    stage.setScene(new javafx.scene.Scene(root, 1400, 800));
                    stage.setTitle("Hệ thống Quản lý Bán hàng - Nhân viên");
                }
                stage.setResizable(true);
                stage.centerOnScreen();
            } else {
                showAlert(Alert.AlertType.ERROR, "Sai tên đăng nhập hoặc mật khẩu, hoặc tài khoản bị khóa.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi kết nối: " + e.getMessage());
        }
    }


    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 