package fr.redfroggy.test.bdd.glue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import fr.redfroggy.test.bdd.customization.CustomErrorResponseHandler;
import fr.redfroggy.test.bdd.scope.ScenarioScope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;

/**
 * Abstract step definition implementation
 * Http request are made using {@link RestTemplate} RestTemplate
 */
@SuppressWarnings("unchecked")
abstract class AbstractStepDefinitionConsumer {

    //Stored base uri
    String baseUri;

    //Parsed http request json body
    Map<String, Object> body;

    //Rest template
    private RestTemplate template;

    //Http headers
    private HttpHeaders headers;

    //List of query params
    private Map<String, String> queryParams;

    //Stored http response
    private ResponseEntity<String> responseEntity;
    private ObjectMapper objectMapper;

    //Scenario Scope
    private ScenarioScope scenarioScope;

    AbstractStepDefinitionConsumer() {
        template = new RestTemplate();
        template.setErrorHandler(new CustomErrorResponseHandler());
        objectMapper = new ObjectMapper();
        scenarioScope = new ScenarioScope();
        headers = new HttpHeaders();
        queryParams = new HashMap<>();
    }

    /**
     * Add an http header
     * {@link #headers}
     * @param name header name
     * @param value header value
     */
    void setHeader(String name, String value) {
        Assert.notNull(name);
        Assert.notNull(value);
        headers.set(name, value);
    }

    /**
     * Add an HTTP query parameter
     * {@link #queryParams}
     * @param newParams Map of parameters
     */
    void addQueryParameters(Map<String, String> newParams){
        Assert.notNull(newParams);
        Assert.isTrue(!newParams.isEmpty());
        queryParams.putAll(newParams);
    }

    /**
     * Add multiple http headers
     * {@link #headers}
     * @param newHeaders Map of headers
     */
    void addHeaders(Map<String, String> newHeaders){
        Assert.notNull(newHeaders);
        Assert.isTrue(!newHeaders.isEmpty());
        newHeaders.entrySet().forEach(headerEntry -> {

            List<String> headerValues = this.headers.get(headerEntry.getKey());
            if(headerValues == null) {
                headerValues = Collections.singletonList(headerEntry.getValue());
            } else {
                headerValues.add(headerEntry.getValue());
            }
            this.headers.put(headerEntry.getKey(), headerValues);
        });
    }

    /**
     * Set the http request body (POST request for example)
     * {@link #body}
     * @param body json string body
     * @throws IOException json parse exception
     */
    void setBody(String body) throws IOException {
        Assert.notNull(body);
        Assert.isTrue(!body.isEmpty());
        this.body = objectMapper.readValue(body, Map.class);
    }

    /**
     * Perform an http request
     * Store the http response to responseEntity {@link #responseEntity}
     * @param resource resource to consume
     * @param method HttpMethod
     */
    void request(String resource, HttpMethod method) {
        Assert.notNull(resource);
        Assert.isTrue(!resource.isEmpty());

        Assert.notNull(method);

        boolean writeMode = !HttpMethod.GET.equals(method)
                && !HttpMethod.DELETE.equals(method)
                && !HttpMethod.OPTIONS.equals(method)
                && !HttpMethod.HEAD.equals(method);

        if(!resource.contains("/")) {
            resource = "/" + resource;
        }

        HttpEntity httpEntity;

        if(writeMode) {
            Assert.notNull(body);
            httpEntity = new HttpEntity(body, headers);
        } else {
            httpEntity = new HttpEntity(headers);
        }

        responseEntity = this.template.exchange(baseUri+resource, method, httpEntity, String.class,queryParams);
        Assert.notNull(responseEntity);
    }

    /**
     * Check http response status code
     * @param status expected/unexpected status
     * @param isNot if true, test equality, inequality if false
     */
    void checkStatus(int status, boolean isNot){
        Assert.isTrue(status > 0);
        Assert.isTrue(isNot ? responseEntity.getStatusCodeValue() != status : responseEntity.getStatusCodeValue() == status);
    }

    /**
     * Check header existence
     * @param headerName name of the header to find
     * @param isNot if true, test equality, inequality if false
     * @return header values if found, null otherwise
     */
    List<String> checkHeaderExists(String headerName, boolean isNot){
        Assert.notNull(headerName);
        Assert.isTrue(!headerName.isEmpty());
        Assert.notNull(responseEntity.getHeaders());
        if(!isNot) {
            Assert.notNull(responseEntity.getHeaders().get(headerName));
            return responseEntity.getHeaders().get(headerName);
        } else {
            Assert.isNull(responseEntity.getHeaders().get(headerName));
            return null;
        }
    }

