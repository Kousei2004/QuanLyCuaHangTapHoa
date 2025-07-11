package com.example.da.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

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
    private StackPane mainContent;

    @FXML private Node customerManagementView;
    @FXML private Node supplierManagementView;
    @FXML private Node employeeManagementView;
    @FXML private Node promotionManagementView;
    @FXML private Node categoryManagementView;
    @FXML private Node productManagementView;

    @FXML
    private void initialize() {
        btnEmployeeMenu.setOnAction(e -> showEmployeeManagement());
        btnCustomerMenu.setOnAction(e -> showCustomerManagement());
        btnSupliersMenu.setOnAction(e -> showSupplierManagement());
        btnPromotionMenu.setOnAction(e -> showPromotionManagement());
        btnCategoryMenu.setOnAction(e -> showCategoryManagement());
        btnProductMenu.setOnAction(e -> showProductManagement());
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
} 