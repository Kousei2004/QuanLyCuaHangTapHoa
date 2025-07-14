package com.example.da.controller;

import com.example.da.model.Customer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.layout.Region;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Insets;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.fxml.FXMLLoader;
import com.example.da.dialog.CustomerFormDialogController;

import java.util.List;
import java.util.Objects;

public class CustomerManagementController {
    @FXML
    private FlowPane employeeGrid;
    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<String> cbDepartment;
    @FXML
    private ComboBox<String> cbPosition;
    @FXML
    private ComboBox<String> cbStatus;
    @FXML
    private Button btnAddEmployee;
    @FXML
    private Button btnImportExcel;
    @FXML
    private Button btnExportExcel;
    @FXML
    private Button btnClearFilters;
    @FXML
    private CheckBox cbSelectAll;
    @FXML
    private Label lblEmployeeCount;
    @FXML
    private ComboBox<String> cbPageSize;

    private ObservableList<Customer> customerList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadCustomers();
        // Thêm lắng nghe tìm kiếm
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            searchCustomers(newValue);
        });
    }

    private void loadCustomers() {
        List<Customer> customers = Customer.getAllCustomers();
        customerList.setAll(customers);
        employeeGrid.getChildren().clear();
        for (Customer c : customers) {
            VBox card = createCustomerCard(c);
            employeeGrid.getChildren().add(card);
        }
        lblEmployeeCount.setText("Hiển thị " + customers.size() + " khách hàng");
    }

    private void searchCustomers(String searchTerm) {
        List<Customer> customers;
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            customers = Customer.getAllCustomers();
        } else {
            customers = Customer.searchCustomers(searchTerm.trim());
        }
        customerList.setAll(customers);
        employeeGrid.getChildren().clear();
        for (Customer c : customers) {
            VBox card = createCustomerCard(c);
            employeeGrid.getChildren().add(card);
        }
        lblEmployeeCount.setText("Hiển thị " + customers.size() + " khách hàng");
    }

    private VBox createCustomerCard(Customer c) {
        VBox card = new VBox();
        card.setSpacing(8);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, #B0BEC5, 4, 0, 0, 2);");
        card.setPrefWidth(320);

        // Avatar
        String iconPath = "/icons/customer.png";
        java.net.URL iconUrl = getClass().getResource(iconPath);
        ImageView avatar;
        if (iconUrl != null) {
            avatar = new ImageView(new Image(iconUrl.toExternalForm()));
        } else {
            avatar = new ImageView();
        }
        avatar.setFitWidth(48);
        avatar.setFitHeight(48);
        avatar.setPreserveRatio(true);
        Circle avatarBg = new Circle(40);
        avatarBg.setFill(javafx.scene.paint.Color.web("#E3F2FD"));
        StackPane avatarPane = new StackPane();
        avatarPane.getChildren().addAll(avatarBg, avatar);
        VBox basicInfo = new VBox(5);
        Label name = new Label(c.getName());
        name.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #263238;");
        Label code = new Label("KH" + String.format("%03d", c.getCustomerId()));
        code.setStyle("-fx-text-fill: #90A4AE; -fx-font-size: 13px;");
        basicInfo.getChildren().addAll(name, code, new HBox(10));
        VBox info = new VBox(15);
        info.getChildren().add(avatarPane);
        info.getChildren().add(basicInfo);
        info.getChildren().add(new Separator());
        // Contact
        VBox contact = new VBox(8);
        HBox phoneBox = new HBox(10);
        Label phone = new Label(c.getPhone());
        phone.setStyle("-fx-text-fill: #607D8B; -fx-font-size: 13px;");
        phoneBox.getChildren().add(phone);
        HBox emailBox = new HBox(10);
        Label email = new Label(c.getEmail());
        email.setStyle("-fx-text-fill: #607D8B; -fx-font-size: 13px;");
        emailBox.getChildren().add(email);
        HBox addressBox = new HBox(10);
        Label address = new Label(c.getAddress());
        address.setStyle("-fx-text-fill: #607D8B; -fx-font-size: 13px;");
        addressBox.getChildren().add(address);
        contact.getChildren().addAll(phoneBox, emailBox, addressBox);
        info.getChildren().add(contact);
        // Stats (placeholder)
        HBox stats = new HBox(20);
        stats.setAlignment(javafx.geometry.Pos.CENTER);
        VBox stat1 = new VBox();
        Label stat1Val = new Label("0");
        stat1Val.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");
        Label stat1Lbl = new Label("Lần mua hàng");
        stat1Lbl.setStyle("-fx-text-fill: #90A4AE; -fx-font-size: 11px;");
        stat1.getChildren().addAll(stat1Val, stat1Lbl);
        Separator sep = new Separator();
        sep.setOrientation(javafx.geometry.Orientation.VERTICAL);
        sep.setPrefHeight(51);
        VBox stat2 = new VBox();
        Label stat2Val = new Label("0");
        stat2Val.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");
        Label stat2Lbl = new Label("Tiền mua hàng");
        stat2Lbl.setStyle("-fx-text-fill: #90A4AE; -fx-font-size: 11px;");
        stat2.getChildren().addAll(stat2Val, stat2Lbl);
        stats.getChildren().addAll(stat1, sep, stat2);
        info.getChildren().add(stats);
        // Action Buttons
        HBox actions = new HBox(8);
        actions.setAlignment(javafx.geometry.Pos.CENTER);
        Button btnDetail = new Button("Xem chi tiết");
        btnDetail.setStyle("-fx-background-color: #E3F2FD; -fx-text-fill: #2196F3; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 20;");
        Button btnEdit = new Button("Sửa");
        btnEdit.setStyle("-fx-background-color: #FFF3E0; -fx-text-fill: #FF9800; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 20;");
        btnEdit.setOnAction(e -> showEditCustomerDialog(c));
        Button btnDelete = new Button("Xóa");
        btnDelete.setStyle("-fx-background-color: #FFEBEE; -fx-text-fill: #F44336; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 20;");
        btnDelete.setOnAction(e -> handleDeleteCustomer(c));
        actions.getChildren().addAll(btnDetail, btnEdit, btnDelete);
        info.getChildren().add(actions);
        card.getChildren().addAll(info);
        return card;
    }

    private void handleDeleteCustomer(Customer customer) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText("Bạn có chắc muốn xóa khách hàng này?");
        alert.setContentText(customer.getName());
        if (alert.showAndWait().get() == ButtonType.OK) {
            Customer.deleteCustomer(customer.getCustomerId());
            loadCustomers();
        }
    }

    @FXML
    private void onAddCustomer(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/da/view/CustomerFormDialog.fxml"));
            DialogPane dialogPane = loader.load();
            CustomerFormDialogController controller = loader.getController();
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Thêm khách hàng mới");
            dialog.initOwner(employeeGrid.getScene().getWindow());
            dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialog.setResizable(false);
            dialog.setResultConverter(button -> button);
            ButtonType result = dialog.showAndWait().orElse(ButtonType.CANCEL);
            if (result.getText().equals("Lưu")) {
                Customer newCustomer = controller.getCustomerResult();
                System.out.println("[DEBUG] Customer add: id=" + newCustomer.getCustomerId() + ", name=" + newCustomer.getName());
                if (Customer.addCustomer(newCustomer)) {
                    System.out.println("[DEBUG] Thêm khách hàng thành công!");
                    loadCustomers();
                } else {
                    System.out.println("[DEBUG] Thêm khách hàng thất bại!");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void onEditCustomer(ActionEvent event) {
        // TODO: Hiển thị dialog sửa thông tin khách hàng
    }

    @FXML
    private void onDeleteCustomer(ActionEvent event) {
        // TODO: Xóa khách hàng được chọn
    }

    @FXML
    private void onImportExcel(ActionEvent event) {
        // TODO: Nhập khách hàng từ file Excel
    }

    @FXML
    private void onExportExcel(ActionEvent event) {
        // TODO: Xuất danh sách khách hàng ra file Excel
    }

    @FXML
    private void onClearFilters(ActionEvent event) {
        // TODO: Xóa các bộ lọc tìm kiếm
    }

    @FXML
    private void onSearch(ActionEvent event) {
        // TODO: Tìm kiếm khách hàng theo tên, số điện thoại, ...
    }

    private void showEditCustomerDialog(Customer customer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/da/view/CustomerFormDialog.fxml"));
            DialogPane dialogPane = loader.load();
            com.example.da.dialog.CustomerFormDialogController controller = loader.getController();
            controller.setCustomer(customer); // Đổ dữ liệu khách hàng vào form

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Sửa thông tin khách hàng");
            dialog.initOwner(employeeGrid.getScene().getWindow());
            dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialog.setResizable(false);
            dialog.setResultConverter(button -> button);

            ButtonType result = dialog.showAndWait().orElse(ButtonType.CANCEL);
            if (result.getText().equals("Lưu")) {
                Customer updatedCustomer = controller.getCustomerResult();
                System.out.println("[DEBUG] Customer update: id=" + updatedCustomer.getCustomerId() + ", name=" + updatedCustomer.getName());
                if (Customer.updateCustomer(updatedCustomer)) {
                    System.out.println("[DEBUG] Sửa khách hàng thành công!");
                    loadCustomers();
                } else {
                    System.out.println("[DEBUG] Sửa khách hàng thất bại!");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
} 