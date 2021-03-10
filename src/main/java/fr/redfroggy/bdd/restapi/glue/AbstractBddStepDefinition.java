package fr.redfroggy.bdd.restapi.glue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import fr.redfroggy.bdd.restapi.scope.ScenarioScope;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * Abstract step definition implementation Http request are made using {@link RestTemplate} RestTemplate
 */
@SuppressWarnings("unchecked")
abstract class AbstractBddStepDefinition {

    // Stored base uri
    protected String baseUri = "";

    // Parsed http request json body
    protected Object body;

    // Rest template
    protected TestRestTemplate template;

    // Http headers
    protected HttpHeaders headers;

    // List of query params
    protected Map<String, String> queryParams;

    // Stored http response
    protected ResponseEntity<String> responseEntity;

    protected ObjectMapper objectMapper;

    protected static final ScenarioScope scenarioScope = new ScenarioScope();

    @Value("${redfroggy.cucumber.restapi.wiremock.port}")
    private int wireMockPort;

    private WireMockRule wireMockServer;

    AbstractBddStepDefinition(TestRestTemplate testRestTemplate) {
        template = testRestTemplate;
        objectMapper = new ObjectMapper();
        headers = new HttpHeaders();
        queryParams = new HashMap<>();

        // Add support for PATCH requests
        testRestTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }

    @PostConstruct
    public void setUp() {
        wireMockServer = new WireMockRule(WireMockConfiguration
                .wireMockConfig().port(wireMockPort).notifier(new ConsoleNotifier(true)));
        wireMockServer.start();
    }

    @PreDestroy
    public void stopWireMockServer() {
        wireMockServer.stop();
    }

    /**
     * Add an http header {@link #headers}
     *
     * @param name
     *            header name
     * @param value
     *            header value
     */
    void setHeader(String name, String value) {
        assertThat(name).isNotNull();
        assertThat(value).isNotNull();
        value = replaceDynamicParameters(value, false);
        headers.set(name, value);
    }

    /**
     * Add an HTTP query parameter {@link #queryParams}
     *
     * @param newParams
     *            Map of parameters
     */
    void addQueryParameters(Map<String, String> newParams) {
        assertThat(newParams).isNotEmpty();
        queryParams.putAll(newParams);
    }

    /**
     * Add multiple http headers {@link #headers}
     *
     * @param newHeaders
     *            Map of headers
     */
    void addHeaders(Map<String, String> newHeaders) {
        assertThat(newHeaders).isNotEmpty();
        newHeaders.forEach((key, value) ->
                this.headers.put(key,  Collections.singletonList(value)));
    }

    /**
     * Set the http request body (POST request for example) {@link #body}
     * Must be a valid json format
     */
    void setBody(String body) throws IOException {
        assertThat(body).isNotEmpty();
        String sanitizedBody = replaceDynamicParameters(body, true);
        this.body = objectMapper.readValue(sanitizedBody, Object.class);
    }

    /**
     * Set the http request body (POST request for example) {@link #body}
     * with a file content
     */
    void setBodyWithFile(String filePath) throws IOException {
        this.setBody(StreamUtils
                .copyToString(getClass().getClassLoader()
                        .getResourceAsStream(filePath), StandardCharsets.UTF_8));
    }
  
    void setBodyPathWithValue(String jsonPath, String value) {
        assertThat(jsonPath).isNotEmpty();
        assertThat(body).isNotNull();

        body = JsonPath.parse(body).set(jsonPath, value).jsonString();
    }

    /**
     * Perform an http request Store the http response to responseEntity {@link #responseEntity}
     *
     * @param resource
     *            resource to consume
     * @param method
     *            HttpMethod
     */
    void request(String resource, HttpMethod method) {
        assertThat(resource).isNotEmpty();
        assertThat(method).isNotNull();

        resource = replaceDynamicParameters(resource, true);

        boolean writeMode = HttpMethod.PUT.equals(method) || HttpMethod.POST.equals(method)
                || HttpMethod.PATCH.equals(method);

        HttpEntity<Object> httpEntity;

        if (writeMode) {
            httpEntity = new HttpEntity<>(body, headers);
        } else {
            httpEntity = new HttpEntity<>(headers);
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUri + resource);
        queryParams.forEach(builder::queryParam);

        responseEntity = template.exchange(builder.build().toUri(), method, httpEntity, String.class);
        assertThat(responseEntity).isNotNull();
    }

