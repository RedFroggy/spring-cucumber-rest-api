package fr.redfroggy.bdd.restapi.user;

import java.util.List;

public final class UserDTO extends PartialUserDTO {

    private String id;

    private String firstName;

    private int age;

    private UserDTO relatedTo;

    private List<String> sessionIds;

    private UserDetailsDTO details;

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

    public UserDTO getRelatedTo() {
        return relatedTo;
    }

    public void setRelatedTo(UserDTO relatedTo) {
        this.relatedTo = relatedTo;
    }

    public List<String> getSessionIds() {
        return sessionIds;
    }

    public void setSessionIds(List<String> sessionIds) {
        this.sessionIds = sessionIds;
    }

    public UserDetailsDTO getDetails() {
        return details;
    }

    public void setDetails(UserDetailsDTO details) {
        this.details = details;
    }
}
