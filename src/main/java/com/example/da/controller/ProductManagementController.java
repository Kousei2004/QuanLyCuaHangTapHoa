package com.example.da.controller;

import com.example.da.model.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import com.example.da.dialog.ProductFormDialogController;

import java.util.List;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.util.Optional;

public class ProductManagementController {
    @FXML private FlowPane productGrid;
    @FXML private Label lblProductCount;
    @FXML private Button btnAddProduct;

    private ObservableList<Product> productList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Bind columns
        // colSelect.setCellValueFactory(cellData -> new SimpleIntegerProperty(productTable.getItems().indexOf(cellData.getValue()) + 1).asObject());
        // colProductCode.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getProductId())));
        // colBarcode.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBarcode()));
        // colProductName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        // colCategory.setCellValueFactory(cellData -> new SimpleStringProperty("")); // TODO: Hiển thị tên danh mục
        // colUnit.setCellValueFactory(cellData -> new SimpleStringProperty("")); // TODO: Thêm thuộc tính đơn vị nếu có
        // colPurchasePrice.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getPrice()).asObject());
        // colSellingPrice.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getPrice()).asObject()); // TODO: Nếu có giá bán riêng
        // colStock.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getQuantityInStock()).asObject());
        // colStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
        // Load data
        loadProducts();
        btnAddProduct.setOnAction(e -> handleAddProduct());
        // btnEditProduct.setOnAction(e -> handleEditProduct());
        // btnDeleteProduct.setOnAction(e -> handleDeleteProduct());
    }

    private void loadProducts() {
        List<Product> products = Product.getAll();
        productGrid.getChildren().clear();
        int count = products.size();
        lblProductCount.setText("Hiển thị " + count + " sản phẩm");
        for (Product p : products) {
            VBox card = createProductCard(p);
            productGrid.getChildren().add(card);
        }
    }

    private VBox createProductCard(Product p) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3); -fx-padding: 18; -fx-cursor: hand;");
        card.setPrefWidth(320);
        // Header
        HBox header = new HBox();
        Label name = new Label(p.getName());
        name.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #263238;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        Label status = new Label(p.getStatus().equals("active") ? "Đang Kinh Doanh" : "Ngừng Kinh Doanh");
        status.setStyle(p.getStatus().equals("active") ? "-fx-text-fill: #4CAF50; -fx-font-weight: bold;" : "-fx-text-fill: #9E9E9E; -fx-font-weight: bold;");
        header.getChildren().addAll(name, spacer, status);
        // Info
        Label barcode = new Label("Mã vạch: " + (p.getBarcode() == null ? "" : p.getBarcode()));
        barcode.setStyle("-fx-text-fill: #607D8B; -fx-font-size: 13px;");
        Label price = new Label("Giá: " + String.format("%,.0f", p.getPrice()));
        price.setStyle("-fx-text-fill: #2196F3; -fx-font-size: 15px; -fx-font-weight: bold;");
        Label stock = new Label("Tồn kho: " + p.getQuantityInStock());
        stock.setStyle("-fx-text-fill: #FF9800; -fx-font-size: 13px;");
        Label desc = new Label(p.getDescription());
        desc.setStyle("-fx-text-fill: #90A4AE; -fx-font-size: 12px;");
        // Actions
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER);
        Button btnEdit = new Button("Sửa");
        btnEdit.setStyle("-fx-background-color: #FFF3E0; -fx-text-fill: #FF9800; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 18;");
        btnEdit.setOnAction(e -> handleEditProduct(p));
        Button btnDelete = new Button("Xóa");
        btnDelete.setStyle("-fx-background-color: #FFEBEE; -fx-text-fill: #F44336; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 18;");
        btnDelete.setOnAction(e -> handleDeleteProduct(p));
        actions.getChildren().addAll(btnEdit, btnDelete);
        card.getChildren().addAll(header, barcode, price, stock, desc, actions);
        card.setPadding(new Insets(12));
        return card;
    }

    private void handleAddProduct() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/da/view/ProductFormDialog.fxml"));
            DialogPane dialogPane = loader.load();
            ProductFormDialogController controller = loader.getController();
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Thêm sản phẩm");
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                Product p = controller.getProductFromFields();
                if (p == null) {
                    showAlert("Dữ liệu không hợp lệ! Vui lòng kiểm tra lại các trường bắt buộc và định dạng.");
                    return;
                }
                if (p.insert()) {
                    loadProducts();
                    showInfo("Thêm sản phẩm thành công!");
                } else {
                    showAlert("Không thể thêm sản phẩm!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi khi mở dialog thêm sản phẩm!");
        }
    }

    private void handleEditProduct(Product p) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/da/view/ProductFormDialog.fxml"));
            DialogPane dialogPane = loader.load();
            ProductFormDialogController controller = loader.getController();
            controller.setProduct(p);
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Sửa sản phẩm");
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                Product newP = controller.getProductFromFields();
                if (newP == null) {
                    showAlert("Dữ liệu không hợp lệ! Vui lòng kiểm tra lại các trường bắt buộc và định dạng.");
                    return;
                }
                newP.setProductId(p.getProductId());
                if (newP.update()) {
                    loadProducts();
                    showInfo("Cập nhật sản phẩm thành công!");
                } else {
                    showAlert("Không thể cập nhật sản phẩm!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi khi mở dialog sửa sản phẩm!");
        }
    }

    private void handleDeleteProduct(Product p) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc chắn muốn xóa sản phẩm này?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText(null);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            if (p.delete()) {
                loadProducts();
                showInfo("Xóa sản phẩm thành công!");
            } else {
                showAlert("Không thể xóa sản phẩm!");
            }
        }
    }

    private void showProductDetail(Product p) {
        // TODO: Hiển thị dialog chi tiết sản phẩm nếu cần
        showInfo("Tên: " + p.getName() + "\nMã vạch: " + p.getBarcode() + "\nGiá: " + p.getPrice() + "\nTồn kho: " + p.getQuantityInStock());
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