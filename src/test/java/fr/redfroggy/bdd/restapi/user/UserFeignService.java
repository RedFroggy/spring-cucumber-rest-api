package fr.redfroggy.bdd.restapi.user;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class UserFeignService {

    @Value("${marvel.api.host}")
    private String apiHost;

    private final TestRestTemplate restTemplate;

    public UserFeignService(TestRestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<UserDetailsDTO> getUserDetails(String id, String format) {
        String url = StringUtils.isNotBlank(format) ? apiHost + "/public/characters/" + id + "?format="
                + format : apiHost + "/public/characters/" + id;
        return this.restTemplate.getForEntity(url, UserDetailsDTO.class);
    }

}
