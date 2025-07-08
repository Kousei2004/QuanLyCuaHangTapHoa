package com.example.da.dialog;

import com.example.da.model.Category;
import com.example.da.model.Product;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;

public class ProductFormDialogController {
    @FXML private TextField txtProductName;
    @FXML private TextField txtBarcode;
    @FXML private TextArea txtDescription;
    @FXML private TextField txtPrice;
    @FXML private TextField txtQuantityInStock;
    @FXML private ComboBox<String> cbStatus;
    @FXML private ComboBox<Category> cbCategory;
    @FXML private DialogPane dialogPane;

    private Product productResult;

    @FXML
    public void initialize() {
        cbStatus.setItems(FXCollections.observableArrayList("active", "inactive"));
        List<Category> categories = Category.getAll();
        cbCategory.setItems(FXCollections.observableArrayList(categories));
        cbCategory.setConverter(new javafx.util.StringConverter<Category>() {
            @Override
            public String toString(Category c) { return c == null ? "" : c.getName(); }
            @Override
            public Category fromString(String s) { return null; }
        });
    }

    public void setProduct(Product product) {
        if (product == null) return;
        txtProductName.setText(product.getName());
        txtBarcode.setText(product.getBarcode());
        txtDescription.setText(product.getDescription());
        txtPrice.setText(String.valueOf(product.getPrice()));
        txtQuantityInStock.setText(String.valueOf(product.getQuantityInStock()));
        cbStatus.setValue(product.getStatus());
        for (Category c : cbCategory.getItems()) {
            if (c.getCategoryId() == product.getCategoryId()) {
                cbCategory.setValue(c);
                break;
            }
        }
    }

    public Product getProductResult() { return productResult; }

    public Product getProductFromFields() {
        String name = txtProductName.getText().trim();
        String priceStr = txtPrice.getText().trim();
        String qtyStr = txtQuantityInStock.getText().trim();
        if (name.isEmpty() || priceStr.isEmpty() || qtyStr.isEmpty()) {
            return null;
        }
        double price;
        int qty;
        try { price = Double.parseDouble(priceStr); } catch (Exception e) { return null; }
        try { qty = Integer.parseInt(qtyStr); } catch (Exception e) { return null; }
        String status = cbStatus.getValue();
        Category cat = cbCategory.getValue();
        return new Product(
            0,
            name,
            txtBarcode.getText().trim(),
            txtDescription.getText().trim(),
            price,
            qty,
            status == null ? "active" : status,
            cat == null ? 0 : cat.getCategoryId()
        );
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.initOwner(dialogPane.getScene().getWindow());
        alert.showAndWait();
    }

    private void closeDialog() {
        Stage stage = (Stage) dialogPane.getScene().getWindow();
        stage.close();
    }
} 