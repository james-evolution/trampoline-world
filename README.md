# Trampoline World

Built for someone very dear to my heart.

A full-stack persistent web application that doubles as both a desktop and mobile application.
<br>Compatible with macOS, Windows, iOS, and Android. This application is effectively a CRM intended for tracking trampoline orders.

## Application Overview
https://youtu.be/134bgAV4l8k

## Application Features
<ul>
<li>Authentication & Authorization: Users must log in to access this application. Individual permissions depend upon account type.</li>
<li>CRUD Operations: Users can create, read, update, and delete orders.</li>
<li>Searching, Sorting, & Filtering: All pages with grid views have these features enabled. Admins can search through orders, system users, and system logs in a variety of ways.</li>
<li>Column Reordering, Resizing, & Filtering: Users can drag columns to reorder and resize them. Additionally, they can hide/show specific columns with the Show/Hide button. (On pages with an abundance of detailed information, some columns are not shown by default, but users can opt in to see them with the aforementioned button.)</li>
<li>Data Exports: Order information can be exported as needed in either PDF or CSV format.</li>
<li>Form Validation: The form used to create new orders and edit existing ones can validate input to meet any specified requirements.</li>
<li>Live Chat: A still-in-development and optional feature is live-chat. This is showcased in the video.</li>
<li>Persistent Data Storage: This application uses a MySQL database to store & retrieve order information.</li>
<li>Archives: When an order gets deleted from the system, it isn't actually deleted from the database. Instead, it is simply flagged so that it does not appear on the orders page. Admins can view deleted orders in the archives, where they can restore them if they desire.</li>
<li>User Management: Admins can manage user accounts and modify their names, passwords, and permissions.</li>
<li>Audit Log: All user actions are logged in the system. Administrators can see who made what changes at what time.</li>
<li>Date/Time Sorting: By default, all orders and logs are sorted by their date or timestamp, with the most recent entries showing at the top of the grid.</li>
<li>Profile Customization: Users can customize their own profile picture, color, display name, email address, and password.</li>
<li>Password Resets: If a user forgets their password they can have a reset code sent to their email via the login page. Alternatively, administrators can change their password by hand.</li>
<li>Tooltips & Helper Text: Some columns on the user management page show helpful hints/information if you hover over them with your cursor. Many input fields also have helper text beneath them as guidelines.</li>
<li>Discord Integration: Admins & Techs can use the Discord Integration page to enable or disable data logging to Discord. Three log categories exist: Audit, Chat, and UUID. Alternatively, they can edit webhook URLs to send this data to whichever channel or server they wish.</li>
<li>Open Source: The full source code used to develop this application is freely available to the owners of Trampoline World.</li>
<li>Questions & Requests: The contact page allows admins to instantly contact the developer via email or text-to-speech Discord message if they have questions or want to make requests.</li>
</ul>

## Running the Application (Localhost)

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

Upon launching, Application.java automatically generates the ce-license.json file in the /app folder of the Linux container that Heroku runs this application on.
This is for sake of enabling Vaadin's CollaborationEngine features. Although unnecessary, a useful bit of information is the atlernate method of placing the license file: Extracting it from the jar. To do so, one can:

1. Run `heroku ps:exec` to open a remote shell on the Heroku's linux server.
2. Run ``jar -xf target/trampolineworld-1.0-SNAPSHOT.jar /META-INF/resources/ce-license.json`` 

Order editing and live chat features currently utilize the CollaborationEngine. While it is possible to recode the application to not use the CollaborationEngine for those capabilities, it isn't necessary with the license, so long as you limit yourself to 20 active user accounts per month. And even if your surpass this, you get a 30-day grace period in which that limit is increased to 200 users per month before being reset back to 20. If the 200 user limit is surpassed during the grace period, or if you surpass the 20 user limit a second time in the future: Order editing & live chat should still work, but users registered after the first twenty will not be able to take advantage of collaborative features. This means their avatar will not show up in the avatar group and forms they're editing won't get highlighted in their profile color to indicate to other users that they're editing it. The first 20 will always have access to collaborative features, though. This application may someday be updated with hard-coded features to prevent any possibility of surpassing this cap, if desired.

Feel free to ask if you have any questions about this subject.

## Deploying to Production (General)

This section is only relevant if you intend on deploying this application somewhere aside from Heroku.
Bear in mind that you will need to place your ce-license.json file in the same directory that you deploy your application for Collabration Engine features to work. The exact directory path can be set via the setDataDir() method in the Application.java file.

To create a production build, call `mvnw clean package -Pproduction` (Windows),
or `./mvnw clean package -Pproduction` (Mac & Linux).
This will build a JAR file with all the dependencies and front-end resources,
ready to be deployed. The file can be found in the `target` folder after the build completes.

Once the JAR file is built, you can run it using
`java -jar target/trampolineworld-1.0-SNAPSHOT.jar`

## Collaboration Engine & The Universal License File

The universal license file only authorizes 20 unique users per month to use the CollaborationEngine. At the beginning of each month, the counter is set back to zero. Each time a new user is created, a UUID (universally unique identifier) is generated for them. If they then interact with any CE features, such as order editing or live chat, their UUID is registered with the CollaborationEngine and counts towards the monthly 20 user quota. 

For this reason, it's best not to delete & re-create new accounts as that will cause unnecessary inflation of the quota due to the repeated generation of new UUIDs. Instead, it's best to simply maintain the same 20 accounts and re-purpose them as people come and go. (This is why the User Management page does not allow account deletion. Existing UUIDs are valuable. They're also logged to Discord via webhook so that they can be re-used if ever they are somehow lost.)

## Project Structure

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
- Spring Boot Starter Mail 
- Spring Security
- JPA (Java Persistence API)
- MySQL Connector & Database
- Vaadin Flow Framework
- Jasper Reports
