# Test your local and remote REST API with Spring boot, Cucumber and Gherkin !

<div align="center">
  <a name="logo" href="https://www.redfroggy.fr"><img src="assets/logo.png" alt="RedFroggy"></a>
  <h4 align="center">A RedFroggy project</h4>
</div>
<br/>
<div align="center">
  <a href="https://forthebadge.com"><img src="https://forthebadge.com/images/badges/fuck-it-ship-it.svg"/></a>
  <a href="https://forthebadge.com"><img src="https://forthebadge.com/images/badges/built-with-love.svg"/></a>
  <a href="https://forthebadge.com"><img src="https://forthebadge.com/images/badges/made-with-java.svg"/></a>
</div>
<div align="center">
   <a href="https://maven-badges.herokuapp.com/maven-central/fr.redfroggy.test.bdd/cucumber-restapi"><img src="https://maven-badges.herokuapp.com/maven-central/fr.redfroggy.test.bdd/cucumber-restapi/badge.svg?style=plastic" /></a>
   <a href="https://travis-ci.com/RedFroggy/spring-cucumber-rest-api"><img src="https://travis-ci.com/RedFroggy/spring-cucumber-rest-api.svg?branch=master"/></a>
   <a href="https://codecov.io/gh/RedFroggy/spring-cucumber-rest-api"><img src="https://codecov.io/gh/RedFroggy/spring-cucumber-rest-api/branch/master/graph/badge.svg?token=XM9R6ZV9SJ"/></a>
   <a href="https://github.com/semantic-release/semantic-release"><img src="https://img.shields.io/badge/%20%20%F0%9F%93%A6%F0%9F%9A%80-semantic--release-e10079.svg"/></a>
   <a href="https://opensource.org/licenses/mit-license.php"><img src="https://badges.frapsoft.com/os/mit/mit.svg?v=103"/></a>
</div>
<br/>
<br/>

