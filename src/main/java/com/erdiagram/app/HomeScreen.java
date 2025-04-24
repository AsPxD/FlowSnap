package com.erdiagram.app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

/**
 * Home screen with options to generate ER or UML diagrams.
 */
public class HomeScreen {
    
    private BorderPane root;
    private final Stage primaryStage;
    
    public HomeScreen(Stage primaryStage) {
        this.primaryStage = primaryStage;
        initializeUI();
    }
    
    private void initializeUI() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #f5f7fa, #e4e7eb);");
        
        // Top bar with title
        HBox topBar = createTopBar();
        root.setTop(topBar);
        
        // Center content with diagram options
        VBox centerContent = createCenterContent();
        root.setCenter(centerContent);
        
        // Bottom status bar
        HBox bottomBar = createBottomBar();
        root.setBottom(bottomBar);
    }
    
    private HBox createTopBar() {
        HBox topBar = new HBox(15);
        topBar.setPadding(new Insets(15));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: linear-gradient(to right, #2c3e50, #4a6491);");
        
        // Application icon/logo
        Text logo = new Text("🗃 📊");
        logo.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        logo.setFill(Color.WHITE);
        
        Text title = new Text("Diagram Generator");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setFill(Color.WHITE);
        
        topBar.getChildren().addAll(logo, title);
        
        return topBar;
    }
    
    private VBox createCenterContent() {
        VBox centerContent = new VBox(30);
        centerContent.setAlignment(Pos.CENTER);
        centerContent.setPadding(new Insets(50));
        
        // Welcome text
        Text welcomeText = new Text("Welcome to Diagram Generator");
        welcomeText.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        welcomeText.setFill(Color.valueOf("#2c3e50"));
        
        Text subtitleText = new Text("Choose a diagram type to get started");
        subtitleText.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        subtitleText.setFill(Color.valueOf("#7f8c8d"));
        
        // Container for diagram options
        HBox optionsContainer = new HBox(40);
        optionsContainer.setAlignment(Pos.CENTER);
        
        // ER Diagram option
        VBox erDiagramOption = createDiagramOption(
                "ER Diagram Generator",
                "Generate Entity-Relationship diagrams from SQL statements",
                "🗃",
                "#3498db");
        
        // UML Diagram option
        VBox umlDiagramOption = createDiagramOption(
                "UML Diagram Generator",
                "Create UML class diagrams from Java class definitions",
                "📊",
                "#9b59b6");
        
        // DFD Diagram option
        VBox dfdDiagramOption = createDiagramOption(
                "DFD Diagram Generator",
                "Design Data Flow Diagrams to visualize system data movement",
                "📈",
                "#27ae60");
        
        optionsContainer.getChildren().addAll(erDiagramOption, umlDiagramOption, dfdDiagramOption);
        
        centerContent.getChildren().addAll(welcomeText, subtitleText, optionsContainer);
        
        return centerContent;
    }
    
    private VBox createDiagramOption(String title, String description, String icon, String color) {
        VBox option = new VBox(15);
        option.setAlignment(Pos.CENTER);
        option.setPadding(new Insets(25));
        option.setMinWidth(300);
        option.setMinHeight(300);
        option.setStyle(
                "-fx-background-color: white;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);" +
                "-fx-background-radius: 10;");
        
        // Icon
        Text iconText = new Text(icon);
        iconText.setFont(Font.font("Arial", FontWeight.BOLD, 60));
        iconText.setFill(Color.web(color));
        
        // Title
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        
        // Description
        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        descLabel.setTextFill(Color.web("#7f8c8d"));
        descLabel.setWrapText(true);
        descLabel.setTextAlignment(TextAlignment.CENTER);
        descLabel.setMaxWidth(250);
        
        // Button
        Button startButton = new Button("Get Started");
        startButton.setStyle(
                "-fx-background-color: " + color + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 10 20;" +
                "-fx-background-radius: 5;");
        
        // Set button action based on diagram type
        if (title.contains("ER")) {
            startButton.setOnAction(e -> openERDiagramGenerator());
        } else if (title.contains("UML")) {
            startButton.setOnAction(e -> openUMLDiagramGenerator());
        } else if (title.contains("DFD")) {
            startButton.setOnAction(e -> openDFDiagramGenerator());
        }
        
        option.getChildren().addAll(iconText, titleLabel, descLabel, startButton);
        
        // Add hover effect
        option.setOnMouseEntered(e -> option.setStyle(
                "-fx-background-color: white;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 8);" +
                "-fx-background-radius: 10;"));
        option.setOnMouseExited(e -> option.setStyle(
                "-fx-background-color: white;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);" +
                "-fx-background-radius: 10;"));
        
        return option;
    }
    
    private HBox createBottomBar() {
        HBox bottomBar = new HBox();
        bottomBar.setPadding(new Insets(10));
        bottomBar.setAlignment(Pos.CENTER_RIGHT);
        bottomBar.setStyle("-fx-background-color: #ecf0f1;");
        
        Label versionLabel = new Label("v1.0.0");
        versionLabel.setTextFill(Color.web("#95a5a6"));
        
        bottomBar.getChildren().add(versionLabel);
        
        return bottomBar;
    }
    
    private void openERDiagramGenerator() {
        // Create the ER Diagram generator UI
        ERDiagramGeneratorUI ui = new ERDiagramGeneratorUI();
        Scene scene = new Scene(ui.getRoot(), 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        
        // Update stage with new scene
        primaryStage.setTitle("ER Diagram Generator");
        primaryStage.setScene(scene);
    }
    
    private void openUMLDiagramGenerator() {
        // Create the UML Diagram generator UI
        UMLDiagramGeneratorUI ui = new UMLDiagramGeneratorUI();
        Scene scene = new Scene(ui.getRoot(), 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        
        // Update stage with new scene
        primaryStage.setTitle("UML Diagram Generator");
        primaryStage.setScene(scene);
    }
    
    private void openDFDiagramGenerator() {
        // Create the DFD Diagram generator UI
        DFDiagramGeneratorUI ui = new DFDiagramGeneratorUI();
        Scene scene = new Scene(ui.getRoot(), 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        
        // Update stage with new scene
        primaryStage.setTitle("Data Flow Diagram Generator");
        primaryStage.setScene(scene);
    }
    
    public BorderPane getRoot() {
        return root;
    }
} 