package com.erdiagram.app.ui;

import com.erdiagram.app.model.ERDiagram;
import com.erdiagram.app.model.Entity;
import com.erdiagram.app.model.Relationship;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;

import java.util.HashMap;
import java.util.Map;

/**
 * JavaFX component for displaying the complete ER diagram.
 */
public class DiagramView extends Pane {
    private ERDiagram diagram;
    private Map<Entity, EntityNode> entityNodes;
    private Map<Relationship, RelationshipLine> relationshipLines;
    private double scaleFactor = 1.0;
    private Canvas gridCanvas;
    private final int GRID_SIZE = 20;
    private final Color GRID_COLOR = Color.rgb(200, 200, 200, 0.3);
    private final Color GRID_BACKGROUND = Color.rgb(245, 247, 250);
    
    public DiagramView() {
        getStyleClass().add("workspace");
        entityNodes = new HashMap<>();
        relationshipLines = new HashMap<>();
        
        // Initialize the grid canvas
        initializeGridCanvas();
        
        // Set up zooming with mouse wheel
        setOnScroll(event -> {
            double delta = event.getDeltaY() > 0 ? 0.1 : -0.1;
            zoom(delta);
            event.consume();
        });
        
        // Set minimum size
        setMinSize(2000, 1500);
        setPrefSize(2000, 1500);
    }
    
    private void initializeGridCanvas() {
        gridCanvas = new Canvas(2000, 1500);
        getChildren().add(gridCanvas);
        drawGrid();
    }
    
    private void drawGrid() {
        GraphicsContext gc = gridCanvas.getGraphicsContext2D();
        
        // Clear the canvas
        gc.clearRect(0, 0, gridCanvas.getWidth(), gridCanvas.getHeight());
        
        // Draw background
        gc.setFill(GRID_BACKGROUND);
        gc.fillRect(0, 0, gridCanvas.getWidth(), gridCanvas.getHeight());
        
        // Draw grid lines
        gc.setStroke(GRID_COLOR);
        gc.setLineWidth(1);
        
        // Draw vertical grid lines
        for (double x = 0; x <= gridCanvas.getWidth(); x += GRID_SIZE) {
            gc.strokeLine(x, 0, x, gridCanvas.getHeight());
        }
        
        // Draw horizontal grid lines
        for (double y = 0; y <= gridCanvas.getHeight(); y += GRID_SIZE) {
            gc.strokeLine(0, y, gridCanvas.getWidth(), y);
        }
        
        // Draw major grid lines (every 5 cells)
        gc.setStroke(Color.rgb(180, 180, 180, 0.5));
        gc.setLineWidth(1.5);
        
        for (double x = 0; x <= gridCanvas.getWidth(); x += GRID_SIZE * 5) {
            gc.strokeLine(x, 0, x, gridCanvas.getHeight());
        }
        
        for (double y = 0; y <= gridCanvas.getHeight(); y += GRID_SIZE * 5) {
            gc.strokeLine(0, y, gridCanvas.getWidth(), y);
        }
    }
    
    /**
     * Sets the ER diagram model to display.
     * 
     * @param diagram The ER diagram model
     */
    public void setDiagram(ERDiagram diagram) {
        this.diagram = diagram;
        refresh();
    }
    
    /**
     * Refreshes the diagram view based on the current ER diagram model.
     */
    public void refresh() {
        // Remove existing nodes except gridCanvas
        getChildren().clear();
        getChildren().add(gridCanvas);
        
        entityNodes.clear();
        relationshipLines.clear();
        
        if (diagram == null) {
            return;
        }
        
        // Create relationship lines first so they appear below entities
        for (Relationship relationship : diagram.getRelationships()) {
            EntityNode sourceNode = createEntityNodeIfNeeded(relationship.getSourceEntity());
            EntityNode targetNode = createEntityNodeIfNeeded(relationship.getTargetEntity());
            
            if (sourceNode != null && targetNode != null) {
                RelationshipLine line = new RelationshipLine(relationship, sourceNode, targetNode);
                relationshipLines.put(relationship, line);
                getChildren().add(line);
            }
        }
        
        // Create remaining entity nodes if any
        for (Entity entity : diagram.getEntities()) {
            createEntityNodeIfNeeded(entity);
        }
        
        // Make sure entities are on top of relationship lines
        for (EntityNode node : entityNodes.values()) {
            node.toFront();
        }
    }
    
    private EntityNode createEntityNodeIfNeeded(Entity entity) {
        if (!entityNodes.containsKey(entity)) {
            EntityNode entityNode = new EntityNode(entity);
            entityNodes.put(entity, entityNode);
            getChildren().add(entityNode);
            return entityNode;
        }
        return entityNodes.get(entity);
    }
    
    /**
     * Updates the positions of all relationship lines based on entity positions.
     */
    public void updateRelationshipLines() {
        for (RelationshipLine line : relationshipLines.values()) {
            line.updatePosition();
        }
    }
    
    /**
     * Applies auto-layout to the diagram.
     */
    public void applyAutoLayout() {
        if (diagram != null) {
            diagram.autoLayout();
            refresh();
        }
    }
    
    /**
     * Zooms the diagram view.
     * 
     * @param deltaFactor The amount to change the zoom by
     */
    public void zoom(double deltaFactor) {
        scaleFactor += deltaFactor;
        
        // Limit zoom range
        if (scaleFactor < 0.2) {
            scaleFactor = 0.2;
        } else if (scaleFactor > 3.0) {
            scaleFactor = 3.0;
        }
        
        // Apply scaling transform
        Scale scale = new Scale();
        scale.setX(scaleFactor);
        scale.setY(scaleFactor);
        getTransforms().clear();
        getTransforms().add(scale);
    }
    
    /**
     * Resets the zoom to 100%.
     */
    public void resetZoom() {
        scaleFactor = 1.0;
        getTransforms().clear();
    }
    
    public ERDiagram getDiagram() {
        return diagram;
    }
} 