package com.example.da.controller;

import com.example.da.dialog.EmployeeFormDialogController;
import com.example.da.model.Users;
import javafx.fxml.FXML;

import javafx.fxml.Initializable;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.collections.FXCollections;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

import javafx.geometry.Pos;
import javafx.geometry.Insets;

import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;


import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.ArrayList;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ButtonType;

public class EmployeeManagementController implements Initializable {
    
    @FXML private Label lblTotalEmployees;
    @FXML private Label lblActiveEmployees;
    @FXML private Label lblLockedAccounts;
    @FXML private Label lblEmployeeCount;
    
    @FXML private Button btnImportExcel;
    @FXML private Button btnExportExcel;
    @FXML private Button btnAddEmployee;
    @FXML private Button btnPrintCard;
    @FXML private Button btnClearFilters;
    
    @FXML private TextField txtSearch;
    
    @FXML private ComboBox<String> cbDepartment;
    @FXML private ComboBox<String> cbPosition;
    @FXML private ComboBox<String> cbStatus;
    @FXML private ComboBox<String> cbPageSize;
    
    @FXML private CheckBox cbSelectAll;
    
    @FXML private ToggleButton btnCardView;
    @FXML private ToggleButton btnTableView;
    @FXML private ToggleGroup viewToggle;
    
    @FXML private FlowPane employeeGrid;
    
    @FXML private Button btnFirstPage;
    @FXML private Button btnPreviousPage;
    @FXML private Button btnNextPage;
    @FXML private Button btnLastPage;
    @FXML private HBox pageButtonsContainer;
    
