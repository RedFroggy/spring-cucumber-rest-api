package fr.redfroggy.bdd.glue;

import fr.redfroggy.bdd.authentication.BddRestTemplateAuthentication;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultRestApiBddStepDefinition extends AbstractBddStepDefinition {

    private final BddRestTemplateAuthentication templateAuthentication;

    public DefaultRestApiBddStepDefinition(TestRestTemplate testRestTemplate, BddRestTemplateAuthentication templateAuthentication) {
        super(testRestTemplate);
        this.templateAuthentication = templateAuthentication;
    }

    @Given("^I authenticate with login/password (.*)/(.*)$")
    public void setAuthenticateUser(String login, String password) {
        this.template = this.templateAuthentication.authenticate(login, password);
    }

    /**
     * First step is to set
     *
     * @param uri
     *            base uri
     */
    @Given("^baseUri is (.*)$")
    public void baseUri(String uri) {
        assertThat(uri).isNotEmpty();
        baseUri = uri;
    }

    /**
     * Set the request body A json string structure is accepted The body will be
     * parse to be sure the json is valid
     *
     * @param body
     *             body to send
     * @throws IOException
     *                     parsing exception
     */
    @Given("^I set body to (.*)$")
    public void setBodyTo(String body) throws IOException {
        this.setBody(body);
    }

    /**
     * Add a new http header
     *
     * @param headerName
     *                    header name
     * @param headerValue
     *                    header value
     */
    @Given("^I set (.*) header to (.*)$")
    public void header(String headerName, String headerValue) {
        this.setHeader(headerName, headerValue);
    }

    @Given("^I set query parameter (.*) to (.*)")
    public void queryParameter(String param , String value) {
        this.addQueryParameters(Collections.singletonMap(param, value));
    }
    /**
     * Add multiple http headers
     *
     * @param parameters
     *                   Map of headers to send with name and value
     */
    @Given("^I set headers to:$")
    public void headers(Map<String, String> parameters) {
        this.addHeaders(parameters);
    }

    /**
     * Perform an HTTP GET request Url will be baseUri+resource The trailing slash
     * is checked, so the value can be "/resource" or "resource"
     *
     * @param resource
     *                 resource name
     */
    @When("^I GET (.*)$")
    public void get(String resource) {
        this.request(resource, HttpMethod.GET);
    }

    /**
     * Perform an HTTP POST request. It supposes that a body exists, i.e that
     * {@link #setBodyTo} must have been called Url will be baseUri+resource The
     * trailing slash is checked, so the value can be "/resource" or "resource"
     *
     * @param resource
     *                 resource name
     */
    @When("^I POST (.*)$")
    public void post(String resource) {
        this.request(resource, HttpMethod.POST);
    }

    /**
     * Perform an HTTP PUT request. It supposes that a body exists, i.e that
     * {@link #setBodyTo} must have been called Url will be baseUri+resource The
     * trailing slash is checked, so the value can be "/resource" or "resource"
     *
     * @param resource
     *                 resource name
     */
    @When("^I PUT (.*)$")
    public void put(String resource) {
        this.request(resource, HttpMethod.PUT);
    }

    /**
     * Perform an HTTP DELETE request. It supposes that a body exists, i.e that
     * {@link #setBodyTo} must have been called Url will be baseUri+resource The
     * trailing slash is checked, so the value can be "/resource" or "resource"
     *
     * @param resource
     *                 resource name
     */
    @When("^I DELETE (.*)$")
    public void delete(String resource) {
        this.request(resource, HttpMethod.DELETE);
    }

    /**
     * Perform an HTTP PATCH request. It supposes that a body exists, i.e that
     * {@link #setBodyTo} must have been called Url will be baseUri+resource The
     * trailing slash is checked, so the value can be "/resource" or "resource"
     *
     * @param resource
     *                 resource name
     */
    @When("^I PATCH (.*)$")
    public void patch(String resource) {
        this.request(resource, HttpMethod.PATCH);
    }

    /**
     * Perform an HTTP HEAD request. Url will be baseUri+resource The trailing slash
     * is checked, so the value can be "/resource" or "resource"
     *
     * @param resource
     *                 resource name
     */
    @When("^I HEAD (.*)$")
    public void head(String resource) {
        this.request(resource, HttpMethod.HEAD);
    }

    /**
     * Test response status code is equal to a given status
     *
     * @param status
     *               status code to test
     */
    @Then("^response code should be (\\d+)$")
    public void responseCode(Integer status) {
        this.checkStatus(status, false);
    }

    /**
     * Test response status code is not equal to a given code
     *
     * @param status
     *               status code to test
     */
    @Then("^response code should not be (\\d+)$")
    public void notResponseCode(Integer status) {
        this.checkStatus(status, true);
    }

    /**
     * Test that a given http header exists
     *
     * @param headerName
     *                   name of the header to find
     */
    @Then("^response header (.*) should exist$")
    public void headerExists(String headerName) {
        this.checkHeaderExists(headerName, false);
    }

    /**
     * Test that a given http header does not exists
     *
     * @param headerName
     *                   name of the header to not find
     */
    @Then("^response header (.*) should not exist$")
    public void headerNotExists(String headerName) {
        this.checkHeaderExists(headerName, true);
    }

    /**
     * Test if a given header value is matching the expected value
     *
     * @param headerName
     *                    name of the header to find
     * @param headerValue
     *                    expected value of the header
     */
    @Then("^response header (.*) should be (.*)$")
    public void headerEqual(String headerName, String headerValue) {
        this.checkHeaderEqual(headerName, headerValue, false);
    }

    /**
     * Test if a given header value is not matching the expected value
     *
     * @param headerName
     *                    name of the header to find
     * @param headerValue
     *                    unexpected value of the header
     */
    @Then("^response header (.*) should not be (.*)$")
    public void headerNotEqual(String headerName, String headerValue) {
        this.checkHeaderEqual(headerName, headerValue, true);
    }

    /**
     * Test if the response body is a valid json. The string response is parsed as a
     * JSON object ot check the integrity
     *
     * @throws IOException
     *                     if the body is not json format
     */
    @Then("^response body should be valid json$")
    public void bodyIsValid() throws IOException {
        this.checkJsonBody();
    }

    /**
     * Test if the response body contains a given value
     *
     * @param bodyValue
     *                  value which the body must contain
     */
    @Then("^response body should contain (.*)$")
    public void bodyContains(String bodyValue) {
        this.checkBodyContains(bodyValue);
    }

    /**
     * Test the given json path query exists in the response body
     *
     * @param jsonPath
     *                 json path query
     */
    @Then("^response body path (.*) should exists$")
    public void bodyPathExists(String jsonPath) {
        this.checkJsonPathExists(jsonPath);
    }

    /**
     * Test the given json path query doesn't exist in the response body
     *
     * @param jsonPath
     *                 json path query
     */
    @Then("^response body path (.*) should not exist$")
    public void bodyPathDoesntExist(String jsonPath) {
        this.checkJsonPathDoesntExist(jsonPath);
    }

    /**
     * Test the given json path exists in the response body and match the given
     * value
     *
     * @param jsonPath
     *                 json path query
     * @param value
     *                 expected value
     */
    @Then("^response body path (.*) should be (.*)$")
    public void bodyPathEqual(String jsonPath, String value) {
        this.checkJsonPath(jsonPath, value, false);
    }

    @Then("^response body path (.*) should not have content$")
    public void emptyBodyPath(String jsonPath) {
        Object json = getJsonPath(jsonPath);

        if (json != null) {
            if (json instanceof Collection<?>) {
                assertThat(((Collection<?>) json).isEmpty()).isTrue();
            }
        }
    }

    /**
     * Test the given json path exists and does not match the given value
     *
     * @param jsonPath
     *                 json path query
     * @param value
     *                 unexpected value
     */
    @Then("^response body path (.*) should not be (.*)$")
    public void bodyPathNotEqual(String jsonPath, String value) {
        this.checkJsonPath(jsonPath, value, true);
    }

    /**
     * Test if the json path exists in the response body and is array typed
     *
     * @param jsonPath
     *                 json path query
     */
    @Then("^response body is typed as array for path (.*)$")
    public void bodyPathIsArray(String jsonPath) {
        this.checkJsonPathIsArray(jsonPath, -1);
    }

    /**
     * Test if the json path exists in the response body, is array typed and as the
     * expected length
     *
     * @param jsonPath
     *                 json path query
     * @param length
     *                 expected length
     */
    @Then("^response body is typed as array using path (.*) with length (\\d+)$")
    public void bodyPathIsArrayWithLength(String jsonPath, int length) {
        this.checkJsonPathIsArray(jsonPath, length);
    }

    /**
     * Store a given response header to the scenario scope The purpose is to reuse
     * its value in another scenario The most common use case is the authentication
     * process
     *
     * @param headerName
     *                    http header name
     * @param headerAlias
     *                    http header alias (which will be stored in the scenario
     *                    scope)
     * @see fr.redfroggy.bdd.scope.ScenarioScope
     */
    @Then("^I store the value of response header (.*) as (.*) in scenario scope$")
    public void storeResponseHeader(String headerName, String headerAlias) {
        this.storeHeader(headerName, headerAlias);
    }

    /**
     * Store a given json path value to the scenario scope The purpose is to reuse
     * its value in another scenario The most common use case is the authentication
     * process
     *
     * @param jsonPath
     *                      json path query
     * @param jsonPathAlias
     *                      json path alias (which will be stored in the scenario
     *                      scope)
     * @see fr.redfroggy.bdd.scope.ScenarioScope
     */
    @Then("^I store the value of body path (.*) as (.*) in scenario scope$")
    public void storeResponseJsonPath(String jsonPath, String jsonPathAlias) {
        this.storeJsonPath(jsonPath, jsonPathAlias);
    }

    /**
     * Test a scenario scope variable value match the expected one
     *
     * @param property
     *                 scenario scope property
     * @param value
     *                 expected property value
     * @see fr.redfroggy.bdd.scope.ScenarioScope
     */
    @Then("^value of scenario variable (.*) should be (.*)$")
    public void scenarioVariableIsValid(String property, String value) {
        this.checkScenarioVariable(property, value);
    }
}
