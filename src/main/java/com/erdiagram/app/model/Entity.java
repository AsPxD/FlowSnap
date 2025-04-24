package com.erdiagram.app.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a database table as an entity in the ER diagram.
 */
public class Entity {
    private String name;
    private List<Attribute> attributes;
    private double x;
    private double y;

    public Entity(String name) {
        this.name = name;
        this.attributes = new ArrayList<>();
        this.x = 0;
        this.y = 0;
    }

    public String getName() {
        return name;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void addAttribute(Attribute attribute) {
        attributes.add(attribute);
    }

    public List<Attribute> getPrimaryKeys() {
        List<Attribute> primaryKeys = new ArrayList<>();
        for (Attribute attribute : attributes) {
            if (attribute.isPrimaryKey()) {
                primaryKeys.add(attribute);
            }
        }
        return primaryKeys;
    }

    public List<Attribute> getForeignKeys() {
        List<Attribute> foreignKeys = new ArrayList<>();
        for (Attribute attribute : attributes) {
            if (attribute.isForeignKey()) {
                foreignKeys.add(attribute);
            }
        }
        return foreignKeys;
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