    private List<Users> allEmployees;
    private List<Users> filteredEmployees;
    private int currentPage = 1;
    private int pageSize = 10;
    private int totalPages = 1;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupUI();
        loadEmployees();
        setupEventHandlers();
        updateStatistics();
    }
    
    private void setupUI() {
        // Setup ComboBoxes
        cbPosition.setItems(FXCollections.observableArrayList("Tất cả chức vụ", "Nhân viên", "Quản lý"));
        cbStatus.setItems(FXCollections.observableArrayList("Tất cả trạng thái", "Đang hoạt động", "Đã khóa"));
        cbPageSize.setItems(FXCollections.observableArrayList("10", "20", "40", "60"));
        cbPageSize.setValue("20");
        
        // Setup search field
        txtSearch.setPromptText("Tìm kiếm theo tên, mã nhân viên, số điện thoại...");
        
        // Setup view toggle
        btnCardView.setSelected(true);
    }
    
    private void setupEventHandlers() {
        // Search functionality
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterEmployees();
        });
        
        // Filter ComboBoxes
        cbPosition.setOnAction(e -> filterEmployees());
        cbStatus.setOnAction(e -> filterEmployees());
        
        // Page size change
        cbPageSize.setOnAction(e -> {
            pageSize = Integer.parseInt(cbPageSize.getValue());
            currentPage = 1;
            displayEmployees();
        });
        
        // Pagination buttons
        btnFirstPage.setOnAction(e -> goToPage(1));
        btnPreviousPage.setOnAction(e -> goToPage(currentPage - 1));
        btnNextPage.setOnAction(e -> goToPage(currentPage + 1));
        btnLastPage.setOnAction(e -> goToPage(totalPages));
        
        // Action buttons
        btnAddEmployee.setOnAction(e -> showAddEmployeeDialog());
        btnClearFilters.setOnAction(e -> clearFilters());
        btnImportExcel.setOnAction(e -> importFromExcel());
        btnExportExcel.setOnAction(e -> exportToExcel());
        btnPrintCard.setOnAction(e -> printEmployeeCards());
        
        // Select all checkbox
        cbSelectAll.setOnAction(e -> selectAllEmployees());
        
        // View toggle
        btnCardView.setOnAction(e -> switchToCardView());
        btnTableView.setOnAction(e -> switchToTableView());
    }
    
    private void loadEmployees() {
        try {
            allEmployees = Users.getAllEmployees();
            filteredEmployees = new ArrayList<>(allEmployees);
            calculatePagination();
            displayEmployees();
        } catch (SQLException | ClassNotFoundException e) {
            showAlert(AlertType.ERROR, "Lỗi tải dữ liệu: " + e.getMessage());
        }
    }
    
    private void filterEmployees() {
        String searchTerm = txtSearch.getText().trim();
        String positionFilter = cbPosition.getValue();
        String statusFilter = cbStatus.getValue();
        
        filteredEmployees.clear();
        
        try {
            List<Users> searchResults;
            
            if (!searchTerm.isEmpty()) {
                searchResults = Users.searchEmployees(searchTerm);
            } else {
                searchResults = new ArrayList<>(allEmployees);
            }
            
            // Apply position filter
            if (positionFilter != null && !positionFilter.equals("Tất cả chức vụ")) {
                Users.Role role = positionFilter.equals("Quản lý") ? Users.Role.quanly : Users.Role.nhanvien;
                searchResults = Users.filterByRole(role);
            }
            
            // Apply status filter
            if (statusFilter != null && !statusFilter.equals("Tất cả trạng thái")) {
                Users.Status status = statusFilter.equals("Đang hoạt động") ? Users.Status.active : Users.Status.inactive;
                searchResults = Users.filterByStatus(status);
            }
            
            filteredEmployees.addAll(searchResults);
            currentPage = 1;
            calculatePagination();
            displayEmployees();
            
        } catch (SQLException | ClassNotFoundException e) {
            showAlert(AlertType.ERROR, "Lỗi lọc dữ liệu: " + e.getMessage());
        }
    }
    
    private void clearFilters() {
        txtSearch.clear();
        cbPosition.setValue("Tất cả chức vụ");
        cbStatus.setValue("Tất cả trạng thái");
        filteredEmployees = new ArrayList<>(allEmployees);
        currentPage = 1;
        calculatePagination();
        displayEmployees();
    }
    
    private void calculatePagination() {
        totalPages = (int) Math.ceil((double) filteredEmployees.size() / pageSize);
        if (totalPages == 0) totalPages = 1;
        if (currentPage > totalPages) currentPage = totalPages;
        if (currentPage < 1) currentPage = 1;
    }
    
    private void displayEmployees() {
        employeeGrid.getChildren().clear();
        
        int startIndex = (currentPage - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, filteredEmployees.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            Users employee = filteredEmployees.get(i);
            Node employeeCard = createEmployeeCard(employee);
            employeeGrid.getChildren().add(employeeCard);
        }
        
        updatePaginationUI();
        updateEmployeeCount();
    }
    
    private Node createEmployeeCard(Users employee) {
        VBox card = new VBox(0);
        card.setPrefWidth(380);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3); -fx-cursor: hand;");
        
        // Header with status
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15));
        
        String headerColor = employee.getStatus() == Users.Status.active ? 
            "linear-gradient(to right, #4CAF50, #388E3C)" : 
            "linear-gradient(to right, #FF9800, #F57C00)";
        header.setStyle("-fx-background-color: " + headerColor + "; -fx-background-radius: 15 15 0 0;");
        
        CheckBox selectCheckBox = new CheckBox();
        selectCheckBox.setStyle("-fx-text-fill: white;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        String statusText = employee.getStatus() == Users.Status.active ? "Đang làm việc" : "Đã khóa";
        Label statusLabel = new Label(statusText);
        statusLabel.setStyle("-fx-background-color: rgba(255,255,255,0.3); -fx-text-fill: white; -fx-padding: 5 12; -fx-background-radius: 15; -fx-font-size: 11px; -fx-font-weight: bold;");
        
        header.getChildren().addAll(selectCheckBox, spacer, statusLabel);
        
        // Employee info section
        VBox infoSection = new VBox(15);
        infoSection.setPadding(new Insets(20));
        
        // Profile section
        HBox profileSection = new HBox(15);
        profileSection.setAlignment(Pos.CENTER_LEFT);
        
        // Avatar
        StackPane avatarPane = new StackPane();
        Circle avatarBg = new Circle(40);
        avatarBg.setFill(javafx.scene.paint.Color.valueOf("#E3F2FD"));
        
        // Tạo avatar với chữ cái đầu của tên nhân viên
        String initials = "";
        if (employee.getFull_name() != null && !employee.getFull_name().trim().isEmpty()) {
            String[] nameParts = employee.getFull_name().trim().split("\\s+");
            if (nameParts.length >= 2) {
                // Lấy chữ cái đầu của từ đầu và từ cuối
                initials = nameParts[0].substring(0, 1).toUpperCase() + 
                          nameParts[nameParts.length - 1].substring(0, 1).toUpperCase();
            } else {
                // Lấy 2 chữ cái đầu nếu chỉ có 1 từ
                initials = employee.getFull_name().substring(0, Math.min(2, employee.getFull_name().length())).toUpperCase();
            }
        } else {
            initials = "NV"; // Mặc định
        }
        
        // Tạo text với chữ cái đầu
        Text avatarText = new Text(initials);
        avatarText.setFill(Color.valueOf("#2196F3"));
        avatarText.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        // Status indicator (chấm tròn hiển thị trạng thái)
        Circle statusIndicator = new Circle(8);
        statusIndicator.setFill(Color.valueOf(employee.getStatus() == Users.Status.active ? "#4CAF50" : "#9E9E9E"));
        statusIndicator.setStroke(Color.WHITE);
        statusIndicator.setStrokeWidth(2);
        statusIndicator.setTranslateX(-5);
        statusIndicator.setTranslateY(-5);
        StackPane.setAlignment(statusIndicator, Pos.BOTTOM_RIGHT);
        
        avatarPane.getChildren().addAll(avatarBg, avatarText, statusIndicator);
        
        // Basic info
        VBox basicInfo = new VBox(5);
        HBox.setHgrow(basicInfo, javafx.scene.layout.Priority.ALWAYS);
        
        Label nameLabel = new Label(employee.getFull_name());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #263238;");
        
        Label idLabel = new Label("NV" + String.format("%03d", employee.getUser_id()));
        idLabel.setStyle("-fx-text-fill: #90A4AE; -fx-font-size: 13px;");
        
        HBox tagsBox = new HBox(10);
        Label roleTag = new Label(employee.getRole() == Users.Role.quanly ? "Quản lý" : "Nhân viên");
        roleTag.setStyle("-fx-background-color: #E3F2FD; -fx-text-fill: #2196F3; -fx-padding: 3 8; -fx-background-radius: 10; -fx-font-size: 12px;");
        
        Label shiftTag = new Label("Toàn thời gian");
        shiftTag.setStyle("-fx-background-color: #F3E5F5; -fx-text-fill: #9C27B0; -fx-padding: 3 8; -fx-background-radius: 10; -fx-font-size: 12px;");
        
        tagsBox.getChildren().addAll(roleTag, shiftTag);
        basicInfo.getChildren().addAll(nameLabel, idLabel, tagsBox);
        
        profileSection.getChildren().addAll(avatarPane, basicInfo);
        
        // Separator
        Separator separator1 = new Separator();
        
        // Contact info
        VBox contactInfo = new VBox(8);
        
        // Phone
        HBox phoneBox = new HBox(10);
        phoneBox.setAlignment(Pos.CENTER_LEFT);
        ImageView phoneIcon = new ImageView();
        phoneIcon.setFitHeight(16);
        phoneIcon.setFitWidth(16);
        try {
            phoneIcon.setImage(new Image(getClass().getResourceAsStream("/com/example/da/icons/phone.png")));
        } catch (Exception e) {}
        
        Label phoneLabel = new Label(employee.getPhone());
        phoneLabel.setStyle("-fx-text-fill: #607D8B; -fx-font-size: 13px;");
        phoneBox.getChildren().addAll(phoneIcon, phoneLabel);
        
        // Email
        HBox emailBox = new HBox(10);
        emailBox.setAlignment(Pos.CENTER_LEFT);
        ImageView emailIcon = new ImageView();
        emailIcon.setFitHeight(16);
        emailIcon.setFitWidth(16);
        try {
            emailIcon.setImage(new Image(getClass().getResourceAsStream("/com/example/da/icons/email.png")));
        } catch (Exception e) {}
        
        Label emailLabel = new Label(employee.getEmail());
        emailLabel.setStyle("-fx-text-fill: #607D8B; -fx-font-size: 13px;");
        emailBox.getChildren().addAll(emailIcon, emailLabel);
        
        contactInfo.getChildren().addAll(phoneBox, emailBox);
        
        // Separator
        Separator separator2 = new Separator();
        
        // Performance stats
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER);
        
        VBox ordersBox = new VBox();
        ordersBox.setAlignment(Pos.CENTER);
        Label ordersLabel = new Label("128");
        ordersLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");
        Label ordersText = new Label("Đơn hôm nay");
        ordersText.setStyle("-fx-text-fill: #90A4AE; -fx-font-size: 11px;");
        ordersBox.getChildren().addAll(ordersLabel, ordersText);
        
        Separator vSeparator1 = new Separator();
        vSeparator1.setOrientation(javafx.geometry.Orientation.VERTICAL);
        vSeparator1.setPrefHeight(40);
        
        VBox ratingBox = new VBox();
        ratingBox.setAlignment(Pos.CENTER);
        Label ratingLabel = new Label("4.8");
        ratingLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #FF9800;");
        Label ratingText = new Label("Đánh giá");
        ratingText.setStyle("-fx-text-fill: #90A4AE; -fx-font-size: 11px;");
        ratingBox.getChildren().addAll(ratingLabel, ratingText);
        
        Separator vSeparator2 = new Separator();
        vSeparator2.setOrientation(javafx.geometry.Orientation.VERTICAL);
        vSeparator2.setPrefHeight(40);
        
        VBox attendanceBox = new VBox();
        attendanceBox.setAlignment(Pos.CENTER);
        Label attendanceLabel = new Label("98%");
        attendanceLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");
        Label attendanceText = new Label("Chuyên cần");
        attendanceText.setStyle("-fx-text-fill: #90A4AE; -fx-font-size: 11px;");
        attendanceBox.getChildren().addAll(attendanceLabel, attendanceText);
        
        statsBox.getChildren().addAll(ordersBox, vSeparator1, ratingBox, vSeparator2, attendanceBox);
        
        // Action buttons
        HBox actionBox = new HBox(8);
        actionBox.setAlignment(Pos.CENTER);
        
        Button detailBtn = new Button("Xem chi tiết");
        detailBtn.setStyle("-fx-background-color: #E3F2FD; -fx-text-fill: #2196F3; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 20;");
        HBox.setHgrow(detailBtn, javafx.scene.layout.Priority.ALWAYS);
        detailBtn.setOnAction(e -> showEmployeeDetails(employee));
        
        Button editBtn = new Button("Sửa");
        editBtn.setStyle("-fx-background-color: #FFF3E0; -fx-text-fill: #FF9800; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 20;");
        editBtn.setOnAction(e -> editEmployee(employee));
        
        Button lockBtn = new Button(employee.getStatus() == Users.Status.active ? "Khóa" : "Mở khóa");
        lockBtn.setStyle("-fx-background-color: #FFEBEE; -fx-text-fill: #F44336; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 20;");
        lockBtn.setOnAction(e -> toggleEmployeeStatus(employee));
        
        Button deleteBtn = new Button("Xóa");
        deleteBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 20;");
        deleteBtn.setOnAction(e -> deleteEmployee(employee));
        
        actionBox.getChildren().addAll(detailBtn, editBtn, lockBtn, deleteBtn);
        
        infoSection.getChildren().addAll(profileSection, separator1, contactInfo, separator2, statsBox, actionBox);
        
        card.getChildren().addAll(header, infoSection);
        
        return card;
    }
    
    private void updatePaginationUI() {
        // Update pagination buttons
        btnFirstPage.setDisable(currentPage == 1);
        btnPreviousPage.setDisable(currentPage == 1);
        btnNextPage.setDisable(currentPage == totalPages);
        btnLastPage.setDisable(currentPage == totalPages);
        
        // Update page number buttons
        pageButtonsContainer.getChildren().clear();
        
        int startPage = Math.max(1, currentPage - 2);
        int endPage = Math.min(totalPages, currentPage + 2);
        
        for (int i = startPage; i <= endPage; i++) {
            Button pageBtn = new Button(String.valueOf(i));
            final int pageNum = i;
            
            if (i == currentPage) {
                pageBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
            } else {
                pageBtn.setStyle("-fx-background-color: white; -fx-border-color: #E0E0E0; -fx-background-radius: 5; -fx-cursor: hand;");
            }
            
            pageBtn.setOnAction(e -> goToPage(pageNum));
            pageButtonsContainer.getChildren().add(pageBtn);
        }
    }
    
    private void updateEmployeeCount() {
        int total = filteredEmployees.size();
        int displayed = Math.min(pageSize, total - (currentPage - 1) * pageSize);
        lblEmployeeCount.setText(String.format("Hiển thị %d trong tổng số %d nhân viên", displayed, total));
    }
    
    private void updateStatistics() {
        try {
            int total = Users.getTotalEmployeeCount();
            int active = Users.getActiveEmployeeCount();
            int locked = Users.getLockedAccountCount();
            
            lblTotalEmployees.setText(String.valueOf(total));
            lblActiveEmployees.setText(String.valueOf(active));
            lblLockedAccounts.setText(String.valueOf(locked));

            
        } catch (SQLException | ClassNotFoundException e) {
            showAlert(AlertType.ERROR, "Lỗi cập nhật thống kê: " + e.getMessage());
        }
    }
    
    private void goToPage(int page) {
        if (page >= 1 && page <= totalPages) {
            currentPage = page;
            displayEmployees();
        }
    }
    
    private void selectAllEmployees() {
        // Implementation for select all functionality
        boolean selected = cbSelectAll.isSelected();
        // Update all employee card checkboxes
    }
    
    private void switchToCardView() {
        // Already in card view
        btnCardView.setSelected(true);
        btnTableView.setSelected(false);
    }
    
    private void switchToTableView() {
        // Implementation for table view
        btnCardView.setSelected(false);
        btnTableView.setSelected(true);
        // Switch to table view implementation
    }
    
    private void showAddEmployeeDialog() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/da/view/EmployeeFormDialog.fxml"));
            DialogPane dialogPane = loader.load();
            EmployeeFormDialogController controller = loader.getController();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Thêm nhân viên");

          
            Button saveButton = (Button) dialogPane.lookupButton(
                dialog.getDialogPane().getButtonTypes().stream().filter(bt -> bt.getButtonData() == javafx.scene.control.ButtonBar.ButtonData.OK_DONE).findFirst().orElse(ButtonType.OK)
            );
            if (saveButton != null) {
                saveButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-border-radius: 12; -fx-background-radius: 12; -fx-cursor: hand; -fx-pref-width: 120; -fx-pref-height: 40;");

                saveButton.setText("LƯU");
            }
            Button cancelButton = (Button) dialogPane.lookupButton(
                dialog.getDialogPane().getButtonTypes().stream().filter(bt -> bt.getButtonData() == javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE).findFirst().orElse(ButtonType.CANCEL)
            );
            if (cancelButton != null) {
                cancelButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-border-radius: 12; -fx-background-radius: 12; -fx-cursor: hand; -fx-pref-width: 120; -fx-pref-height: 40;");
                cancelButton.setText("HỦY");
            }
            // Căn giữa và tăng spacing cho 2 nút
            dialogPane.lookupAll(".button-bar").forEach(node -> {
                if (node instanceof javafx.scene.control.ButtonBar) {
                    ((javafx.scene.control.ButtonBar) node).setStyle("-fx-alignment: center; -fx-spacing: 40px;");
                }
            });

            dialog.showAndWait().ifPresent(result -> {
                System.out.println("Dialog result (add): " + result);
                if (result.getButtonData() == javafx.scene.control.ButtonBar.ButtonData.OK_DONE) {
                    Users newUser = controller.getEmployeeFromForm();
                    System.out.println("[ADD] user_id=" + newUser.getUser_id() + ", username=" + newUser.getUsername() + ", full_name=" + newUser.getFull_name() + ", email=" + newUser.getEmail() + ", phone=" + newUser.getPhone() + ", role=" + newUser.getRole() + ", status=" + newUser.getStatus());
                    try {
                        if (Users.addEmployee(newUser)) {
                            showAlert(Alert.AlertType.INFORMATION, "Thêm nhân viên thành công!");
                            loadEmployees();
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Thêm nhân viên thất bại!");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showAlert(Alert.AlertType.ERROR, "Lỗi: " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi: " + e.getMessage());
        }
    }
    
    private void editEmployee(Users employee) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/da/view/EmployeeFormDialog.fxml"));
            DialogPane dialogPane = loader.load();
            EmployeeFormDialogController controller = loader.getController();
            controller.setEmployee(employee);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Chỉnh sửa nhân viên");

            // Style nút Lưu và Hủy
            Button saveButton = (Button) dialogPane.lookupButton(
                dialog.getDialogPane().getButtonTypes().stream().filter(bt -> bt.getButtonData() == javafx.scene.control.ButtonBar.ButtonData.OK_DONE).findFirst().orElse(ButtonType.OK)
            );
            if (saveButton != null) {
                saveButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-border-radius: 12; -fx-background-radius: 12; -fx-cursor: hand; -fx-pref-width: 120; -fx-pref-height: 40;");
                saveButton.setText("\uD83D\uDCBE LƯU");
            }
            Button cancelButton = (Button) dialogPane.lookupButton(
                dialog.getDialogPane().getButtonTypes().stream().filter(bt -> bt.getButtonData() == javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE).findFirst().orElse(ButtonType.CANCEL)
            );
            if (cancelButton != null) {
                cancelButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-border-radius: 12; -fx-background-radius: 12; -fx-cursor: hand; -fx-pref-width: 120; -fx-pref-height: 40;");
                cancelButton.setText("\u2716 HỦY");
            }

            dialog.showAndWait().ifPresent(result -> {
                System.out.println("Dialog result (edit): " + result);
                if (result.getButtonData() == javafx.scene.control.ButtonBar.ButtonData.OK_DONE) {
                    Users updatedUser = controller.getEmployeeFromForm();
                    System.out.println("[EDIT] user_id=" + updatedUser.getUser_id() + ", username=" + updatedUser.getUsername() + ", full_name=" + updatedUser.getFull_name() + ", email=" + updatedUser.getEmail() + ", phone=" + updatedUser.getPhone() + ", role=" + updatedUser.getRole() + ", status=" + updatedUser.getStatus());
                    try {
                        if (Users.updateEmployee(updatedUser)) {
                            showAlert(Alert.AlertType.INFORMATION, "Cập nhật nhân viên thành công!");
                            loadEmployees();
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Cập nhật nhân viên thất bại!");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showAlert(Alert.AlertType.ERROR, "Lỗi: " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi: " + e.getMessage());
        }
    }
    
    private void showEmployeeDetails(Users employee) {
        // Implementation for show employee details
        showAlert(AlertType.INFORMATION, "Chi tiết nhân viên: " + employee.getFull_name());
    }
    
    private void toggleEmployeeStatus(Users employee) {
        try {
            Users.Status newStatus = employee.getStatus() == Users.Status.active ? 
                Users.Status.inactive : Users.Status.active;
            
            if (Users.toggleAccountStatus(employee.getUser_id(), newStatus)) {
                employee.setStatus(newStatus);
                loadEmployees(); // Reload to update UI
                updateStatistics();
                showAlert(AlertType.INFORMATION, 
                    "Đã " + (newStatus == Users.Status.active ? "mở khóa" : "khóa") + " tài khoản của " + employee.getFull_name());
            } else {
                showAlert(AlertType.ERROR, "Không thể cập nhật trạng thái tài khoản");
            }
        } catch (SQLException | ClassNotFoundException e) {
            showAlert(AlertType.ERROR, "Lỗi cập nhật trạng thái: " + e.getMessage());
        }
    }
    
    private void importFromExcel() {
        showAlert(AlertType.INFORMATION, "Chức năng nhập Excel sẽ được triển khai");
    }
    
    private void exportToExcel() {
        showAlert(AlertType.INFORMATION, "Chức năng xuất Excel sẽ được triển khai");
    }
    
    private void printEmployeeCards() {
        showAlert(AlertType.INFORMATION, "Chức năng in thẻ nhân viên sẽ được triển khai");
    }
    
    private void deleteEmployee(Users employee) {
        System.out.println("Delete user_id: " + employee.getUser_id());
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc chắn muốn xóa nhân viên: " + employee.getFull_name() + "?");
        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    if (Users.deleteEmployee(employee.getUser_id())) {
                        showAlert(Alert.AlertType.INFORMATION, "Xóa nhân viên thành công!");
                        loadEmployees();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Xóa nhân viên thất bại!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Lỗi: " + e.getMessage());
                }
            }
        });
    }
    
    private void showAlert(AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 