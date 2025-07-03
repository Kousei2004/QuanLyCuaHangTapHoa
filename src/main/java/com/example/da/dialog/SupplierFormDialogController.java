package com.example.da.dialog;

import com.example.da.model.Supplier;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.collections.FXCollections;

public class SupplierFormDialogController {
    @FXML
    private TextField txtName;
    @FXML
    private TextField txtPhone;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtAddress;
    @FXML
    private ComboBox<String> cbStatus;

    private Supplier supplierResult;

    public void setSupplier(Supplier supplier) {
        if (supplier != null) {
            txtName.setText(supplier.getName());
            txtPhone.setText(supplier.getPhone());
            txtEmail.setText(supplier.getEmail());
            txtAddress.setText(supplier.getAddress());
            cbStatus.setValue(supplier.getStatus());
            this.supplierResult = supplier;
        }
    }

    public Supplier getSupplierResult() {
        if (supplierResult == null) {
            supplierResult = new Supplier();
        }
        supplierResult.setName(txtName.getText());
        supplierResult.setPhone(txtPhone.getText());
        supplierResult.setEmail(txtEmail.getText());
        supplierResult.setAddress(txtAddress.getText());
        supplierResult.setStatus(cbStatus.getValue());
        return supplierResult;
    }

    @FXML
    public void initialize() {
        cbStatus.setItems(FXCollections.observableArrayList("Active", "Inactive"));
    }
} 