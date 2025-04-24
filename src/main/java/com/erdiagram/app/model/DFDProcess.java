package com.erdiagram.app.model;

import java.util.UUID;

/**
 * Represents a process in a Data Flow Diagram.
 * Processes transform data inputs into data outputs.
 */
public class DFDProcess extends DFDElement {
    
    private String processNumber;
    
    /**
     * Creates a new process with a random ID and the given name.
     */
    public DFDProcess(String name) {
        this(UUID.randomUUID().toString(), name);
    }
    
    /**
     * Creates a new process with the specified ID and name.
     */
    public DFDProcess(String id, String name) {
        super(id, name);
        this.processNumber = "1"; // Default process number
    }
    
    /**
     * Gets the process number (used in hierarchical DFDs).
     */
    public String getProcessNumber() {
        return processNumber;
    }
    
    /**
     * Sets the process number.
     */
    public void setProcessNumber(String processNumber) {
        this.processNumber = processNumber;
    }
    
    @Override
    public String toString() {
        return processNumber + ": " + getName();
    }
} 