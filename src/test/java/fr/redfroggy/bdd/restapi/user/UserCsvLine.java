package fr.redfroggy.bdd.restapi.user;

import com.opencsv.bean.CsvBindByName;

public class UserCsvLine {

    @CsvBindByName(column = "id")
    private String id;

    @CsvBindByName(column = "First name")
    private String firstName;

    @CsvBindByName(column = "Last name")
    private String lastName;

    @CsvBindByName(column = "Age")
    private int age;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