    void postMultipart(String method, String uri, List<Map<String, String>> data) {
        LinkedMultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
        data.forEach(row -> parameters.add(row.get("Name"),
                new org.springframework.core.io.ClassPathResource(row.get("Filepath"))));

        this.headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        this.body = parameters;

        request(uri, HttpMethod.valueOf(method));
    }

    void checkStatus(String status, boolean isNot) {

        int sanitizedStatus = Integer.parseInt(replaceDynamicParameters(status, true));
        assertThat(sanitizedStatus).isGreaterThan(0);

        if (isNot) {
            assertThat(responseEntity.getStatusCodeValue()).isNotEqualTo(sanitizedStatus);
        } else {
            assertThat(responseEntity.getStatusCodeValue()).isEqualTo(sanitizedStatus);
        }
    }

    /**
     * Check header existence
     *
     * @param headerName
     *            name of the header to find
     * @param isNot
     *            if true, test equality, inequality if false
     * @return header values if found, null otherwise
     */
    List<String> checkHeaderExists(String headerName, boolean isNot) {
        assertThat(headerName).isNotEmpty();
        assertThat(responseEntity.getHeaders()).isNotNull();

        if (!isNot) {
            assertThat(responseEntity.getHeaders().get(headerName)).isNotNull();
            return responseEntity.getHeaders().get(headerName);
        } else {
            assertThat(responseEntity.getHeaders().get(headerName)).isNull();
            return null;
        }
    }

    /**
     * Test header value
     *
     * @param headerName
     *            name of the header to test
     * @param headerValue
     *            expected/unexpected value
     * @param isNot
     *            if true, test equality, inequality if false
     */
    void checkHeaderEqual(String headerName, String headerValue, boolean isNot) {
        assertThat(headerName).isNotEmpty();
        assertThat(headerValue).isNotEmpty();
        assertThat(responseEntity.getHeaders()).isNotNull();

        if (!isNot) {
            assertThat(responseEntity.getHeaders().getFirst(headerName)).contains(headerValue);
        } else {
            assertThat(responseEntity.getHeaders().getFirst(headerName)).doesNotContain(headerValue);
        }
    }

    /**
     * Test response body is json typed {@link #responseEntity}
     *
     * @throws IOException
     *             json parse exception
     */
    void checkJsonBody() throws IOException {
        String body = responseEntity.getBody();
        assertThat(body).isNotEmpty();

        // Check body json structure is valid
        objectMapper.readValue(body, Object.class);
    }

    /**
     * Test body content {@link #responseEntity}
     *
     * @param bodyValue
     *            expected content
     */
    void checkBodyContains(String bodyValue) {

        String sanitizedValue = replaceDynamicParameters(bodyValue, true);

        assertThat(sanitizedValue).isNotEmpty();
        assertThat(responseEntity.getBody()).contains(sanitizedValue);
    }

    Object checkJsonPathExists(String jsonPath, boolean mandatory) {
        return getJsonPathValue(jsonPath, mandatory);
    }

    void checkJsonPathDoesntExist(String jsonPath) {
        ReadContext ctx = getBodyDocument();
        if (ctx != null) {
            assertThat(jsonPath).isNotEmpty();
        }
    }

    void checkJsonPath(String jsonPath, String expectedValue, boolean isNot, boolean mandatory) {
        Object currentValue = checkJsonPathExists(jsonPath, mandatory);

        if (mandatory) {
            assertThat(String.valueOf(currentValue)).isNotEmpty();
        } else if (currentValue == null) {
            return;
        }

        if (currentValue instanceof Collection) {
            checkJsonCollection((Collection) currentValue, expectedValue, isNot);
            return;
        }
        String sanitizedExpectedValue = replaceDynamicParameters(expectedValue, true);
        Object expectedJsonValue = ReflectionTestUtils.invokeMethod(currentValue, "valueOf", sanitizedExpectedValue);

        if (!isNot) {
            assertThat(currentValue).isEqualTo(expectedJsonValue);
        } else {
            assertThat(currentValue).isNotEqualTo(expectedJsonValue);
        }
    }

    private void checkJsonCollection(Collection currentValue, String jsonValue, boolean isNot) {
        assertThat(currentValue).isNotEmpty();

        if (!isNot) {
            assertThat(currentValue).isEqualTo(JsonPath.parse(jsonValue).json());
        } else {
            assertThat(currentValue).isNotEqualTo(JsonPath.parse(jsonValue).json());
        }
    }

