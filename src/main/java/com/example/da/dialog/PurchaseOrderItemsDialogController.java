package com.example.da.dialog;

import com.example.da.model.Product;
import com.example.da.model.PurchaseOrderItem;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.collections.transformation.FilteredList;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;

public class PurchaseOrderItemsDialogController implements Initializable {
    @FXML private DialogPane dialogPane;
    @FXML private ComboBox<Product> cbProduct;
    @FXML private TextField txtQuantity;
    @FXML private TextField txtUnitPrice;
    @FXML private Button btnAddItem;
    @FXML private TableView<PurchaseOrderItem> itemsTable;
    @FXML private TableColumn<PurchaseOrderItem, String> colProduct;
    @FXML private TableColumn<PurchaseOrderItem, Integer> colQuantity;
    @FXML private TableColumn<PurchaseOrderItem, BigDecimal> colUnitPrice;
    @FXML private TableColumn<PurchaseOrderItem, BigDecimal> colTotal;
    @FXML private TableColumn<PurchaseOrderItem, String> colAction;

    private ObservableList<PurchaseOrderItem> items = FXCollections.observableArrayList();
    private int poId;

    public void setPurchaseOrderId(int poId) {
        this.poId = poId;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<Product> allProducts = FXCollections.observableArrayList(Product.getAll());
        FilteredList<Product> filteredProducts = new FilteredList<>(allProducts, p -> true);
        cbProduct.setItems(filteredProducts);

        // Filter khi gõ
        cbProduct.setEditable(false); // Không cho nhập text tự do, chỉ chọn từ danh sách
        cbProduct.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            final String filter = newVal.toLowerCase();
            filteredProducts.setPredicate(product -> {
                if (filter == null || filter.isEmpty()) return true;
                return product.getName().toLowerCase().contains(filter)
                    || String.valueOf(product.getProductId()).contains(filter)
                    || product.toString().toLowerCase().contains(filter);
            });
        });
        cbProduct.setOnHidden(e -> {
            cbProduct.getEditor().clear();
            filteredProducts.setPredicate(p -> true);
        });
        colProduct.setCellValueFactory(cellData -> {
            Product p = findProductById(cellData.getValue().getProductId());
            return new SimpleStringProperty(p != null ? p.getName() : "");
        });
        colQuantity.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("quantity"));
        colUnitPrice.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("unitPrice"));
        colTotal.setCellValueFactory(cellData -> {
            BigDecimal total = cellData.getValue().getUnitPrice().multiply(BigDecimal.valueOf(cellData.getValue().getQuantity()));
            return new javafx.beans.property.SimpleObjectProperty<>(total);
        });
        colAction.setCellValueFactory(cellData -> new SimpleStringProperty("Xóa"));
        itemsTable.setItems(items);

        btnAddItem.setOnAction(e -> addItem());
        itemsTable.setRowFactory(tv -> {
            TableRow<PurchaseOrderItem> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    items.remove(row.getItem());
                }
            });
            return row;
        });
        
        // Tìm nút Save và gắn event handler
        javafx.application.Platform.runLater(() -> {
            try {
                for (javafx.scene.Node node : dialogPane.lookupAll("Button")) {
                    if (node instanceof javafx.scene.control.Button) {
                        javafx.scene.control.Button button = (javafx.scene.control.Button) node;
                        if ("Lưu".equals(button.getText())) {
                            button.setOnAction(e -> saveItemsToDB());
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void addItem() {
        Product product = cbProduct.getValue();
        
        int quantity;
        BigDecimal unitPrice;
        try {
            quantity = Integer.parseInt(txtQuantity.getText());
            unitPrice = new BigDecimal(txtUnitPrice.getText());
        } catch (Exception e) {
            showAlert("Vui lòng nhập số lượng và đơn giá hợp lệ!");
            return;
        }
        
        if (product == null || quantity <= 0 || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            showAlert("Vui lòng chọn sản phẩm, nhập số lượng và đơn giá hợp lệ!");
            return;
        }
        
        PurchaseOrderItem item = new PurchaseOrderItem(0, poId, product.getProductId(), quantity, unitPrice);
        items.add(item);
        
        cbProduct.setValue(null);
        txtQuantity.clear();
        txtUnitPrice.clear();
    }

    public void saveItemsToDB() {
        if (items.isEmpty()) {
            showAlert("Vui lòng thêm ít nhất một sản phẩm!");
            return;
        }
        
        int successCount = 0;
        for (PurchaseOrderItem item : items) {
            item.setPoId(poId);
            if (item.insert()) {
                // Tăng tồn kho sản phẩm
                Product p = findProductById(item.getProductId());
                if (p != null) {
                    p.setQuantityInStock(p.getQuantityInStock() + item.getQuantity());
                    p.update(); // Cập nhật lại DB
                }
                successCount++;
            }
        }
        
        if (successCount > 0) {
            showAlert("Đã lưu " + successCount + " sản phẩm thành công!");
        } else {
            showAlert("Không thể lưu sản phẩm nào!");
        }
    }

    public ObservableList<PurchaseOrderItem> getItems() {
        return items;
    }

    private Product findProductById(Integer id) {
        if (id == null) return null;
        for (Product p : cbProduct.getItems()) {
            if (p.getProductId() == id) return p;
        }
        return null;
    }

    private void showAlert(String msg) {
        System.out.println("DEBUG: showAlert called with msg = " + msg);
        String finalMsg = (msg == null || msg.trim().isEmpty()) ? "Có lỗi xảy ra hoặc không có thông báo cụ thể!" : msg;
        javafx.application.Platform.runLater(() -> {
            Alert.AlertType type = finalMsg.toLowerCase().contains("thành công") ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING;
            Alert alert = new Alert(type, finalMsg, ButtonType.OK);
            alert.showAndWait();
        });
    }
} 