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
    When I mock third party api call GET /public/characters/1 with return code 200 and body: {"comicName": "IronMan", "city": "New York", "mainColor": ["red", "yellow"]}
    And I set http body to {"id":"1","firstName":"Tony","lastName":"Stark","age":"60"}
    And I PUT /users/1
    Then http response code should be 200
    And http response body path $.age should be 60
    When I GET /users/1
    Then http response code should be 200
    And http response body path $.age should be 60

  Scenario: Patch bruce wayne user
    When I mock third party api call GET /public/characters/2 with return code 200 and body: {"comicName": "Batman", "city": "Gotham City", "mainColor": ["black"]}
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
    When I mock third party api call GET /public/characters/1?format=json with return code 200 and body: {"comicName": "IronMan", "city": "New York", "mainColor": ["red", "yellow"]}
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
