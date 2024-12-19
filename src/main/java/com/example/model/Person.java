package com.example.model;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

public class Person {

    private static final long serialVersionUID = 1L;

    @QuerySqlField(index = true)
    private String id;
    
    @QuerySqlField
    private String lastname;
    @QuerySqlField
    private String firstname;
    @QuerySqlField
    private int age;
    @QuerySqlField
    private String city;
    @QuerySqlField
    private String country;
    
    public Person(String id, String lastname, String firstname, int age, String city, String country) {
        this.id = id;
        this.lastname = lastname;
        this.firstname = firstname;
        this.age = age;
        this.city = city;
        this.country = country;
    }
    
    
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        Person person = (Person) obj;
        return id == person.id && (lastname == person.lastname || (lastname != null && lastname.equals(person.lastname)))
                && (firstname == person.firstname || (firstname != null && firstname.equals(person.firstname))) && age == person.age
                && (city == person.city || (city != null && city.equals(person.city)))
                && (country == person.country || (country != null && country.equals(person.country)));
    }
    
    public int hashCode() {
        int result = 17;
        result = 31 * result + (id == null ? 0 : id.hashCode());
        result = 31 * result + (lastname == null ? 0 : lastname.hashCode());
        result = 31 * result + (firstname == null ? 0 : firstname.hashCode());
        result = 31 * result + Integer.hashCode(age);
        result = 31 * result + (city == null ? 0 : city.hashCode());
        result = 31 * result + (country == null ? 0 : country.hashCode());
        return result;
    }
}
