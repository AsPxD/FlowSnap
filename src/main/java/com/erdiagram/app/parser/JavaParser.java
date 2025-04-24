package com.erdiagram.app.parser;

import com.erdiagram.app.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for Java code to generate UML class diagrams.
 */
public class JavaParser {
    
    private static final Pattern CLASS_PATTERN = Pattern.compile(
            "(?:public|private|protected)?\\s+(?:abstract\\s+)?(class|interface|enum)\\s+(\\w+)(?:\\s+extends\\s+(\\w+))?(?:\\s+implements\\s+([\\w,\\s]+))?\\s*\\{",
            Pattern.MULTILINE);
    
    private static final Pattern FIELD_PATTERN = Pattern.compile(
            "\\s+(?:(public|private|protected|)\\s+)?(?:(static|final|)\\s+)?(?:(static|final|)\\s+)?(\\w+(?:<[\\w<>\\[\\],\\s]*>)?)\\s+(\\w+)(?:\\s*=\\s*[^;]+)?;",
            Pattern.MULTILINE);
    
    private static final Pattern METHOD_PATTERN = Pattern.compile(
            "\\s+(?:(public|private|protected)\\s+)?(?:(static|abstract|final)\\s+)?(?:(static|abstract|final)\\s+)?(\\w+(?:<[\\w<>\\[\\],\\s]*>)?)\\s+(\\w+)\\s*\\(([^)]*)\\)\\s*(?:\\{|;)",
            Pattern.MULTILINE);
    
    private static final Pattern PARAMETER_PATTERN = Pattern.compile(
            "(\\w+(?:<[\\w<>\\[\\],\\s]*>)?)\\s+(\\w+)(?:\\s*,\\s*)?",
            Pattern.MULTILINE);
    
    private static final Pattern PACKAGE_PATTERN = Pattern.compile(
            "package\\s+([\\w.]+);",
            Pattern.MULTILINE);
    
    private UMLDiagram diagram;
    private Map<String, UMLClass> classMap;
    
    public JavaParser() {
        diagram = new UMLDiagram();
        classMap = new HashMap<>();
    }
    
    /**
     * Parse Java source code and build a UML diagram
     */
    public UMLDiagram parseJavaCode(String javaCode) {
        // Clear any existing data
        diagram.clear();
        classMap.clear();
        
        // Extract package name
        String packageName = parsePackageName(javaCode);
        
        // First pass: Find and create all classes
        Matcher classMatcher = CLASS_PATTERN.matcher(javaCode);
        while (classMatcher.find()) {
            String type = classMatcher.group(1); // class, interface, enum
            String className = classMatcher.group(2);
            String extendsClass = classMatcher.group(3); // may be null
            String implementsInterfaces = classMatcher.group(4); // may be null
            
            UMLClass umlClass = new UMLClass(className, type);
            umlClass.setPackageName(packageName);
            
            // Add the class to our diagram and map
            diagram.addClass(umlClass);
            classMap.put(className, umlClass);
            
            // Set initial positions
            umlClass.setX(100 + diagram.getClasses().size() * 50);
            umlClass.setY(100 + diagram.getClasses().size() * 50);
            
            // Add inheritance relationship if extends is specified
            if (extendsClass != null) {
                // Store the relationship information for later resolution
                // (we might not have parsed the parent class yet)
                inheritanceRelationships.add(new String[]{className, extendsClass});
            }
            
            // Add implementation relationships if implements is specified
            if (implementsInterfaces != null) {
                String[] interfaces = implementsInterfaces.split(",");
                for (String interfaceName : interfaces) {
                    interfaceName = interfaceName.trim();
                    implementationRelationships.add(new String[]{className, interfaceName});
                }
            }
        }
        
        // Second pass: Find and add fields and methods
        for (UMLClass umlClass : diagram.getClasses()) {
            // Extract the class definition and body
            String classPattern = "(?:public|private|protected)?\\s+(?:abstract\\s+)?(?:class|interface|enum)\\s+" + 
                    umlClass.getName() + "(?:\\s+extends\\s+\\w+)?(?:\\s+implements\\s+[\\w,\\s]+)?\\s*\\{([^}]+)\\}";
            Pattern p = Pattern.compile(classPattern, Pattern.DOTALL);
            Matcher m = p.matcher(javaCode);
            
            if (m.find()) {
                String classBody = m.group(1);
                
                // Parse fields
                Matcher fieldMatcher = FIELD_PATTERN.matcher(classBody);
                while (fieldMatcher.find()) {
                    String visibility = fieldMatcher.group(1);
                    String modifier1 = fieldMatcher.group(2); // static or final
                    String modifier2 = fieldMatcher.group(3); // static or final (the other one if both specified)
                    String type = fieldMatcher.group(4);
                    String name = fieldMatcher.group(5);
                    
                    // Set default visibility if not specified
                    if (visibility == null || visibility.isEmpty()) {
                        visibility = "package";
                    }
                    
                    // Determine if static and/or final
                    boolean isStatic = "static".equals(modifier1) || "static".equals(modifier2);
                    boolean isFinal = "final".equals(modifier1) || "final".equals(modifier2);
                    
                    UMLAttribute attribute = new UMLAttribute(name, type, visibility, isStatic, isFinal);
                    umlClass.addAttribute(attribute);
                }
                
                // Parse methods
                Matcher methodMatcher = METHOD_PATTERN.matcher(classBody);
                while (methodMatcher.find()) {
                    String visibility = methodMatcher.group(1);
                    String modifier1 = methodMatcher.group(2); // static, abstract, or final
                    String modifier2 = methodMatcher.group(3); // another modifier if specified
                    String returnType = methodMatcher.group(4);
                    String name = methodMatcher.group(5);
                    String parametersList = methodMatcher.group(6);
                    
                    // Skip constructors (same name as class)
                    if (name.equals(umlClass.getName())) {
                        continue;
                    }
                    
                    // Set default visibility if not specified
                    if (visibility == null || visibility.isEmpty()) {
                        visibility = "package";
                    }
                    
                    // Determine if static and/or abstract
                    boolean isStatic = "static".equals(modifier1) || "static".equals(modifier2);
                    boolean isAbstract = "abstract".equals(modifier1) || "abstract".equals(modifier2);
                    
                    UMLMethod method = new UMLMethod(name, returnType, visibility);
                    method.setStatic(isStatic);
                    method.setAbstract(isAbstract);
                    
                    // Parse parameters
                    if (parametersList != null && !parametersList.trim().isEmpty()) {
                        Matcher paramMatcher = PARAMETER_PATTERN.matcher(parametersList);
                        while (paramMatcher.find()) {
                            String paramType = paramMatcher.group(1);
                            String paramName = paramMatcher.group(2);
                            
                            UMLParameter parameter = new UMLParameter(paramName, paramType);
                            method.addParameter(parameter);
                        }
                    }
                    
                    umlClass.addMethod(method);
                }
            }
        }
        
        // Third pass: Resolve inheritance and implementation relationships
        resolveRelationships();
        
        return diagram;
    }
    
