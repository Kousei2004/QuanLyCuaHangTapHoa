package com.example.da.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.net.URLEncoder;

public class VietQRService {

    private static final String BIN = "970422";
    private static final String ACCOUNT_NO = "5678720112004";
    private static final String ACCOUNT_NAME = "NGUYEN VAN DUOC";
    private static final String TEMPLATE = "CB33gMi";
    
    private final HttpClient httpClient;
    private final Gson gson;
    
    public VietQRService() {
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
    }
    
    /**
     * Tạo URL mã QR động VietQR
     * @param amount Số tiền thanh toán
     * @param note Ghi chú chuyển khoản
     * @return URL ảnh QR
     */
    public String generateVietQRUrl(double amount, String note) {
        try {
            String url = String.format(
                "https://api.vietqr.io/image/%s-%s-%s.jpg?accountName=%s&amount=%d&addInfo=%s",
                BIN,
                ACCOUNT_NO,
                TEMPLATE,
                URLEncoder.encode(ACCOUNT_NAME, "UTF-8"),
                (long) amount,
                URLEncoder.encode(note, "UTF-8")
            );
            return url;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Tạo ImageView chứa mã QR VietQR
     * @param amount Số tiền thanh toán
     * @param note Ghi chú chuyển khoản
     * @param width Chiều rộng ảnh
     * @param height Chiều cao ảnh
     * @return ImageView mã QR
     */
    public ImageView createQRImageView(double amount, String note, double width, double height) {
        String url = generateVietQRUrl(amount, note);
        if (url == null) return null;
        Image qrImage = new Image(url, width, height, true, true);
        ImageView imageView = new ImageView(qrImage);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(true);
        return imageView;
    }
    
    /**
     * Tạo mã QR từ base64 string
     * @param base64QR Base64 string của mã QR
     * @param width Chiều rộng
     * @param height Chiều cao
     * @return ImageView chứa mã QR
     */
    public ImageView createQRFromBase64(String base64QR, double width, double height) {
        try {
            byte[] imageData = Base64.getDecoder().decode(base64QR);
            ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
            BufferedImage bufferedImage = ImageIO.read(bis);
            
            WritableImage fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
            ImageView imageView = new ImageView(fxImage);
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
            imageView.setPreserveRatio(true);
            
            return imageView;
        } catch (Exception e) {
            System.err.println("Error creating QR from base64: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Xóa file QR tạm
     * @param qrCodePath Đường dẫn file cần xóa
     */
    public void cleanupQRFile(String qrCodePath) {
        try {
            java.io.File file = new java.io.File(qrCodePath);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            System.err.println("Error cleaning up QR file: " + e.getMessage());
        }
    }
    
    /**
     * Tạo thông tin thanh toán cho VietQR
     * @param amount Số tiền
     * @param orderId Mã đơn hàng
     * @param customerName Tên khách hàng
     * @return Mô tả giao dịch
     */
    public String createPaymentDescription(double amount, String orderId, String customerName) {
        return String.format("Thanh toan don hang %s - %s", orderId, customerName);
    }
} 