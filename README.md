# REST API tests with Spring and Cucumber

<div align="center">
  <a name="logo" href="https://www.redfroggy.fr"><img src="src/main/resources/images/logo.png" alt="RedFroggy"></a>
  <h4 align="center">A RedFroggy project</h4>
</div>
<br/>
<div align="center">
  <a href="https://forthebadge.com"><img src="https://forthebadge.com/images/badges/fuck-it-ship-it.svg"/></a>
  <a href="https://forthebadge.com"><img src="https://forthebadge.com/images/badges/built-with-love.svg"/></a>
<a href="https://forthebadge.com"><img src="https://forthebadge.com/images/badges/made-with-java.svg"/></a>
</div>
<div align="center">
  <a href="https://circleci.com/gh/RedFroggy/spring-cucumber-rest-api"><img src="https://circleci.com/gh/RedFroggy/spring-cucumber-rest-api.svg?style=svg"/></a>
</div>

Test your Sprint rest API with cucumber and Gherkin !
Inspired from the awesome [apickli project](https://github.com/apickli/apickli) project.

## Stack
- Spring Boot
- Cucumber
- Jayway JsonPath
- Gherkin

## Example

Here a gherkin example file: [users.feature](src/test/resources/features/users.feature)

```gherkin
Feature: Spring api controller bdd

  Background:
    Given baseUri is http://localhost:8080

  Scenario: Add tony stark user
    When I authenticate with login/password tstark/marvel
    And I set Accept header to application/json
    And I set Content-Type header to application/json
    And I set body to {"id":"1","firstName":"Tony","lastName":"Stark","age":"40"}
    And I POST /users
    Then response code should be 201
    And response body path $.id should be 1
    And response body path $.firstName should be Tony
    And response body path $.lastName should be Stark
    And response body path $.age should be 40
    And I store the value of body path $.id as starkUser in scenario scope

  Scenario: Add bruce wayne user
    When I set Accept header to application/json
    And I set Content-Type header to application/json
    And I set body to {"id": "2","firstName":"Bruce","lastName":"Wayne","age":"50", "relatedTo": {"id":`$starkUser`}}
    And I POST /users
    Then response code should be 201
    And response body path $.id should be 2
    And response body path $.firstName should be Bruce
    And response body path $.lastName should be Wayne
    And response body path $.age should be 50
    And response body path $.relatedTo.id should be 1

  Scenario: Get users
    When I GET /users
    Then response code should be 200
    And response body is typed as array using path $ with length 2
    And response body path $.[0].firstName should be Tony


  Scenario: Get user
    When I GET /users/1
    Then response code should be 200
    And response body path $.id should be 1
    And response body path $.firstName should be Tony
    And response body path $.lastName should be Stark
    And response body path $.age should be 40

  Scenario: Get user
    When I GET /users/2
    Then response code should be 200
    And response body path $.id should be 2
    And response body path $.firstName should be Bruce
    And response body path $.lastName should be Wayne
    And response body path $.age should be 50


  Scenario: Delete wrong user
    When I DELETE /users/3
    Then response code should be 404

  Scenario: Delete user
    When I DELETE /users/1
    Then response code should be 200

```

## Feature template
- In order to successfully use this library, you need to respect the following template for your `.feature` files
(an example file can be found under src/test/resources/template_feature)
- The template was inspired by the awesome [apickli project](https://github.com/apickli/apickli)
  

## Share date between steps
You can use this step to store data in a shared context:
```gherkin
And I store the value of body path $.id as idUser in scenario scope
```
- The result of the JsonPath `$.id` will be stored in an `id` variable.
- To reuse this variable in the `.feature` file you can do:
```gherkin
When I DELETE /users/`\`$id\``
```


## How to use it in my existing project ?

### Add CucumberTest.java file

    * Set the glue property to  "fr.redfroggy.test.bdd.glue" (+ your glue if you have one)
    * Set the features folder property

```
@RunWith(Cucumber.class)
@CucumberOptions(
        plugin = {"pretty"},
        features = "src/test/resources/features",
        glue = {"fr.redfroggy.test.bdd.glue"})
public class CucumberTest {

}
````

    
- Add a "fr.redfroggy.test.bdd" package under src/test/java
- Add a Spring Boot Application.java file under fr.redfroggy.test.bdd
- Replace "your.package" with your package (under which spring will be able to find your @Component,@Service,
@RestController, etc...)
- Don't forget to add your .feature files under "src/test/resources/features" for example


### Add default step definition file
It is mandatory to have a cucumber context to be able to start unit tests.
This file must be in the `glue` package.

```
@CucumberContextConfiguration
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DefaultStepDefinitionFile {

}
````


## Run local unit tests

````bash
$ mvn test
````
