package fr.redfroggy.bdd.authentication;

import org.springframework.boot.test.web.client.TestRestTemplate;

public interface BddRestTemplateAuthentication {

    TestRestTemplate authenticate(String login, String password);
}
