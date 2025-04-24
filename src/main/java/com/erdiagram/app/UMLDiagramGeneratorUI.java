package com.erdiagram.app;

import com.erdiagram.app.model.UMLDiagram;
import com.erdiagram.app.parser.JavaParser;
import com.erdiagram.app.ui.UMLDiagramView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
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
 * Main UI component for the UML Diagram Generator.
 */
public class UMLDiagramGeneratorUI {
    private BorderPane root;
    private TextArea javaTextArea;
    private UMLDiagramView diagramView;
    private JavaParser javaParser;
    private UMLDiagram currentDiagram;
    
    public UMLDiagramGeneratorUI() {
        this.javaParser = new JavaParser();
        initializeUI();
    }
    
    private void initializeUI() {
        root = new BorderPane();
        
        // Top section - Title and Controls
        HBox topBar = createTopBar();
        root.setTop(topBar);
        
        // Left section - Java Input
        VBox leftPanel = createLeftPanel();
        root.setLeft(leftPanel);
        
        // Center section - Diagram View
        diagramView = new UMLDiagramView();
        ScrollPane scrollPane = new ScrollPane(diagramView);
        scrollPane.setPannable(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        
        // Add a border to the scroll pane
        scrollPane.setStyle("-fx-border-color: #ddd; -fx-border-width: 1;");
        
        root.setCenter(scrollPane);
        
        // Add animation timer to update relationship lines during class dragging
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
        topBar.setStyle("-fx-background-color: linear-gradient(to right, #6a3093, #9b59b6);");
        
        // Application icon/logo
        Text logo = new Text("📊");
        logo.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        logo.setFill(Color.WHITE);
        
        Text title = new Text("UML Diagram Generator");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setFill(Color.WHITE);
        
        // Create a toolbar for the buttons
        ToolBar toolbar = new ToolBar();
        toolbar.setStyle("-fx-background-color: transparent; -fx-spacing: 8;");
        
        Button loadJavaButton = createStyledButton("Load Java", "📂");
        loadJavaButton.setOnAction(e -> loadJavaFile());
        
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
        
        Button homeButton = createStyledButton("Home", "🏠");
        homeButton.setOnAction(e -> returnToHome());
        
        Separator separator1 = new Separator();
        separator1.setOrientation(javafx.geometry.Orientation.VERTICAL);
        
        Separator separator2 = new Separator();
        separator2.setOrientation(javafx.geometry.Orientation.VERTICAL);
        
        toolbar.getItems().addAll(
                loadJavaButton, 
                exportButton, 
                separator1,
                autoLayoutButton, 
                separator2,
                zoomInButton, 
                zoomOutButton, 
                resetZoomButton,
                new Separator(javafx.geometry.Orientation.VERTICAL),
                homeButton);
        
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
        
        // Title for the Java input area
        Label javaLabel = new Label("Java Input");
        javaLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        // Java input with syntax highlighting (TextArea for now)
        javaTextArea = new TextArea();
        javaTextArea.setPrefHeight(400);
        javaTextArea.setWrapText(true);
        javaTextArea.setPromptText("Enter Java class definitions here...");
        javaTextArea.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 14px;");
        
        // Button container
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        
        Button generateButton = new Button("Generate UML Diagram");
        generateButton.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-weight: bold;");
        generateButton.setPrefWidth(200);
        generateButton.setOnAction(e -> generateDiagram());
        
        Button exampleButton = new Button("Load Example");
        exampleButton.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
        exampleButton.setOnAction(e -> loadExampleJava());
        
        buttonBox.getChildren().addAll(generateButton, exampleButton);
        
        // Create tabbed instructions and legend
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setPrefHeight(275); // Increased height for the tab pane
        
        Tab instructionsTab = new Tab("Instructions");
        instructionsTab.setContent(createInstructionsBox());
        
        Tab legendTab = new Tab("Legend");
        legendTab.setContent(createLegendBox());
        
        tabPane.getTabs().addAll(instructionsTab, legendTab);
        
        leftPanel.getChildren().addAll(javaLabel, javaTextArea, buttonBox, tabPane);
        
        return leftPanel;
    }
    
    private VBox createInstructionsBox() {
        VBox instructionsBox = new VBox(10);
        instructionsBox.setPadding(new Insets(15));
        instructionsBox.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #ddd; -fx-border-width: 1;");
        
        Text instructionsTitle = new Text("How to Create a UML Diagram:");
        instructionsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        // Create a separate VBox for the instructions content
        VBox instructionsContent = new VBox(12);
        instructionsContent.setPadding(new Insets(5, 0, 5, 0));
        
        // Basic instructions
        VBox basicSteps = new VBox(6);
        Text stepsTitle = new Text("Basic Steps:");
        stepsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        Text stepsText = new Text(
                "1. Enter Java class code in the text area above\n" +
                "2. Click 'Generate UML Diagram'\n" +
                "3. Drag classes to arrange them\n" +
                "4. Use the toolbar buttons to adjust the view");
        stepsText.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        
        basicSteps.getChildren().addAll(stepsTitle, stepsText);
        
        // Additional features
        VBox features = new VBox(6);
        Text featuresTitle = new Text("\nAdditional Features:");
        featuresTitle.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        Text loadingText = new Text("• Load a Java file using the 'Load Java' button");
        loadingText.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        
        Text exampleText = new Text("• Try an example with the 'Load Example' button");
        exampleText.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        
        Text exportText = new Text("• Export your diagram as an image using 'Export Image'");
        exportText.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        
        Text layoutText = new Text("• Auto-arrange classes with 'Auto Layout'");
        layoutText.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        
        Text zoomText = new Text("• Zoom in/out or reset zoom using the zoom controls");
        zoomText.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        
        Text homeText = new Text("• Return to the home screen with the 'Home' button");
        homeText.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        
        features.getChildren().addAll(featuresTitle, loadingText, exampleText, exportText, layoutText, zoomText, homeText);
        
        // Tips
        VBox tips = new VBox(6);
        Text tipsTitle = new Text("\nTips:");
        tipsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        Text draggingTip = new Text("• Drag classes to better visualize relationships");
        draggingTip.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        
        Text syntaxTip = new Text("• Ensure your Java syntax is correct for accurate parsing");
        syntaxTip.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        
        tips.getChildren().addAll(tipsTitle, draggingTip, syntaxTip);
        
        // Add all sections to the content
        instructionsContent.getChildren().addAll(basicSteps, features, tips);
        
        // Create a ScrollPane for the instructions content
        ScrollPane scrollPane = new ScrollPane(instructionsContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(200); // Set a preferred height
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-width: 0;");
        
        instructionsBox.getChildren().addAll(instructionsTitle, scrollPane);
        
        return instructionsBox;
    }
    
    private VBox createLegendBox() {
        VBox legendBox = new VBox(10);
        legendBox.setPadding(new Insets(15));
        legendBox.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #ddd; -fx-border-width: 1;");
        
        Text legendTitle = new Text("UML Diagram Legend:");
        legendTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        // Create a separate VBox for the legend items
        VBox legendItems = new VBox(10);
        legendItems.setPadding(new Insets(5, 0, 5, 0));
        
        // Add legend items for relationship types
        legendItems.getChildren().add(createLegendItem("——————>", "Association", "#000000"));
        legendItems.getChildren().add(createLegendItem("----->", "Dependency", "#000000"));
        legendItems.getChildren().add(createLegendItem("◁——————", "Inheritance", "#000000"));
        legendItems.getChildren().add(createLegendItem("◁- - - -", "Implementation", "#000000"));
        legendItems.getChildren().add(createLegendItem("◇——————", "Aggregation", "#000000"));
        legendItems.getChildren().add(createLegendItem("◆——————", "Composition", "#000000"));
        
        // Add more detailed explanations
        Text explanationTitle = new Text("\nRelationship Explanations:");
        explanationTitle.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        legendItems.getChildren().add(explanationTitle);
        
        legendItems.getChildren().add(createExplanationItem("Association", 
            "Basic relationship between classes. One class uses another class as a parameter or return value."));
        
        legendItems.getChildren().add(createExplanationItem("Dependency", 
            "Weaker form of relationship where one class depends on another, but doesn't store an instance of it."));
        
        legendItems.getChildren().add(createExplanationItem("Inheritance", 
            "An 'is-a' relationship. Child class extends parent class, inheriting its attributes and behavior."));
        
        legendItems.getChildren().add(createExplanationItem("Implementation", 
            "Class implements an interface, promising to provide implementations for all interface methods."));
        
        legendItems.getChildren().add(createExplanationItem("Aggregation", 
            "A 'has-a' relationship. One class contains references to another, but they can exist independently."));
        
        legendItems.getChildren().add(createExplanationItem("Composition", 
            "Stronger form of aggregation. Child objects cannot exist independent of the parent."));
        
        // Create a ScrollPane for the legend items
        ScrollPane scrollPane = new ScrollPane(legendItems);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(200); // Set a preferred height
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-width: 0;");
        
        legendBox.getChildren().addAll(legendTitle, scrollPane);
        
        return legendBox;
    }
    
    private VBox createExplanationItem(String title, String explanation) {
        VBox item = new VBox(3);
        item.setPadding(new Insets(5, 0, 5, 10));
        
        Text titleText = new Text(title + ":");
        titleText.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        
        Text explanationText = new Text(explanation);
        explanationText.setFont(Font.font("Arial", FontWeight.NORMAL, 11));
        explanationText.setWrappingWidth(380);
        
        item.getChildren().addAll(titleText, explanationText);
        return item;
    }
    
    private HBox createLegendItem(String symbol, String description, String color) {
        HBox item = new HBox(15);
        item.setAlignment(Pos.CENTER_LEFT);
        
        Text symbolText = new Text(symbol);
        symbolText.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        if (color != null) {
            symbolText.setFill(Color.web(color));
        }
        
        Text descText = new Text(description);
        descText.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        
        item.getChildren().addAll(symbolText, descText);
        
        return item;
    }
    
    private HBox createStatusBar() {
        HBox statusBar = new HBox();
        statusBar.setPadding(new Insets(5, 10, 5, 10));
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-width: 1 0 0 0;");
        
        Label statusLabel = new Label("Ready");
        statusLabel.setTextFill(Color.web("#555"));
        
        statusBar.getChildren().add(statusLabel);
        
        return statusBar;
    }
    
    private void generateDiagram() {
        String javaCode = javaTextArea.getText();
        if (javaCode.isEmpty()) {
            showAlert("Empty Input", "Please enter some Java code.");
            return;
        }
        
        try {
            // Parse the Java code
            currentDiagram = javaParser.parseJavaCode(javaCode);
            
            // Display the diagram
            diagramView.setDiagram(currentDiagram);
            
            // Apply auto layout
            diagramView.applyAutoLayout();
            
        } catch (Exception e) {
            showAlert("Parsing Error", "Error parsing Java code: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadJavaFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Java File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Java Files", "*.java"));
        
        File selectedFile = fileChooser.showOpenDialog(root.getScene().getWindow());
        if (selectedFile != null) {
            try {
                String content = new String(Files.readAllBytes(Paths.get(selectedFile.getPath())));
                javaTextArea.setText(content);
            } catch (IOException e) {
                showAlert("File Error", "Error reading file: " + e.getMessage());
            }
        }
    }
    
    private void exportDiagramAsImage() {
        if (currentDiagram == null || currentDiagram.getClasses().isEmpty()) {
            showAlert("No Diagram", "There is no diagram to export.");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Diagram Image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Images", "*.png"));
        
        File file = fileChooser.showSaveDialog(root.getScene().getWindow());
        if (file != null) {
            try {
                // Create image of current diagram view
                javafx.scene.Node node = diagramView;
                
                // Reset zoom temporarily for export
                double originalZoom = diagramView.getScaleX();
                diagramView.resetZoom();
                
                javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
                params.setFill(Color.WHITE);
                javafx.scene.image.WritableImage snapshot = node.snapshot(params, null);
                
                // Restore original zoom
                diagramView.setScaleX(originalZoom);
                diagramView.setScaleY(originalZoom);
                
                // Save the image
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(snapshot, null);
                ImageIO.write(bufferedImage, "png", file);
                
                showAlert("Success", "Diagram exported successfully.");
            } catch (IOException e) {
                showAlert("Export Error", "Error exporting image: " + e.getMessage());
            }
        }
    }
    
    private void loadExampleJava() {
        String exampleCode = "package com.example;\n\n" +
                "public class Student extends Person implements Comparable<Student> {\n" +
                "    private int studentId;\n" +
                "    private double gpa;\n" +
                "    private Course currentCourse;\n" +
                "    \n" +
                "    public Student(int studentId, String name, int age) {\n" +
                "        super(name, age);\n" +
                "        this.studentId = studentId;\n" +
                "        this.gpa = 0.0;\n" +
                "    }\n" +
                "    \n" +
                "    public int getStudentId() {\n" +
                "        return studentId;\n" +
                "    }\n" +
                "    \n" +
                "    public double getGpa() {\n" +
                "        return gpa;\n" +
                "    }\n" +
                "    \n" +
                "    public void setGpa(double gpa) {\n" +
                "        this.gpa = gpa;\n" +
                "    }\n" +
                "    \n" +
                "    public Course getCurrentCourse() {\n" +
                "        return currentCourse;\n" +
                "    }\n" +
                "    \n" +
                "    public void enrollInCourse(Course course) {\n" +
                "        this.currentCourse = course;\n" +
                "    }\n" +
                "    \n" +
                "    @Override\n" +
                "    public int compareTo(Student other) {\n" +
                "        return Double.compare(this.gpa, other.gpa);\n" +
                "    }\n" +
                "}\n\n" +
                "abstract class Person {\n" +
                "    protected String name;\n" +
                "    protected int age;\n" +
                "    \n" +
                "    public Person(String name, int age) {\n" +
                "        this.name = name;\n" +
                "        this.age = age;\n" +
                "    }\n" +
                "    \n" +
                "    public String getName() {\n" +
                "        return name;\n" +
                "    }\n" +
                "    \n" +
                "    public int getAge() {\n" +
                "        return age;\n" +
                "    }\n" +
                "    \n" +
                "    public abstract void displayDetails();\n" +
                "}\n\n" +
                "class Course {\n" +
                "    private String courseId;\n" +
                "    private String title;\n" +
                "    private Professor instructor;\n" +
                "    \n" +
                "    public Course(String courseId, String title) {\n" +
                "        this.courseId = courseId;\n" +
                "        this.title = title;\n" +
                "    }\n" +
                "    \n" +
                "    public String getCourseId() {\n" +
                "        return courseId;\n" +
                "    }\n" +
                "    \n" +
                "    public String getTitle() {\n" +
                "        return title;\n" +
                "    }\n" +
                "    \n" +
                "    public Professor getInstructor() {\n" +
                "        return instructor;\n" +
                "    }\n" +
                "    \n" +
                "    public void assignInstructor(Professor instructor) {\n" +
                "        this.instructor = instructor;\n" +
                "    }\n" +
                "}\n\n" +
                "class Professor extends Person {\n" +
                "    private String employeeId;\n" +
                "    private String department;\n" +
                "    \n" +
                "    public Professor(String employeeId, String name, int age, String department) {\n" +
                "        super(name, age);\n" +
                "        this.employeeId = employeeId;\n" +
                "        this.department = department;\n" +
                "    }\n" +
                "    \n" +
                "    public String getEmployeeId() {\n" +
                "        return employeeId;\n" +
                "    }\n" +
                "    \n" +
                "    public String getDepartment() {\n" +
                "        return department;\n" +
                "    }\n" +
                "    \n" +
                "    @Override\n" +
                "    public void displayDetails() {\n" +
                "        System.out.println(\"Professor: \" + name + \", Department: \" + department);\n" +
                "    }\n" +
                "}\n\n" +
                "interface Comparable<T> {\n" +
                "    int compareTo(T other);\n" +
                "}";
        
        javaTextArea.setText(exampleCode);
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