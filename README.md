#REST API integration testing framework based on cucumber.js and Spring [![Build Status](https://travis-ci.org/RedFroggy/spring-cucumber-rest-api.svg?branch=master)](https://travis-ci.org/RedFroggy/spring-cucumber-rest-api)

#Stack
- Spring Boot
- Cucumber
- Cucumber-Spring
- Jayway JsonPath
- Rest template (Spring web framework)

#Description
- Predefined steps
- Handle RESTFUL requests
- Possibility to set request headers or parameters
- Possibility to test response headers
- Possibility to test response status code
- Possibility to test the body response using a json path

#Feature template
- In order to successfully use this library, you need to respect the following template for your .feature files
(an example file can be found under src/test/resources/template_feature)
- The template was inspired by the [apickli project](https://github.com/apickli/apickli)

  * GIVEN
    * I set (.*) header to (.*)
    * I set body to (.*)
    * I pipe contents of file (.*) to body
    * I have basic authentication credentials (.*) and (.*)
    * I set bearer token
    * I set query parameters to (data table with headers |parameter|value|)
    * I set headers to (data table with headers |name|value|)
  * WHEN
    * I GET $resource
    * I POST to $resource
    * I PUT $resource
    * I DELETE $resource
    * I PATCH $resource
    * I request OPTIONS for $resource
    * I request HEAD for $resource
  * THEN
    * response code should be (\d+)
    * response code should not be (\d+)
    * response header (.*) should exist
    * response header (.*) should not exist
    * response header (.*) should be (.*)
    * response header (.*) should not be (.*)
    * response body should be valid (xml|json)
    * response body should contain (.*)
    * response body should not contain (.*)
    * response body path (.*) should be (.*)
    * response body path (.*) should not be (.*)
    * response body path (.*) should be of type array
    * response body path (.*) should be of type array with length (\d+)
    * response body should be valid according to schema file (.*)
    * response body should be valid according to swagger definition (.*) in file (.*)
    * I store the value of body path (.*) as access token
    * I store the value of response header (.*) as (.*) in scenario scope
    * I store the value of body path (.*) as (.*) in scenario scope
    * value of scenario variable (.*) should be (.*)
    * I store the value of response header (.*) as (.*) in global scope
    * I store the value of body path (.*) as (.*) in global scope


#Examples
Two example feature files are available under src/test/resources/features folder

#How to use in my existing project ?

#To run Java unit tests
````bash
$ mvn test
````

It will run two features and test two kind of apis:
- An external one: Swagger petstore api
- An internal one: A spring rest api declared in the project using @RestController