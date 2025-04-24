package com.erdiagram.app.model;

import java.util.UUID;

/**
 * Represents a data store in a Data Flow Diagram.
 * Data stores hold data that processes can read from or write to.
 */
public class DFDDataStore extends DFDElement {
    
    private String storeId;
    
    /**
     * Creates a new data store with a random ID and the given name.
     */
    public DFDDataStore(String name) {
        this(UUID.randomUUID().toString(), name);
    }
    
    /**
     * Creates a new data store with the specified ID and name.
     */
    public DFDDataStore(String id, String name) {
        super(id, name);
        this.storeId = "D1"; // Default data store ID
    }
    
    /**
     * Gets the data store identifier (e.g., D1, D2).
     */
    public String getStoreId() {
        return storeId;
    }
    
    /**
     * Sets the data store identifier.
     */
    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }
    
    @Override
    public String toString() {
        return storeId + ": " + getName();
    }
} 