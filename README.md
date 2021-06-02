###Ttechnology
* JDK 15
* SpringBoot 2.4.5
* JUnit 5
* Docker

###The application has one endpoint to find flights
* GET /ryanair/flights
    * Params:
        - departure - IATA CODE // REQUIRED
        - arrival - IATA CODE // REQUIRED
        - departureDateTime - FORMAT (YYYY-MM-DDTHH:MM) // REQUIRED
        - arrivalDateTime - FORMAT (YYYY-MM-DDTHH:MM) // REQUIRED

###To deploy the application:

* LOCAL - Execute in your favorite IDE with the class RyanairChallengeApplication

* DOCKER - 
  * Build the project in the root of the project executing -> ./gradlew build
  * In the route of the project execute docker compose up
    
* JAR 
    * Build the project in the root of the project executing -> ./gradlew build
    * In the command line "java -jar build/libs/challenge-0.0.1-SNAPSHOT.jar"