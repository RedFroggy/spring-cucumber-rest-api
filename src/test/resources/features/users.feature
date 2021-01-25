Feature: Users api tests

  Background:
    Given baseUri is http://localhost:8080
    And I set Accept-Language header to en-US
    And I set headers to:
    | Accept        | application/json  |
    | Content-Type  | application/json  |
  
  Scenario: Should be authenticated
    When I HEAD /authenticated
    Then response code should be 401
    When I authenticate with login/password tstark/marvel
    And I HEAD /authenticated
    Then response code should be 200
    And I store the value of response header Authorization as authToken in scenario scope

  Scenario: Add tony stark user
    When I authenticate with login/password tstark/marvel
    And I set body to {"id":"1","firstName":"Tony","lastName":"Stark","age":"40"}
    And I POST /users
    Then response header Content-Type should not be application/xml
    And response code should be 201
    And response header Content-Type should be application/json
    And response body path $.id should be 1
    And response body path $.firstName should be Tony
    And response body path $.lastName should be Stark
    And response body path $.age should be 40
    And I store the value of body path $.id as starkUser in scenario scope
    And value of scenario variable starkUser should be 1

  Scenario: Add bruce wayne user
    And I set body to {"id": "2","firstName":"Bruce","lastName":"Wayne","age":"50", "relatedTo": {"id":`$starkUser`}}
    And I POST /users
    Then response header Content-Type should exist
    Then response header xsrf-token should not exist
    And response body should be valid json
    Then response code should not be 404
    And response code should be 201
    And response body path $.id should be 2
    And response body path $.firstName should be Bruce
    And response body path $.lastName should be Wayne
    And response body path $.age should be 50
    And response body path $.relatedTo.id should be 1

  Scenario: Update tony stark user
    When I set body to {"id":"1","firstName":"Tony","lastName":"Stark","age":"60"}
    And I PUT /users/1
    Then response code should be 200
    And response body path $.age should be 60
    When I GET /users/1
    Then response code should be 200
    And response body path $.age should be 60

  Scenario: Patch bruce wayne user
    When I set body to {"lastName":"WAYNE"}
    And I PATCH /users/2
    Then response code should be 200
    And response body path $.lastName should be WAYNE
    When I GET /users/2
    Then response code should be 200
    And response body path $.lastName should be WAYNE

  Scenario: Get users
    When I GET /users
    And response body should be valid json
    Then response code should be 200
    And response body is typed as array for path $
    And response body is typed as array using path $ with length 2
    And response body path $.[0].firstName should be Tony
    And response body path $.[0].lastName should not be Wayne
    And response body path $.[1].id should exists
    And response body path $.[4].id should not exist
    And response body should contain Bruce

  Scenario: Search for users
    And I set query parameter name to bruce
    When I GET /users
    And response body should be valid json
    Then response code should be 200
    And response body is typed as array using path $ with length 1
    And response body path $.[0].id should be 2
    And response body path $.[0].firstName should be Bruce

  Scenario: Get user
    When I GET /users/1
    Then response code should be 200
    And response body path $.id should be 1
    And response body path $.firstName should be Tony
    And response body path $.lastName should be Stark
    And response body path $.age should be 60

  Scenario: Get user
    When I GET /users/2
    Then response code should be 200
    And response body path $.id should be 2
    And response body path $.firstName should be Bruce
    And response body path $.lastName should be WAYNE
    And response body path $.age should be 50


  Scenario: Get wrong user
    When I GET /users/24333
    Then response code should be 404
    And response body path $ should not have content

  Scenario: Delete wrong user
    When I DELETE /users/3
    Then response code should be 404
    And response body path $ should not exist
    And response body path $ should not have content

  Scenario: Delete user
    When I DELETE /users/1
    Then response code should be 200
    And I DELETE /users/2
    Then response code should be 200
    And  I GET /users
    And response body is typed as array using path $ with length 0
    And response body path $ should not have content
