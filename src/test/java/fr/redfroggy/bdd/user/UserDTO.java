package fr.redfroggy.bdd.user;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class UserDTO extends PartialUserDTO {

    private String id;

    private String firstName;

    private int age;

    private String status;

    private UserDTO relatedTo;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UserDTO getRelatedTo() {
        return relatedTo;
    }

    public void setRelatedTo(UserDTO relatedTo) {
        this.relatedTo = relatedTo;
    }
}
