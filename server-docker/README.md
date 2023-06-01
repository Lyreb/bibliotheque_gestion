# MMI Library Server

A Rest API to manage a library, built upon Java Spring and MongoDb.

## Author

Rémi Venant <remi.venant@univ-lemans.fr>

## License

AGPL https://www.gnu.org/licenses/agpl-3.0.en.html

Copyright (C) 2022 IUT Laval - Le Mans Université.

## Getting started

### Deploying the web server and database on docker

Prerequisite:
- Docker
- docker-compose

Run the following command from the root project folder:
```
$ docker-compose up
```

The server will be available at http://localhost:8080

### Deploying the server locally and the database on docker

Prerequisite:
- Docker
- JDK ≥ 17
- Maven 3

Steps:
1. Start the MongoDb server with the script launchTestingDbServer.sh
2. Optionnaly, start the MongoDb client with the script launchTestingDbClient.sh
3. Start the Web server with maven (`mvn spring-boot:run`)

## Configuration

The application can be customized with the _src/main/resources/application.properties_ file. Using docker-compose, pay attention that some mongo properties are overriden in the docker-compose file.

Data sample:
- By default, only if the mongo database is empty, application will load sample data.
- If you always want to clear the database and reload the sample data at the application startup, set the _mmiLibraryServer.sampleData.alwayResetData_ property to __true__.
- If you do not want to load data sample, remove the __sample-data__ profile from the _spring.profiles.active_ property (or comment that line in the application.properties file)

## Documentation

REST Endpoints documentation can be found in the __doc/RESTEndpoints__ folder.