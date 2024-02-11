Feature: To save the audio

  Scenario: client makes call to POST /resources to upload the file
    Given the audio file with data
    When the client calls "/resources"
    Then the client receives status code of 200
    And the response contains message "Media has been successfully pushed to queue."