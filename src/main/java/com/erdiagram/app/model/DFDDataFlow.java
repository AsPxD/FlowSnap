package com.erdiagram.app.model;

import java.util.UUID;

/**
 * Represents a data flow in a Data Flow Diagram.
 * Data flows show the movement of data between processes, data stores, and external entities.
 */
public class DFDDataFlow extends DFDElement {
    
    private DFDElement source;
    private DFDElement target;
    
    /**
     * Creates a new data flow with a random ID, the given name, source and target.
     */
    public DFDDataFlow(String name, DFDElement source, DFDElement target) {
        this(UUID.randomUUID().toString(), name, source, target);
    }
    
    /**
     * Creates a new data flow with the specified ID, name, source and target.
     */
    public DFDDataFlow(String id, String name, DFDElement source, DFDElement target) {
        super(id, name);
        this.source = source;
        this.target = target;
    }
    
    /**
     * Gets the source element of this data flow.
     */
    public DFDElement getSource() {
        return source;
    }
    
    /**
     * Sets the source element of this data flow.
     */
    public void setSource(DFDElement source) {
        this.source = source;
    }
    
    /**
     * Gets the target element of this data flow.
     */
    public DFDElement getTarget() {
        return target;
    }
    
    /**
     * Sets the target element of this data flow.
     */
    public void setTarget(DFDElement target) {
        this.target = target;
    }
    
    @Override
    public String toString() {
        return getName() + " (" + source.getName() + " → " + target.getName() + ")";
    }
} 