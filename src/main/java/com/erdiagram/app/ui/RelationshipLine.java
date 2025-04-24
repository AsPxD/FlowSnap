package com.erdiagram.app.ui;

import com.erdiagram.app.model.Relationship;
import javafx.scene.Group;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.transform.Rotate;

/**
 * JavaFX component for rendering a relationship line in the ER diagram.
 */
public class RelationshipLine extends Group {
    private Relationship relationship;
    private EntityNode sourceNode;
    private EntityNode targetNode;
    private Line line;
    private Group sourceMarker;
    private Group targetMarker;
    private Group labelGroup;
    
    public RelationshipLine(Relationship relationship, EntityNode sourceNode, EntityNode targetNode) {
        this.relationship = relationship;
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
        
        initializeLine();
        initializeMarkers();
        initializeLabel();
        
        getChildren().addAll(line, sourceMarker, targetMarker, labelGroup);
        
        updatePosition();
    }
    
    private void initializeLine() {
        line = new Line();
        line.getStyleClass().add("relationship-line");
        line.getStyleClass().add(relationship.getStyleClass());
        line.setStrokeWidth(2.5);
        line.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        line.getStrokeDashArray().clear();
        
        // Set color based on relationship type
        switch (relationship.getType()) {
            case ONE_TO_ONE:
                line.setStroke(Color.web("#27ae60"));
                break;
            case ONE_TO_MANY:
            case MANY_TO_ONE:
                line.setStroke(Color.web("#e67e22"));
                break;
            case MANY_TO_MANY:
                line.setStroke(Color.web("#8e44ad"));
                // Use dashed line for many-to-many
                line.getStrokeDashArray().addAll(10.0, 5.0);
                break;
        }
        
        // Add drop shadow for better visibility
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(4.0);
        dropShadow.setOffsetX(1.0);
        dropShadow.setOffsetY(1.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.3));
        line.setEffect(dropShadow);
    }
    
    private void initializeMarkers() {
        // Create source marker
        sourceMarker = createMarker(relationship.getType(), true);
        sourceMarker.getStyleClass().add("relationship-marker");
        
        // Create target marker
        targetMarker = createMarker(relationship.getType(), false);
        targetMarker.getStyleClass().add("relationship-marker");
    }
    
    private void initializeLabel() {
        labelGroup = new Group();
        
        // Create background for the label for better readability
        Rectangle background = new Rectangle();
        background.setFill(Color.rgb(255, 255, 255, 0.95));
        background.setArcWidth(12);
        background.setArcHeight(12);
        background.setStroke(getColorForRelationshipType());
        background.setStrokeWidth(2.5);
        
        // Create relationship type text - larger and bolder
        Text typeText = new Text(getRelationshipTypeSymbol());
        typeText.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        typeText.setFill(getColorForRelationshipType());
        
        // Add info about source and target if available
        StringBuilder detailsBuilder = new StringBuilder();
        if (relationship.getSourceAttribute() != null && relationship.getTargetAttribute() != null) {
            detailsBuilder.append("\n")
                    .append(relationship.getSourceEntity().getName())
                    .append(".")
                    .append(relationship.getSourceAttribute().getName())
                    .append(" → ")
                    .append(relationship.getTargetEntity().getName())
                    .append(".")
                    .append(relationship.getTargetAttribute().getName());
        }
        
        Text detailsText = new Text(detailsBuilder.toString());
        detailsText.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 12));
        detailsText.setFill(Color.DARKSLATEGRAY);
        
        // Create text flow to display the relationship info
        TextFlow textFlow = new TextFlow(typeText, detailsText);
        textFlow.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        textFlow.setLineSpacing(2);
        
        // Add the text to the label group
        labelGroup.getChildren().addAll(background, textFlow);
        
        // Adjust background size to fit text
        background.widthProperty().bind(textFlow.prefWidthProperty().add(24));
        background.heightProperty().bind(textFlow.prefHeightProperty().add(16));
        
        // Center the text over the background
        textFlow.setLayoutX(12);
        textFlow.setLayoutY(8);
        
        // Add drop shadow for better visibility
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(6.0);
        dropShadow.setOffsetX(2.0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.4));
        labelGroup.setEffect(dropShadow);
    }
    
    private String getRelationshipTypeSymbol() {
        switch (relationship.getType()) {
            case ONE_TO_ONE:
                return "ONE-TO-ONE";
            case ONE_TO_MANY:
                return "ONE-TO-MANY";
            case MANY_TO_ONE:
                return "MANY-TO-ONE";
            case MANY_TO_MANY:
                return "MANY-TO-MANY";
            default:
                return "";
        }
    }
    
    private Color getColorForRelationshipType() {
        switch (relationship.getType()) {
            case ONE_TO_ONE:
                return Color.web("#27ae60");
            case ONE_TO_MANY:
            case MANY_TO_ONE:
                return Color.web("#e67e22");
            case MANY_TO_MANY:
                return Color.web("#8e44ad");
            default:
                return Color.BLACK;
        }
    }
    
    /**
     * Creates a marker shape based on the relationship type.
     * 
     * @param type The relationship type
     * @param isSource Whether this is the source side of the relationship
     * @return A JavaFX Group containing the marker shape
     */
    private Group createMarker(Relationship.RelationshipType type, boolean isSource) {
        Group marker = new Group();
        
        switch (type) {
            case ONE_TO_ONE:
                if (isSource) {
                    // One marker (vertical line)
                    Line verticalLine = new Line(0, -10, 0, 10);
                    verticalLine.setStrokeWidth(2.5);
                    verticalLine.setStroke(Color.web("#27ae60"));
                    marker.getChildren().add(verticalLine);
                } else {
                    // One marker (vertical line)
                    Line verticalLine = new Line(0, -10, 0, 10);
                    verticalLine.setStrokeWidth(2.5);
                    verticalLine.setStroke(Color.web("#27ae60"));
                    marker.getChildren().add(verticalLine);
                }
                break;
                
            case ONE_TO_MANY:
                if (isSource) {
                    // One marker (vertical line)
                    Line verticalLine = new Line(0, -10, 0, 10);
                    verticalLine.setStrokeWidth(2.5);
                    verticalLine.setStroke(Color.web("#e67e22"));
                    marker.getChildren().add(verticalLine);
                } else {
                    // Many marker (crow's foot)
                    Line centerLine = new Line(0, 0, 15, 0);
                    Line topLine = new Line(15, 0, 0, -10);
                    Line bottomLine = new Line(15, 0, 0, 10);
                    
                    centerLine.setStrokeWidth(2.5);
                    topLine.setStrokeWidth(2.5);
                    bottomLine.setStrokeWidth(2.5);
                    
                    centerLine.setStroke(Color.web("#e67e22"));
                    topLine.setStroke(Color.web("#e67e22"));
                    bottomLine.setStroke(Color.web("#e67e22"));
                    
                    marker.getChildren().addAll(centerLine, topLine, bottomLine);
                }
                break;
                
            case MANY_TO_ONE:
                if (isSource) {
                    // Many marker (crow's foot)
                    Line centerLine = new Line(0, 0, 15, 0);
                    Line topLine = new Line(15, 0, 0, -10);
                    Line bottomLine = new Line(15, 0, 0, 10);
                    
                    centerLine.setStrokeWidth(2.5);
                    topLine.setStrokeWidth(2.5);
                    bottomLine.setStrokeWidth(2.5);
                    
                    centerLine.setStroke(Color.web("#e67e22"));
                    topLine.setStroke(Color.web("#e67e22"));
                    bottomLine.setStroke(Color.web("#e67e22"));
                    
                    marker.getChildren().addAll(centerLine, topLine, bottomLine);
                } else {
                    // One marker (vertical line)
                    Line verticalLine = new Line(0, -10, 0, 10);
                    verticalLine.setStrokeWidth(2.5);
                    verticalLine.setStroke(Color.web("#e67e22"));
                    marker.getChildren().add(verticalLine);
                }
                break;
                
            case MANY_TO_MANY:
                // Many marker (crow's foot) for both ends
                Line centerLine = new Line(0, 0, 15, 0);
                Line topLine = new Line(15, 0, 0, -10);
                Line bottomLine = new Line(15, 0, 0, 10);
                
                centerLine.setStrokeWidth(2.5);
                topLine.setStrokeWidth(2.5);
                bottomLine.setStrokeWidth(2.5);
                
                centerLine.setStroke(Color.web("#8e44ad"));
                topLine.setStroke(Color.web("#8e44ad"));
                bottomLine.setStroke(Color.web("#8e44ad"));
                
                marker.getChildren().addAll(centerLine, topLine, bottomLine);
                break;
        }
        
        // Add drop shadow for better visibility
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(3.0);
        dropShadow.setOffsetX(1.0);
        dropShadow.setOffsetY(1.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.3));
        marker.setEffect(dropShadow);
        
        return marker;
    }
    
    /**
     * Updates the position of the line and markers based on the positions of the entities.
     */
    public void updatePosition() {
        // Get the centers of the nodes
        double sourceX = sourceNode.getLayoutX() + sourceNode.getWidth() / 2;
        double sourceY = sourceNode.getLayoutY() + sourceNode.getHeight() / 2;
        double targetX = targetNode.getLayoutX() + targetNode.getWidth() / 2;
        double targetY = targetNode.getLayoutY() + targetNode.getHeight() / 2;
        
        // Update the line
        line.setStartX(sourceX);
        line.setStartY(sourceY);
        line.setEndX(targetX);
        line.setEndY(targetY);
        
        // Calculate the angle for markers
        double angle = Math.toDegrees(Math.atan2(targetY - sourceY, targetX - sourceX));
        
        // Position and rotate the source marker
        sourceMarker.setLayoutX(sourceX);
        sourceMarker.setLayoutY(sourceY);
        sourceMarker.getTransforms().clear();
        sourceMarker.getTransforms().add(new Rotate(angle, 0, 0));
        
        // Position and rotate the target marker (180 degrees offset)
        targetMarker.setLayoutX(targetX);
        targetMarker.setLayoutY(targetY);
        targetMarker.getTransforms().clear();
        targetMarker.getTransforms().add(new Rotate(angle + 180, 0, 0));
        
        // Position the label exactly at the center of the line
        double midX = (sourceX + targetX) / 2;
        double midY = (sourceY + targetY) / 2;
        
        // Get the label width and height for better centering
        double labelWidth = labelGroup.getBoundsInLocal().getWidth();
        double labelHeight = labelGroup.getBoundsInLocal().getHeight();
        
        // Center the label on the line
        labelGroup.setLayoutX(midX - (labelWidth / 2));
        labelGroup.setLayoutY(midY - (labelHeight / 2));
    }
    
    public Relationship getRelationship() {
        return relationship;
    }
} 