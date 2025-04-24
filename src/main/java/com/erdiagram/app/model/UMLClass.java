package com.erdiagram.app.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a class in a UML diagram.
 */
public class UMLClass {
    private String name;
    private List<UMLAttribute> attributes;
    private List<UMLMethod> methods;
    private String type; // class, interface, abstract class, enum
    private String packageName;
    private double x;
    private double y;
    
    public UMLClass(String name) {
        this.name = name;
        this.attributes = new ArrayList<>();
        this.methods = new ArrayList<>();
        this.type = "class";
    }
    
    public UMLClass(String name, String type) {
        this.name = name;
        this.attributes = new ArrayList<>();
        this.methods = new ArrayList<>();
        this.type = type;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public List<UMLAttribute> getAttributes() {
        return attributes;
    }
    
    public void addAttribute(UMLAttribute attribute) {
        this.attributes.add(attribute);
    }
    
    public List<UMLMethod> getMethods() {
        return methods;
    }
    
    public void addMethod(UMLMethod method) {
        this.methods.add(method);
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getPackageName() {
        return packageName;
    }
    
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    
    public double getX() {
        return x;
    }
    
    public void setX(double x) {
        this.x = x;
    }
    
    public double getY() {
        return y;
    }
    
    public void setY(double y) {
        this.y = y;
    }
    
    @Override
    public String toString() {
        return name;
    }
} 