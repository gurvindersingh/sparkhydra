package com.lordjoe.distributed.database;

import com.lordjoe.distributed.*;
import org.apache.spark.api.java.*;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.database.BuildLargePeopleDatabase
 * User: Steve
 * Date: 10/16/2014
 */
public class BuildLargePeopleDatabase {

    public static final Random RND = new Random();

    public static final int DATABSES_SIZE = 10000000;
    public static final String DATABSES_NAME = "MillionPeople.txt";

    private final List<String> firstNames = new ArrayList<String>();
    private final List<String> lastNames = new ArrayList<String>();


    protected void buildManyPeople(final String pArg) throws Exception {
        JavaSparkContext sc = SparkUtilities.getCurrentContext();
        populateNames(pArg, sc);


        PrintWriter out = new PrintWriter(new FileWriter(DATABSES_NAME));
        for (int i = 0; i < DATABSES_SIZE; i++) {
            Person p = randomPerson();
            out.append(p.toString() + "\n");
        }
        out.close();
    }

    private Person randomPerson() {
        String first = firstNames.get(RND.nextInt(firstNames.size()));
        String last = lastNames.get(RND.nextInt(lastNames.size()));
        int age = RND.nextInt(80);
        Person ret = new Person();
        ret.setName(first + " " + last);
        ret.setAge(age);
        return ret;
    }

    protected void populateNames(final String pArg, final JavaSparkContext pSc) {
        JavaRDD<Person> people = pSc.textFile(pArg).map(
                new ReadPeopleFile());
        List<Person> collect = people.collect();
        for (Person person : collect) {
            firstNames.add(person.getFirstName());
            lastNames.add(person.getLastName());
        }
    }

    public static void main(String[] args) throws Exception {
        new BuildLargePeopleDatabase().buildManyPeople(args[0]);

    }


}
