package com.erdiagram.app;

import com.erdiagram.app.model.ERDiagram;
import com.erdiagram.app.parser.SQLParser;
import com.erdiagram.app.ui.DiagramView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.embed.swing.SwingFXUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Main UI component for the ER Diagram Generator application.
 */
public class ERDiagramGeneratorUI {
    private BorderPane root;
    private TextArea sqlTextArea;
    private DiagramView diagramView;
    private SQLParser sqlParser;
    private ERDiagram currentDiagram;
    private ToggleButton darkModeToggle;
    
    public ERDiagramGeneratorUI() {
        this.sqlParser = new SQLParser();
        initializeUI();
    }
    
    private void initializeUI() {
        root = new BorderPane();
        
        // Top section - Title and Controls
        HBox topBar = createTopBar();
        root.setTop(topBar);
        
        // Left section - SQL Input
        VBox leftPanel = createLeftPanel();
        root.setLeft(leftPanel);
        
        // Center section - Diagram View
        diagramView = new DiagramView();
        ScrollPane scrollPane = new ScrollPane(diagramView);
        scrollPane.setPannable(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        
        // Add a border to the scroll pane
        scrollPane.setStyle("-fx-border-color: #ddd; -fx-border-width: 1;");
        
        root.setCenter(scrollPane);
        
        // Add animation timer to update relationship lines during entity dragging
        javafx.animation.AnimationTimer timer = new javafx.animation.AnimationTimer() {
            @Override
            public void handle(long now) {
                diagramView.updateRelationshipLines();
            }
        };
        timer.start();
        
        // Add bottom status bar
        HBox statusBar = createStatusBar();
        root.setBottom(statusBar);
    }
    
    private HBox createTopBar() {
        HBox topBar = new HBox(15);
        topBar.setPadding(new Insets(15));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: linear-gradient(to right, #2c3e50, #4a6491);");
        
        // Application icon/logo
        Text logo = new Text("🗃");
        logo.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        logo.setFill(Color.WHITE);
        
        Text title = new Text("ER Diagram Generator");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setFill(Color.WHITE);
        
        // Create a toolbar for the buttons
        ToolBar toolbar = new ToolBar();
        toolbar.setStyle("-fx-background-color: transparent; -fx-spacing: 8;");
        
        Button loadSqlButton = createStyledButton("Load SQL", "📂");
        loadSqlButton.setOnAction(e -> loadSqlFile());
        
        Button exportButton = createStyledButton("Export Image", "📷");
        exportButton.setOnAction(e -> exportDiagramAsImage());
        
        Button autoLayoutButton = createStyledButton("Auto Layout", "🔄");
        autoLayoutButton.setOnAction(e -> diagramView.applyAutoLayout());
        
        Button zoomInButton = createStyledButton("", "🔍+");
        zoomInButton.setOnAction(e -> diagramView.zoom(0.1));
        
        Button zoomOutButton = createStyledButton("", "🔍-");
        zoomOutButton.setOnAction(e -> diagramView.zoom(-0.1));
        
        Button resetZoomButton = createStyledButton("Reset Zoom", "↺");
        resetZoomButton.setOnAction(e -> diagramView.resetZoom());
        
        // Add Home button
        Button homeButton = createStyledButton("Home", "🏠");
        homeButton.setOnAction(e -> returnToHome());
        
        darkModeToggle = new ToggleButton("🌙");
        darkModeToggle.setTooltip(new Tooltip("Toggle Dark Mode"));
        darkModeToggle.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 16px;");
        //darkModeToggle.setOnAction(e -> toggleDarkMode());
        
        Separator separator1 = new Separator();
        separator1.setOrientation(javafx.geometry.Orientation.VERTICAL);
        
        Separator separator2 = new Separator();
        separator2.setOrientation(javafx.geometry.Orientation.VERTICAL);
        
        toolbar.getItems().addAll(
                loadSqlButton, 
                exportButton, 
                separator1,
                autoLayoutButton, 
                separator2,
                zoomInButton, 
                zoomOutButton, 
                resetZoomButton,
                new Separator(javafx.geometry.Orientation.VERTICAL),
                homeButton,
                new Separator(javafx.geometry.Orientation.VERTICAL),
                darkModeToggle);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        topBar.getChildren().addAll(logo, title, spacer, toolbar);
        
        return topBar;
    }
    
    private Button createStyledButton(String text, String icon) {
        Button button = new Button();
        
        if (!text.isEmpty() && !icon.isEmpty()) {
            button.setText(icon + " " + text);
        } else if (!text.isEmpty()) {
            button.setText(text);
        } else if (!icon.isEmpty()) {
            button.setText(icon);
        }
        
        button.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-background-radius: 4;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: rgba(255,255,255,0.3); -fx-text-fill: white; -fx-background-radius: 4;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-background-radius: 4;"));
        
        return button;
    }
    
    private VBox createLeftPanel() {
        VBox leftPanel = new VBox(15);
        leftPanel.setPadding(new Insets(15));
        leftPanel.setPrefWidth(450);
        leftPanel.getStyleClass().add("input-area");
        
        // Title for the SQL input area
        Label sqlLabel = new Label("SQL Input");
        sqlLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        // SQL input with syntax highlighting (TextArea for now)
        sqlTextArea = new TextArea();
        sqlTextArea.setPrefHeight(400);
        sqlTextArea.setWrapText(true);
        sqlTextArea.setPromptText("Enter SQL CREATE TABLE statements here...");
        sqlTextArea.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 14px;");
        
        // Button container
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        
        Button generateButton = new Button("Generate ER Diagram");
        generateButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        generateButton.setPrefWidth(200);
        generateButton.setOnAction(e -> generateDiagram());
        
        Button exampleButton = new Button("Load Example");
        exampleButton.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
        exampleButton.setOnAction(e -> loadExampleSQL());
        
        buttonBox.getChildren().addAll(generateButton, exampleButton);
        
        // Create tabbed instructions and legend
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Instructions tab
        Tab instructionsTab = new Tab("Instructions");
        instructionsTab.setContent(createInstructionsBox());
        
        // Legend tab
        Tab legendTab = new Tab("Legend");
        legendTab.setContent(createLegendBox());
        
        tabPane.getTabs().addAll(instructionsTab, legendTab);
        
        // Add all components to the left panel
        leftPanel.getChildren().addAll(sqlLabel, sqlTextArea, buttonBox, tabPane);
        
        return leftPanel;
    }
    
    private VBox createInstructionsBox() {
        VBox instructionsBox = new VBox(10);
        instructionsBox.setPadding(new Insets(15));
        instructionsBox.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-radius: 5;");
        
        Text instructions = new Text(
                "1. Enter SQL CREATE TABLE statements in the text area above\n" +
                "2. Include PRIMARY KEY and FOREIGN KEY constraints\n" +
                "3. Each CREATE TABLE statement must end with a semicolon (;)\n" +
                "4. Click 'Generate ER Diagram' to create the visualization\n" +
                "5. Drag tables to reposition them\n" +
                "6. Use mouse wheel to zoom in/out\n" +
                "7. Export as image when finished"
        );
        
        instructions.setWrappingWidth(380);
        
        instructionsBox.getChildren().add(instructions);
        
        return instructionsBox;
    }
    
    private VBox createLegendBox() {
        VBox legendBox = new VBox(10);
        legendBox.setPadding(new Insets(15));
        legendBox.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-radius: 5;");
        
        // Primary key legend item
        HBox pkLegend = createLegendItem("🔑", "Primary Key", "#c0392b");
        
        // Foreign key legend item
        HBox fkLegend = createLegendItem("🔗", "Foreign Key", "#3498db");
        
        // Combined PK/FK legend item
        HBox pkfkLegend = createLegendItem("⚷", "Primary + Foreign Key", "#8e44ad");
        
        // One-to-One relationship
        HBox oneToOneLegend = createLegendItem("1:1", "One-to-One Relationship", "#27ae60");
        
        // One-to-Many relationship
        HBox oneToManyLegend = createLegendItem("1:N", "One-to-Many Relationship", "#e67e22");
        
        // Many-to-Many relationship
        HBox manyToManyLegend = createLegendItem("N:M", "Many-to-Many Relationship", "#8e44ad");
        
        legendBox.getChildren().addAll(
                pkLegend, 
                fkLegend, 
                pkfkLegend, 
                new Separator(), 
                oneToOneLegend, 
                oneToManyLegend, 
                manyToManyLegend);
        
        return legendBox;
    }
    
    private HBox createLegendItem(String symbol, String description, String color) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER_LEFT);
        
        Text symbolText = new Text(symbol);
        symbolText.setFill(Color.web(color));
        symbolText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        Text descText = new Text(description);
        
        Rectangle colorBox = new Rectangle(15, 15);
        colorBox.setFill(Color.web(color));
        colorBox.setArcWidth(3);
        colorBox.setArcHeight(3);
        
        item.getChildren().addAll(symbolText, colorBox, descText);
        
        return item;
    }
    
