package fr.redfroggy.bdd.authentication;


import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.stereotype.Component;

@Component
public class BasicAuthAuthentication implements BddRestTemplateAuthentication {

    final TestRestTemplate template;

    public BasicAuthAuthentication(TestRestTemplate template) {
        this.template = template;
    }

    @Override
    public TestRestTemplate authenticate(String login, String password) {
        return this.template.withBasicAuth(login, password);
    }
}
