package fr.redfroggy.bdd.restapi.glue;

import fr.redfroggy.bdd.restapi.authentication.BddRestTemplateAuthentication;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
    @Given("^http baseUri is (.*)$")
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
    @Given("^I set http body to (.*)$")
    public void setBodyTo(String body) throws IOException {
        this.setBody(body);
    }

    @Given("^I set http body with file (.*)$")
    public void setBodyWithFile(String filePath) throws IOException {
        super.setBodyWithFile(filePath);
    }
  
    @And("^I set http body path (.*) to (.*)$")
    public void setBodyWithJsonPath(String jsonPath, String value) {
        this.setBodyPathWithValue(jsonPath, value);
    }

    /**
     * Add a new http header
     *
     * @param headerName
     *                    header name
     * @param headerValue
     *                    header value
     */
    @Given("^I set (.*) http header to (.*)$")
    public void header(String headerName, String headerValue) {
        this.setHeader(headerName, headerValue);
    }

    @Given("^I set http query parameter (.*) to (.*)")
    public void queryParameter(String param , String value) {
        this.addQueryParameters(Collections.singletonMap(param, value));
    }
    /**
     * Add multiple http headers
     *
     * @param parameters
     *                   Map of headers to send with name and value
     */
    @Given("^I set http headers to:$")
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


    @When("^I send a multipart (.*) request to (.*) with:$")
    public void uploadFile(String method, String resource, List<Map<String, String>> data) {
        this.postMultipart(method, resource, data);
    }

    /**
     * Test response status code is equal to a given status
     *
     * @param status
     *               status code to test
     */
    @Then("^http response code should be (\\d+)$")
    public void responseCode(String status) {
        this.checkStatus(status, false);
    }

    /**
     * Test response status code is not equal to a given code
     *
     * @param status
     *               status code to test
     */
    @Then("^http response code should not be (\\d+)$")
    public void notResponseCode(String status) {
        this.checkStatus(status, true);
    }

    /**
     * Test that a given http header exists
     *
     * @param headerName
     *                   name of the header to find
     */
    @Then("^http response header (.*) should exist$")
    public void headerExists(String headerName) {
        this.checkHeaderExists(headerName, false);
    }

    /**
     * Test that a given http header does not exists
     *
     * @param headerName
     *                   name of the header to not find
     */
    @Then("^http response header (.*) should not exist$")
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
    @Then("^http response header (.*) should be (.*)$")
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
    @Then("^http response header (.*) should not be (.*)$")
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
    @Then("^http response body should be valid json$")
    public void bodyIsValid() throws IOException {
        this.checkJsonBody();
    }

    /**
     * Test if the response body contains a given value
     *
     * @param bodyValue
     *                  value which the body must contain
     */
    @Then("^http response body should contain (.*)$")
    public void bodyContains(String bodyValue) {
        this.checkBodyContains(bodyValue);
    }

    /**
     * Test the given json path query exists in the response body
     *
     * @param jsonPath
     *                 json path query
     */
    @Then("^http response body path (.*) should exists$")
    public void bodyPathExists(String jsonPath) {
        this.checkJsonPathExists(jsonPath, true);
    }

    /**
     * Test the given json path query doesn't exist in the response body
     *
     * @param jsonPath
     *                 json path query
     */
    @Then("^http response body path (.*) should not exist$")
    public void bodyPathDoesntExist(String jsonPath) {
        this.checkJsonPathDoesntExist(jsonPath);
    }

    /**
     * Test the given json path exists in the response body and match the given
     * value
     */
    @Then("^http response body path (.*) should be (.*)$")
    public void bodyPathShouldBeEqualTo(String jsonPath, String value) {
        this.checkJsonPath(jsonPath, value, false, false);
    }

    /**
     * Test the given json path exists and does not match the given value
     */
    @Then("^http response body path (.*) should not be (.*)$")
    public void bodyPathShouldNotBeEqualTo(String jsonPath, String value) {
        this.checkJsonPath(jsonPath, value, true, false);
    }

    @Then("^http response body path (.*) must be (.*)$")
    public void bodyPathMustBeEqualTo(String jsonPath, String value) {
        this.checkJsonPath(jsonPath, value, false, true);
    }

    @Then("^http response body path (.*) must not be (.*)$")
    public void bodyPathMustNotBeEqualTo(String jsonPath, String value) {
        this.checkJsonPath(jsonPath, value, true, true);
    }

    @Then("^http response body path (.*) should not have content$")
    public void bodyPathShouldNotHaveContent(String jsonPath) {
        Object json = getJsonPathValue(jsonPath, true);

        if (json != null) {
            assertThat(((Collection<?>) json).isEmpty()).isTrue();
        }
    }

    /**
     * Test if the json path exists in the response body and is array typed
     *
     * @param jsonPath
     *                 json path query
     */
    @Then("^http response body is typed as array for path (.*)$")
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
    @Then("^http response body is typed as array using path (.*) with length (\\d+)$")
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
     * @see fr.redfroggy.bdd.restapi.scope.ScenarioScope
     */
    @Then("^I store the value of http response header (.*) as (.*) in scenario scope$")
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
     * @see fr.redfroggy.bdd.restapi.scope.ScenarioScope
     */
    @Then("^I store the value of http body path (.*) as (.*) in scenario scope$")
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
     * @see fr.redfroggy.bdd.restapi.scope.ScenarioScope
     */
    @Then("^http value of scenario variable (.*) should be (.*)$")
    public void scenarioVariableIsValid(String property, String value) {
        this.checkScenarioVariable(property, value);
    }

    @Given("^I mock third party api call (.*) (.*) with return code (.*), content type: (.*) and body: (.*)$")
    public void mockThirdPartyApiCallWithJSON(String method, String resource, int status, String contentType, String willReturnJson) throws URISyntaxException {
        mockThirdPartyApiCall(method, resource, status, MediaType.valueOf(contentType).toString(), willReturnJson);
    }

    @Given("^I mock third party api call (.*) (.*) with return code (.*), content type: (.*) and file: (.*)$")
    public void mockThirdPartyApiCallWithFileContent(String method, String resource, int status, String contentType, String filePath) throws IOException, URISyntaxException {
        mockThirdPartyApiCall(method, resource, status, MediaType.valueOf(contentType).toString(), StreamUtils
                .copyToString(getClass().getClassLoader()
                        .getResourceAsStream(filePath), StandardCharsets.UTF_8));
    }
}
