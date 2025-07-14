package com.example.da.dialog;

import com.example.da.model.PurchaseOrder;
import com.example.da.model.Supplier;
import com.example.da.model.Users;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class PurchaseOrderFormDialogController {
    @FXML private DialogPane dialogPane;
    @FXML private ComboBox<Supplier> cbSupplier;
    @FXML private ComboBox<Users> cbUser;
    @FXML private DatePicker dpOrderDate;
    @FXML private TextField txtNote;

    private PurchaseOrder purchaseOrder; // null = thêm mới, != null = sửa

    public void setPurchaseOrder(PurchaseOrder order) {
        this.purchaseOrder = order;
        if (order != null) {
            cbSupplier.getSelectionModel().select(findSupplierById(order.getSupplierId() != null ? order.getSupplierId().longValue() : null));
            cbUser.getSelectionModel().select(findUserById(order.getUserId()));
            if (order.getOrderDate() != null) dpOrderDate.setValue(order.getOrderDate().toLocalDate());
            txtNote.setText(order.getNote());
        }
    }

    @FXML
    private void initialize() {
        // Load danh sách nhà cung cấp
        List<Supplier> suppliers = Supplier.getAllSuppliers();
        cbSupplier.setItems(FXCollections.observableArrayList(suppliers));
        // Load danh sách user
        List<Users> users;
        try {
            users = Users.getAllEmployees();
        } catch (Exception e) {
            users = java.util.Collections.emptyList();
        }
        cbUser.setItems(FXCollections.observableArrayList(users));
        
        // Tìm nút Save và gắn event handler
        for (javafx.scene.Node node : dialogPane.lookupAll("Button")) {
            if (node instanceof javafx.scene.control.Button) {
                javafx.scene.control.Button button = (javafx.scene.control.Button) node;
                if ("Lưu".equals(button.getText())) {
                    button.setOnAction(this::handleSave);
                    break;
                }
            }
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        try {
            PurchaseOrder result = getResult();
            
            if (result == null) {
                return; // Validation failed, don't close dialog
            }
            
            // Insert vào database
            boolean success = result.insert();
            
            if (!success) {
                showAlert("Không thể lưu đơn hàng. Vui lòng thử lại!");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi: " + e.getMessage());
        }
    }



    // Phương thức này sẽ được gọi từ bên ngoài để setup event handler
    public void setupSaveButtonHandler() {
        // Đợi một chút để scene được tạo
        javafx.application.Platform.runLater(() -> {
            try {
                // Tìm nút Save trong dialog pane
                for (javafx.scene.Node node : dialogPane.lookupAll("Button")) {
                    if (node instanceof javafx.scene.control.Button) {
                        javafx.scene.control.Button button = (javafx.scene.control.Button) node;
                        if ("Lưu".equals(button.getText())) {
                            button.setOnAction(this::handleSave);
                            return;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public PurchaseOrder getResult() {
        // Validate
        if (cbSupplier.getValue() == null) {
            showAlert("Vui lòng chọn nhà cung cấp!");
            return null;
        }
        if (cbUser.getValue() == null) {
            showAlert("Vui lòng chọn người nhập!");
            return null;
        }
        if (dpOrderDate.getValue() == null) {
            showAlert("Vui lòng chọn ngày nhập!");
            return null;
        }
        // Lấy dữ liệu
        Long supplierIdLong = cbSupplier.getValue() != null ? cbSupplier.getValue().getSupplierId() : null;
        Integer supplierId = supplierIdLong != null ? supplierIdLong.intValue() : null;
        Integer userId = cbUser.getValue() != null ? cbUser.getValue().getUser_id() : null;
        LocalDate date = dpOrderDate.getValue();
        LocalDateTime orderDate = (date != null) ? LocalDateTime.of(date, LocalTime.now()) : null;
        String note = txtNote.getText();
        
        if (purchaseOrder == null) {
            return new PurchaseOrder(0, supplierId, userId, orderDate, note);
        } else {
            purchaseOrder.setSupplierId(supplierId);
            purchaseOrder.setUserId(userId);
            purchaseOrder.setOrderDate(orderDate);
            purchaseOrder.setNote(note);
            return purchaseOrder;
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        try {
            if (dialogPane.getScene() != null && dialogPane.getScene().getWindow() != null) {
                alert.initOwner(dialogPane.getScene().getWindow());
            }
        } catch (Exception e) {
            // Ignore
        }
        alert.showAndWait();
    }

    private Supplier findSupplierById(Long id) {
        if (id == null) return null;
        for (Supplier s : cbSupplier.getItems()) {
            if (s.getSupplierId().equals(id)) return s;
        }
        return null;
    }
    private Users findUserById(Integer id) {
        if (id == null) return null;
        for (Users u : cbUser.getItems()) {
            if (u.getUser_id() == id) return u;
        }
        return null;
    }
} 