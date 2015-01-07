package com.lordjoe.distributed.database;

/**
* com.lordjoe.distributed.database.Person
* User: Steve
* Date: 10/16/2014
*/
public class Person implements IDatabaseBean {
    private String name;
    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getFirstName() {
        return name.split(" ") [0];
    }
    public String getLastName() {
         return name.split(" ") [1];
     }

    public String toString() {
        return getName().trim() + ","  + getAge();
    }
 }
