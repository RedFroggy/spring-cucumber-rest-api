package fr.redfroggy.bdd.restapi.user;

import java.util.List;

public class UserDetailsDTO {

    private String comicName;

    private String city;

    private List<String> mainColors;

    public String getComicName() {
        return comicName;
    }

    public void setComicName(String comicName) {
        this.comicName = comicName;
    }

    public List<String> getMainColors() {
        return mainColors;
    }

    public void setMainColors(List<String> mainColors) {
        this.mainColors = mainColors;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
