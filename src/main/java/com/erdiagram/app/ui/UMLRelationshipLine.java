package com.erdiagram.app.ui;

import com.erdiagram.app.model.UMLRelationship;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;

/**
 * Visual representation of a relationship between UML classes.
 */
public class UMLRelationshipLine extends Group {
    
    private UMLClassNode sourceNode;
    private UMLClassNode targetNode;
    private UMLRelationship relationship;
    
    private Line line;
    private Group sourceEnd;
    private Group targetEnd;
    private Text sourceLabel;
    private Text targetLabel;
    
    public UMLRelationshipLine(UMLClassNode sourceNode, UMLClassNode targetNode, UMLRelationship relationship) {
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
        this.relationship = relationship;
        
        initialize();
        update();
    }
    
    private void initialize() {
        // Create main line
        line = new Line();
        line.setStrokeWidth(1.5);
        
        // Create source end (if needed)
        sourceEnd = createRelationshipEnd(relationship.getType(), false);
        
        // Create target end
        targetEnd = createRelationshipEnd(relationship.getType(), true);
        
        // Create labels
        if (relationship.getSourceLabel() != null) {
            sourceLabel = new Text(relationship.getSourceLabel());
            sourceLabel.setFont(Font.font(10));
            getChildren().add(sourceLabel);
        }
        
        if (relationship.getTargetLabel() != null) {
            targetLabel = new Text(relationship.getTargetLabel());
            targetLabel.setFont(Font.font(10));
            getChildren().add(targetLabel);
        }
        
        // Add all components
        getChildren().addAll(line, sourceEnd, targetEnd);
    }
    
    /**
     * Creates the appropriate arrow/symbol for a relationship end
     */
    private Group createRelationshipEnd(String relationType, boolean isTarget) {
        Group endGroup = new Group();
        
        if (isTarget) {
            switch (relationType) {
                case UMLRelationship.INHERITANCE:
                    // White arrow head
                    Polygon inheritanceArrow = new Polygon(0, 0, -15, -8, -15, 8);
                    inheritanceArrow.setFill(Color.WHITE);
                    inheritanceArrow.setStroke(Color.BLACK);
                    inheritanceArrow.setStrokeWidth(1.5);
                    endGroup.getChildren().add(inheritanceArrow);
                    break;
                
                case UMLRelationship.IMPLEMENTATION:
                    // White arrow head with dashed line (will be set later)
                    Polygon implementationArrow = new Polygon(0, 0, -15, -8, -15, 8);
                    implementationArrow.setFill(Color.WHITE);
                    implementationArrow.setStroke(Color.BLACK);
                    implementationArrow.setStrokeWidth(1.5);
                    endGroup.getChildren().add(implementationArrow);
                    line.getStrokeDashArray().addAll(5d, 5d);
                    break;
                
                case UMLRelationship.DEPENDENCY:
                    // Dashed line with open arrow
                    Polyline dependencyArrow = new Polyline(0, 0, -15, -8, -15, 8, 0, 0);
                    dependencyArrow.setFill(null);
                    dependencyArrow.setStroke(Color.BLACK);
                    dependencyArrow.setStrokeWidth(1.5);
                    endGroup.getChildren().add(dependencyArrow);
                    line.getStrokeDashArray().addAll(5d, 5d);
                    break;
                
                case UMLRelationship.AGGREGATION:
                    // Diamond, white filled
                    Polygon aggregationDiamond = new Polygon(0, 0, -10, -6, -20, 0, -10, 6);
                    aggregationDiamond.setFill(Color.WHITE);
                    aggregationDiamond.setStroke(Color.BLACK);
                    aggregationDiamond.setStrokeWidth(1.5);
                    endGroup.getChildren().add(aggregationDiamond);
                    break;
                
                case UMLRelationship.COMPOSITION:
                    // Diamond, black filled
                    Polygon compositionDiamond = new Polygon(0, 0, -10, -6, -20, 0, -10, 6);
                    compositionDiamond.setFill(Color.BLACK);
                    compositionDiamond.setStroke(Color.BLACK);
                    compositionDiamond.setStrokeWidth(1.5);
                    endGroup.getChildren().add(compositionDiamond);
                    break;
                
                default:
                    // Association - simple arrow
                    Polyline associationArrow = new Polyline(0, 0, -15, -8, -15, 8, 0, 0);
                    associationArrow.setFill(null);
                    associationArrow.setStroke(Color.BLACK);
                    associationArrow.setStrokeWidth(1.5);
                    endGroup.getChildren().add(associationArrow);
                    break;
            }
        }
        
        return endGroup;
    }
    
