package com.erdiagram.app.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a complete ER diagram with all entities and relationships.
 */
public class ERDiagram {
    private List<Entity> entities;
    private List<Relationship> relationships;
    private String name;
    private Map<String, Entity> entityMap;
    
    public ERDiagram(String name) {
        this.name = name;
        this.entities = new ArrayList<>();
        this.relationships = new ArrayList<>();
        this.entityMap = new HashMap<>();
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public List<Entity> getEntities() {
        return entities;
    }
    
    public List<Relationship> getRelationships() {
        return relationships;
    }
    
    public void addEntity(Entity entity) {
        entities.add(entity);
        entityMap.put(entity.getName().toLowerCase(), entity);
    }
    
    public void addRelationship(Relationship relationship) {
        relationships.add(relationship);
    }
    
    public Entity getEntityByName(String name) {
        return entityMap.get(name.toLowerCase());
    }
    
    public boolean hasEntity(String name) {
        return entityMap.containsKey(name.toLowerCase());
    }
    
    /**
     * Automatically distributes the entities in a grid layout.
     */
    public void autoLayout() {
        int numEntities = entities.size();
        if (numEntities == 0) {
            return;
        }
        
        int cols = (int) Math.ceil(Math.sqrt(numEntities));
        int rows = (int) Math.ceil((double) numEntities / cols);
        
        double cellWidth = 250;
        double cellHeight = 300;
        double startX = 50;
        double startY = 50;
        
        for (int i = 0; i < numEntities; i++) {
            int row = i / cols;
            int col = i % cols;
            
            Entity entity = entities.get(i);
            entity.setX(startX + col * cellWidth);
            entity.setY(startY + row * cellHeight);
        }
    }
    
    /**
     * Clears all entities and relationships.
     */
    public void clear() {
        entities.clear();
        relationships.clear();
        entityMap.clear();
    }
} 