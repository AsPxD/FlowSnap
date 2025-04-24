package com.erdiagram.app.model;

import java.util.UUID;

/**
 * Represents an external entity in a Data Flow Diagram.
 * External entities are sources or destinations of data outside the system boundary.
 */
public class DFDExternalEntity extends DFDElement {
    
    private String entityId;
    
    /**
     * Creates a new external entity with a random ID and the given name.
     */
    public DFDExternalEntity(String name) {
        this(UUID.randomUUID().toString(), name);
    }
    
    /**
     * Creates a new external entity with the specified ID and name.
     */
    public DFDExternalEntity(String id, String name) {
        super(id, name);
        this.entityId = "E1"; // Default entity ID
    }
    
    /**
     * Gets the external entity identifier (e.g., E1, E2).
     */
    public String getEntityId() {
        return entityId;
    }
    
    /**
     * Sets the external entity identifier.
     */
    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }
    
    @Override
    public String toString() {
        return entityId + ": " + getName();
    }
} 