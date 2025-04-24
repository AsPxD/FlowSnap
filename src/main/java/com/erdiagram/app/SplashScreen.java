package com.erdiagram.app;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * Splash screen shown when the application starts.
 */
public class SplashScreen {
    
    private Stage splashStage;
    private final Stage mainStage;
    
    public SplashScreen(Stage mainStage) {
        this.mainStage = mainStage;
        createSplashScreen();
    }
    
    private void createSplashScreen() {
        splashStage = new Stage();
        splashStage.initStyle(StageStyle.UNDECORATED);
        
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #2c3e50, #4a6491);");
        root.setPrefWidth(600);
        root.setPrefHeight(400);
        
        // App title
        Text title = new Text("Diagram Generator");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setFill(Color.WHITE);
        
        // App subtitle
        Text subtitle = new Text("ER & UML Diagrams Made Easy");
        subtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        subtitle.setFill(Color.LIGHTGRAY);
        
        // Icon or logo
        Text logo = new Text("🗃 📊");
        logo.setFont(Font.font("Arial", FontWeight.BOLD, 60));
        
        // Loading indicator
        ProgressBar progressBar = new ProgressBar();
        progressBar.setPrefWidth(300);
        progressBar.setProgress(-1); // Indeterminate progress
        
        // Version info
        Label versionLabel = new Label("v1.0.0");
        versionLabel.setTextFill(Color.LIGHTGRAY);
        
        root.getChildren().addAll(logo, title, subtitle, progressBar, versionLabel);
        
        Scene splashScene = new Scene(root);
        splashStage.setScene(splashScene);
        
        // Center on screen
        splashStage.centerOnScreen();
    }
    
    public void show() {
        // Create fade-in animation
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), splashStage.getScene().getRoot());
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        // Create pause
        PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
        
        // Create fade-out animation
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), splashStage.getScene().getRoot());
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            splashStage.close();
            mainStage.show();
        });
        
        // Create sequential transition
        SequentialTransition animation = new SequentialTransition(fadeIn, pause, fadeOut);
        
        // Show splash screen and start animation
        splashStage.show();
        animation.play();
    }
} 