Feature: Spring api controller bdd

  Background:
    Given baseUri is http://localhost:8080

  Scenario: Should test header
    When I GET /all
    Then response code should be 200
    And response code should not be 401
    And response header Content-Type should exist
    And response header X-CSRF-TOKEN should not exist
    And response body should contain Hello world