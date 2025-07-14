package com.example.da.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import com.example.da.model.WorkShift;
import com.example.da.model.Session;
import javafx.event.ActionEvent;

public class ManagementController {
    // Controller cho Admin.fxml
    @FXML
    private Button btnEmployeeMenu;
    @FXML
    private Button btnCustomerMenu;
    @FXML
    private Button btnSupliersMenu;
    @FXML
    private Button btnPromotionMenu;
    @FXML
    private Button btnCategoryMenu;
    @FXML
    private Button btnProductMenu;
    @FXML
    private Button btnInventoryMenu;
    @FXML
    private Button btnShiftMenu;
    @FXML
    private Button btnOrderMenu;
    @FXML
    private StackPane mainContent;

    @FXML private Node customerManagementView;
    @FXML private Node supplierManagementView;
    @FXML private Node employeeManagementView;
    @FXML private Node promotionManagementView;
    @FXML private Node categoryManagementView;
    @FXML private Node productManagementView;
    @FXML private Node orderManagementView;

    @FXML
    private void initialize() {
        btnEmployeeMenu.setOnAction(e -> showEmployeeManagement());
        btnCustomerMenu.setOnAction(e -> showCustomerManagement());
        btnSupliersMenu.setOnAction(e -> showSupplierManagement());
        btnPromotionMenu.setOnAction(e -> showPromotionManagement());
        btnCategoryMenu.setOnAction(e -> showCategoryManagement());
        btnProductMenu.setOnAction(e -> showProductManagement());
        btnInventoryMenu.setOnAction(e -> showInventoryManagement());
        btnShiftMenu.setOnAction(e -> showShiftManagement());
        btnOrderMenu.setOnAction(e -> showOrderManagement());
    }

    private void showEmployeeManagement() {
        try {
            Node employeeView = FXMLLoader.load(getClass().getResource("/com/example/da/view/EmployeeManagement.fxml"));
            mainContent.getChildren().setAll(employeeView);

            Stage stage = (Stage) mainContent.getScene().getWindow();
            stage.setTitle("Hệ thống Quản lý Bán hàng - Quản lý Nhân viên");
            
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void showCustomerManagement() {
        try {
            Node customerView = FXMLLoader.load(getClass().getResource("/com/example/da/view/CustomerManagement.fxml"));
            mainContent.getChildren().setAll(customerView);
            Stage stage = (Stage) mainContent.getScene().getWindow();
            stage.setTitle("Hệ thống Quản lý Bán hàng - Quản lý Khách hàng");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void showSupplierManagement() {
        try {
            Node supplierView = FXMLLoader.load(getClass().getResource("/com/example/da/view/SupplierManagement.fxml"));
            mainContent.getChildren().setAll(supplierView);
            Stage stage = (Stage) mainContent.getScene().getWindow();
            stage.setTitle("Hệ thống Quản lý Bán hàng - Quản lý nhà cung cấp");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void showPromotionManagement() {
        try {
            Node promotionView = FXMLLoader.load(getClass().getResource("/com/example/da/view/PromotionManagement.fxml"));
            mainContent.getChildren().setAll(promotionView);
            Stage stage = (Stage) mainContent.getScene().getWindow();
            stage.setTitle("Hệ thống Quản lý Bán hàng - Quản lý Khuyến mãi");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void showCategoryManagement() {
        try {
            Node categoryView = FXMLLoader.load(getClass().getResource("/com/example/da/view/CategoryManagement.fxml"));
            mainContent.getChildren().setAll(categoryView);
            Stage stage = (Stage) mainContent.getScene().getWindow();
            stage.setTitle("Hệ thống Quản lý Bán hàng - Quản lý Danh mục sản phẩm");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void showProductManagement() {
        try {
            Node productView = FXMLLoader.load(getClass().getResource("/com/example/da/view/ProductManagement.fxml"));
            mainContent.getChildren().setAll(productView);
            Stage stage = (Stage) mainContent.getScene().getWindow();
            stage.setTitle("Hệ thống Quản lý Bán hàng - Quản lý Sản phẩm");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void showInventoryManagement() {
        try {
            Node inventoryView = FXMLLoader.load(getClass().getResource("/com/example/da/view/InventoryManagement.fxml"));
            mainContent.getChildren().setAll(inventoryView);
            Stage stage = (Stage) mainContent.getScene().getWindow();
            stage.setTitle("Hệ thống Quản lý Bán hàng - Quản lý Kho hàng");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void showShiftManagement() {
        try {
            Node shiftView = FXMLLoader.load(getClass().getResource("/com/example/da/view/ShiftManagement.fxml"));
            mainContent.getChildren().setAll(shiftView);
            Stage stage = (Stage) mainContent.getScene().getWindow();
            stage.setTitle("Hệ thống Quản lý Bán hàng - Quản lý Ca làm việc");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void showOrderManagement() {
        try {
            Node orderView = FXMLLoader.load(getClass().getResource("/com/example/da/view/OrderManagement.fxml"));
            mainContent.getChildren().setAll(orderView);
            Stage stage = (Stage) mainContent.getScene().getWindow();
            stage.setTitle("Hệ thống Quản lý Bán hàng - Quản lý Đơn hàng");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleCloseShift(ActionEvent event) {
        showCloseShiftDialog();
    }

    private void showCloseShiftDialog() {
        try {
            int userId = Session.getCurrentUser().getUser_id();
            WorkShift currentShift = WorkShift.getCurrentShiftForUser(userId);
            if (currentShift == null) {
                showAlert("Lỗi", "Không tìm thấy ca làm việc hiện tại!");
                return;
            }
            javafx.scene.control.Dialog<ButtonType> dialog = new javafx.scene.control.Dialog<>();
            dialog.setTitle("Chốt ca làm việc");
            dialog.setHeaderText("Xác nhận chốt ca");
            VBox vbox = new VBox(10);
            vbox.setPrefWidth(400);
            Label lblStart = new Label("Bắt đầu: " + currentShift.getStartTime());
            Label lblEnd = new Label("Kết thúc: " + java.time.LocalDateTime.now());
            double totalSales = currentShift.calculateTotalSales();
            Label lblTotalSales = new Label("Tổng doanh thu: " + String.format("%,.0f ₫", totalSales));
            TextField txtDiscrepancy = new TextField();
            txtDiscrepancy.setPromptText("Nhập lệch quỹ (nếu có)");
            vbox.getChildren().addAll(lblStart, lblEnd, lblTotalSales, new Label("Lệch quỹ:"), txtDiscrepancy);
            dialog.getDialogPane().setContent(vbox);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dialog.setResultConverter(bt -> {
                if (bt == ButtonType.OK) {
                    try {
                        double discrepancy = 0.0;
                        if (!txtDiscrepancy.getText().isEmpty()) {
                            discrepancy = Double.parseDouble(txtDiscrepancy.getText());
                        }
                        currentShift.setEndTime(new java.sql.Timestamp(System.currentTimeMillis()));
                        currentShift.setTotalSales(totalSales);
                        currentShift.setDiscrepancy(discrepancy);
                        currentShift.setConfirmedBy(userId);
                        if (currentShift.update()) {
                            showAlert("Thành công", "Đã chốt ca thành công!");
                        } else {
                            showAlert("Lỗi", "Không thể cập nhật ca làm việc!");
                        }
                    } catch (Exception ex) {
                        showAlert("Lỗi", "Chốt ca thất bại: " + ex.getMessage());
                    }
                }
                return null;
            });
            dialog.showAndWait();
        } catch (Exception ex) {
            showAlert("Lỗi", "Không thể hiển thị dialog chốt ca!");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 