Made with , [Cucumber](https://cucumber.io/) and [Gherkin](https://cucumber.io/docs/gherkin/) !
Inspired from the awesome [apickli project](https://github.com/apickli/apickli) project.
<br/>
To test your messaging system with cucumber, gherkin please use this [library](https://github.com/RedFroggy/spring-cucumber-messaging)

## Stack
- Spring Boot
- Cucumber / Gherkin
- Jayway JsonPath

## Installation
```xml
<dependency>
  <groupId>fr.redfroggy.test.bdd</groupId>
  <artifactId>cucumber-restapi</artifactId>
</dependency>
```
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/fr.redfroggy.test.bdd/cucumber-restapi/badge.svg)](https://maven-badges.herokuapp.com/maven-central/fr.redfroggy.test.bdd/cucumber-restapi)

Run `npm install` to add commitlint + husky

## Example

```gherkin
Feature: Users api tests

  Background:
    Given http baseUri is /api/
    And I set Accept-Language http header to en-US
    And I set http headers to:
      | Accept        | application/json  |
      | Content-Type  | application/json  |

  Scenario: Should be authenticated
    When I HEAD authenticated
    Then http response code should be 401
    When I authenticate with login/password tstark/marvel
    And I HEAD /authenticated
    Then http response code should be 200
    And I store the value of http response header Authorization as authToken in scenario scope

  Scenario: Add tony stark user
    When I authenticate with login/password tstark/marvel
    And I set http body to {"id":"1","firstName":"Tony","lastName":"Stark","age":"40", "sessionIds": ["43233333", "45654345"]}
    And I set http body path $.age to 42
    And I POST /users
    Then http response header Content-Type should not be application/xml
    And http response code should be 201
    And http response header Content-Type should be application/json
    And http response body path $.id must be 1
    # Should not fail even if relatedTo has null value, will fail only if relatedTo exists and its value is different from 1
    And http response body path $.relatedTo should be 1
    And http response body path $.firstName should be Tony
    And http response body path $.lastName should be Stark
    And http response body path $.age should be 42
    And http response body path $.sessionIds should be ["43233333", "45654345"]
    And http response body path $.sessionIds must not be []
    And I store the value of http body path $.id as starkUser in scenario scope
    And I store the value of http body path $.sessionIds.[0] as firstSessionId in scenario scope
    And http value of scenario variable starkUser should be 1

  Scenario: Add bruce wayne user
    And I set Authorization http header to `$authToken`
    And I set http body to {"id": "2","firstName":"Bruce","lastName":"Wayne","age":"50", "relatedTo": {"id":`$starkUser`}, "sessionIds": [`$firstSessionId`]}
    And I POST /users
    Then http response header Content-Type should exist
    Then http response header xsrf-token should not exist
    And http response body should be valid json
    Then http response code should not be 404
    And http response code should be 201
    And http response body path $.id should be 2
    And http response body path $.firstName should be Bruce
    And http response body path $.lastName should be Wayne
    And http response body path $.age should be 50
    And http response body path $.relatedTo.id should be 1
    And http response body path $.sessionIds should be ["43233333"]

  Scenario: Add bruce banner user
    And I set Authorization http header to `$authToken`
    And I set http body with file fixtures/bruce-banner.user.json
    And I POST /users
    And http response code should be 201
    And http response body path $.id should be 15948349393
    And http response body path $.firstName should be Bruce
    And http response body path $.lastName should be Banner
    And http response body path $.age should be 45
    And http response body path $.sessionIds should be ["99869448"]

  Scenario: Update tony stark user
    When I mock third party api call GET /public/characters/1 with return code 200, content type: application/json and body: {"comicName": "IronMan", "city": "New York", "mainColor": ["red", "yellow"]}
    And I set http body to {"id":"1","firstName":"Tony","lastName":"Stark","age":"60"}
    And I PUT /users/1
    Then http response code should be 200
    And http response body path $.age should be 60
    When I GET /users/1
    Then http response code should be 200
    And http response body path $.age should be 60

  Scenario: Patch bruce wayne user
    When I mock third party api call GET /public/characters/2 with return code 200, content type: application/json and body: {"comicName": "Batman", "city": "Gotham City", "mainColor": ["black"]}
    And I set http body to {"lastName":"WAYNE"}
    And I PATCH /users/2
    Then http response code should be 200
    And http response body path $.lastName should be WAYNE
    When I GET /users/2
    Then http response code should be 200
    And http response body path $.lastName should be WAYNE

  Scenario: Get users
    When I GET /users
    And http response body should be valid json
    Then http response code should be 200
    And http response body is typed as array for path $
    And http response body is typed as array using path $ with length 3
    And http response body path $.[0].id should be `$starkUser`
    And http response body path $.[0].firstName should be Tony
    And http response body path $.[0].lastName should not be Wayne
    And http response body path $.[1].id should exists
    And http response body path $.[4].id should not exist
    And http response body should contain Bruce

  Scenario: Search for valid users
    And I set http query parameter name to wayne
    When I GET /users
    And http response body should be valid json
    Then http response code should be 200
    And http response body is typed as array using path $ with length 1
    And http response body path $.[0].id should be 2
    And http response body path $.[0].firstName should be Bruce

  Scenario: Search for invalid users
    And I set http query parameter name to djndjndsqds
    When I GET /users
    And http response body should be valid json
    Then http response code should be 200
    And http response body is typed as array using path $ with length 0
    And http response body path $ should not have content

  Scenario: Get user Tony Stark
    When I mock third party api call GET /public/characters/1?format=json with return code 200, content type: application/json and body: {"comicName": "IronMan", "city": "New York", "mainColor": ["red", "yellow"]}
    And I GET /users/1?format=json
    Then http response code should be 200
    And http response body path $.id should be 1
    And http response body path $.firstName should be Tony
    And http response body path $.lastName should be Stark
    And http response body path $.age should be 60
    And http response body path $.details.comicName should be IronMan
    And I store the value of http response header Content-Type as httpContentType in scenario scope
    And http value of scenario variable httpContentType should be application/json
    And http response body should contain `$firstSessionId`

  Scenario: Get user Bruce Wayne
    When I mock third party api call GET /public/characters/2 with return code 200, content type: application/json and file: fixtures/bruce_wayne_marvel_api.fixture.json
    And I GET /users/2
    Then http response code should be 200
    And http response body path $.id should be 2
    And http response body path $.firstName should be Bruce
    And http response body path $.lastName should be WAYNE
    And http response body path $.age should be 50


  Scenario: Get wrong user
    When I GET /users/24333
    Then http response code should be 404
    And http response body path $ should not have content

  Scenario: Delete wrong user
    When I DELETE /users/3
    Then http response code should be 404
    And http response body path $ should not exist
    And http response body path $ should not have content

  Scenario: Delete user
    When I DELETE /users/1
    Then http response code should be 200
    And I DELETE /users/2
    Then http response code should be 200
    And I DELETE /users/15948349393
    Then http response code should be 200
    And  I GET /users
    And http response body is typed as array using path $ with length 0
    And http response body path $ should not have content

```

You can look at the [users.feature](src/test/resources/features/users.feature) file for a more detailed example.

## Share data between steps
- You can use the following step to store data from a json response body to a shared context:
```gherkin
And I store the value of body path $.id as idUser in scenario scope
```
- You can use the following step to store data from a response header to a shared context:
```gherkin
And I store the value of response header Authorization as authHeader in scenario scope
```
- The result of the JsonPath `$.id` will be stored in the `idUser` variable.
- To reuse this variable in another step, you can do:
```gherkin
When I DELETE /users/`$idUser`
And I set Authorization header to `$authHeader`
```

## How to use it in my existing project ?

You can see a usage example in the [test folder](src/test/java/fr/redfroggy/bdd/restapi).

### Add a CucumberTest  file

```java
@RunWith(Cucumber.class)
@CucumberOptions(
        plugin = {"pretty"},
        features = "src/test/resources/features",
        glue = {"fr.redfroggy.bdd.restapi.glue"})
public class CucumberTest {

}
````
- Set the glue property to  `fr.redfroggy.bdd.glue` and add your package glue.
- Set your `features` folder property
- Add your `.feature` files under your `features` folder
- In your `.feature` files you should have access to all the steps defined in the [DefaultRestApiBddStepDefinition](src/main/java/fr/redfroggy/bdd/restapi/glue/DefaultRestApiBddStepDefinition.java) file.


### Add default step definition file
It is mandatory to have a class annotated with `@CucumberContextConfiguration` to be able to run the tests.
This class must be in the same `glue` package that you've specified in the `CucumberTest` class.

```java
@CucumberContextConfiguration
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DefaultStepDefinition {

}
````

### Specify an authentication mode
- You can authenticate using the step: `I authenticate with login/password (.*)/(.*)` but the authentication 
  mode must be implemented by you.
- You need to implement the `BddRestTemplateAuthentication` interface.
- You can inject a `TestRestTemplate` instance in your code, so you can do pretty much anything you want.
- For example, for a JWT authentication you can do :
```java
@CucumberContextConfiguration
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DefaultStepDefinition implements BddRestTemplateAuthentication {
    
    final TestRestTemplate template;

    public DefaultRestApiStepDefinitionTest(TestRestTemplate template) {
        this.template = template;
    }

    @Override
    public TestRestTemplate authenticate(String login, String password) {
        String token = generateJwt();
        restTemplate.getRestTemplate().getInterceptors().add(
                (outReq, bytes, clientHttpReqExec) -> {
                    outReq.getHeaders().set(
                            HttpHeaders.AUTHORIZATION, token
                    );
                    return clientHttpReqExec.execute(outReq, bytes);
                });

        return restTemplate;
    }
}
```
- For a basic authentication, you can do :
```java
@CucumberContextConfiguration
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DefaultStepDefinition implements BddRestTemplateAuthentication {

    final TestRestTemplate template;

    public DefaultRestApiStepDefinitionTest(TestRestTemplate template) {
        this.template = template;
    }

    @Override
    public TestRestTemplate authenticate(String login, String password) {
        return this.template.withBasicAuth(login, password);
    }
}
```
- If you use a specific class, it must be annotated with `@Component` to be detected by spring context scan.
```java
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
```

## Mock third party call
If you need to mock a third party API, you can use the following steps:

```gherkin
I mock third party api call (.*) (.*) with return code (.*), content type: (.*) and body: (.*)
  # Example: I mock third party api call GET /public/characters/1?format=json with return code 200, content type: application/json and body: {"comicName": "IronMan", "city": "New York", "mainColor": ["red", "yellow"]}
I mock third party api call (.*) (.*) with return code (.*), content type: (.*) and file: (.*)
  # Example: I mock third party api call GET /public/characters/2 with return code 200, content type: application/json and file: fixtures/bruce_wayne_marvel_api.fixture.json
```

It relies on [WireMock](http://wiremock.org) for stubbing api calls.
By default, the wiremock port is `8888`, if you need to override it you need to change the 
`redfroggy.cucumber.restapi.wiremock.port` property in your project.

## Run local unit tests

````bash
$ mvn test
````
