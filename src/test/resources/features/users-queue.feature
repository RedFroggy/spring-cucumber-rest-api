@user @messaging
Feature: Users messaging tests

  Scenario: Should get user
    Given I PUSH to queue input-valid-user with message {"id": "2","firstName":"Bruce","lastName":"Wayne","age":"50"}
    And I POLL first message from queue output-valid-user
    And queue message body path $.status should be VALID
    And queue message body path $.firstName should be Bruce
    And queue message body path $.lastName should be Wayne
    And queue message body path $.age should be 50
