package com.erdiagram.app.model;

/**
 * Represents an attribute in a UML class.
 */
public class UMLAttribute {
    private String name;
    private String type;
    private String visibility; // public (+), private (-), protected (#), package (~)
    private boolean isStatic;
    private boolean isFinal;
    
    public UMLAttribute(String name, String type) {
        this.name = name;
        this.type = type;
        this.visibility = "private";
        this.isStatic = false;
        this.isFinal = false;
    }
    
    public UMLAttribute(String name, String type, String visibility) {
        this.name = name;
        this.type = type;
        this.visibility = visibility;
        this.isStatic = false;
        this.isFinal = false;
    }
    
    public UMLAttribute(String name, String type, String visibility, boolean isStatic, boolean isFinal) {
        this.name = name;
        this.type = type;
        this.visibility = visibility;
        this.isStatic = isStatic;
        this.isFinal = isFinal;
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
    
    public boolean isFinal() {
        return isFinal;
    }
    
    public void setFinal(boolean isFinal) {
        this.isFinal = isFinal;
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
            default: return "-";
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getVisibilitySymbol()).append(" ");
        
        if (isStatic) {
            sb.append("static ");
        }
        
        if (isFinal) {
            sb.append("final ");
        }
        
        sb.append(name).append(" : ").append(type);
        
        return sb.toString();
    }
} 