Feature: Spring api controller bdd

  Background:
    Given baseUri is http://localhost:8080

  Scenario: Add tony stark user
    When I set Accept header to application/json
    And I set Content-Type header to application/json
    And I set body to {"id":"1","firstName":"Tony","lastName":"Stark","age":"40"}
    And I POST /users
    Then response code should be 201
    And response body path $.id should be 1
    And response body path $.firstName should be Tony
    And response body path $.lastName should be Stark
    And response body path $.age should be 40

  Scenario: Add bruce wayne user
    When I set Accept header to application/json
    And I set Content-Type header to application/json
    And I set body to {"id":"2","firstName":"Bruce","lastName":"Wayne","age":"50"}
    And I POST /users
    Then response code should be 201
    And response body path $.id should be 2
    And response body path $.firstName should be Bruce
    And response body path $.lastName should be Wayne
    And response body path $.age should be 50

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