    private String parsePackageName(String javaCode) {
        Matcher matcher = PACKAGE_PATTERN.matcher(javaCode);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }
    
    private List<String[]> inheritanceRelationships = new ArrayList<>();
    private List<String[]> implementationRelationships = new ArrayList<>();
    
    private void resolveRelationships() {
        // Resolve inheritance relationships
        for (String[] inheritance : inheritanceRelationships) {
            String childClassName = inheritance[0];
            String parentClassName = inheritance[1];
            
            UMLClass childClass = classMap.get(childClassName);
            UMLClass parentClass = classMap.get(parentClassName);
            
            // If parent class doesn't exist in our map, create a stub for it
            if (parentClass == null) {
                parentClass = new UMLClass(parentClassName);
                diagram.addClass(parentClass);
                classMap.put(parentClassName, parentClass);
            }
            
            // Create inheritance relationship
            UMLRelationship relationship = new UMLRelationship(childClass, parentClass, UMLRelationship.INHERITANCE);
            diagram.addRelationship(relationship);
        }
        
        // Resolve implementation relationships
        for (String[] implementation : implementationRelationships) {
            String className = implementation[0];
            String interfaceName = implementation[1];
            
            UMLClass implementingClass = classMap.get(className);
            UMLClass interfaceClass = classMap.get(interfaceName);
            
            // If interface class doesn't exist in our map, create a stub for it
            if (interfaceClass == null) {
                interfaceClass = new UMLClass(interfaceName, "interface");
                diagram.addClass(interfaceClass);
                classMap.put(interfaceName, interfaceClass);
            }
            
            // Create implementation relationship
            UMLRelationship relationship = new UMLRelationship(implementingClass, interfaceClass, UMLRelationship.IMPLEMENTATION);
            diagram.addRelationship(relationship);
        }
        
        // Add associations based on field types
        for (UMLClass umlClass : diagram.getClasses()) {
            for (UMLAttribute attribute : umlClass.getAttributes()) {
                String attributeType = attribute.getType();
                
                // Skip primitive types and common types like String, Integer, etc.
                if (isPrimitiveOrCommonType(attributeType)) {
                    continue;
                }
                
                // Check if the attribute type is one of our classes
                UMLClass targetClass = classMap.get(attributeType);
                if (targetClass != null) {
                    // Determine if it's an aggregation or composition (simplified)
                    // Using composition as default for simplicity
                    UMLRelationship relationship = new UMLRelationship(umlClass, targetClass, UMLRelationship.COMPOSITION);
                    relationship.setTargetLabel("1");
                    
                    diagram.addRelationship(relationship);
                }
            }
        }
    }
    
    private boolean isPrimitiveOrCommonType(String type) {
        String[] commonTypes = {"String", "Integer", "Boolean", "Double", "Float", "Long", "Short", "Byte",
                                "Object", "List", "Map", "Set", "Collection", "ArrayList", "HashMap", "HashSet"};
        
        // Check if primitive
        if (type.equals("int") || type.equals("boolean") || type.equals("double") || type.equals("float") ||
            type.equals("long") || type.equals("short") || type.equals("byte") || type.equals("char")) {
            return true;
        }
        
        // Check if common class
        for (String commonType : commonTypes) {
            if (type.contains(commonType)) {
                return true;
            }
        }
        
        return false;
    }
} 