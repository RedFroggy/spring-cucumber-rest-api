Feature: Users api tests

  Background:
    Given http baseUri is http://localhost:8080
    And I set Accept-Language http header to en-US
    And I set http headers to:
    | Accept        | application/json  |
    | Content-Type  | application/json  |
  
  Scenario: Should be authenticated
    When I HEAD /authenticated
    Then http response code should be 401
    When I authenticate with login/password tstark/marvel
    And I HEAD /authenticated
    Then http response code should be 200
    And I store the value of http response header Authorization as authToken in scenario scope

  Scenario: Add tony stark user
    When I authenticate with login/password tstark/marvel
    And I set http body to {"id":"1","firstName":"Tony","lastName":"Stark","age":"40", "sessionIds": ["43233333", "45654345"]}
    And I POST /users
    Then http response header Content-Type should not be application/xml
    And http response code should be 201
    And http response header Content-Type should be application/json
    And http response body path $.id should be 1
    And http response body path $.firstName should be Tony
    And http response body path $.lastName should be Stark
    And http response body path $.age should be 40
    And http response body path $.sessionIds should be ["43233333", "45654345"]
    And http response body path $.sessionIds should not be []
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

  Scenario: Update tony stark user
    When I set http body to {"id":"1","firstName":"Tony","lastName":"Stark","age":"60"}
    And I PUT /users/1
    Then http response code should be 200
    And http response body path $.age should be 60
    When I GET /users/1
    Then http response code should be 200
    And http response body path $.age should be 60

  Scenario: Patch bruce wayne user
    When I set http body to {"lastName":"WAYNE"}
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
    And http response body is typed as array using path $ with length 2
    And http response body path $.[0].firstName should be Tony
    And http response body path $.[0].lastName should not be Wayne
    And http response body path $.[1].id should exists
    And http response body path $.[4].id should not exist
    And http response body should contain Bruce

  Scenario: Search for valid users
    And I set http query parameter name to bruce
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

  Scenario: Get user
    When I GET /users/1
    Then http response code should be 200
    And http response body path $.id should be 1
    And http response body path $.firstName should be Tony
    And http response body path $.lastName should be Stark
    And http response body path $.age should be 60
    And I store the value of http response header Content-Type as httpContentType in scenario scope
    And http value of scenario variable httpContentType should be application/json

  Scenario: Get user
    When I GET /users/2
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
    And  I GET /users
    And http response body is typed as array using path $ with length 0
    And http response body path $ should not have content
