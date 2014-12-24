package com.lordjoe.distributed.database;

import org.apache.spark.api.java.function.*;

/**
* com.lordjoe.distributed.database.ReadPeopleFile
* User: Steve
* Date: 10/16/2014
*/
public class ReadPeopleFile implements Function<String, Person> {
    public Person call(String line) throws Exception {
        String[] parts = line.split(",");

        Person person = new Person();
        String name = parts[0].trim();
        person.setName(name);
        person.setAge(Integer.parseInt(parts[1].trim()));

        return person;
    }
}
