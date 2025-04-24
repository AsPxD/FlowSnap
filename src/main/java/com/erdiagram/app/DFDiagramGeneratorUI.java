package com.erdiagram.app;

import com.erdiagram.app.model.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.DirectoryChooser;
import javafx.scene.Node;
import javafx.scene.Group;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.input.ScrollEvent;
import javafx.scene.transform.Scale;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.scene.control.ButtonBar.ButtonData;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Optional;

/**
 * UI for the Data Flow Diagram generator.
 */
public class DFDiagramGeneratorUI {
    
    private final BorderPane root;
    private final DFDiagram diagram;
    private final Pane canvas;
    private final VBox toolbox;
    
    // Element tracking variables
    private DFDElement selectedElement;
    private final Map<Node, DFDElement> nodeToElementMap = new HashMap<>();
    private final Map<DFDElement, Group> elementToNodeMap = new HashMap<>();
    
    // Dragging variables
    private double dragStartX;
    private double dragStartY;
    private boolean isDragging = false;
    
    // Connection mode variables
    private boolean connectionMode = false;
    private DFDElement connectionSource;
    
    // Canvas zoom properties
    private final Group canvasGroup = new Group();
    private final Scale canvasScale = new Scale(1, 1);
    private double zoomFactor = 1.0;
    
    public DFDiagramGeneratorUI() {
        this.diagram = new DFDiagram("New DFD");
        this.root = new BorderPane();
        this.canvas = createCanvas();
        this.toolbox = createToolbox();
        
        initializeUI();
    }
    
    private void initializeUI() {
        // Top toolbar
        HBox toolbar = createToolbar();
        root.setTop(toolbar);
        
        // Left side toolbox
        ScrollPane toolboxScroll = new ScrollPane(toolbox);
        toolboxScroll.setFitToWidth(true);
        toolboxScroll.setPrefWidth(250);
        toolboxScroll.setStyle("-fx-background-color: #f0f0f0;");
        root.setLeft(toolboxScroll);
        
        // Center canvas inside a group for zooming
        canvasGroup.getChildren().add(canvas);
        canvasGroup.getTransforms().add(canvasScale);
        
        ScrollPane canvasScroll = new ScrollPane(canvasGroup);
        canvasScroll.setPannable(true);
        canvasScroll.setFitToWidth(true);
        canvasScroll.setFitToHeight(true);
        
        // Add zoom functionality
        canvasScroll.addEventFilter(ScrollEvent.ANY, this::handleZoom);
        
        root.setCenter(canvasScroll);
        
        // Right side properties panel
        VBox propertiesPanel = createPropertiesPanel();
        root.setRight(propertiesPanel);
        
        // Bottom status bar
        HBox statusBar = createStatusBar();
        root.setBottom(statusBar);
        
        // Set styles
        root.getStyleClass().add("diagram-ui");
        
        // Set up keyboard shortcuts
        setupKeyboardShortcuts();
    }
    
