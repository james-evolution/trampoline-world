# Trampoline World

Built for someone very dear to my heart.

A full-stack persistent web application that doubles as both a standalone desktop application and a mobile application.
<br>Compatible with both iOS and Android, this is effectively a CRM intended for tracking trampoline orders.

## Running the application

The project is a standard Maven project. To run it from the command line,
type `mvnw` (Windows), or `./mvnw` (Mac & Linux), then open
http://localhost:8090 in your browser.

You can also import the project to your IDE of choice as you would with any
Maven project. Read more on [how to import Vaadin projects to different 
IDEs](https://vaadin.com/docs/latest/flow/guide/step-by-step/importing) (Eclipse, IntelliJ IDEA, NetBeans, and VS Code).

## Deploying to Production (Heroku)

At current, this application is hosted on Heroku. However, if redeployment is ever needed, this is how to go about it.

1. Ensure you have the Heroku CLI installed.
2. Open a terminal or command prompt from the project's root folder.
3. Run `heroku login`
4. Run `mvnw package -Pproduction && heroku deploy:jar target\trampolineworld-1.0-SNAPSHOT.jar -a trampolineworld && heroku open`
5. Run `heroku ps:exec` to open a remote shell on the Heroku's linux server.
6. Run ``jar -xf target/trampolineworld-1.0-SNAPSHOT.jar /META-INF/resources/ce-license.json`` to extract the ce-license.json file from the jar file and into the Heroku linux server at /app/META-INF/resources. This is essential for enabling Vaadin's Collabration Engine features. Order editing and live chat features will not work without this.

## Deploying to Production (General)

This section is only relevant if you intend on deploying this application somewhere aside from Heroku.
Bear in mind that you will need to create a /META-INF/resouces folder in the host's root folder and place your ce-license.json file there for Collabration Engine features to work.

To create a production build, call `mvnw clean package -Pproduction` (Windows),
or `./mvnw clean package -Pproduction` (Mac & Linux).
This will build a JAR file with all the dependencies and front-end resources,
ready to be deployed. The file can be found in the `target` folder after the build completes.

Once the JAR file is built, you can run it using
`java -jar target/trampolineworld-1.0-SNAPSHOT.jar`

## Project structure

- `MainLayout.java` in `src/main/java` contains the navigation setup (i.e., the
  side/top bar and the main menu). This setup uses
  [App Layout](https://vaadin.com/components/vaadin-app-layout).
- `views` package in `src/main/java` contains the server-side Java views of your application.
- `views` folder in `frontend/` contains the client-side JavaScript views of your application.
- `themes` folder in `frontend/` contains the custom CSS styles.

## Dependencies

This application was predominantly developed in the Java programming language.
It uses:
- Spring Boot
- Spring Security
- JPA (Java Persistence API)
- Vaadin Flow Framework
