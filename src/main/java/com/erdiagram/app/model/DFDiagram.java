package com.erdiagram.app.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Data Flow Diagram (DFD) with processes, data stores, external entities, and data flows.
 */
public class DFDiagram {
    private String name;
    private String description;
    private List<DFDProcess> processes;
    private List<DFDDataStore> dataStores;
    private List<DFDExternalEntity> externalEntities;
    private List<DFDDataFlow> dataFlows;
    private int level; // 0 for context diagram, 1+ for detail levels

    public DFDiagram(String name) {
        this.name = name;
        this.description = "";
        this.processes = new ArrayList<>();
        this.dataStores = new ArrayList<>();
        this.externalEntities = new ArrayList<>();
        this.dataFlows = new ArrayList<>();
        this.level = 0; // Default to context diagram
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<DFDProcess> getProcesses() {
        return processes;
    }

    public void addProcess(DFDProcess process) {
        processes.add(process);
    }

    public void removeProcess(DFDProcess process) {
        processes.remove(process);
        // Remove any data flows connected to this process
        dataFlows.removeIf(flow -> flow.getSource().equals(process) || flow.getTarget().equals(process));
    }

    public List<DFDDataStore> getDataStores() {
        return dataStores;
    }

    public void addDataStore(DFDDataStore dataStore) {
        dataStores.add(dataStore);
    }

    public void removeDataStore(DFDDataStore dataStore) {
        dataStores.remove(dataStore);
        // Remove any data flows connected to this data store
        dataFlows.removeIf(flow -> flow.getSource().equals(dataStore) || flow.getTarget().equals(dataStore));
    }

    public List<DFDExternalEntity> getExternalEntities() {
        return externalEntities;
    }

    public void addExternalEntity(DFDExternalEntity entity) {
        externalEntities.add(entity);
    }

    public void removeExternalEntity(DFDExternalEntity entity) {
        externalEntities.remove(entity);
        // Remove any data flows connected to this external entity
        dataFlows.removeIf(flow -> flow.getSource().equals(entity) || flow.getTarget().equals(entity));
    }

    public List<DFDDataFlow> getDataFlows() {
        return dataFlows;
    }

    public void addDataFlow(DFDDataFlow dataFlow) {
        dataFlows.add(dataFlow);
    }

    public void removeDataFlow(DFDDataFlow dataFlow) {
        dataFlows.remove(dataFlow);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public String toString() {
        return "DFDiagram{" +
                "name='" + name + '\'' +
                ", level=" + level +
                ", processes=" + processes.size() +
                ", dataStores=" + dataStores.size() +
                ", externalEntities=" + externalEntities.size() +
                ", dataFlows=" + dataFlows.size() +
                '}';
    }
} 