    /**
     * Update the position and rotation of the relationship line
     */
    public void update() {
        // Calculate source and target node centers
        double sourceX = sourceNode.getLayoutX() + sourceNode.getWidth() / 2;
        double sourceY = sourceNode.getLayoutY() + sourceNode.getHeight() / 2;
        double targetX = targetNode.getLayoutX() + targetNode.getWidth() / 2;
        double targetY = targetNode.getLayoutY() + targetNode.getHeight() / 2;
        
        // Find intersection points with node boundaries
        double[] sourceIntersection = findIntersectionPoint(sourceX, sourceY, targetX, targetY, sourceNode);
        double[] targetIntersection = findIntersectionPoint(targetX, targetY, sourceX, sourceY, targetNode);
        
        // Update line coordinates
        line.setStartX(sourceIntersection[0]);
        line.setStartY(sourceIntersection[1]);
        line.setEndX(targetIntersection[0]);
        line.setEndY(targetIntersection[1]);
        
        // Calculate angle for arrow heads
        double dx = targetIntersection[0] - sourceIntersection[0];
        double dy = targetIntersection[1] - sourceIntersection[1];
        double angle = Math.toDegrees(Math.atan2(dy, dx));
        
        // Position source end
        if (sourceEnd != null) {
            sourceEnd.setLayoutX(sourceIntersection[0]);
            sourceEnd.setLayoutY(sourceIntersection[1]);
            sourceEnd.getTransforms().clear();
            sourceEnd.getTransforms().add(new Rotate(angle, 0, 0));
        }
        
        // Position target end
        if (targetEnd != null) {
            targetEnd.setLayoutX(targetIntersection[0]);
            targetEnd.setLayoutY(targetIntersection[1]);
            targetEnd.getTransforms().clear();
            targetEnd.getTransforms().add(new Rotate(angle + 180, 0, 0));
        }
        
        // Position labels
        if (sourceLabel != null) {
            sourceLabel.setLayoutX(sourceIntersection[0] + dx * 0.2 - sourceLabel.getBoundsInLocal().getWidth() / 2);
            sourceLabel.setLayoutY(sourceIntersection[1] + dy * 0.2 - sourceLabel.getBoundsInLocal().getHeight() / 2);
        }
        
        if (targetLabel != null) {
            targetLabel.setLayoutX(targetIntersection[0] - dx * 0.2 - targetLabel.getBoundsInLocal().getWidth() / 2);
            targetLabel.setLayoutY(targetIntersection[1] - dy * 0.2 - targetLabel.getBoundsInLocal().getHeight() / 2);
        }
    }
    
    /**
     * Find the point where a line from (x1,y1) to (x2,y2) intersects with the node boundary
     */
    private double[] findIntersectionPoint(double x1, double y1, double x2, double y2, UMLClassNode node) {
        // Get node bounds
        double nodeX = node.getLayoutX();
        double nodeY = node.getLayoutY();
        double nodeWidth = node.getWidth();
        double nodeHeight = node.getHeight();
        
        // Calculate direction vector
        double dx = x2 - x1;
        double dy = y2 - y1;
        
        // Normalize to get unit vector
        double length = Math.sqrt(dx * dx + dy * dy);
        if (length == 0) return new double[] {x1, y1};
        dx /= length;
        dy /= length;
        
        // Find possible intersection points with the four sides of the node
        double[] intersections = new double[8]; // [x1, y1, x2, y2, x3, y3, x4, y4]
        double[] distances = new double[4];
        
        // Top edge
        double t1 = (nodeY - y1) / dy;
        intersections[0] = x1 + t1 * dx;
        intersections[1] = nodeY;
        distances[0] = (intersections[0] >= nodeX && intersections[0] <= nodeX + nodeWidth) ? t1 : Double.MAX_VALUE;
        
        // Right edge
        double t2 = (nodeX + nodeWidth - x1) / dx;
        intersections[2] = nodeX + nodeWidth;
        intersections[3] = y1 + t2 * dy;
        distances[1] = (intersections[3] >= nodeY && intersections[3] <= nodeY + nodeHeight) ? t2 : Double.MAX_VALUE;
        
        // Bottom edge
        double t3 = (nodeY + nodeHeight - y1) / dy;
        intersections[4] = x1 + t3 * dx;
        intersections[5] = nodeY + nodeHeight;
        distances[2] = (intersections[4] >= nodeX && intersections[4] <= nodeX + nodeWidth) ? t3 : Double.MAX_VALUE;
        
        // Left edge
        double t4 = (nodeX - x1) / dx;
        intersections[6] = nodeX;
        intersections[7] = y1 + t4 * dy;
        distances[3] = (intersections[7] >= nodeY && intersections[7] <= nodeY + nodeHeight) ? t4 : Double.MAX_VALUE;
        
        // Find the closest positive intersection
        int minIndex = 0;
        double minDistance = Double.MAX_VALUE;
        
        for (int i = 0; i < 4; i++) {
            if (distances[i] > 0 && distances[i] < minDistance) {
                minDistance = distances[i];
                minIndex = i;
            }
        }
        
        // Return the intersection point
        return new double[] {intersections[minIndex * 2], intersections[minIndex * 2 + 1]};
    }
    
    public UMLRelationship getRelationship() {
        return relationship;
    }
    
    public UMLClassNode getSourceNode() {
        return sourceNode;
    }
    
    public UMLClassNode getTargetNode() {
        return targetNode;
    }
} 