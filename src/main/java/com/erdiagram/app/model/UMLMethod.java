package com.erdiagram.app.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a method in a UML class.
 */
public class UMLMethod {
    private String name;
    private String returnType;
    private List<UMLParameter> parameters;
    private String visibility; // public (+), private (-), protected (#), package (~)
    private boolean isStatic;
    private boolean isAbstract;
    
    public UMLMethod(String name, String returnType) {
        this.name = name;
        this.returnType = returnType;
        this.parameters = new ArrayList<>();
        this.visibility = "public";
        this.isStatic = false;
        this.isAbstract = false;
    }
    
    public UMLMethod(String name, String returnType, String visibility) {
        this.name = name;
        this.returnType = returnType;
        this.parameters = new ArrayList<>();
        this.visibility = visibility;
        this.isStatic = false;
        this.isAbstract = false;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getReturnType() {
        return returnType;
    }
    
    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }
    
    public List<UMLParameter> getParameters() {
        return parameters;
    }
    
    public void addParameter(UMLParameter parameter) {
        this.parameters.add(parameter);
    }
    
    public String getVisibility() {
        return visibility;
    }
    
    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }
    
    public boolean isStatic() {
        return isStatic;
    }
    
    public void setStatic(boolean isStatic) {
        this.isStatic = isStatic;
    }
    
    public boolean isAbstract() {
        return isAbstract;
    }
    
    public void setAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
    }
    
    /**
     * Gets the visibility symbol (+, -, #, ~)
     */
    public String getVisibilitySymbol() {
        switch (visibility) {
            case "public": return "+";
            case "private": return "-";
            case "protected": return "#";
            case "package": return "~";
            default: return "+";
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getVisibilitySymbol()).append(" ");
        
        if (isStatic) {
            sb.append("static ");
        }
        
        if (isAbstract) {
            sb.append("abstract ");
        }
        
        sb.append(name).append("(");
        
        // Add parameters
        for (int i = 0; i < parameters.size(); i++) {
            sb.append(parameters.get(i));
            if (i < parameters.size() - 1) {
                sb.append(", ");
            }
        }
        
        sb.append(")");
        
        if (returnType != null && !returnType.equals("void")) {
            sb.append(" : ").append(returnType);
        }
        
        return sb.toString();
    }
} 