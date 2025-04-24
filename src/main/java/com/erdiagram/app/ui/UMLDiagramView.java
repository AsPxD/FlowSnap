package com.erdiagram.app.ui;

import com.erdiagram.app.model.UMLClass;
import com.erdiagram.app.model.UMLDiagram;
import com.erdiagram.app.model.UMLRelationship;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Scale;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Visual component that displays the UML diagram.
 */
public class UMLDiagramView extends Pane {
    
    private UMLDiagram diagram;
    private Map<UMLClass, UMLClassNode> classNodeMap;
    private Map<UMLRelationship, UMLRelationshipLine> relationshipLineMap;
    
    private double zoomFactor = 1.0;
    
    public UMLDiagramView() {
        this.classNodeMap = new HashMap<>();
        this.relationshipLineMap = new ConcurrentHashMap<>();
        
        // Styling
        setStyle("-fx-background-color: white;");
    }
    
    /**
     * Set a new diagram to display
     */
    public void setDiagram(UMLDiagram diagram) {
        this.diagram = diagram;
        refreshView();
    }
    
    /**
     * Update the view with the current diagram data
     */
    public void refreshView() {
        if (diagram == null) return;
        
        // Clear existing nodes
        getChildren().clear();
        classNodeMap.clear();
        relationshipLineMap.clear();
        
        // Create class nodes first
        for (UMLClass umlClass : diagram.getClasses()) {
            UMLClassNode classNode = new UMLClassNode(umlClass);
            classNodeMap.put(umlClass, classNode);
            getChildren().add(classNode);
        }
        
        // Create relationship lines
        for (UMLRelationship relationship : diagram.getRelationships()) {
            UMLClassNode sourceNode = classNodeMap.get(relationship.getSource());
            UMLClassNode targetNode = classNodeMap.get(relationship.getTarget());
            
            if (sourceNode != null && targetNode != null) {
                UMLRelationshipLine relationshipLine = new UMLRelationshipLine(sourceNode, targetNode, relationship);
                relationshipLineMap.put(relationship, relationshipLine);
                getChildren().add(0, relationshipLine); // Add at index 0 to put it below nodes
            }
        }
    }
    
    /**
     * Apply auto layout to the diagram using a simple force-directed algorithm
     */
    public void applyAutoLayout() {
        int classCount = diagram.getClasses().size();
        double radius = 250;
        
        // Spread classes in a circle if few classes, or grid for many
        if (classCount <= 10) {
            // Circle layout for few classes
            for (int i = 0; i < classCount; i++) {
                UMLClass umlClass = diagram.getClasses().get(i);
                double angle = 2 * Math.PI * i / classCount;
                
                umlClass.setX(getWidth() / 2 + radius * Math.cos(angle));
                umlClass.setY(getHeight() / 2 + radius * Math.sin(angle));
            }
        } else {
            // Grid layout for many classes
            int cols = (int) Math.ceil(Math.sqrt(classCount));
            int rows = (int) Math.ceil(classCount / (double) cols);
            
            double horizontalSpacing = getWidth() / (cols + 1);
            double verticalSpacing = getHeight() / (rows + 1);
            
            for (int i = 0; i < classCount; i++) {
                int row = i / cols;
                int col = i % cols;
                
                UMLClass umlClass = diagram.getClasses().get(i);
                umlClass.setX(horizontalSpacing * (col + 1));
                umlClass.setY(verticalSpacing * (row + 1));
            }
        }
        
        // Update class nodes with new positions
        for (UMLClass umlClass : diagram.getClasses()) {
            UMLClassNode classNode = classNodeMap.get(umlClass);
            if (classNode != null) {
                classNode.setLayoutX(umlClass.getX());
                classNode.setLayoutY(umlClass.getY());
            }
        }
        
        // Update relationship lines
        updateRelationshipLines();
    }
    
    /**
     * Update all relationship lines' positions based on connected nodes
     */
    public void updateRelationshipLines() {
        for (UMLRelationshipLine line : relationshipLineMap.values()) {
            line.update();
        }
    }
    
    /**
     * Remove a class node and associated relationship lines
     */
    public void removeClass(UMLClass umlClass) {
        UMLClassNode classNode = classNodeMap.get(umlClass);
        if (classNode == null) return;
        
        // Remove node
        getChildren().remove(classNode);
        classNodeMap.remove(umlClass);
        
        // Remove associated relationship lines
        for (UMLRelationship relationship : diagram.getRelationshipsForClass(umlClass)) {
            UMLRelationshipLine line = relationshipLineMap.get(relationship);
            if (line != null) {
                getChildren().remove(line);
                relationshipLineMap.remove(relationship);
            }
        }
        
        // Remove from model
        diagram.removeClass(umlClass);
    }
    
    /**
     * Apply zoom to the diagram
     * 
     * @param delta Zoom change, positive for zoom in, negative for zoom out
     */
    public void zoom(double delta) {
        zoomFactor += delta;
        
        // Limit zoom range
        if (zoomFactor < 0.2) zoomFactor = 0.2;
        if (zoomFactor > 3.0) zoomFactor = 3.0;
        
        // Apply zoom
        setScaleX(zoomFactor);
        setScaleY(zoomFactor);
    }
    
    /**
     * Reset zoom to 100%
     */
    public void resetZoom() {
        zoomFactor = 1.0;
        setScaleX(zoomFactor);
        setScaleY(zoomFactor);
    }
    
    public UMLDiagram getDiagram() {
        return diagram;
    }
} 