    private HBox createStatusBar() {
        HBox statusBar = new HBox(10);
        statusBar.setPadding(new Insets(5, 15, 5, 15));
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #ddd; -fx-border-width: 1 0 0 0;");
        
        Label statusLabel = new Label("Ready");
        statusLabel.setTextFill(Color.DARKGRAY);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Version info
        Label versionLabel = new Label("v1.0");
        versionLabel.setTextFill(Color.DARKGRAY);
        
        statusBar.getChildren().addAll(statusLabel, spacer, versionLabel);
        
        return statusBar;
    }
    
    private void generateDiagram() {
        String sqlText = sqlTextArea.getText().trim();
        
        if (sqlText.isEmpty()) {
            showAlert("Error", "Please enter SQL CREATE TABLE statements.");
            return;
        }
        
        try {
            currentDiagram = sqlParser.parseSQL(sqlText);
            diagramView.setDiagram(currentDiagram);
        } catch (Exception e) {
            showAlert("Error", "Failed to parse SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadSqlFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open SQL File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("SQL Files", "*.sql"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        
        File file = fileChooser.showOpenDialog(root.getScene().getWindow());
        
        if (file != null) {
            try {
                String content = new String(Files.readAllBytes(Paths.get(file.getPath())));
                sqlTextArea.setText(content);
            } catch (IOException e) {
                showAlert("Error", "Failed to load SQL file: " + e.getMessage());
            }
        }
    }
    
    private void exportDiagramAsImage() {
        if (currentDiagram == null || currentDiagram.getEntities().isEmpty()) {
            showAlert("Error", "No diagram to export. Please generate a diagram first.");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save ER Diagram");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PNG Images", "*.png"));
        
        File file = fileChooser.showSaveDialog(root.getScene().getWindow());
        
        if (file != null) {
            try {
                // Capture the current view as an image
                javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
                params.setFill(Color.WHITE);
                
                // Create a snapshot of the current visible area of the diagram
                javafx.scene.image.WritableImage snapshot = diagramView.snapshot(params, null);
                
                // Convert to a format that can be saved
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(snapshot, null);
                
                // Save the image
                ImageIO.write(bufferedImage, "png", file);
                
                showAlert("Success", "ER diagram saved successfully to: " + file.getPath());
            } catch (IOException e) {
                showAlert("Error", "Failed to save diagram: " + e.getMessage());
            }
        }
    }
    
    private void loadExampleSQL() {
        String exampleSQL = 
                "CREATE TABLE Customers (\n" +
                "    customer_id INT PRIMARY KEY,\n" +
                "    name VARCHAR(100) NOT NULL,\n" +
                "    email VARCHAR(100) UNIQUE,\n" +
                "    phone VARCHAR(20),\n" +
                "    address TEXT\n" +
                ");\n\n" +
                "CREATE TABLE Products (\n" +
                "    product_id INT PRIMARY KEY,\n" +
                "    name VARCHAR(100) NOT NULL,\n" +
                "    description TEXT,\n" +
                "    price DECIMAL(10, 2) NOT NULL,\n" +
                "    stock_quantity INT NOT NULL DEFAULT 0\n" +
                ");\n\n" +
                "CREATE TABLE Orders (\n" +
                "    order_id INT PRIMARY KEY,\n" +
                "    customer_id INT NOT NULL,\n" +
                "    order_date DATETIME NOT NULL,\n" +
                "    total_amount DECIMAL(10, 2),\n" +
                "    status VARCHAR(20),\n" +
                "    FOREIGN KEY (customer_id) REFERENCES Customers(customer_id)\n" +
                ");\n\n" +
                "CREATE TABLE OrderItems (\n" +
                "    order_item_id INT PRIMARY KEY,\n" +
                "    order_id INT NOT NULL,\n" +
                "    product_id INT NOT NULL,\n" +
                "    quantity INT NOT NULL,\n" +
                "    price DECIMAL(10, 2) NOT NULL,\n" +
                "    FOREIGN KEY (order_id) REFERENCES Orders(order_id),\n" +
                "    FOREIGN KEY (product_id) REFERENCES Products(product_id)\n" +
                ");\n\n" +
                "CREATE TABLE Categories (\n" +
                "    category_id INT PRIMARY KEY,\n" +
                "    name VARCHAR(50) NOT NULL,\n" +
                "    description TEXT\n" +
                ");\n\n" +
                "CREATE TABLE ProductCategories (\n" +
                "    product_id INT NOT NULL,\n" +
                "    category_id INT NOT NULL,\n" +
                "    PRIMARY KEY (product_id, category_id),\n" +
                "    FOREIGN KEY (product_id) REFERENCES Products(product_id),\n" +
                "    FOREIGN KEY (category_id) REFERENCES Categories(category_id)\n" +
                ");";
        
        sqlTextArea.setText(exampleSQL);
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void returnToHome() {
        // Get the current stage
        javafx.stage.Stage stage = (javafx.stage.Stage) root.getScene().getWindow();
        
        // Create new home screen
        HomeScreen homeScreen = new HomeScreen(stage);
        
        // Set scene with home screen
        javafx.scene.Scene scene = new javafx.scene.Scene(homeScreen.getRoot(), 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        
        // Update stage
        stage.setTitle("Diagram Generator");
        stage.setScene(scene);
    }
    
    public BorderPane getRoot() {
        return root;
    }
} 