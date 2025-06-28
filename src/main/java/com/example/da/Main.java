package com.example.da;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load màn hình login đầu tiên
        Parent root = FXMLLoader.load(getClass().getResource("/com/example/da/view/Login.fxml"));

        Scene scene = new Scene(root, 400, 570);

        primaryStage.setTitle("Hệ thống Quản lý - Đăng nhập");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}