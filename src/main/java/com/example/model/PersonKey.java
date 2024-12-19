package com.example.model;

public class PersonKey {
    private static final long serialVersionUID = 1L;

    private String id;

    
    public PersonKey(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PersonKey that = (PersonKey) obj;
        return id == that.id;
    }
}