    /**
     * Test header value
     * @param headerName name of the header to test
     * @param headerValue expected/unexpected value
     * @param isNot if true, test equality, inequality if false
     */
    void checkHeaderEqual(String headerName, String headerValue, boolean isNot){
        Assert.notNull(headerName);
        Assert.isTrue(!headerName.isEmpty());

        Assert.notNull(headerValue);
        Assert.isTrue(!headerValue.isEmpty());

        Assert.notNull(responseEntity.getHeaders());

        if(!isNot) {
            Assert.isTrue(responseEntity.getHeaders().get(headerName).contains(headerValue));
        } else {
            Assert.isTrue(!responseEntity.getHeaders().get(headerName).contains(headerValue));
        }
    }

    /**
     * Test response body is json typed
     * {@link #responseEntity}
     * @throws IOException json parse exception
     */
    void checkJsonBody() throws IOException {
        String body = responseEntity.getBody();
        Assert.notNull(body);
        Assert.isTrue(!body.isEmpty());

        // Check body json structure is valid
        objectMapper.readValue(body,Map.class);
    }

    /**
     * Test body content
     * {@link #responseEntity}
     * @param bodyValue expected content
     */
    void checkBodyContains(String bodyValue) {
        Assert.notNull(bodyValue);
        Assert.isTrue(!bodyValue.isEmpty());

        Assert.isTrue(responseEntity.getBody().contains(bodyValue));
    }

    /**
     * Test json path validity
     * @param jsonPath json path query
     * @return value found using <code>jsonPath</code>
     */
    Object checkJsonPathExists(String jsonPath){
        return getJsonPath(jsonPath);
    }

    /**
     * Test json path value
     * @param jsonPath json path query
     * @param jsonValue expected/unexpected json path value
     * @param isNot if true, test equality, inequality if false
     */
    void checkJsonPath(String jsonPath, String jsonValue, boolean isNot){
        Object pathValue = checkJsonPathExists(jsonPath);
        Assert.isTrue(!String.valueOf(pathValue).isEmpty());

        if(!isNot) {
            Assert.isTrue(pathValue.equals(jsonValue));
        } else {
            Assert.isTrue(!pathValue.equals(jsonValue));
        }
    }

    /**
     * Test json path is array typed and its size is matching the expected length
     * @param jsonPath json path query
     * @param length expected length (-1 to not control the size)
     */
    void checkJsonPathIsArray(String jsonPath, int length){
        Object pathValue = getJsonPath(jsonPath);
        Assert.isTrue(pathValue instanceof Collection);
        if(length != -1) {
            Assert.isTrue(((Collection)pathValue).size() == length);
        }
    }

    /**
     * Store a given header in the scenario scope using the given alias
     * @param headerName header to save
     * @param headerAlias new header name in the scenario scope
     */
    void storeHeader(String headerName, String headerAlias){

        Assert.notNull(headerName);
        Assert.isTrue(!headerName.isEmpty());

        Assert.notNull(headerAlias);
        Assert.isTrue(!headerAlias.isEmpty());

        List<String> headerValues = checkHeaderExists(headerName, false);
        Assert.notNull(headerValues);
        Assert.isTrue(!headerValues.isEmpty());

        scenarioScope.getHeaders().put(headerAlias, headerValues);
    }

    /**
     * Store a json path value using the given alias
     * @param jsonPath json path query
     * @param jsonPathAlias new json path alias in the scenario scope
     */
    void storeJsonPath(String jsonPath, String jsonPathAlias){
        Assert.notNull(jsonPath);
        Assert.isTrue(!jsonPath.isEmpty());

        Assert.notNull(jsonPathAlias);
        Assert.isTrue(!jsonPathAlias.isEmpty());

        Object pathValue = getJsonPath(jsonPath);
        scenarioScope.getJsonPaths().put(jsonPathAlias, pathValue);
    }

    /**
     * Test a scenario variable existence
     * @param property name of the variable
     * @param value expected value
     */
    void checkScenarioVariable(String property, String value){
        Assert.isTrue(scenarioScope.checkProperty(property, value));
    }

    /**
     * Parse the http response json body
     * @return ReadContext instance
     */
    private ReadContext getBodyDocument(){
        //Object document = Configuration.defaultConfiguration().jsonProvider().parse();
        ReadContext ctx = JsonPath.parse(responseEntity.getBody());
        Assert.notNull(ctx);

        return ctx;
    }

    /**
     * Get values for a given json path query and the http response body
     * @param jsonPath json path query
     * @return json path value
     */
    private Object getJsonPath(String jsonPath){

        Assert.notNull(jsonPath);
        Assert.isTrue(!jsonPath.isEmpty());

        ReadContext ctx = getBodyDocument();
        Object pathValue = ctx.read(jsonPath);

        Assert.notNull(pathValue);

        return pathValue;
    }
}
