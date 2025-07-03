package com.example.da.dialog;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import com.example.da.model.Customer;

public class CustomerFormDialogController {
    @FXML
    private TextField txtFullName;
    @FXML
    private TextField txtPhone;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtAddress;
    @FXML
    private DialogPane dialogPane;
    @FXML
    private ButtonType btnSave;
    @FXML
    private ButtonType btnCancel;

    private Customer customer;

    public void setCustomer(Customer customer) {
        this.customer = customer;
        if (customer != null) {
            txtFullName.setText(customer.getName());
            txtPhone.setText(customer.getPhone());
            txtEmail.setText(customer.getEmail());
            txtAddress.setText(customer.getAddress());
        }
    }

    public Customer getCustomerResult() {
        if (customer == null) customer = new Customer();
        customer.setCustomerId(this.customer != null ? this.customer.getCustomerId() : null);
        customer.setName(txtFullName.getText());
        customer.setPhone(txtPhone.getText());
        customer.setEmail(txtEmail.getText());
        customer.setAddress(txtAddress.getText());
        return customer;
    }

    @FXML
    private void initialize() {
        dialogPane.lookupButton(btnSave).addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (txtFullName.getText().trim().isEmpty()) {
                txtFullName.requestFocus();
                event.consume();
            }
        });
    }
} 