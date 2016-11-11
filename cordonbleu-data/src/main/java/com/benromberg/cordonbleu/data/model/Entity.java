package com.benromberg.cordonbleu.data.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Entity<I> {
    private static final String ID_PROPERTY = "_id";

    @JsonProperty(ID_PROPERTY)
    private final I id;

    public Entity(I id) {
        this.id = id;
    }

    public I getId() {
        return id;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (!getClass().equals(object.getClass())) {
            return false;
        }
        @SuppressWarnings("unchecked")
        Entity<I> other = (Entity<I>) object;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
