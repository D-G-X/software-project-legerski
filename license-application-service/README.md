# License Application Service

## Project Setup

### Prerequisites

- [Docker Engine](https://docs.docker.com/engine/) version 20.10 or higher is required.
- [Docker Compose](https://docs.docker.com/compose/) version 1.29 or higher is required.
- [Java JDK](https://www.oracle.com/de/java/) version 21 or higher is required.
- [Node.js](https://nodejs.org/) version 22 or higher is required.

### Quick Start

1. Have Docker running.
2. When prompted: "Maven build script found `license-application-service`", accept by clicking "Load".
3. Run `init.sh` (Linux/macOS) or `init.ps1` (Windows) in `software-project-legerski` to set up git lfs and create the `.env` file in `docker`. 
4. Launch the Maven setup config: `Setup Backend` which performs the following steps:

   - `docker compose up postgres keycloak` to start the database and Keycloak,
   - `mvn liquibase:update` to apply database changelogs,
   - `mvn package` to generate OpenAPI and jOOQ classes and buld the backend jar.

   > ⚠️ If this step fails, run `docker compose up postgres keycloak` manually in the `license-application-service` directory and re-run the Maven setup config.

5. Open the two freshly auto-generated modules:

   - `license-application-service/target/generated-sources/jooq`
   - `license-application-service/target/generated-sources/openapi`

   and set their `src` folders as "Generated Sources Root".

   > ℹ️ Right-click on the folder in the Project view -> "Mark Directory as" -> "Generated Sources Root".

6. Backend client secret setup:
   - Go to the [Keycloak admin console](http://localhost:8081/realms/master/protocol/openid-connect/auth?client_id=security-admin-console&redirect_uri=http%3A%2F%2Flocalhost%3A8081%2Fadmin%2Fmaster%2Fconsole%2F&state=5a67f871-4955-446e-b7a4-dcf04d1510fa&response_mode=query&response_type=code&scope=openid&nonce=bdd6a9a4-aaa7-40fa-a6dd-bd0c842c0789&code_challenge=1dG9jQi6II2K35M8kujnMSicF6yUOZfMQmZx-8BAIUI&code_challenge_method=S256) and log in with admin/admin credentials
   - Select the `license-realm` realm from the dropdown and navigate to the `Clients` tab
   - Select the `backend-api` client, go to the "Credentials" tab and copy the `Client Secret` value. If the secret is not visible, click on the `Regenerate` button to create a new one 
   - Paste it to the `KEYCLOAK_CLIENT_SECRET` key in the `docker/.env` file (copy and rename .env.example, if necessary).

   > Note that the keycloak dashboard doesn't work on Safari!

### Launching the Application

1. Launch the backend Docker stack: `Docker: Compose Deployment`.
2. Launch the frontend npm client: `npm dev` or `npm prod`.

   > ℹ️ All npm configurations automatically perform `npm install` before building or testing the frontend.

### Testing (optional)

1. _TODO: Add JUnit test instructions here_
2. Launch the npm configuration: `npm test`.

### Clean-Up (only necessary after merges)

1. Stop the npm applications if running.
2. Run `clean.sh` (Linux/macOS) or `clean.ps1` (Windows) in `license-application-service`.

   > This removes outdated npm packages and maven dependencies.

---

### Quick Fixes

If you have issues with compiling or starting the backend, try the following steps in IntelliJ:

	•	Settings/Preferences → Build, Execution, Deployment → Build Tools → Maven
	•	Runner → “Delegate IDE build/run actions to Maven”

(or depending on your IntelliJ-Version: “Use Maven for build and run”)

By doing this, IntelliJ will use Maven to build and run the project instead of its internal build process.

if you have issues with connection to keycloak (error: "We are sorry... HTTPS required"), try the following steps:

    •	Remove the keycloak Docker container and image
    •	Go to the Docker Desktop settings:
    •	Under "Resources" → "Network", set the "Enable host networking" option
    •	Restart Docker Desktop
    •	Rebuild and restart the keycloak container

---

## API Documentation

- #### [Swagger API](http://localhost:8080/swagger-ui/index.html)
- #### [OpenAPI Docs](http://localhost:8080/v3/api-docs)
