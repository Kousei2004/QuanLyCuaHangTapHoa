package com.example.da.controller;

import com.example.da.model.Supplier;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import com.example.da.dialog.SupplierFormDialogController;

import java.util.List;

public class SupplierManagementController {
    @FXML
    private TableView<Supplier> supplierTable;
    @FXML
    private TableColumn<Supplier, Number> colSelect;
    @FXML
    private TableColumn<Supplier, String> colSupplierCode;
    @FXML
    private TableColumn<Supplier, String> colSupplierName;
    @FXML
    private TableColumn<Supplier, String> colPhone;
    @FXML
    private TableColumn<Supplier, String> colEmail;
    @FXML
    private TableColumn<Supplier, String> colAddress;
    @FXML
    private TableColumn<Supplier, String> colStatus;
    @FXML
    private Button btnAddSupplier;
    @FXML
    private Button btnEditSupplier;
    @FXML
    private Button btnDeleteSupplier;
    @FXML
    private TextField txtSearch;
    @FXML
    private Label lblSupplierCount;
    @FXML
    private ComboBox<String> cbPageSize;
    @FXML
    private Label lblTotalSuppliers;
    @FXML
    private Label lblActiveSuppliers;
    @FXML
    private Label lblInactiveSuppliers;

    private ObservableList<Supplier> supplierList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        supplierTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        // STT
        colSelect.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(supplierTable.getItems().indexOf(cellData.getValue()) + 1));
        colSupplierCode.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty("NCC" + String.format("%04d", cellData.getValue().getSupplierId())));
        colSupplierName.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        colPhone.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPhone()));
        colEmail.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEmail()));
        colAddress.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getAddress()));
        colStatus.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus()));

        btnAddSupplier.setOnAction(this::onAddSupplier);
        btnEditSupplier.setOnAction(this::onEditSupplier);
        btnDeleteSupplier.setOnAction(this::onDeleteSupplier);
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> filterSuppliers());

        loadSuppliers();
    }

    private void loadSuppliers() {
        List<Supplier> suppliers = Supplier.getAllSuppliers();
        supplierList.setAll(suppliers);
        supplierTable.setItems(supplierList);
        updateStats();
        filterSuppliers();
    }

    private void filterSuppliers() {
        String keyword = txtSearch.getText() != null ? txtSearch.getText().toLowerCase() : "";
        if (keyword.isEmpty()) {
            supplierTable.setItems(supplierList);
        } else {
            ObservableList<Supplier> filtered = supplierList.filtered(s ->
                s.getName().toLowerCase().contains(keyword)
                || (s.getSupplierId() != null && ("NCC" + String.format("%04d", s.getSupplierId())).toLowerCase().contains(keyword))
                || (s.getPhone() != null && s.getPhone().toLowerCase().contains(keyword))
            );
            supplierTable.setItems(filtered);
        }
        lblSupplierCount.setText("Hiển thị " + supplierTable.getItems().size() + " nhà cung cấp");
    }

    private void updateStats() {
        int total = supplierList.size();
        int active = (int) supplierList.stream().filter(s -> "Active".equalsIgnoreCase(s.getStatus())).count();
        int inactive = (int) supplierList.stream().filter(s -> "Inactive".equalsIgnoreCase(s.getStatus())).count();
        if (lblTotalSuppliers != null) lblTotalSuppliers.setText(String.valueOf(total));
        if (lblActiveSuppliers != null) lblActiveSuppliers.setText(String.valueOf(active));
        if (lblInactiveSuppliers != null) lblInactiveSuppliers.setText(String.valueOf(inactive));
    }

    @FXML
    private void onAddSupplier(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/da/view/SupplierFormDialog.fxml"));
            DialogPane dialogPane = loader.load();
            SupplierFormDialogController controller = loader.getController();
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Thêm nhà cung cấp mới");
            dialog.initOwner(supplierTable.getScene().getWindow());
            dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialog.setResizable(false);
            dialog.setResultConverter(button -> button);
            ButtonType result = dialog.showAndWait().orElse(ButtonType.CANCEL);
            if (result.getText().equals("Lưu")) {
                Supplier newSupplier = controller.getSupplierResult();
                if (Supplier.addSupplier(newSupplier)) {
                    loadSuppliers();
                } else {
                    // Có thể hiện thông báo lỗi ở đây
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void onEditSupplier(ActionEvent event) {
        Supplier selected = supplierTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/da/view/SupplierFormDialog.fxml"));
            DialogPane dialogPane = loader.load();
            SupplierFormDialogController controller = loader.getController();
            controller.setSupplier(selected);
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Sửa thông tin nhà cung cấp");
            dialog.initOwner(supplierTable.getScene().getWindow());
            dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialog.setResizable(false);
            dialog.setResultConverter(button -> button);
            ButtonType result = dialog.showAndWait().orElse(ButtonType.CANCEL);
            if (result.getText().equals("Lưu")) {
                Supplier updatedSupplier = controller.getSupplierResult();
                updatedSupplier.setSupplierId(selected.getSupplierId());
                if (Supplier.updateSupplier(updatedSupplier)) {
                    loadSuppliers();
                } else {
                    // Có thể hiện thông báo lỗi ở đây
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void onDeleteSupplier(ActionEvent event) {
        Supplier selected = supplierTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Xác nhận xóa");
            alert.setHeaderText("Bạn có chắc muốn xóa nhà cung cấp này?");
            alert.setContentText(selected.getName());
            if (alert.showAndWait().get() == ButtonType.OK) {
                Supplier.deleteSupplier(selected.getSupplierId());
                loadSuppliers();
            }
        }
    }
} 