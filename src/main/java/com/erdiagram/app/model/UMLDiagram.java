package com.erdiagram.app.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a complete UML diagram with classes and relationships.
 */
public class UMLDiagram {
    private List<UMLClass> classes;
    private List<UMLRelationship> relationships;
    private String name;
    
    public UMLDiagram() {
        this.classes = new ArrayList<>();
        this.relationships = new ArrayList<>();
    }
    
    public UMLDiagram(String name) {
        this.classes = new ArrayList<>();
        this.relationships = new ArrayList<>();
        this.name = name;
    }
    
    public List<UMLClass> getClasses() {
        return classes;
    }
    
    public void addClass(UMLClass umlClass) {
        this.classes.add(umlClass);
    }
    
    public void removeClass(UMLClass umlClass) {
        this.classes.remove(umlClass);
        
        // Remove all relationships involving this class
        List<UMLRelationship> toRemove = new ArrayList<>();
        for (UMLRelationship relationship : relationships) {
            if (relationship.getSource().equals(umlClass) || relationship.getTarget().equals(umlClass)) {
                toRemove.add(relationship);
            }
        }
        this.relationships.removeAll(toRemove);
    }
    
    public List<UMLRelationship> getRelationships() {
        return relationships;
    }
    
    public void addRelationship(UMLRelationship relationship) {
        this.relationships.add(relationship);
    }
    
    public void removeRelationship(UMLRelationship relationship) {
        this.relationships.remove(relationship);
    }
    
    public UMLClass findClassByName(String name) {
        for (UMLClass umlClass : classes) {
            if (umlClass.getName().equals(name)) {
                return umlClass;
            }
        }
        return null;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Gets all relationships where the given class is either source or target
     */
    public List<UMLRelationship> getRelationshipsForClass(UMLClass umlClass) {
        List<UMLRelationship> result = new ArrayList<>();
        for (UMLRelationship relationship : relationships) {
            if (relationship.getSource().equals(umlClass) || relationship.getTarget().equals(umlClass)) {
                result.add(relationship);
            }
        }
        return result;
    }
    
    /**
     * Clear all classes and relationships from the diagram
     */
    public void clear() {
        classes.clear();
        relationships.clear();
    }
} 