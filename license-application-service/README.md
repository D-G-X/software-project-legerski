# License Application Service

## Project Setup

### Prerequisites

- [Docker Engine](https://docs.docker.com/engine/) version 20.10 or higher and [Docker Compose](https://docs.docker.com/compose/) version 1.29 or higher are required.
- [Java JDK](https://www.oracle.com/de/java/) version 21 or higher is required.
- [Node.js](https://nodejs.org/) version 22 or higher is required.

### Quick Start

1. Have Docker running
2. Launch the Maven config: `Setup Database and API` which performs the following steps:
    - `docker compose up` to start the database,
    - `mvn liquibase:update` to apply database changelogs,
    - `mvn compile` to generate OpenAPI and jOOQ classes,
    - `mvn clean` to clean up leftovers.

### Development

1. Launch the Spring Boot configuration: `Dev LicenseApplicationService`
2. Launch the npm configuration: `npm dev`

    > All npm configurations automatically perform `npm install` before building or testing the frontend.

### Manual Production Build (optional)

1. Launch the Spring Boot configuration: `Prod LicenseApplicationService`
2. Launch the npm configuration: `npm prod`

### Manual Testing (optional)

1. _TODO: Add JUnit test instructions here_
2. Launch the npm configuration: `npm test`

---

The application is now running on [localhost:3000](localhost:3000). All changes are immediately visible in the browser.

## API Documentation

- #### [Swagger API](http://localhost:8080/swagger-ui/index.html)
- #### [OpenAPI Docs](http://localhost:8080/v3/api-docs)

## Further readings

- [Maven docs](https://maven.apache.org/guides/index.html)  
- [Spring Boot reference](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)  
- [Learn React](https://react.dev/learn)
- [Webpack concepts](https://webpack.js.org/concepts/)  
- [npm docs](https://docs.npmjs.com/)  
- [Tailwind CSS](https://tailwindcss.com/)  
