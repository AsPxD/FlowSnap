package com.erdiagram.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Diagram Generator");

        // Create HomeScreen
        HomeScreen homeScreen = new HomeScreen(primaryStage);
        Scene scene = new Scene(homeScreen.getRoot(), 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        
        primaryStage.setScene(scene);
        
        // Show splash screen first
        SplashScreen splashScreen = new SplashScreen(primaryStage);
        splashScreen.show();
    }
} 