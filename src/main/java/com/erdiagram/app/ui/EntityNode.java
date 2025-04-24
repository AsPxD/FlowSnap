package com.erdiagram.app.ui;

import com.erdiagram.app.model.Attribute;
import com.erdiagram.app.model.Entity;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.effect.DropShadow;

/**
 * JavaFX component for rendering an entity in the ER diagram.
 */
public class EntityNode extends VBox {
    private Entity entity;
    private double mouseAnchorX;
    private double mouseAnchorY;
    
    public EntityNode(Entity entity) {
        this.entity = entity;
        
        getStyleClass().add("entity-table");
        setPadding(new Insets(12));
        setSpacing(8);
        setMinWidth(220);
        
        // Create and style the title
        createHeader();
        
        // Add separator
        Line separator = new Line(0, 0, getMinWidth() - 24, 0);
        separator.getStyleClass().add("entity-separator");
        getChildren().add(separator);
        
        // Add all attributes
        for (Attribute attribute : entity.getAttributes()) {
            createAttributeRow(attribute);
        }
        
        // Position the node
        relocate(entity.getX(), entity.getY());
        
        // Set up dragging
        setUpDragging();
    }
    
    private void createHeader() {
        Text title = new Text(entity.getName());
        title.getStyleClass().add("entity-title");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER);
        headerBox.getChildren().add(title);
        
        getChildren().add(headerBox);
    }
    
    private void createAttributeRow(Attribute attribute) {
        HBox row = new HBox(10);
        row.setPadding(new Insets(2, 4, 2, 4));
        row.getStyleClass().add("attribute-box");
        
        // Create icon for primary/foreign key
        Text keyIcon = new Text("");
        if (attribute.isPrimaryKey() && attribute.isForeignKey()) {
            keyIcon.setText("⚷ ");  // Combined PK/FK
            keyIcon.getStyleClass().add("primary-key-icon");
        } else if (attribute.isPrimaryKey()) {
            keyIcon.setText("🔑 ");  // Primary key
            keyIcon.getStyleClass().add("primary-key-icon");
        } else if (attribute.isForeignKey()) {
            keyIcon.setText("🔗 ");  // Foreign key
            keyIcon.getStyleClass().add("foreign-key-icon");
        }
        
        // Create attribute name
        Text attrName = new Text(attribute.getName());
        attrName.getStyleClass().add("attribute");
        
        // Create data type with styled format
        Text dataType = new Text(" : " + attribute.getDataType());
        dataType.getStyleClass().add("data-type");
        
        // Create not null indicator if needed
        Text nullableText = new Text("");
        if (!attribute.isNullable()) {
            nullableText.setText(" *");
            nullableText.getStyleClass().add("not-null-indicator");
        }
        
        // Create the text flow for the attribute
        TextFlow attributeText = new TextFlow(keyIcon, attrName, dataType, nullableText);
        
        // Apply style classes based on key type
        if (attribute.isPrimaryKey() && attribute.isForeignKey()) {
            row.getStyleClass().add("primary-key");
            row.getStyleClass().add("foreign-key");
        } else if (attribute.isPrimaryKey()) {
            row.getStyleClass().add("primary-key");
        } else if (attribute.isForeignKey()) {
            row.getStyleClass().add("foreign-key");
        }
        
        row.getChildren().add(attributeText);
        getChildren().add(row);
    }
    
    public Entity getEntity() {
        return entity;
    }
    
    private void setUpDragging() {
        setOnMousePressed(event -> {
            // Save the initial mouse position
            mouseAnchorX = event.getSceneX();
            mouseAnchorY = event.getSceneY();
            
            // Bring to front
            toFront();
            
            // Add pressed effect
            DropShadow shadow = new DropShadow();
            shadow.setRadius(15);
            shadow.setColor(Color.DARKBLUE);
            setEffect(shadow);
            
            event.consume();
        });
        
        setOnMouseDragged(event -> {
            // Calculate the new position
            double deltaX = event.getSceneX() - mouseAnchorX;
            double deltaY = event.getSceneY() - mouseAnchorY;
            
            // Update the position of the entity
            entity.setX(getLayoutX() + deltaX);
            entity.setY(getLayoutY() + deltaY);
            
            // Update the position of the node
            relocate(entity.getX(), entity.getY());
            
            // Update the mouse anchor
            mouseAnchorX = event.getSceneX();
            mouseAnchorY = event.getSceneY();
            
            event.consume();
        });
        
        setOnMouseReleased(event -> {
            // Reset to normal shadow effect
            DropShadow shadow = new DropShadow();
            shadow.setRadius(12);
            shadow.setOffsetX(2);
            shadow.setOffsetY(2);
            shadow.setColor(Color.rgb(0, 0, 0, 0.4));
            setEffect(shadow);
            
            event.consume();
        });
    }
} 