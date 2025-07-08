package com.example.da.controller;

import com.example.da.model.Category;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TreeItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryManagementController {
    @FXML private TreeView<String> categoryTree;
    @FXML private TextField txtCategoryName, txtDisplayOrder;
    @FXML private TextArea txtDescription;
    @FXML private RadioButton rbActive, rbInactive;
    @FXML private ToggleGroup statusGroup;
    @FXML private Button btnAddCategory, btnEditCategory, btnDeleteCategory;

    private Map<String, Category> nameToCategory = new HashMap<>();
    private Category selectedCategory = null;

    @FXML
    public void initialize() {
        loadCategoriesToTree();
        categoryTree.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> onCategorySelected(newVal));
        btnAddCategory.setOnAction(e -> handleAddCategory());
        btnEditCategory.setOnAction(e -> handleEditCategory());
        btnDeleteCategory.setOnAction(e -> handleDeleteCategory());
    }

    private void loadCategoriesToTree() {
        List<Category> categories = Category.getAll();
        nameToCategory.clear();
        TreeItem<String> root = new TreeItem<>("Tất cả danh mục");
        for (Category c : categories) {
            TreeItem<String> item = new TreeItem<>(c.getName());
            root.getChildren().add(item);
            nameToCategory.put(c.getName(), c);
        }
        root.setExpanded(true);
        categoryTree.setRoot(root);
    }

    private void onCategorySelected(TreeItem<String> selectedItem) {
        if (selectedItem == null || selectedItem.getValue().equals("Tất cả danh mục")) {
            clearForm();
            selectedCategory = null;
            return;
        }
        Category c = nameToCategory.get(selectedItem.getValue());
        if (c != null) {
            selectedCategory = c;
            txtCategoryName.setText(c.getName());
            txtDisplayOrder.setText(String.valueOf(c.getDisplayOrder()));
            txtDescription.setText(c.getDescription());
            if ("active".equals(c.getStatus())) rbActive.setSelected(true); else rbInactive.setSelected(true);
        }
    }

    private void clearForm() {
        txtCategoryName.clear();
        txtDisplayOrder.clear();
        txtDescription.clear();
        rbActive.setSelected(true);
        categoryTree.getSelectionModel().clearSelection();
        selectedCategory = null;
    }

    private void handleAddCategory() {
        Category c = new Category();
        String name = txtCategoryName.getText().trim();
        if (name.isEmpty()) {
            showAlert("Vui lòng nhập tên danh mục!");
            return;
        }
        c.setName(name);
        try { c.setDisplayOrder(Integer.parseInt(txtDisplayOrder.getText())); } catch (Exception ex) { c.setDisplayOrder(0); }
        c.setDescription(txtDescription.getText());
        c.setStatus(rbActive.isSelected() ? "active" : "inactive");
        if (c.insert()) {
            loadCategoriesToTree();
            clearForm();
            showInfo("Thêm danh mục thành công!");
        } else {
            showAlert("Không thể thêm danh mục. Có thể tên đã bị trùng!");
        }
    }

    private void handleEditCategory() {
        if (selectedCategory == null) {
            showAlert("Vui lòng chọn danh mục để sửa!");
            return;
        }
        String name = txtCategoryName.getText().trim();
        if (name.isEmpty()) {
            showAlert("Vui lòng nhập tên danh mục!");
            return;
        }
        selectedCategory.setName(name);
        try { selectedCategory.setDisplayOrder(Integer.parseInt(txtDisplayOrder.getText())); } catch (Exception ex) { selectedCategory.setDisplayOrder(0); }
        selectedCategory.setDescription(txtDescription.getText());
        selectedCategory.setStatus(rbActive.isSelected() ? "active" : "inactive");
        if (selectedCategory.update()) {
            loadCategoriesToTree();
            showInfo("Cập nhật danh mục thành công!");
        } else {
            showAlert("Không thể cập nhật danh mục. Có thể tên đã bị trùng!");
        }
    }

    private void handleDeleteCategory() {

        if (selectedCategory == null) {
            showAlert("Vui lòng chọn danh mục để xóa!");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc chắn muốn xóa danh mục này?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText(null);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            if (selectedCategory.delete()) {
                loadCategoriesToTree();
                clearForm();
                showInfo("Xóa danh mục thành công!");
            } else {
                showAlert("Không thể xóa danh mục!");
            }
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thành công");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 