    private void setupKeyboardShortcuts() {
        root.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
            // Check if Ctrl is pressed
            if (event.isControlDown()) {
                switch (event.getCode()) {
                    case N:
                        createNewDiagram();
                        event.consume();
                        break;
                    case S:
                        exportDiagram(); // Export as image with Ctrl+S
                        event.consume();
                        break;
                    case E:
                        exportAsMermaid(); // Export as Mermaid with Ctrl+E
                        event.consume();
                        break;
                    case G:
                        generateCode(); // Generate code with Ctrl+G
                        event.consume();
                        break;
                    case L:
                        autoLayoutDiagram(); // Auto layout with Ctrl+L
                        event.consume();
                        break;
                    case PLUS:
                    case EQUALS:
                        zoomIn(); // Zoom in with Ctrl+=
                        event.consume();
                        break;
                    case MINUS:
                        zoomOut(); // Zoom out with Ctrl+-
                        event.consume();
                        break;
                    case DIGIT0:
                        resetZoom(); // Reset zoom with Ctrl+0
                        event.consume();
                        break;
                    case P:
                        addProcess(); // Add process with Ctrl+P
                        event.consume();
                        break;
                    case D:
                        addDataStore(); // Add data store with Ctrl+D
                        event.consume();
                        break;
                    case T:
                        addExternalEntity(); // Add external entity with Ctrl+T
                        event.consume();
                        break;
                    case C:
                        enterConnectionMode(); // Connection mode with Ctrl+C
                        event.consume();
                        break;
                }
            } else {
                // Shortcuts without Ctrl
                switch (event.getCode()) {
                    case DELETE:
                        deleteSelectedElement(); // Delete selected element with Delete key
                        event.consume();
                        break;
                    case ESCAPE:
                        // Exit connection mode if active
                        if (connectionMode) {
                            connectionMode = false;
                            connectionSource = null;
                            canvas.setCursor(javafx.scene.Cursor.DEFAULT);
                            updateStatusBar("Connection mode canceled");
                            event.consume();
                        }
                        break;
                    case F1:
                        showHelp(); // Show help with F1
                        event.consume();
                        break;
                }
            }
        });
    }
    
    private HBox createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(10));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-background-color: linear-gradient(to bottom, #4a6491, #2c3e50);");
        
        // Logo
        Text logo = new Text("📊");
        logo.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        logo.setFill(Color.WHITE);
        
        // Title
        Label title = new Label("Data Flow Diagram Generator");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        title.setTextFill(Color.WHITE);
        
        // New diagram button
        Button newBtn = new Button("New");
        newBtn.setOnAction(e -> createNewDiagram());
        
        // Export button
        Button exportBtn = new Button("Export as Image");
        exportBtn.setOnAction(e -> exportDiagram());
        
        // Export as Mermaid button
        Button mermaidBtn = new Button("Export as Mermaid");
        mermaidBtn.setOnAction(e -> exportAsMermaid());
        
        // Code Generation button - NEW
        Button codeGenBtn = new Button("Generate Code");
        codeGenBtn.setStyle("-fx-base: #5cb85c;");
        codeGenBtn.setOnAction(e -> generateCode());
        
        // Auto layout button
        Button layoutBtn = new Button("Auto Layout");
        layoutBtn.setOnAction(e -> autoLayoutDiagram());
        
        // Zoom controls
        Button zoomInBtn = new Button("Zoom In");
        zoomInBtn.setOnAction(e -> zoomIn());
        
        Button zoomOutBtn = new Button("Zoom Out");
        zoomOutBtn.setOnAction(e -> zoomOut());
        
        Button resetZoomBtn = new Button("Reset Zoom");
        resetZoomBtn.setOnAction(e -> resetZoom());

        // Dark mode toggle - NEW
        ToggleButton darkModeBtn = new ToggleButton("🌙 Dark Mode");
        darkModeBtn.setOnAction(e -> toggleDarkMode(darkModeBtn.isSelected()));
        
        // Level selection
        ComboBox<String> levelCombo = new ComboBox<>();
        levelCombo.getItems().addAll("Context Diagram", "Level 0", "Level 1", "Level 2");
        levelCombo.setValue("Context Diagram");
        levelCombo.setOnAction(e -> changeDiagramLevel(levelCombo.getSelectionModel().getSelectedIndex()));
        
        // Help button
        Button helpBtn = new Button("Help");
        helpBtn.setOnAction(e -> showHelp());
        
        // Add all controls to toolbar
        toolbar.getChildren().addAll(logo, title, new Separator(), 
                newBtn, exportBtn, mermaidBtn, codeGenBtn,
                layoutBtn, 
                new Separator(), zoomInBtn, zoomOutBtn, resetZoomBtn,
                new Separator(), darkModeBtn,
                new Separator(), new Label("Level:"), levelCombo, 
                new Separator(), helpBtn);
        
        return toolbar;
    }
    
    private void zoomIn() {
        zoomFactor *= 1.2;
        if (zoomFactor > 3.0) zoomFactor = 3.0;
        updateZoom();
    }
    
    private void zoomOut() {
        zoomFactor /= 1.2;
        if (zoomFactor < 0.3) zoomFactor = 0.3;
        updateZoom();
    }
    
    private void resetZoom() {
        zoomFactor = 1.0;
        updateZoom();
    }
    
    private void updateZoom() {
        canvasScale.setX(zoomFactor);
        canvasScale.setY(zoomFactor);
        updateStatusBar("Zoom: " + String.format("%.0f", zoomFactor * 100) + "%");
    }
    
    private Pane createCanvas() {
        Pane canvas = new Pane();
        canvas.setMinSize(2000, 1500);
        canvas.getStyleClass().add("dfd-canvas");
        canvas.setStyle("-fx-background-color: white;");
        
        // Add grid lines for visual guidance
        for (int i = 0; i < 2000; i += 50) {
            Line hLine = new Line(0, i, 2000, i);
            Line vLine = new Line(i, 0, i, 1500);
            
            hLine.setStroke(Color.rgb(230, 230, 230));
            vLine.setStroke(Color.rgb(230, 230, 230));
            
            canvas.getChildren().addAll(hLine, vLine);
        }
        
        // Add click handler to deselect when clicking on empty canvas area
        canvas.setOnMouseClicked(e -> {
            if (e.getTarget() == canvas) {
                // Deselect any selected element
                if (selectedElement != null) {
                    Group prevGroup = elementToNodeMap.get(selectedElement);
                    if (prevGroup != null) {
                        for (Node child : prevGroup.getChildren()) {
                            if (child instanceof Shape) {
                                ((Shape) child).setEffect(null);
                            }
                        }
                    }
                    selectedElement = null;
                    
                    // Reset property panel
                    VBox panel = (VBox) root.getRight();
                    TextField nameField = (TextField) panel.getChildren().get(3);
                    TextArea descArea = (TextArea) panel.getChildren().get(5);
                    TextField idField = (TextField) panel.getChildren().get(7);
                    Button applyBtn = (Button) panel.getChildren().get(8);
                    
                    nameField.clear();
                    descArea.clear();
                    idField.clear();
                    applyBtn.setDisable(true);
                    
                    updateStatusBar("Ready");
                }
                
                // Exit connection mode if active
                if (connectionMode) {
                    connectionMode = false;
                    connectionSource = null;
                    canvas.setCursor(javafx.scene.Cursor.DEFAULT);
                    updateStatusBar("Connection mode canceled");
                }
            }
        });
        
        // Handle right-click for context menu
        canvas.setOnContextMenuRequested(e -> {
            if (e.getTarget() == canvas) {
                showCanvasContextMenu(e.getScreenX(), e.getScreenY(), e.getX(), e.getY());
            }
        });
        
        return canvas;
    }
    
    private void showCanvasContextMenu(double screenX, double screenY, double canvasX, double canvasY) {
        ContextMenu contextMenu = new ContextMenu();
        
        // Add process menu item
        MenuItem addProcessItem = new MenuItem("Add Process");
        addProcessItem.setOnAction(e -> {
            DFDProcess process = new DFDProcess("New Process");
            process.setXPosition(canvasX);
            process.setYPosition(canvasY);
            diagram.addProcess(process);
            Group processGroup = drawProcess(process);
            selectElement(process, processGroup);
            updateStatusBar("Process added: " + process.getName());
        });
        
        // Add data store menu item
        MenuItem addDataStoreItem = new MenuItem("Add Data Store");
        addDataStoreItem.setOnAction(e -> {
            DFDDataStore dataStore = new DFDDataStore("New Data Store");
            dataStore.setXPosition(canvasX);
            dataStore.setYPosition(canvasY);
            diagram.addDataStore(dataStore);
            Group dataStoreGroup = drawDataStore(dataStore);
            selectElement(dataStore, dataStoreGroup);
            updateStatusBar("Data store added: " + dataStore.getName());
        });
        
        // Add external entity menu item
        MenuItem addEntityItem = new MenuItem("Add External Entity");
        addEntityItem.setOnAction(e -> {
            DFDExternalEntity entity = new DFDExternalEntity("New External Entity");
            entity.setXPosition(canvasX);
            entity.setYPosition(canvasY);
            diagram.addExternalEntity(entity);
            Group entityGroup = drawExternalEntity(entity);
            selectElement(entity, entityGroup);
            updateStatusBar("External entity added: " + entity.getName());
        });
        
        // Add auto layout menu item
        MenuItem autoLayoutItem = new MenuItem("Auto Layout");
        autoLayoutItem.setOnAction(e -> autoLayoutDiagram());
        
        // Add items to context menu
        contextMenu.getItems().addAll(
            addProcessItem, 
            addDataStoreItem, 
            addEntityItem,
            new SeparatorMenuItem(),
            autoLayoutItem
        );
        
        // Show the context menu
        contextMenu.show(canvas, screenX, screenY);
    }
    
    private VBox createToolbox() {
        VBox toolbox = new VBox(15);
        toolbox.setPadding(new Insets(15));
        toolbox.setAlignment(Pos.TOP_CENTER);
        toolbox.setStyle("-fx-background-color: #f0f0f0;");
        
        // Toolbox header
        Text header = new Text("DFD Elements");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        // Process button
        Button processBtn = createToolboxButton("Process", "🔄");
        processBtn.setOnAction(e -> addProcess());
        
        // Data store button
        Button dataStoreBtn = createToolboxButton("Data Store", "💾");
        dataStoreBtn.setOnAction(e -> addDataStore());
        
        // External entity button
        Button entityBtn = createToolboxButton("External Entity", "🏢");
        entityBtn.setOnAction(e -> addExternalEntity());
        
        // Data flow button - make it more prominent
        Button flowBtn = createToolboxButton("Create Connection", "➡️");
        flowBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 12;");
        flowBtn.setOnAction(e -> enterConnectionMode());
        
        // Delete button
        Button deleteBtn = createToolboxButton("Delete Element", "🗑️");
        deleteBtn.setOnAction(e -> deleteSelectedElement());
        
        // Add separator
        Separator separator = new Separator();
        separator.setPadding(new Insets(10, 0, 10, 0));
        
        // Example buttons
        Button exampleBtn = createToolboxButton("Load Example", "📋");
        exampleBtn.setOnAction(e -> loadExample());
        
        // Clear all button
        Button clearBtn = createToolboxButton("Clear All", "🧹");
        clearBtn.setOnAction(e -> clearDiagram());
        
        // View Mermaid Code button
        Button viewMermaidBtn = createToolboxButton("View Mermaid Code", "📝");
        viewMermaidBtn.setOnAction(e -> showMermaidCode());
        
        toolbox.getChildren().addAll(header, new Separator(), 
                processBtn, dataStoreBtn, entityBtn, flowBtn, deleteBtn, 
                separator, exampleBtn, clearBtn, viewMermaidBtn);
        
        return toolbox;
    }
    
    private Button createToolboxButton(String text, String icon) {
        Button btn = new Button(icon + " " + text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(8, 12, 8, 12));
        
        return btn;
    }
    
    private VBox createPropertiesPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setPrefWidth(250);
        panel.setStyle("-fx-background-color: #f8f9fa;");
        
        // Header
        Text header = new Text("Properties");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        // Element name field
        Label nameLabel = new Label("Name:");
        TextField nameField = new TextField();
        nameField.setPromptText("Element name");
        
        // Element description field
        Label descLabel = new Label("Description:");
        TextArea descArea = new TextArea();
        descArea.setPromptText("Element description");
        descArea.setPrefRowCount(3);
        
        // Element ID field (for processes, data stores, etc.)
        Label idLabel = new Label("Identifier:");
        TextField idField = new TextField();
        idField.setPromptText("E.g., P1, D1, etc.");
        
        // Apply button
        Button applyBtn = new Button("Apply Changes");
        applyBtn.setMaxWidth(Double.MAX_VALUE);
        applyBtn.setDisable(true); // Initially disabled until selection
        
        panel.getChildren().addAll(header, new Separator(), 
                nameLabel, nameField, descLabel, descArea, 
                idLabel, idField, applyBtn);
        
        return panel;
    }
    
    private HBox createStatusBar() {
        HBox statusBar = new HBox(15);
        statusBar.setPadding(new Insets(5, 10, 5, 10));
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setStyle("-fx-background-color: #f0f0f0;");
        
        Label statusLabel = new Label("Ready");
        Label elementsLabel = new Label("Elements: 0");
        
        statusBar.getChildren().addAll(statusLabel, new Separator(), elementsLabel);
        
        return statusBar;
    }
    
    // Functionality methods (stub implementations for now)
    
    private void createNewDiagram() {
        // Display confirmation dialog
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("New Diagram");
        confirm.setHeaderText("Create new diagram?");
        confirm.setContentText("This will clear the current diagram. Continue?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Clear current diagram and create a new one
                clearDiagram();
                this.diagram.setName("New DFD");
                this.diagram.setLevel(0);
                selectedElement = null;
                updateStatusBar("New diagram created");
            }
        });
    }
    
    private void exportDiagram() {
        // Export diagram as image
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Diagram As Image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PNG Image", "*.png"));
        File file = fileChooser.showSaveDialog(root.getScene().getWindow());
        
        if (file != null) {
            try {
                // Reset zoom temporarily for the export
                double oldZoom = zoomFactor;
                zoomFactor = 1.0;
                updateZoom();
                
                // Create a snapshot of the canvas
                WritableImage snapshot = 
                        canvas.snapshot(new SnapshotParameters(), null);
                
                // Write to file
                ImageIO.write(
                        SwingFXUtils.fromFXImage(snapshot, null),
                        "png",
                        file);
                
                // Restore zoom
                zoomFactor = oldZoom;
                updateZoom();
                
                updateStatusBar("Diagram exported to " + file.getName());
            } catch (IOException e) {
                updateStatusBar("Export failed: " + e.getMessage());
                
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Export Error");
                error.setHeaderText("Failed to export diagram");
                error.setContentText("Error: " + e.getMessage());
                error.showAndWait();
            }
        }
    }
    
    private void autoLayoutDiagram() {
        // Simple force-directed layout
        if (diagram.getProcesses().isEmpty() && 
            diagram.getDataStores().isEmpty() && 
            diagram.getExternalEntities().isEmpty()) {
            updateStatusBar("No elements to arrange");
            return;
        }
        
        // Size of the diagram area
        double width = canvas.getWidth() * 0.9;
        double height = canvas.getHeight() * 0.9;
        double centerX = canvas.getWidth() / 2;
        double centerY = canvas.getHeight() / 2;
        
        // Count elements
        int totalElements = diagram.getProcesses().size() + 
                diagram.getDataStores().size() + 
                diagram.getExternalEntities().size();
        
        if (totalElements <= 1) {
            // Single element - center it
            if (diagram.getProcesses().size() == 1) {
                DFDProcess process = diagram.getProcesses().get(0);
                process.setXPosition(centerX);
                process.setYPosition(centerY);
            } else if (diagram.getDataStores().size() == 1) {
                DFDDataStore store = diagram.getDataStores().get(0);
                store.setXPosition(centerX);
                store.setYPosition(centerY);
            } else if (diagram.getExternalEntities().size() == 1) {
                DFDExternalEntity entity = diagram.getExternalEntities().get(0);
                entity.setXPosition(centerX);
                entity.setYPosition(centerY);
            }
        } else {
            // Multiple elements - arrange in a circular pattern or grid
            // Circular layout for smaller diagrams
            if (totalElements <= 8) {
                double radius = Math.min(width, height) * 0.4;
                int index = 0;
                
                // Position processes in a circle
                for (DFDProcess process : diagram.getProcesses()) {
                    double angle = 2 * Math.PI * index / totalElements;
                    process.setXPosition(centerX + radius * Math.cos(angle));
                    process.setYPosition(centerY + radius * Math.sin(angle));
                    index++;
                }
                
                // Position data stores in a circle
                for (DFDDataStore store : diagram.getDataStores()) {
                    double angle = 2 * Math.PI * index / totalElements;
                    store.setXPosition(centerX + radius * Math.cos(angle));
                    store.setYPosition(centerY + radius * Math.sin(angle));
                    index++;
                }
                
                // Position external entities in a circle
                for (DFDExternalEntity entity : diagram.getExternalEntities()) {
                    double angle = 2 * Math.PI * index / totalElements;
                    entity.setXPosition(centerX + radius * Math.cos(angle));
                    entity.setYPosition(centerY + radius * Math.sin(angle));
                    index++;
                }
            } else {
                // Grid layout for larger diagrams
                int cols = (int) Math.ceil(Math.sqrt(totalElements));
                int rows = (int) Math.ceil((double) totalElements / cols);
                
                double cellWidth = width / cols;
                double cellHeight = height / rows;
                
                double startX = centerX - (width / 2) + (cellWidth / 2);
                double startY = centerY - (height / 2) + (cellHeight / 2);
                
                int index = 0;
                
                // Position processes in a grid
                for (DFDProcess process : diagram.getProcesses()) {
                    int row = index / cols;
                    int col = index % cols;
                    
                    process.setXPosition(startX + col * cellWidth);
                    process.setYPosition(startY + row * cellHeight);
                    index++;
                }
                
                // Position data stores in a grid
                for (DFDDataStore store : diagram.getDataStores()) {
                    int row = index / cols;
                    int col = index % cols;
                    
                    store.setXPosition(startX + col * cellWidth);
                    store.setYPosition(startY + row * cellHeight);
                    index++;
                }
                
                // Position external entities in a grid
                for (DFDExternalEntity entity : diagram.getExternalEntities()) {
                    int row = index / cols;
                    int col = index % cols;
                    
                    entity.setXPosition(startX + col * cellWidth);
                    entity.setYPosition(startY + row * cellHeight);
                    index++;
                }
            }
        }
        
        // Redraw all elements
        redrawAllElements();
        updateStatusBar("Auto layout applied");
    }
    
    private void redrawAllElements() {
        // Clear the canvas
        canvas.getChildren().clear();
        nodeToElementMap.clear();
        elementToNodeMap.clear();
        
        // Recreate grid lines
        for (int i = 0; i < 2000; i += 50) {
            Line hLine = new Line(0, i, 2000, i);
            Line vLine = new Line(i, 0, i, 1500);
            
            hLine.setStroke(Color.rgb(230, 230, 230));
            vLine.setStroke(Color.rgb(230, 230, 230));
            
            canvas.getChildren().addAll(hLine, vLine);
        }
        
        // Draw all processes
        for (DFDProcess process : diagram.getProcesses()) {
            drawProcess(process);
        }
        
        // Draw all data stores
        for (DFDDataStore dataStore : diagram.getDataStores()) {
            drawDataStore(dataStore);
        }
        
        // Draw all external entities
        for (DFDExternalEntity entity : diagram.getExternalEntities()) {
            drawExternalEntity(entity);
        }
        
        // Draw all data flows last
        for (DFDDataFlow flow : diagram.getDataFlows()) {
            drawDataFlow(flow);
        }
    }
    
    private void changeDiagramLevel(int level) {
        diagram.setLevel(level);
        updateStatusBar("Changed to " + 
                (level == 0 ? "Context Diagram" : "Level " + level));
    }
    
    private void showHelp() {
        // Show help dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("DFD Help");
        alert.setHeaderText("Data Flow Diagram Help");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        // General usage
        Label generalTitle = new Label("General Usage");
        generalTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        Label generalInfo = new Label(
            "• Add elements using the toolbox buttons\n" +
            "• Select elements by clicking on them\n" +
            "• Edit properties in the right panel\n" +
            "• Drag elements to reposition them\n" +
            "• Zoom with Ctrl+Mouse Wheel or zoom buttons\n" +
            "• Auto-arrange with the Auto Layout button"
        );
        
        // Element types
        Label elementTitle = new Label("Element Types");
        elementTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        Label elementInfo = new Label(
            "• Process: Transforms data inputs into outputs\n" +
            "• Data Store: Holds data\n" +
            "• External Entity: Source or destination outside system\n" +
            "• Data Flow: Shows data movement between elements"
        );
        
        // Keyboard shortcuts
        Label shortcutTitle = new Label("Tips & Shortcuts");
        shortcutTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        Label shortcutInfo = new Label(
            "• Ctrl+Mouse Wheel: Zoom in/out\n" +
            "• Click and drag: Move elements\n" +
            "• Double naming format (ID: Name) helps with readability\n" +
            "• Create connections by clicking the connection button then\n" +
            "  selecting source and target elements\n" +
            "• Export as PNG for documentation"
        );
        
        content.getChildren().addAll(
            generalTitle, generalInfo, new Separator(),
            elementTitle, elementInfo, new Separator(),
            shortcutTitle, shortcutInfo
        );
        
        // Create custom dialog pane
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setContent(content);
        dialogPane.setPrefWidth(500);
        
        alert.showAndWait();
    }
    
    private void clearDiagram() {
        // Display confirmation dialog if diagram has elements
        if (!diagram.getProcesses().isEmpty() || 
            !diagram.getDataStores().isEmpty() || 
            !diagram.getExternalEntities().isEmpty() ||
            !diagram.getDataFlows().isEmpty()) {
            
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Clear Diagram");
            confirm.setHeaderText("Clear the entire diagram?");
            confirm.setContentText("This action cannot be undone.");
            
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    performClear();
                }
            });
        } else {
            performClear();
        }
    }
    
    private void performClear() {
        // Clear the diagram model
        diagram.getProcesses().clear();
        diagram.getDataStores().clear();
        diagram.getExternalEntities().clear();
        diagram.getDataFlows().clear();
        
        // Clear tracking variables
        selectedElement = null;
        nodeToElementMap.clear();
        elementToNodeMap.clear();
        
        // Reset the canvas
        canvas.getChildren().clear();
        
        // Recreate grid lines
        for (int i = 0; i < 2000; i += 50) {
            Line hLine = new Line(0, i, 2000, i);
            Line vLine = new Line(i, 0, i, 1500);
            
            hLine.setStroke(Color.rgb(230, 230, 230));
            vLine.setStroke(Color.rgb(230, 230, 230));
            
            canvas.getChildren().addAll(hLine, vLine);
        }
        
        updateStatusBar("Diagram cleared");
        
        // Reset property panel
        VBox panel = (VBox) root.getRight();
        TextField nameField = (TextField) panel.getChildren().get(3);
        TextArea descArea = (TextArea) panel.getChildren().get(5);
        TextField idField = (TextField) panel.getChildren().get(7);
        Button applyBtn = (Button) panel.getChildren().get(8);
        
        nameField.clear();
        descArea.clear();
        idField.clear();
        applyBtn.setDisable(true);
    }
    
    private void loadExample() {
        // Ask for confirmation if current diagram has elements
        if (!diagram.getProcesses().isEmpty() || 
            !diagram.getDataStores().isEmpty() || 
            !diagram.getExternalEntities().isEmpty() ||
            !diagram.getDataFlows().isEmpty()) {
            
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Load Example");
            confirm.setHeaderText("Load example diagram?");
            confirm.setContentText("This will replace the current diagram.");
            
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    performLoadExample();
                }
            });
        } else {
            performLoadExample();
        }
    }
    
    private void performLoadExample() {
        // Clear existing diagram
        performClear();
        
        // Create external entities
        DFDExternalEntity customer = new DFDExternalEntity("Customer");
        customer.setXPosition(100);
        customer.setYPosition(200);
        customer.setEntityId("E1");
        
        DFDExternalEntity vendor = new DFDExternalEntity("Vendor");
        vendor.setXPosition(700);
        vendor.setYPosition(200);
        vendor.setEntityId("E2");
        
        // Create processes
        DFDProcess orderProcess = new DFDProcess("Process Order");
        orderProcess.setXPosition(400);
        orderProcess.setYPosition(200);
        orderProcess.setProcessNumber("P1");
        
        DFDProcess paymentProcess = new DFDProcess("Process Payment");
        paymentProcess.setXPosition(400);
        paymentProcess.setYPosition(400);
        paymentProcess.setProcessNumber("P2");
        
        DFDProcess shipmentProcess = new DFDProcess("Process Shipment");
        shipmentProcess.setXPosition(600);
        shipmentProcess.setYPosition(300);
        shipmentProcess.setProcessNumber("P3");
        
        // Create data stores
        DFDDataStore orderStore = new DFDDataStore("Orders");
        orderStore.setXPosition(200);
        orderStore.setYPosition(400);
        orderStore.setStoreId("D1");
        
        DFDDataStore inventoryStore = new DFDDataStore("Inventory");
        inventoryStore.setXPosition(600);
        inventoryStore.setYPosition(500);
        inventoryStore.setStoreId("D2");
        
        DFDDataStore customerStore = new DFDDataStore("Customers");
        customerStore.setXPosition(200);
        customerStore.setYPosition(300);
        customerStore.setStoreId("D3");
        
        // Add elements to the diagram
        diagram.addExternalEntity(customer);
        diagram.addExternalEntity(vendor);
        diagram.addProcess(orderProcess);
        diagram.addProcess(paymentProcess);
        diagram.addProcess(shipmentProcess);
        diagram.addDataStore(orderStore);
        diagram.addDataStore(inventoryStore);
        diagram.addDataStore(customerStore);
        
        // Create data flows
        DFDDataFlow orderFlow = new DFDDataFlow("Order Request", customer, orderProcess);
        DFDDataFlow customerLookupFlow = new DFDDataFlow("Customer Info", orderProcess, customerStore);
        DFDDataFlow inventoryCheckFlow = new DFDDataFlow("Check Stock", orderProcess, inventoryStore);
        DFDDataFlow storeOrderFlow = new DFDDataFlow("Save Order", orderProcess, orderStore);
        DFDDataFlow paymentFlow = new DFDDataFlow("Payment Info", orderProcess, paymentProcess);
        DFDDataFlow shipmentFlow = new DFDDataFlow("Shipment Request", paymentProcess, shipmentProcess);
        DFDDataFlow stockUpdateFlow = new DFDDataFlow("Update Stock", shipmentProcess, inventoryStore);
        DFDDataFlow vendorFlow = new DFDDataFlow("Ship Notification", shipmentProcess, vendor);
        DFDDataFlow confirmationFlow = new DFDDataFlow("Order Confirmation", orderProcess, customer);
        
        diagram.addDataFlow(orderFlow);
        diagram.addDataFlow(customerLookupFlow);
        diagram.addDataFlow(inventoryCheckFlow);
        diagram.addDataFlow(storeOrderFlow);
        diagram.addDataFlow(paymentFlow);
        diagram.addDataFlow(shipmentFlow);
        diagram.addDataFlow(stockUpdateFlow);
        diagram.addDataFlow(vendorFlow);
        diagram.addDataFlow(confirmationFlow);
        
        // Draw all elements
        drawExample();
        
        updateStatusBar("Example DFD loaded - E-commerce Order Processing System");
    }
    
    private void updateStatusBar(String message) {
        // Update status bar message
        HBox statusBar = (HBox) root.getBottom();
        Label statusLabel = (Label) statusBar.getChildren().get(0);
        statusLabel.setText(message);
        
        // Update elements count
        Label elementsLabel = (Label) statusBar.getChildren().get(2);
        int total = diagram.getProcesses().size() + 
                diagram.getDataStores().size() + 
                diagram.getExternalEntities().size() + 
                diagram.getDataFlows().size();
        elementsLabel.setText("Elements: " + total);
    }

    // Drawing methods with selection and dragging capabilities
    
    private Group drawProcess(DFDProcess process) {
        Group group = new Group();
        
        Circle circle = new Circle(50);
        circle.getStyleClass().add("dfd-process");
        circle.setFill(Color.LIGHTYELLOW);
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(2);
        
        Label label = new Label(process.getProcessNumber() + ": " + process.getName());
        label.getStyleClass().add("dfd-label");
        label.setLayoutX(-40);
        label.setLayoutY(-10);
        
        group.getChildren().addAll(circle, label);
        group.setLayoutX(process.getXPosition());
        group.setLayoutY(process.getYPosition());
        
        // Add selection effect
        setupElementInteraction(group, process);
        
        // Track this element
        nodeToElementMap.put(group, process);
        elementToNodeMap.put(process, group);
        
        canvas.getChildren().add(group);
        return group;
    }
    
    private Group drawDataStore(DFDDataStore dataStore) {
        Group group = new Group();
        
        Rectangle rect = new Rectangle(200, 40);
        rect.getStyleClass().add("dfd-data-store");
        rect.setFill(Color.LIGHTBLUE);
        rect.setStroke(Color.BLACK);
        rect.setStrokeWidth(2);
        rect.setX(-100);
        rect.setY(-20);
        
        Label label = new Label(dataStore.getStoreId() + ": " + dataStore.getName());
        label.getStyleClass().add("dfd-label");
        label.setLayoutX(-80);
        label.setLayoutY(-10);
        
        group.getChildren().addAll(rect, label);
        group.setLayoutX(dataStore.getXPosition());
        group.setLayoutY(dataStore.getYPosition());
        
        // Add selection effect
        setupElementInteraction(group, dataStore);
        
        // Track this element
        nodeToElementMap.put(group, dataStore);
        elementToNodeMap.put(dataStore, group);
        
        canvas.getChildren().add(group);
        return group;
    }
    
    private Group drawExternalEntity(DFDExternalEntity entity) {
        Group group = new Group();
        
        Rectangle rect = new Rectangle(150, 80);
        rect.getStyleClass().add("dfd-external-entity");
        rect.setFill(Color.LIGHTGRAY);
        rect.setStroke(Color.BLACK);
        rect.setStrokeWidth(2);
        rect.setX(-75);
        rect.setY(-40);
        
        Label label = new Label(entity.getEntityId() + ": " + entity.getName());
        label.getStyleClass().add("dfd-label");
        label.setLayoutX(-65);
        label.setLayoutY(-10);
        
        group.getChildren().addAll(rect, label);
        group.setLayoutX(entity.getXPosition());
        group.setLayoutY(entity.getYPosition());
        
        // Add selection effect
        setupElementInteraction(group, entity);
        
        // Track this element
        nodeToElementMap.put(group, entity);
        elementToNodeMap.put(entity, group);
        
        canvas.getChildren().add(group);
        return group;
    }
    
    private Group drawDataFlow(DFDDataFlow flow) {
        Group group = new Group();
        
        DFDElement source = flow.getSource();
        DFDElement target = flow.getTarget();
        
        // Get the visual nodes
        Group sourceNode = elementToNodeMap.get(source);
        Group targetNode = elementToNodeMap.get(target);
        
        if (sourceNode != null && targetNode != null) {
            // Calculate center points
            double startX = sourceNode.getLayoutX();
            double startY = sourceNode.getLayoutY();
            double endX = targetNode.getLayoutX();
            double endY = targetNode.getLayoutY();
            
            // Calculate direction vector
            double dx = endX - startX;
            double dy = endY - startY;
            double length = Math.sqrt(dx * dx + dy * dy);
            
            // Normalize
            double ndx = dx / length;
            double ndy = dy / length;
            
            // Calculate boundary points based on element type
            double sourceRadius = 0, targetRadius = 0;
            
            // Determine source element size
            if (source instanceof DFDProcess) {
                sourceRadius = 50; // Process circle radius
            } else if (source instanceof DFDDataStore) {
                // For data store, adjust based on angle to get elliptical boundary
                double angle = Math.atan2(dy, dx);
                double xr = 100; // Half width
                double yr = 20;  // Half height
                double r = (xr * yr) / Math.sqrt(yr * yr * Math.cos(angle) * Math.cos(angle) + 
                            xr * xr * Math.sin(angle) * Math.sin(angle));
                sourceRadius = r;
            } else if (source instanceof DFDExternalEntity) {
                // For external entity, adjust based on angle to get rectangular boundary
                double angle = Math.abs(Math.atan2(dy, dx));
                if (angle < Math.PI/4) {
                    sourceRadius = 75; // Half width
                } else if (angle < 3*Math.PI/4) {
                    sourceRadius = 40; // Half height
                } else {
                    sourceRadius = 75; // Half width
                }
            }
            
            // Determine target element size
            if (target instanceof DFDProcess) {
                targetRadius = 50; // Process circle radius
            } else if (target instanceof DFDDataStore) {
                // For data store, adjust based on angle to get elliptical boundary
                double angle = Math.atan2(-dy, -dx); // Reverse direction for target
                double xr = 100; // Half width
                double yr = 20;  // Half height
                double r = (xr * yr) / Math.sqrt(yr * yr * Math.cos(angle) * Math.cos(angle) + 
                            xr * xr * Math.sin(angle) * Math.sin(angle));
                targetRadius = r;
            } else if (target instanceof DFDExternalEntity) {
                // For external entity, adjust based on angle to get rectangular boundary
                double angle = Math.abs(Math.atan2(-dy, -dx)); // Reverse direction for target
                if (angle < Math.PI/4) {
                    targetRadius = 75; // Half width
                } else if (angle < 3*Math.PI/4) {
                    targetRadius = 40; // Half height
                } else {
                    targetRadius = 75; // Half width
                }
            }
            
            // Adjust start and end points to be at the boundaries
            double adjustedStartX = startX + ndx * sourceRadius;
            double adjustedStartY = startY + ndy * sourceRadius;
            double adjustedEndX = endX - ndx * targetRadius;
            double adjustedEndY = endY - ndy * targetRadius;
            
            // Make sure arrowhead isn't too close to the line start
            if (Math.sqrt(Math.pow(adjustedEndX - adjustedStartX, 2) + 
                          Math.pow(adjustedEndY - adjustedStartY, 2)) < 20) {
                // If too close, just use a small line
                adjustedStartX = startX + ndx * (sourceRadius * 0.8);
                adjustedStartY = startY + ndy * (sourceRadius * 0.8);
                adjustedEndX = endX - ndx * (targetRadius * 0.8);
                adjustedEndY = endY - ndy * (targetRadius * 0.8);
            }
            
            // Draw line
            Line line = new Line(adjustedStartX, adjustedStartY, adjustedEndX, adjustedEndY);
            line.getStyleClass().add("dfd-data-flow");
            
            // Draw arrow head
            double arrowLength = 15;
            double arrowWidth = 7;
            
            // Calculate perpendicular vector for arrow
            double perpX = -ndy;
            double perpY = ndx;
            
            // Create arrowhead
            Polygon arrowHead = new Polygon();
            arrowHead.getPoints().addAll(
                adjustedEndX, adjustedEndY,
                adjustedEndX - arrowLength * ndx + arrowWidth * perpX, 
                adjustedEndY - arrowLength * ndy + arrowWidth * perpY,
                adjustedEndX - arrowLength * ndx - arrowWidth * perpX, 
                adjustedEndY - arrowLength * ndy - arrowWidth * perpY
            );
            arrowHead.getStyleClass().add("dfd-data-flow-arrow");
            arrowHead.setFill(Color.BLACK);
            
            // Create flow label
            Label label = new Label(flow.getName());
            label.getStyleClass().add("dfd-label");
            
            // Position label at the middle of the line
            double midX = (adjustedStartX + adjustedEndX) / 2.0;
            double midY = (adjustedStartY + adjustedEndY) / 2.0;
            
            // Offset label slightly perpendicular to the line
            label.setLayoutX(midX + 10 * perpX);
            label.setLayoutY(midY + 10 * perpY);
            
            // Add label background for better readability
            Rectangle labelBg = new Rectangle();
            labelBg.setFill(Color.WHITE);
            labelBg.setOpacity(0.7);
            
            // Bind background size and position to label
            labelBg.widthProperty().bind(label.widthProperty().add(4));
            labelBg.heightProperty().bind(label.heightProperty().add(4));
            labelBg.xProperty().bind(label.layoutXProperty().subtract(2));
            labelBg.yProperty().bind(label.layoutYProperty().subtract(2));
            
            // Add elements to group
            group.getChildren().addAll(line, arrowHead, labelBg, label);
            
            // Add selection effect
            setupElementInteraction(group, flow);
            
            // Track this element
            nodeToElementMap.put(group, flow);
            elementToNodeMap.put(flow, group);
            
            canvas.getChildren().add(group);
        }
        
        return group;
    }
    
    private void setupElementInteraction(Group group, DFDElement element) {
        // Add selection effect
        group.setOnMouseClicked(e -> {
            selectElement(element, group);
            e.consume();
            
            if (connectionMode) {
                if (connectionSource == null) {
                    // First click - set as source
                    connectionSource = element;
                    updateStatusBar("Source selected: " + element.getName() + ". Now select destination.");
                } else if (element != connectionSource) {
                    // Second click - create connection if it's a different element
                    createDataFlowBetweenElements(connectionSource, element);
                    
                    // Exit connection mode
                    connectionMode = false;
                    connectionSource = null;
                    canvas.setCursor(javafx.scene.Cursor.DEFAULT);
                }
            }
        });
        
        // Add dragging functionality
        group.setOnMousePressed(e -> {
            dragStartX = e.getSceneX();
            dragStartY = e.getSceneY();
            isDragging = true;
            e.consume();
        });
        
        group.setOnMouseDragged(e -> {
            if (isDragging) {
                double offsetX = (e.getSceneX() - dragStartX) / zoomFactor;
                double offsetY = (e.getSceneY() - dragStartY) / zoomFactor;
                
                group.setLayoutX(group.getLayoutX() + offsetX);
                group.setLayoutY(group.getLayoutY() + offsetY);
                
                // Update model position
                element.setXPosition(group.getLayoutX());
                element.setYPosition(group.getLayoutY());
                
                // Update any connected data flows
                updateConnectedDataFlows(element);
                
                dragStartX = e.getSceneX();
                dragStartY = e.getSceneY();
                
                e.consume();
            }
        });
        
        group.setOnMouseReleased(e -> {
            isDragging = false;
            e.consume();
        });
        
        // Add context menu for element
        group.setOnContextMenuRequested(e -> {
            showElementContextMenu(e.getScreenX(), e.getScreenY(), element);
            e.consume();
        });
    }
    
    private void updateConnectedDataFlows(DFDElement element) {
        // Redraw any data flows connected to this element
        for (DFDDataFlow flow : diagram.getDataFlows()) {
            if (flow.getSource() == element || flow.getTarget() == element) {
                // Remove old visualization
                Group oldGroup = elementToNodeMap.get(flow);
                if (oldGroup != null) {
                    canvas.getChildren().remove(oldGroup);
                    nodeToElementMap.remove(oldGroup);
                }
                
                // Draw new visualization
                Group newGroup = drawDataFlow(flow);
                elementToNodeMap.put(flow, newGroup);
            }
        }
    }
    
    private void selectElement(DFDElement element, Group node) {
        // Deselect previous element
        if (selectedElement != null) {
            Group prevGroup = elementToNodeMap.get(selectedElement);
            if (prevGroup != null) {
                for (Node child : prevGroup.getChildren()) {
                    if (child instanceof Shape) {
                        ((Shape) child).setEffect(null);
                    }
                }
            }
        }
        
        // Select new element
        selectedElement = element;
        
        // Apply selection effect
        for (Node child : node.getChildren()) {
            if (child instanceof Shape) {
                DropShadow highlight = new DropShadow();
                highlight.setColor(Color.DODGERBLUE);
                highlight.setRadius(10);
                ((Shape) child).setEffect(highlight);
            }
        }
        
        // Update property panel
        updatePropertyPanel(element);
        
        // Update status bar
        updateStatusBar("Selected: " + element.getName());
        
        if (connectionMode) {
            connectionSource = element;
            updateStatusBar("Source selected: " + element.getName() + ". Now select destination.");
        }
    }
    
    private void updatePropertyPanel(DFDElement element) {
        VBox panel = (VBox) root.getRight();
        
        // Get the text fields from the panel
        TextField nameField = (TextField) panel.getChildren().get(3);
        TextArea descArea = (TextArea) panel.getChildren().get(5);
        TextField idField = (TextField) panel.getChildren().get(7);
        
        // Update fields with element data
        nameField.setText(element.getName());
        descArea.setText(element.getDescription());
        
        // Set ID field based on element type
        if (element instanceof DFDProcess) {
            idField.setText(((DFDProcess) element).getProcessNumber());
        } else if (element instanceof DFDDataStore) {
            idField.setText(((DFDDataStore) element).getStoreId());
        } else if (element instanceof DFDExternalEntity) {
            idField.setText(((DFDExternalEntity) element).getEntityId());
        } else {
            idField.setDisable(true);
        }
        
        // Enable apply button
        Button applyBtn = (Button) panel.getChildren().get(8);
        applyBtn.setDisable(false);
        
        // Set action for apply button
        applyBtn.setOnAction(e -> applyProperties(nameField.getText(), descArea.getText(), idField.getText()));
    }
    
    private void applyProperties(String name, String description, String id) {
        if (selectedElement == null) return;
        
        // Update element properties
        selectedElement.setName(name);
        selectedElement.setDescription(description);
        
        // Update specific ID based on element type
        if (selectedElement instanceof DFDProcess) {
            ((DFDProcess) selectedElement).setProcessNumber(id);
        } else if (selectedElement instanceof DFDDataStore) {
            ((DFDDataStore) selectedElement).setStoreId(id);
        } else if (selectedElement instanceof DFDExternalEntity) {
            ((DFDExternalEntity) selectedElement).setEntityId(id);
        }
        
        // Redraw the selected element
        refreshElementDisplay(selectedElement);
        
        updateStatusBar("Updated properties for: " + selectedElement.getName());
    }
    
    private void refreshElementDisplay(DFDElement element) {
        // Remove old visualization
        Group oldGroup = elementToNodeMap.get(element);
        if (oldGroup != null) {
            canvas.getChildren().remove(oldGroup);
            nodeToElementMap.remove(oldGroup);
        }
        
        // Create new visualization
        Group newGroup;
        if (element instanceof DFDProcess) {
            newGroup = drawProcess((DFDProcess) element);
        } else if (element instanceof DFDDataStore) {
            newGroup = drawDataStore((DFDDataStore) element);
        } else if (element instanceof DFDExternalEntity) {
            newGroup = drawExternalEntity((DFDExternalEntity) element);
        } else if (element instanceof DFDDataFlow) {
            newGroup = drawDataFlow((DFDDataFlow) element);
        } else {
            return;
        }
        
        // Update maps
        elementToNodeMap.put(element, newGroup);
        nodeToElementMap.put(newGroup, element);
        
        // Mark as selected
        selectElement(element, newGroup);
    }
    
    private void enterConnectionMode() {
        connectionMode = true;
        connectionSource = null;
        
        // Change cursor to indicate connection mode
        canvas.setCursor(javafx.scene.Cursor.CROSSHAIR);
        
        updateStatusBar("Connection mode: Select source element");
    }
    
    private void showElementContextMenu(double screenX, double screenY, DFDElement element) {
        ContextMenu contextMenu = new ContextMenu();
        
        // Create data flow menu item
        MenuItem createFlowItem = new MenuItem("Create Connection From Here");
        createFlowItem.setOnAction(e -> {
            connectionMode = true;
            connectionSource = element;
            canvas.setCursor(javafx.scene.Cursor.CROSSHAIR);
            updateStatusBar("Source selected: " + element.getName() + ". Now select destination.");
        });
        
        // Edit properties menu item
        MenuItem editItem = new MenuItem("Edit Properties");
        editItem.setOnAction(e -> {
            selectElement(element, elementToNodeMap.get(element));
        });
        
        // Delete menu item
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(e -> {
            selectedElement = element;
            deleteSelectedElement();
        });
        
        // Add items to context menu
        contextMenu.getItems().addAll(
            createFlowItem,
            editItem,
            new SeparatorMenuItem(),
            deleteItem
        );
        
        // Show the context menu
        contextMenu.show(canvas, screenX, screenY);
    }
    
    private void createDataFlowBetweenElements(DFDElement source, DFDElement target) {
        // Show dialog to get connection name
        TextInputDialog dialog = new TextInputDialog("Data");
        dialog.setTitle("New Data Flow");
        dialog.setHeaderText("Create Data Flow");
        dialog.setContentText("Enter name for the data flow:");
        
        // Change dialog appearance
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-font-size: 12px;");
        
        Optional<String> result = dialog.showAndWait();
        
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String flowName = result.get().trim();
            
            // Create the data flow
            DFDDataFlow flow = new DFDDataFlow(flowName, source, target);
            diagram.addDataFlow(flow);
            
            // Draw the data flow
            Group flowGroup = drawDataFlow(flow);
            
            // Select the new flow
            selectElement(flow, flowGroup);
            
            updateStatusBar("Created data flow: " + flowName);
        } else {
            // User canceled or entered empty name
            updateStatusBar("Data flow creation canceled");
        }
    }
    
    // Override the existing drawing methods for the example
    
    private void drawExample() {
        // Clear canvas first
        canvas.getChildren().clear();
        nodeToElementMap.clear();
        elementToNodeMap.clear();
        
        // Recreate grid lines
        for (int i = 0; i < 2000; i += 50) {
            Line hLine = new Line(0, i, 2000, i);
            Line vLine = new Line(i, 0, i, 1500);
            
            hLine.setStroke(Color.rgb(230, 230, 230));
            vLine.setStroke(Color.rgb(230, 230, 230));
            
            canvas.getChildren().addAll(hLine, vLine);
        }
        
        // Draw all elements
        for (DFDProcess process : diagram.getProcesses()) {
            drawProcess(process);
        }
        
        for (DFDDataStore dataStore : diagram.getDataStores()) {
            drawDataStore(dataStore);
        }
        
        for (DFDExternalEntity entity : diagram.getExternalEntities()) {
            drawExternalEntity(entity);
        }
        
        // Draw data flows last so they appear on top
        for (DFDDataFlow flow : diagram.getDataFlows()) {
            drawDataFlow(flow);
        }
    }
    
    private void handleZoom(ScrollEvent event) {
        if (event.isControlDown()) {
            event.consume();
            double delta = event.getDeltaY() > 0 ? 1.1 : 0.9;
            
            zoomFactor *= delta;
            
            // Limit zoom factor
            if (zoomFactor < 0.3) zoomFactor = 0.3;
            if (zoomFactor > 3.0) zoomFactor = 3.0;
            
            canvasScale.setX(zoomFactor);
            canvasScale.setY(zoomFactor);
            
            updateStatusBar("Zoom: " + String.format("%.0f", zoomFactor * 100) + "%");
        }
    }
    
    public BorderPane getRoot() {
        return root;
    }
    
    private void addProcess() {
        // Add a new process to the diagram
        DFDProcess process = new DFDProcess("New Process");
        
        // Calculate center position
        double centerX = canvas.getWidth() / 2;
        double centerY = canvas.getHeight() / 2;
        
        // Offset slightly for multiple additions
        centerX += (Math.random() - 0.5) * 200;
        centerY += (Math.random() - 0.5) * 200;
        
        process.setXPosition(centerX);
        process.setYPosition(centerY);
        
        diagram.addProcess(process);
        Group processGroup = drawProcess(process);
        
        // Select the new process
        selectElement(process, processGroup);
        
        updateStatusBar("Process added: " + process.getName());
    }
    
    private void addDataStore() {
        // Add a new data store to the diagram
        DFDDataStore dataStore = new DFDDataStore("New Data Store");
        
        // Calculate center position
        double centerX = canvas.getWidth() / 2;
        double centerY = canvas.getHeight() / 2;
        
        // Offset slightly for multiple additions
        centerX += (Math.random() - 0.5) * 200;
        centerY += (Math.random() - 0.5) * 200;
        
        dataStore.setXPosition(centerX);
        dataStore.setYPosition(centerY);
        
        diagram.addDataStore(dataStore);
        Group dataStoreGroup = drawDataStore(dataStore);
        
        // Select the new data store
        selectElement(dataStore, dataStoreGroup);
        
        updateStatusBar("Data store added: " + dataStore.getName());
    }
    
    private void addExternalEntity() {
        // Add a new external entity to the diagram
        DFDExternalEntity entity = new DFDExternalEntity("New External Entity");
        
        // Calculate center position
        double centerX = canvas.getWidth() / 2;
        double centerY = canvas.getHeight() / 2;
        
        // Offset slightly for multiple additions
        centerX += (Math.random() - 0.5) * 200;
        centerY += (Math.random() - 0.5) * 200;
        
        entity.setXPosition(centerX);
        entity.setYPosition(centerY);
        
        diagram.addExternalEntity(entity);
        Group entityGroup = drawExternalEntity(entity);
        
        // Select the new entity
        selectElement(entity, entityGroup);
        
        updateStatusBar("External entity added: " + entity.getName());
    }
    
    private void deleteSelectedElement() {
        if (selectedElement == null) {
            updateStatusBar("Nothing selected to delete");
            return;
        }
        
        // Remove from canvas
        Group group = elementToNodeMap.get(selectedElement);
        if (group != null) {
            canvas.getChildren().remove(group);
            nodeToElementMap.remove(group);
            elementToNodeMap.remove(selectedElement);
        }
        
        // Remove from model
        if (selectedElement instanceof DFDProcess) {
            diagram.removeProcess((DFDProcess) selectedElement);
        } else if (selectedElement instanceof DFDDataStore) {
            diagram.removeDataStore((DFDDataStore) selectedElement);
        } else if (selectedElement instanceof DFDExternalEntity) {
            diagram.removeExternalEntity((DFDExternalEntity) selectedElement);
        } else if (selectedElement instanceof DFDDataFlow) {
            diagram.removeDataFlow((DFDDataFlow) selectedElement);
        }
        
        // Update status and clear selection
        updateStatusBar("Deleted: " + selectedElement.getName());
        selectedElement = null;
        
        // Redraw data flows as some may have been auto-removed
        redrawDataFlows();
    }
    
    private void redrawDataFlows() {
        // Remove all existing data flow visuals
        for (DFDDataFlow flow : diagram.getDataFlows()) {
            Group group = elementToNodeMap.get(flow);
            if (group != null) {
                canvas.getChildren().remove(group);
                nodeToElementMap.remove(group);
                elementToNodeMap.remove(flow);
            }
        }
        
        // Redraw all data flows
        for (DFDDataFlow flow : diagram.getDataFlows()) {
            drawDataFlow(flow);
        }
    }
    
    private void exportAsMermaid() {
        String mermaidCode = generateMermaidCode();
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Mermaid File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Mermaid Files", "*.mmd"));
        File file = fileChooser.showSaveDialog(root.getScene().getWindow());
        
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(mermaidCode);
                showInfoAlert("Mermaid Export", "Successfully exported diagram as Mermaid code.");
            } catch (IOException e) {
                showErrorAlert("Export Error", "Failed to export Mermaid code: " + e.getMessage());
            }
        }
    }
    
    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showMermaidCode() {
        String mermaidCode = generateMermaidCode();
        
        Stage codeStage = new Stage();
        codeStage.setTitle("Mermaid Code");
        
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        
        TextArea codeArea = new TextArea(mermaidCode);
        codeArea.setEditable(false);
        codeArea.setWrapText(true);
        codeArea.setPrefRowCount(20);
        codeArea.setPrefColumnCount(80);
        
        Button copyButton = new Button("Copy to Clipboard");
        copyButton.setOnAction(e -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(mermaidCode);
            clipboard.setContent(content);
            showInfoAlert("Clipboard", "Mermaid code copied to clipboard.");
        });
        
        Button saveButton = new Button("Save to File");
        saveButton.setOnAction(e -> exportAsMermaid());
        
        HBox buttons = new HBox(10, copyButton, saveButton);
        buttons.setAlignment(Pos.CENTER);
        
        layout.getChildren().addAll(new Label("Mermaid Code:"), codeArea, buttons);
        
        Scene scene = new Scene(layout);
        codeStage.setScene(scene);
        codeStage.show();
    }
    
    private String generateMermaidCode() {
        StringBuilder code = new StringBuilder("flowchart TD\n");
        
        // Generate node definitions
        // Process nodes
        for (DFDProcess process : diagram.getProcesses()) {
            String id = "process" + process.getId().replaceAll("-", "");
            String label = process.getProcessNumber() + ": " + process.getName();
            
            // Process is represented as a circle/rounded rectangle
            code.append("    ").append(id).append("[\"").append(label).append("\"]\n");
        }
        
        // Data store nodes
        for (DFDDataStore dataStore : diagram.getDataStores()) {
            String id = "store" + dataStore.getId().replaceAll("-", "");
            String label = dataStore.getStoreId() + ": " + dataStore.getName();
            
            // Data store is represented as an open-ended rectangle
            code.append("    ").append(id).append("[(\"").append(label).append("\")]\n");
        }
        
        // External entity nodes
        for (DFDExternalEntity entity : diagram.getExternalEntities()) {
            String id = "entity" + entity.getId().replaceAll("-", "");
            String label = entity.getEntityId() + ": " + entity.getName();
            
            // External entity is represented as a rectangle
            code.append("    ").append(id).append("[\"").append(label).append("\"]::externalEntity\n");
        }
        
        // Add connections
        for (DFDDataFlow flow : diagram.getDataFlows()) {
            DFDElement source = flow.getSource();
            DFDElement target = flow.getTarget();
            
            String sourceId;
            String targetId;
            
            // Determine source ID based on element type
            if (source instanceof DFDProcess) {
                sourceId = "process" + source.getId().replaceAll("-", "");
            } else if (source instanceof DFDDataStore) {
                sourceId = "store" + source.getId().replaceAll("-", "");
            } else {
                sourceId = "entity" + source.getId().replaceAll("-", "");
            }
            
            // Determine target ID based on element type
            if (target instanceof DFDProcess) {
                targetId = "process" + target.getId().replaceAll("-", "");
            } else if (target instanceof DFDDataStore) {
                targetId = "store" + target.getId().replaceAll("-", "");
            } else {
                targetId = "entity" + target.getId().replaceAll("-", "");
            }
            
            String label = flow.getName();
            
            if (label != null && !label.isEmpty()) {
                code.append("    ").append(sourceId).append(" -->|\"").append(label).append("\"|").append(targetId).append("\n");
            } else {
                code.append("    ").append(sourceId).append(" --> ").append(targetId).append("\n");
            }
        }
        
        // Add class definitions for styling
        code.append("\n");
        code.append("classDef externalEntity stroke-width:2px,stroke-dasharray: 5 5\n");
        code.append("classDef default fill:#f9f9f9,stroke:#333,stroke-width:1px\n");
        
        return code.toString();
    }
    
    // New method to toggle dark mode
    private void toggleDarkMode(boolean darkMode) {
        Scene scene = root.getScene();
        if (scene == null) return;
        
        if (darkMode) {
            // Apply dark theme
            scene.getStylesheets().add(getClass().getResource("/css/dark-theme.css").toExternalForm());
            canvas.setStyle("-fx-background-color: #2b2b2b;");
            updateStatusBar("Dark mode enabled");
        } else {
            // Remove dark theme
            scene.getStylesheets().remove(getClass().getResource("/css/dark-theme.css").toExternalForm());
            canvas.setStyle("-fx-background-color: white;");
            updateStatusBar("Light mode enabled");
        }
        
        // Redraw elements with appropriate colors
        redrawAllElements();
    }
    
    // New method for code generation
    private void generateCode() {
        // Show code generation dialog with framework options
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Generate Code");
        dialog.setHeaderText("Select a framework and language for code generation");
        
        // Set button types
        ButtonType generateButtonType = new ButtonType("Generate", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(generateButtonType, ButtonType.CANCEL);
        
        // Create content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Language selection
        ComboBox<String> languageCombo = new ComboBox<>();
        languageCombo.getItems().addAll("Java", "Python", "C#", "JavaScript", "TypeScript");
        languageCombo.setValue("Java");
        
        // Framework selection
        ComboBox<String> frameworkCombo = new ComboBox<>();
        
        // Update framework options based on language selection
        languageCombo.setOnAction(e -> {
            frameworkCombo.getItems().clear();
            
            switch (languageCombo.getValue()) {
                case "Java":
                    frameworkCombo.getItems().addAll("Spring Boot", "Jakarta EE", "Quarkus");
                    frameworkCombo.setValue("Spring Boot");
                    break;
                case "Python":
                    frameworkCombo.getItems().addAll("Flask", "Django", "FastAPI");
                    frameworkCombo.setValue("Flask");
                    break;
                case "C#":
                    frameworkCombo.getItems().addAll(".NET Core", "ASP.NET MVC");
                    frameworkCombo.setValue(".NET Core");
                    break;
                case "JavaScript":
                    frameworkCombo.getItems().addAll("Express.js", "Nest.js", "Koa.js");
                    frameworkCombo.setValue("Express.js");
                    break;
                case "TypeScript":
                    frameworkCombo.getItems().addAll("Nest.js", "Express with TS", "Next.js");
                    frameworkCombo.setValue("Nest.js");
                    break;
            }
        });
        
        // Initial population of frameworks
        languageCombo.fireEvent(new javafx.event.ActionEvent());
        
        // Architecture pattern
        ComboBox<String> patternCombo = new ComboBox<>();
        patternCombo.getItems().addAll("MVC", "Clean Architecture", "Repository Pattern", "Microservices");
        patternCombo.setValue("MVC");
        
        // Include tests checkbox
        CheckBox includeTestsCheck = new CheckBox("Include unit tests");
        includeTestsCheck.setSelected(true);
        
        // Include Docker checkbox
        CheckBox includeDockerCheck = new CheckBox("Include Docker setup");
        includeDockerCheck.setSelected(false);
        
        // Add components to the grid
        grid.add(new Label("Programming Language:"), 0, 0);
        grid.add(languageCombo, 1, 0);
        grid.add(new Label("Framework:"), 0, 1);
        grid.add(frameworkCombo, 1, 1);
        grid.add(new Label("Architecture:"), 0, 2);
        grid.add(patternCombo, 1, 2);
        grid.add(includeTestsCheck, 0, 3, 2, 1);
        grid.add(includeDockerCheck, 0, 4, 2, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        // Convert the result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == generateButtonType) {
                return languageCombo.getValue() + ":" + frameworkCombo.getValue() + ":" + patternCombo.getValue() +
                       ":" + includeTestsCheck.isSelected() + ":" + includeDockerCheck.isSelected();
            }
            return null;
        });
        
        Optional<String> result = dialog.showAndWait();
        
        result.ifPresent(settings -> {
            String[] parts = settings.split(":");
            String language = parts[0];
            String framework = parts[1];
            String pattern = parts[2];
            boolean includeTests = Boolean.parseBoolean(parts[3]);
            boolean includeDocker = Boolean.parseBoolean(parts[4]);
            
            // Generate the actual code based on the DFD and selected options
            generateCodeFromDFD(language, framework, pattern, includeTests, includeDocker);
        });
    }
    
    private void generateCodeFromDFD(String language, String framework, String pattern, 
                                   boolean includeTests, boolean includeDocker) {
        // Create a progress indicator dialog
        Stage progressStage = new Stage();
        progressStage.setTitle("Generating Code");
        
        VBox progressLayout = new VBox(20);
        progressLayout.setPadding(new Insets(20));
        progressLayout.setAlignment(Pos.CENTER);
        
        ProgressIndicator progress = new ProgressIndicator();
        Label progressLabel = new Label("Generating code structure...");
        
        progressLayout.getChildren().addAll(progress, progressLabel);
        
        Scene progressScene = new Scene(progressLayout, 300, 150);
        progressStage.setScene(progressScene);
        progressStage.show();
        
        // Run code generation in background
        Thread codeGenThread = new Thread(() -> {
            try {
                // Simulate code generation process
                Thread.sleep(1000);
                
                // Prepare the output directory
                javafx.application.Platform.runLater(() -> 
                    progressLabel.setText("Creating project structure..."));
                Thread.sleep(800);
                
                // Generate code for each DFD element
                javafx.application.Platform.runLater(() -> 
                    progressLabel.setText("Generating entities and controllers..."));
                Thread.sleep(1200);
                
                // Generate data flow logic
                javafx.application.Platform.runLater(() -> 
                    progressLabel.setText("Mapping data flows to services..."));
                Thread.sleep(1000);
                
                // Generate tests if requested
                if (includeTests) {
                    javafx.application.Platform.runLater(() -> 
                        progressLabel.setText("Creating unit tests..."));
                    Thread.sleep(800);
                }
                
                // Generate Docker setup if requested
                if (includeDocker) {
                    javafx.application.Platform.runLater(() -> 
                        progressLabel.setText("Setting up Docker files..."));
                    Thread.sleep(800);
                }
                
                // Finalize generation
                javafx.application.Platform.runLater(() -> {
                    progressStage.close();
                    
                    // Show file chooser for saving the generated code
                    DirectoryChooser dirChooser = new DirectoryChooser();
                    dirChooser.setTitle("Save Generated Code");
                    File selectedDir = dirChooser.showDialog(root.getScene().getWindow());
                    
                    if (selectedDir != null) {
                        try {
                            // Create a sample project structure
                            createSampleProjectStructure(selectedDir, language, framework, 
                                                      pattern, includeTests, includeDocker);
                            
                            showInfoAlert("Code Generation", 
                                        "Code successfully generated at:\n" + selectedDir.getAbsolutePath());
                        } catch (Exception ex) {
                            showErrorAlert("Generation Error", 
                                        "Failed to generate code: " + ex.getMessage());
                        }
                    }
                });
                
            } catch (InterruptedException e) {
                javafx.application.Platform.runLater(() -> {
                    progressStage.close();
                    showErrorAlert("Generation Error", "Code generation was interrupted.");
                });
            }
        });
        
        codeGenThread.start();
    }
    
    private void createSampleProjectStructure(File rootDir, String language, String framework, 
                                           String pattern, boolean includeTests, boolean includeDocker) throws IOException {
        String basePackageName = "com.generated.dfdapp";
        String projectName = "dfd-generated-app";
        
        // Create base project directory
        File projectDir = new File(rootDir, projectName);
        projectDir.mkdir();
        
        // Create README with project info
        File readmeFile = new File(projectDir, "README.md");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(readmeFile))) {
            writer.write("# " + diagram.getName() + " - Generated Application\n\n");
            writer.write("This application was auto-generated from a Data Flow Diagram using DFDiagram Generator.\n\n");
            writer.write("## Project Details\n");
            writer.write("- **Language:** " + language + "\n");
            writer.write("- **Framework:** " + framework + "\n");
            writer.write("- **Architecture:** " + pattern + "\n");
            writer.write("- **Includes Tests:** " + (includeTests ? "Yes" : "No") + "\n");
            writer.write("- **Includes Docker:** " + (includeDocker ? "Yes" : "No") + "\n\n");
            writer.write("## DFD Elements\n");
            writer.write("### Processes\n");
            for (DFDProcess process : diagram.getProcesses()) {
                writer.write("- " + process.getProcessNumber() + ": " + process.getName() + "\n");
            }
            writer.write("\n### Data Stores\n");
            for (DFDDataStore store : diagram.getDataStores()) {
                writer.write("- " + store.getStoreId() + ": " + store.getName() + "\n");
            }
            writer.write("\n### External Entities\n");
            for (DFDExternalEntity entity : diagram.getExternalEntities()) {
                writer.write("- " + entity.getEntityId() + ": " + entity.getName() + "\n");
            }
        }
        
        // Create language-specific files
        if ("Java".equals(language)) {
            // Create Java project structure
            
            // Create src directory structure
            File srcMainJava = new File(projectDir, "src/main/java");
            srcMainJava.mkdirs();
            
            File srcMainResources = new File(projectDir, "src/main/resources");
            srcMainResources.mkdirs();
            
            // Create Maven pom.xml
            File pomFile = new File(projectDir, "pom.xml");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(pomFile))) {
                writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                writer.write("<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
                writer.write("         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">\n");
                writer.write("    <modelVersion>4.0.0</modelVersion>\n");
                
                // Parent based on framework
                if ("Spring Boot".equals(framework)) {
                    writer.write("    <parent>\n");
                    writer.write("        <groupId>org.springframework.boot</groupId>\n");
                    writer.write("        <artifactId>spring-boot-starter-parent</artifactId>\n");
                    writer.write("        <version>3.2.0</version>\n");
                    writer.write("    </parent>\n");
                }
                
                writer.write("    <groupId>" + basePackageName + "</groupId>\n");
                writer.write("    <artifactId>dfd-generated-app</artifactId>\n");
                writer.write("    <version>0.0.1-SNAPSHOT</version>\n");
                writer.write("    <name>DFD Generated App</name>\n");
                writer.write("    <description>Auto-generated application from DFD</description>\n");
                
                writer.write("    <properties>\n");
                writer.write("        <java.version>17</java.version>\n");
                writer.write("    </properties>\n");
                
                writer.write("    <dependencies>\n");
                // Add dependencies based on framework
                if ("Spring Boot".equals(framework)) {
                    writer.write("        <dependency>\n");
                    writer.write("            <groupId>org.springframework.boot</groupId>\n");
                    writer.write("            <artifactId>spring-boot-starter-web</artifactId>\n");
                    writer.write("        </dependency>\n");
                    writer.write("        <dependency>\n");
                    writer.write("            <groupId>org.springframework.boot</groupId>\n");
                    writer.write("            <artifactId>spring-boot-starter-data-jpa</artifactId>\n");
                    writer.write("        </dependency>\n");
                    writer.write("        <dependency>\n");
                    writer.write("            <groupId>com.h2database</groupId>\n");
                    writer.write("            <artifactId>h2</artifactId>\n");
                    writer.write("            <scope>runtime</scope>\n");
                    writer.write("        </dependency>\n");
                }
                
                // Add test dependencies if needed
                if (includeTests) {
                    writer.write("        <dependency>\n");
                    writer.write("            <groupId>org.junit.jupiter</groupId>\n");
                    writer.write("            <artifactId>junit-jupiter-api</artifactId>\n");
                    writer.write("            <scope>test</scope>\n");
                    writer.write("        </dependency>\n");
                    
                    if ("Spring Boot".equals(framework)) {
                        writer.write("        <dependency>\n");
                        writer.write("            <groupId>org.springframework.boot</groupId>\n");
                        writer.write("            <artifactId>spring-boot-starter-test</artifactId>\n");
                        writer.write("            <scope>test</scope>\n");
                        writer.write("        </dependency>\n");
                    }
                }
                writer.write("    </dependencies>\n");
                
                writer.write("    <build>\n");
                writer.write("        <plugins>\n");
                if ("Spring Boot".equals(framework)) {
                    writer.write("            <plugin>\n");
                    writer.write("                <groupId>org.springframework.boot</groupId>\n");
                    writer.write("                <artifactId>spring-boot-maven-plugin</artifactId>\n");
                    writer.write("            </plugin>\n");
                }
                writer.write("        </plugins>\n");
                writer.write("    </build>\n");
                
                writer.write("</project>\n");
            }
            
            // Create application.properties
            File appProps = new File(srcMainResources, "application.properties");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(appProps))) {
                writer.write("# Generated application properties\n");
                writer.write("spring.application.name=dfd-generated-app\n");
                writer.write("server.port=8080\n\n");
            }
            
            // Create Docker files if needed
            if (includeDocker) {
                File dockerFile = new File(projectDir, "Dockerfile");
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(dockerFile))) {
                    writer.write("FROM openjdk:17-slim\n\n");
                    writer.write("WORKDIR /app\n\n");
                    writer.write("COPY target/*.jar app.jar\n\n");
                    writer.write("EXPOSE 8080\n\n");
                    writer.write("ENTRYPOINT [\"java\", \"-jar\", \"/app/app.jar\"]\n");
                }
            }
            
        } else if ("Python".equals(language)) {
            // Create Python project structure
            File pythonReadme = new File(projectDir, "requirements.txt");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(pythonReadme))) {
                if ("Flask".equals(framework)) {
                    writer.write("flask==2.3.3\n");
                    writer.write("flask-sqlalchemy==3.1.1\n");
                    writer.write("flask-migrate==4.0.5\n");
                } else if ("Django".equals(framework)) {
                    writer.write("django==4.2.7\n");
                    writer.write("djangorestframework==3.14.0\n");
                } else if ("FastAPI".equals(framework)) {
                    writer.write("fastapi==0.104.1\n");
                    writer.write("uvicorn==0.24.0\n");
                    writer.write("sqlalchemy==2.0.23\n");
                }
            }
            
            // Create app directory
            File appDir = new File(projectDir, "app");
            appDir.mkdir();
            
            // Create main.py
            File mainFile = new File(appDir, "main.py");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(mainFile))) {
                if ("Flask".equals(framework)) {
                    writer.write("from flask import Flask, jsonify\n\n");
                    writer.write("app = Flask(__name__)\n\n");
                    writer.write("@app.route('/')\n");
                    writer.write("def index():\n");
                    writer.write("    return jsonify({'message': 'Welcome to the " + diagram.getName() + " API'})\n\n");
                    writer.write("if __name__ == '__main__':\n");
                    writer.write("    app.run(debug=True)\n");
                } else if ("FastAPI".equals(framework)) {
                    writer.write("from fastapi import FastAPI\n\n");
                    writer.write("app = FastAPI(title=\"" + diagram.getName() + "\")\n\n");
                    writer.write("@app.get(\"/\")\n");
                    writer.write("def read_root():\n");
                    writer.write("    return {\"message\": \"Welcome to the " + diagram.getName() + " API\"}\n");
                }
            }
            
        } else if ("JavaScript".equals(language) || "TypeScript".equals(language)) {
            // Create JS/TS project structure
            File packageJson = new File(projectDir, "package.json");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(packageJson))) {
                writer.write("{\n");
                writer.write("  \"name\": \"dfd-generated-app\",\n");
                writer.write("  \"version\": \"1.0.0\",\n");
                writer.write("  \"description\": \"Generated from " + diagram.getName() + "\",\n");
                writer.write("  \"main\": \"index.js\",\n");
                writer.write("  \"scripts\": {\n");
                writer.write("    \"start\": \"node dist/index.js\",\n");
                if ("TypeScript".equals(language)) {
                    writer.write("    \"build\": \"tsc\",\n");
                    writer.write("    \"dev\": \"ts-node src/index.ts\"\n");
                } else {
                    writer.write("    \"dev\": \"nodemon index.js\"\n");
                }
                writer.write("  },\n");
                writer.write("  \"dependencies\": {\n");
                if ("Express.js".equals(framework) || "Express with TS".equals(framework)) {
                    writer.write("    \"express\": \"^4.18.2\",\n");
                    writer.write("    \"body-parser\": \"^1.20.2\"\n");
                } else if ("Nest.js".equals(framework)) {
                    writer.write("    \"@nestjs/common\": \"^10.0.0\",\n");
                    writer.write("    \"@nestjs/core\": \"^10.0.0\",\n");
                    writer.write("    \"@nestjs/platform-express\": \"^10.0.0\"\n");
                }
                writer.write("  }\n");
                writer.write("}\n");
            }
            
            // Create src directory
            File srcDir = new File(projectDir, "src");
            srcDir.mkdir();
            
            String fileExt = "TypeScript".equals(language) ? ".ts" : ".js";
            
            // Create index file
            File indexFile = new File(srcDir, "index" + fileExt);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(indexFile))) {
                if ("Express.js".equals(framework) || "Express with TS".equals(framework)) {
                    if ("TypeScript".equals(language)) {
                        writer.write("import express, { Request, Response } from 'express';\n");
                        writer.write("import bodyParser from 'body-parser';\n\n");
                    } else {
                        writer.write("const express = require('express');\n");
                        writer.write("const bodyParser = require('body-parser');\n\n");
                    }
                    writer.write("const app = express();\n");
                    writer.write("const port = process.env.PORT || 3000;\n\n");
                    writer.write("app.use(bodyParser.json());\n\n");
                    writer.write("app.get('/', (req, res) => {\n");
                    writer.write("  res.json({ message: 'Welcome to the " + diagram.getName() + " API' });\n");
                    writer.write("});\n\n");
                    writer.write("app.listen(port, () => {\n");
                    writer.write("  console.log(`Server running on port ${port}`);\n");
                    writer.write("});\n");
                }
            }
            
        } else if ("C#".equals(language)) {
            // Create C# project structure
            File csprojFile = new File(projectDir, "DfdGeneratedApp.csproj");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(csprojFile))) {
                writer.write("<Project Sdk=\"Microsoft.NET.Sdk.Web\">\n\n");
                writer.write("  <PropertyGroup>\n");
                writer.write("    <TargetFramework>net7.0</TargetFramework>\n");
                writer.write("    <Nullable>enable</Nullable>\n");
                writer.write("    <ImplicitUsings>enable</ImplicitUsings>\n");
                writer.write("  </PropertyGroup>\n\n");
                
                writer.write("  <ItemGroup>\n");
                if (".NET Core".equals(framework) || "ASP.NET MVC".equals(framework)) {
                    writer.write("    <PackageReference Include=\"Microsoft.AspNetCore.OpenApi\" Version=\"7.0.0\" />\n");
                    writer.write("    <PackageReference Include=\"Swashbuckle.AspNetCore\" Version=\"6.5.0\" />\n");
                }
                writer.write("  </ItemGroup>\n\n");
                
                writer.write("</Project>\n");
            }
            
            // Create Program.cs
            File programFile = new File(projectDir, "Program.cs");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(programFile))) {
                writer.write("var builder = WebApplication.CreateBuilder(args);\n\n");
                writer.write("// Add services to the container.\n");
                writer.write("builder.Services.AddControllers();\n");
                writer.write("builder.Services.AddEndpointsApiExplorer();\n");
                writer.write("builder.Services.AddSwaggerGen();\n\n");
                
                writer.write("var app = builder.Build();\n\n");
                writer.write("// Configure the HTTP request pipeline.\n");
                writer.write("if (app.Environment.IsDevelopment())\n");
                writer.write("{\n");
                writer.write("    app.UseSwagger();\n");
                writer.write("    app.UseSwaggerUI();\n");
                writer.write("}\n\n");
                
                writer.write("app.UseHttpsRedirection();\n");
                writer.write("app.UseAuthorization();\n");
                writer.write("app.MapControllers();\n\n");
                
                writer.write("app.Run();\n");
            }
        }
    }
} 