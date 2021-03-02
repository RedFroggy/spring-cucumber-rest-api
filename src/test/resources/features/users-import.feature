@user
Feature: Users import api tests

  @import
  Scenario: Should import users csv file
    When I upload file upload/users.csv to POST /api/users
    Then http response code should be 200
    And http response body is typed as array using path $ with length 2
    And http response body path $.[0].id should be 123
    And http response body path $.[0].firstName should be Peter
    And http response body path $.[0].lastName should be Parker
    And http response body path $.[0].age should be 22
