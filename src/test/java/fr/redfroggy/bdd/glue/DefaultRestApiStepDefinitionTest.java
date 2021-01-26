package fr.redfroggy.bdd.glue;

import fr.redfroggy.bdd.authentication.BddRestTemplateAuthentication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ContextConfiguration;

/**
 * This file is mandatory for cucumber tests because it needs a Cucumber context to be able to work.
 * In this file you can add your own steps implementation.
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class DefaultRestApiStepDefinitionTest implements BddRestTemplateAuthentication {

    final TestRestTemplate template;

    public DefaultRestApiStepDefinitionTest(TestRestTemplate template) {
        this.template = template;
    }

    @Override
    public TestRestTemplate authenticate(String login, String password) {
        return this.template.withBasicAuth(login, password);
    }
}
