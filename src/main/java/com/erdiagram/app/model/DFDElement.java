package com.erdiagram.app.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Base class for all Data Flow Diagram elements.
 */
public abstract class DFDElement {
    private final String id;
    private final StringProperty name;
    private final StringProperty description;
    private final DoubleProperty xPosition;
    private final DoubleProperty yPosition;
    
    public DFDElement(String id, String name) {
        this.id = id;
        this.name = new SimpleStringProperty(name);
        this.description = new SimpleStringProperty("");
        this.xPosition = new SimpleDoubleProperty(0);
        this.yPosition = new SimpleDoubleProperty(0);
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name.get();
    }
    
    public void setName(String name) {
        this.name.set(name);
    }
    
    public StringProperty nameProperty() {
        return name;
    }
    
    public String getDescription() {
        return description.get();
    }
    
    public void setDescription(String description) {
        this.description.set(description);
    }
    
    public StringProperty descriptionProperty() {
        return description;
    }
    
    public double getXPosition() {
        return xPosition.get();
    }
    
    public void setXPosition(double x) {
        this.xPosition.set(x);
    }
    
    public DoubleProperty xPositionProperty() {
        return xPosition;
    }
    
    public double getYPosition() {
        return yPosition.get();
    }
    
    public void setYPosition(double y) {
        this.yPosition.set(y);
    }
    
    public DoubleProperty yPositionProperty() {
        return yPosition;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DFDElement that = (DFDElement) o;
        return id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
} 