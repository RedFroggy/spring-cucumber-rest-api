Feature: Swagger petstore api bdd

  Background:
    Given baseUri is http://petstore.swagger.io/v2

  Scenario: Get root document
    Given I set Accept header to application/json
    And I set query parameters to:
      | name   | value  |
      | toJson  | true  |
    And I set headers to:
      | name   | value  |
      | lang  | fr  |
    When I GET /swagger.json
    Then response code should be 200
    And response code should not be 401
    And response header Content-Type should exist
    And response header X-CSRF-TOKEN should not exist
    And response header Content-Type should be application/json
    And response header Server should not be Apache
    And response body should be valid json
    And response body path $.info.version should be 1.0.0
    And response body path $.info.title should not be InvalidTitle
    And response body is typed as array for path $.tags
    And response body is typed as array using path $.tags with length 3
    And I store the value of response header Content-Type as Response-Type in scenario scope
    And I store the value of body path $.info.version as apiVersion in scenario scope
    And value of scenario variable apiVersion should be 1.0.0
    
  Scenario: Add new Pet
    Given I set Accept header to application/json
    And I set Content-Type header to application/json
    And I set body to {"category": {"id": 0,"name": "string"},"name": "doggie","photoUrls": ["string"],"tags": [{"id": 0,"name": "string"}],"status": "available"}
    When I POST /pet
    Then response code should be 200
    And response header Content-Type should exist
    And response body should be valid json
    And response body path $.id should exists
    And response body path $.tags[0].name should exists
