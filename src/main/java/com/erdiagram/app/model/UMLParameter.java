package com.erdiagram.app.model;

/**
 * Represents a parameter in a UML method.
 */
public class UMLParameter {
    private String name;
    private String type;
    
    public UMLParameter(String name, String type) {
        this.name = name;
        this.type = type;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    @Override
    public String toString() {
        return name + " : " + type;
    }
} 