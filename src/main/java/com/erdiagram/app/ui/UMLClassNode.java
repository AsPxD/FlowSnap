package com.erdiagram.app.ui;

import com.erdiagram.app.model.UMLAttribute;
import com.erdiagram.app.model.UMLClass;
import com.erdiagram.app.model.UMLMethod;
import javafx.geometry.Insets;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.input.MouseEvent;
import javafx.scene.Cursor;

/**
 * A visual node representing a UML class in the diagram.
 */
public class UMLClassNode extends VBox {
    private UMLClass umlClass;
    private Rectangle background;
    private VBox attributesBox;
    private VBox methodsBox;
    private Text titleText;
    private Text typeText;
    private Text packageText;
    
    private double mouseAnchorX;
    private double mouseAnchorY;
    
    private static final double DEFAULT_WIDTH = 200;
    private static final double HEADER_HEIGHT = 40;
    private static final double PADDING = 10;
    
    public UMLClassNode(UMLClass umlClass) {
        this.umlClass = umlClass;
        
        // Initial position
        setLayoutX(umlClass.getX());
        setLayoutY(umlClass.getY());
        
        // Configure the node
        setPadding(new Insets(0));
        setSpacing(0);
        
        // Create the sections
        createHeaderSection();
        createAttributesSection();
        createMethodsSection();
        
        // Make the node draggable
        setupDragHandling();
        
        // Create context menu
        setupContextMenu();
    }
    
    private void createHeaderSection() {
        VBox header = new VBox(2);
        header.setPadding(new Insets(PADDING));
        header.setStyle("-fx-background-color: #5573B7;");
        
        // Class type
        typeText = new Text(getClassTypePrefix() + " " + umlClass.getName());
        typeText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        typeText.setFill(Color.WHITE);
        
        // Package
        if (umlClass.getPackageName() != null && !umlClass.getPackageName().isEmpty()) {
            packageText = new Text(umlClass.getPackageName());
            packageText.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
            packageText.setFill(Color.LIGHTGRAY);
            header.getChildren().add(packageText);
        }
        
        header.getChildren().add(typeText);
        
        getChildren().add(header);
    }
    
    private String getClassTypePrefix() {
        switch (umlClass.getType()) {
            case "interface": return "«interface»";
            case "enum": return "«enum»";
            case "abstract class": return "«abstract»";
            default: return "";
        }
    }
    
    private void createAttributesSection() {
        attributesBox = new VBox(2);
        attributesBox.setPadding(new Insets(PADDING));
        attributesBox.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 0 0 1 0;");
        
        // Add attributes
        for (UMLAttribute attribute : umlClass.getAttributes()) {
            Text attrText = new Text(attribute.toString());
            attrText.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
            attributesBox.getChildren().add(attrText);
        }
        
        // If no attributes, add empty space
        if (attributesBox.getChildren().isEmpty()) {
            attributesBox.setMinHeight(10);
        }
        
        getChildren().add(attributesBox);
    }
    
    private void createMethodsSection() {
        methodsBox = new VBox(2);
        methodsBox.setPadding(new Insets(PADDING));
        methodsBox.setStyle("-fx-background-color: white;");
        
        // Add methods
        for (UMLMethod method : umlClass.getMethods()) {
            Text methodText = new Text(method.toString());
            methodText.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
            methodsBox.getChildren().add(methodText);
        }
        
        // If no methods, add empty space
        if (methodsBox.getChildren().isEmpty()) {
            methodsBox.setMinHeight(10);
        }
        
        getChildren().add(methodsBox);
    }
    
    private void setupDragHandling() {
        setCursor(Cursor.HAND);
        
        setOnMousePressed(event -> {
            mouseAnchorX = event.getSceneX() - getLayoutX();
            mouseAnchorY = event.getSceneY() - getLayoutY();
            toFront();
            event.consume();
        });
        
        setOnMouseDragged(event -> {
            setLayoutX(event.getSceneX() - mouseAnchorX);
            setLayoutY(event.getSceneY() - mouseAnchorY);
            
            // Update model position
            umlClass.setX(getLayoutX());
            umlClass.setY(getLayoutY());
            
            event.consume();
        });
    }
    
    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem removeItem = new MenuItem("Remove");
        removeItem.setOnAction(e -> {
            // Will be implemented in UMLDiagramView
        });
        
        contextMenu.getItems().add(removeItem);
        
        setOnContextMenuRequested(event -> {
            contextMenu.show(this, event.getScreenX(), event.getScreenY());
            event.consume();
        });
    }
    
    public UMLClass getUmlClass() {
        return umlClass;
    }
    
    public void update() {
        // Update type and name
        typeText.setText(getClassTypePrefix() + " " + umlClass.getName());
        
        // Update attributes
        attributesBox.getChildren().clear();
        for (UMLAttribute attribute : umlClass.getAttributes()) {
            Text attrText = new Text(attribute.toString());
            attrText.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
            attributesBox.getChildren().add(attrText);
        }
        
        // Update methods
        methodsBox.getChildren().clear();
        for (UMLMethod method : umlClass.getMethods()) {
            Text methodText = new Text(method.toString());
            methodText.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
            methodsBox.getChildren().add(methodText);
        }
        
        // Update position
        setLayoutX(umlClass.getX());
        setLayoutY(umlClass.getY());
    }
} 