    /**
     * Test json path is array typed and its size is matching the expected length
     *
     * @param jsonPath
     *            json path query
     * @param length
     *            expected length (-1 to not control the size)
     */
    void checkJsonPathIsArray(String jsonPath, int length) {
        Object pathValue = getJsonPathValue(jsonPath, true);
        assertThat(pathValue).isInstanceOf(Collection.class);
        if (length != -1) {
            assertThat(((Collection) pathValue)).hasSize(length);
        }
    }

    /**
     * Store a given header in the scenario scope using the given alias
     *
     * @param headerName
     *            header to save
     * @param headerAlias
     *            new header name in the scenario scope
     */
    void storeHeader(String headerName, String headerAlias) {
        assertThat(headerName).isNotEmpty();
        assertThat(headerAlias).isNotEmpty();

        List<String> headerValues = checkHeaderExists(headerName, false);
        assertThat(headerValues).isNotEmpty();

        scenarioScope.getHeaders().put(headerAlias, headerValues);
    }

    /**
     * Store a json path value using the given alias
     *
     * @param jsonPath
     *            json path query
     * @param jsonPathAlias
     *            new json path alias in the scenario scope
     */
    void storeJsonPath(String jsonPath, String jsonPathAlias) {
        assertThat(jsonPath).isNotEmpty();
        assertThat(jsonPathAlias).isNotEmpty();

        Object pathValue = getJsonPathValue(jsonPath, true);
        scenarioScope.getJsonPaths().put(jsonPathAlias, pathValue);
    }

    /**
     * Test a scenario variable existence
     *
     * @param property
     *            name of the variable
     * @param value
     *            expected value
     */
    void checkScenarioVariable(String property, String value) {
        Object scopeValue;
        scopeValue = scenarioScope.getJsonPaths().get(property);

        if (scopeValue == null) {
            scopeValue = scenarioScope.getHeaders().get(property);
        }
        assertThat(scopeValue).isNotNull();

        if (scopeValue instanceof Collection) {
            assertThat(((Collection) scopeValue)).contains(value);
        } else {
            assertThat(scopeValue).isEqualTo(value);
        }
    }

    /**
     * Parse the http response json body
     *
     * @return ReadContext instance
     */
    private ReadContext getBodyDocument() {

        if (responseEntity.getBody() == null) {
            return null;
        }

        ReadContext ctx = JsonPath.parse(responseEntity.getBody());
        assertThat(ctx).isNotNull();

        return ctx;
    }

    protected Object getJsonPathValue(String jsonPath, boolean mandatory) {

        assertThat(jsonPath).isNotEmpty();

        ReadContext ctx = getBodyDocument();

        if (ctx == null) {
            return null;
        }

        Object pathValue = ctx.read(jsonPath);

        if (mandatory) {
            assertThat(pathValue).isNotNull();
        }

        return pathValue;
    }

    protected String replaceDynamicParameters(String value, boolean jsonPath) {
        Pattern pattern = Pattern.compile("`\\${1}(.*?)`");
        Matcher matcher = pattern.matcher(value);
        if (matcher.find()) {
            Object scopeValue = jsonPath ? scenarioScope.getJsonPaths().get(matcher.group(1))
                    : scenarioScope.getHeaders().get(matcher.group(1));
            assertThat(scopeValue).isNotNull();

            return replaceDynamicParameters(value.replace("`$"+ matcher.group(1) +"`",
                    scopeValue.toString()), jsonPath);
        }
        return value;
    }

    protected void mockThirdPartyApiCall(String method, String resource, int status, String mediaType, String body) throws URISyntaxException {

        String url = resource;
        MappingBuilder mappingBuilder;

        List<NameValuePair> params = URLEncodedUtils.parse(new URI(resource), StandardCharsets.UTF_8);
        if (!CollectionUtils.isEmpty(params)) {

            Map<String, StringValuePattern> queryParams = new HashMap<>();

            params.forEach(nameValuePair -> queryParams.put(nameValuePair.getName(),
                    new EqualToPattern(nameValuePair.getValue())));

            url = resource.substring(0, resource.indexOf("?"));
            mappingBuilder = WireMock.request(method, WireMock.urlPathMatching(url));
            mappingBuilder.withQueryParams(queryParams);
        } else {
            mappingBuilder = WireMock.request(method, WireMock.urlPathMatching(url));
        }

        wireMockServer.stubFor(mappingBuilder
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, mediaType)
                        .withHeader(HttpHeaders.ACCEPT, mediaType)
                        .withStatus(status)
                        .withBody(body)));
    }
}
