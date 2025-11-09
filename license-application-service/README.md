# License Application Service

This app was created with Bootify.io - tips on working with the code [can be found here](https://bootify.io/next-steps/).

## Project Setup

### Prerequisites

[Docker](https://www.docker.com/get-started/) must be installed and running.

[Java JDK](https://www.oracle.com/de/java/) version 21 or higher is required.

[Node.js](https://nodejs.org/) version 22 or higher is required.

### 1. Backend

The project uses Maven as build tool. To download the required dependencies, run the following command in the project root directory:

```
mvnw clean package
```

#### - Development

Start your application with the IntelliJ run configuration `Dev LicenseApplicationServiceApplication` or with the following command:

```
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

#### - Production

Start your application with the IntelliJ run configuration `Prod LicenseApplicationServiceApplication` or with the following command:

```
./mvnw spring-boot:run -Dspring-boot.run.profiles=production
```

#### - Testing

TODO: Add JUnit test instructions here

### 2. Frontend

The project uses npm as package manager. To download the required dependencies, run the following command in the project root directory:

```
npm install
```

#### - Development

Start your application with the IntelliJ run configuration `npm run dev` or with the following commands:

```
npm install
```

```
npm run devserver
```

#### - Production

Start your application with the IntelliJ run configuration `npm run prod` or with the following commands:

```
npm install
```

```
npm run build
```

#### - Testing

Run the frontend unit tests with the IntelliJ run configuration `npm run test` or with the following commands:

Frontend unit tests can be executed with `npm run test`.

```
npm install
```

```
npm run test
```

---

> The IntelliJ run configurations automatically run `npm install` before building or testing the frontend.

The whole application is now accessible under [localhost:3000](localhost:3000). All changes are immediately visible in the browser.

## Further readings

* [Maven docs](https://maven.apache.org/guides/index.html)  
* [Spring Boot reference](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)  
* [Spring Data JPA reference](https://docs.spring.io/spring-data/jpa/reference/jpa.html)
* [Learn React](https://react.dev/learn)
* [Webpack concepts](https://webpack.js.org/concepts/)  
* [npm docs](https://docs.npmjs.com/)  
* [Tailwind CSS](https://tailwindcss.com/)  
