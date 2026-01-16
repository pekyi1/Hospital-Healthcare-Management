package com.hospital;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Start with login screen instead of main layout
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/view/LoginView.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1000, 700);

            primaryStage.setTitle("Hospital Management System - Login");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
