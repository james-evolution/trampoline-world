# Trampoline World

Built for someone very dear to my heart.

A full-stack persistent web application that doubles as both a desktop and mobile application.
<br>Compatible with macOS, Windows, iOS, and Android. This application is effectively a CRM intended for tracking trampoline orders.

https://youtu.be/134bgAV4l8k

# Running the Application (Localhost)

The project is a standard Maven project. To run it from the command line,
type `mvnw` (Windows), or `./mvnw` (Mac & Linux), then open
http://localhost:8090 in your browser.

You can also import the project to your IDE of choice as you would with any
Maven project. Read more on [how to import Vaadin projects to different 
IDEs](https://vaadin.com/docs/latest/flow/guide/step-by-step/importing) (Eclipse, IntelliJ IDEA, NetBeans, and VS Code).

# Deploying to Production (Heroku)

At current, this application is hosted on Heroku. However, if redeployment is ever needed, this is how to go about it.

1. Ensure you have the Heroku CLI installed.
2. Open a terminal or command prompt from the project's root folder.
3. Run `heroku login`
4. Run `mvnw package -Pproduction && heroku deploy:jar target\trampolineworld-1.0-SNAPSHOT.jar -a trampolineworld && heroku open`

Upon launching, Application.java automatically generates the ce-license.json file in the /app folder of the Linux container that Heroku runs this application on.
This is for sake of enabling Vaadin's CollaborationEngine features. Although unnecessary, a useful bit of information is the atlernate method of placing the license file: Extracting it from the jar. To do so, one can:

1. Run `heroku ps:exec` to open a remote shell on the Heroku's linux server.
2. Run ``jar -xf target/trampolineworld-1.0-SNAPSHOT.jar /META-INF/resources/ce-license.json`` 

# Deploying to Production (General)

This section is only relevant if you intend on deploying this application somewhere aside from Heroku.
Bear in mind that you will need to place your ce-license.json file in the same directory that you deploy your application for Collabration Engine features to work. The exact directory path can be set via the setDataDir() method in the Application.java file.

To create a production build, call `mvnw clean package -Pproduction` (Windows),
or `./mvnw clean package -Pproduction` (Mac & Linux).
This will build a JAR file with all the dependencies and front-end resources,
ready to be deployed. The file can be found in the `target` folder after the build completes.

Once the JAR file is built, you can run it using
`java -jar target/trampolineworld-1.0-SNAPSHOT.jar`

# Database Configuration

The following SQL script can be executed in any MySQL database to rebuild the schema:
```
CREATE SCHEMA trampolineworld;

CREATE TABLE application_user ( 
	id                   VARCHAR(200)  NOT NULL DEFAULT ''   PRIMARY KEY,
	username             VARCHAR(255)   DEFAULT NULL   ,
	display_name         VARCHAR(255)   DEFAULT NULL   ,
	email                VARCHAR(255)   DEFAULT NULL   ,
	hashed_password      VARCHAR(255)   DEFAULT NULL   ,
	roles                SET('Value A','Value B')   DEFAULT NULL   ,
	profile_picture_url  VARCHAR(255)   DEFAULT NULL   ,
	color_index          INT  NOT NULL DEFAULT '0'   
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE audit_logs ( 
	id                   VARCHAR(200)  NOT NULL    PRIMARY KEY,
	user_id              VARCHAR(200)  NOT NULL    ,
	username             VARCHAR(255)  NOT NULL    ,
	target_user_id       VARCHAR(200)   DEFAULT NULL   ,
	target_order_id      VARCHAR(255)   DEFAULT NULL   ,
	customer_name        VARCHAR(255)   DEFAULT NULL   ,
	action_category      VARCHAR(255)   DEFAULT NULL   ,
	action_details       TEXT      ,
	timestamp            TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP   
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE chat_logs ( 
	topic                VARCHAR(255)  NOT NULL    ,
	`text`               VARCHAR(255)  NOT NULL    ,
	author_id            VARCHAR(200)  NOT NULL    ,
	timestamp            TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP   ,
	id                   VARCHAR(200)  NOT NULL    PRIMARY KEY,
	author_name          VARCHAR(255)   DEFAULT NULL   ,
	author_avatar_url    VARCHAR(255)   DEFAULT NULL   ,
	author_color_index   INT   DEFAULT NULL   
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE trampoline_order ( 
	id                   BIGINT  NOT NULL  AUTO_INCREMENT  PRIMARY KEY,
	complete             TINYINT   DEFAULT NULL   ,
	first_name           VARCHAR(255)   DEFAULT NULL   ,
	last_name            VARCHAR(255)   DEFAULT NULL   ,
	phone_number         VARCHAR(255)   DEFAULT NULL   ,
	email                VARCHAR(255)   DEFAULT NULL   ,
	order_description    TEXT      ,
	measurements         VARCHAR(255)   DEFAULT NULL   ,
	subtotal             DOUBLE   DEFAULT NULL   ,
	total                DOUBLE   DEFAULT NULL   ,
	`date`               DATE   DEFAULT NULL   ,
	deleted              TINYINT   DEFAULT '0'   
 ) ENGINE=InnoDB AUTO_INCREMENT=70039 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE user_roles ( 
	user_id              VARCHAR(200)   DEFAULT NULL   ,
	roles                VARCHAR(255)   DEFAULT NULL   
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE webhooks ( 
	id                   VARCHAR(200)  NOT NULL    PRIMARY KEY,
	webhook_name         VARCHAR(255)  NOT NULL    ,
	webhook_url          VARCHAR(255)  NOT NULL    
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
```

## Database Structure
While the above script should create the necessary schema automatically, if you wish to do it by hand, the database structure is outlined below.

<img src="https://faintdev.net/trampolineworld/tw_layout.svg">


## Tables

1. [trampolineworld.application_user](#table-application_user) 2. [trampolineworld.audit_logs](#table-audit_logs) 3. [trampolineworld.chat_logs](#table-chat_logs) 4. [trampolineworld.trampoline_order](#table-trampoline_order) 5. [trampolineworld.user_roles](#table-user_roles) 6. [trampolineworld.webhooks](#table-webhooks) 


### Table application_user 
| Idx | Field Name | Data Type |
|---|---|---|
| *🔑 | <a name='trampolineworld.application_user_id'>id</a>| VARCHAR&#40;200&#41; COLLATE utf8&#95;unicode&#95;ci DEFAULT '' |
|  | <a name='trampolineworld.application_user_username'>username</a>| VARCHAR&#40;255&#41; COLLATE utf8&#95;unicode&#95;ci DEFAULT NULL |
|  | <a name='trampolineworld.application_user_display_name'>display&#95;name</a>| VARCHAR&#40;255&#41; COLLATE utf8&#95;unicode&#95;ci DEFAULT NULL |
|  | <a name='trampolineworld.application_user_email'>email</a>| VARCHAR&#40;255&#41; COLLATE utf8&#95;unicode&#95;ci DEFAULT NULL |
|  | <a name='trampolineworld.application_user_hashed_password'>hashed&#95;password</a>| VARCHAR&#40;255&#41; COLLATE utf8&#95;unicode&#95;ci DEFAULT NULL |
|  | <a name='trampolineworld.application_user_roles'>roles</a>| SET&#40;&#39;Value A&#39;&#44;&#39;Value B&#39;&#41; COLLATE utf8&#95;unicode&#95;ci DEFAULT NULL |
|  | <a name='trampolineworld.application_user_profile_picture_url'>profile&#95;picture&#95;url</a>| VARCHAR&#40;255&#41; COLLATE utf8&#95;unicode&#95;ci DEFAULT NULL |
| *| <a name='trampolineworld.application_user_color_index'>color&#95;index</a>| INT  DEFAULT '0' |
| Indexes |
| 🔑 | pk&#95;application&#95;user || ON id|
| Options |
| ENGINE&#61;InnoDB DEFAULT CHARSET&#61;utf8 COLLATE&#61;utf8&#95;unicode&#95;ci |


### Table audit_logs 
| Idx | Field Name | Data Type |
|---|---|---|
| *🔑 | <a name='trampolineworld.audit_logs_id'>id</a>| VARCHAR&#40;200&#41; COLLATE utf8&#95;unicode&#95;ci |
| *| <a name='trampolineworld.audit_logs_user_id'>user&#95;id</a>| VARCHAR&#40;200&#41; COLLATE utf8&#95;unicode&#95;ci |
| *| <a name='trampolineworld.audit_logs_username'>username</a>| VARCHAR&#40;255&#41; COLLATE utf8&#95;unicode&#95;ci |
|  | <a name='trampolineworld.audit_logs_target_user_id'>target&#95;user&#95;id</a>| VARCHAR&#40;200&#41; COLLATE utf8&#95;unicode&#95;ci DEFAULT NULL |
|  | <a name='trampolineworld.audit_logs_target_order_id'>target&#95;order&#95;id</a>| VARCHAR&#40;255&#41; COLLATE utf8&#95;unicode&#95;ci DEFAULT NULL |
|  | <a name='trampolineworld.audit_logs_customer_name'>customer&#95;name</a>| VARCHAR&#40;255&#41; COLLATE utf8&#95;unicode&#95;ci DEFAULT NULL |
|  | <a name='trampolineworld.audit_logs_action_category'>action&#95;category</a>| VARCHAR&#40;255&#41; COLLATE utf8&#95;unicode&#95;ci DEFAULT NULL |
|  | <a name='trampolineworld.audit_logs_action_details'>action&#95;details</a>| TEXT COLLATE utf8&#95;unicode&#95;ci |
| *| <a name='trampolineworld.audit_logs_timestamp'>timestamp</a>| TIMESTAMP  DEFAULT CURRENT_TIMESTAMP |
| Indexes |
| 🔑 | pk&#95;audit&#95;logs || ON id|
| Options |
| ENGINE&#61;InnoDB DEFAULT CHARSET&#61;utf8 COLLATE&#61;utf8&#95;unicode&#95;ci |


### Table chat_logs 
| Idx | Field Name | Data Type |
|---|---|---|
| *| <a name='trampolineworld.chat_logs_topic'>topic</a>| VARCHAR&#40;255&#41; COLLATE utf8&#95;unicode&#95;ci |
| *| <a name='trampolineworld.chat_logs_text'>text</a>| VARCHAR&#40;255&#41; COLLATE utf8&#95;unicode&#95;ci |
| *| <a name='trampolineworld.chat_logs_author_id'>author&#95;id</a>| VARCHAR&#40;200&#41; COLLATE utf8&#95;unicode&#95;ci |
| *| <a name='trampolineworld.chat_logs_timestamp'>timestamp</a>| TIMESTAMP ON UPDATE CURRENT&#95;TIMESTAMP DEFAULT CURRENT_TIMESTAMP |
| *🔑 | <a name='trampolineworld.chat_logs_id'>id</a>| VARCHAR&#40;200&#41; COLLATE utf8&#95;unicode&#95;ci |
|  | <a name='trampolineworld.chat_logs_author_name'>author&#95;name</a>| VARCHAR&#40;255&#41; COLLATE utf8&#95;unicode&#95;ci DEFAULT NULL |
|  | <a name='trampolineworld.chat_logs_author_avatar_url'>author&#95;avatar&#95;url</a>| VARCHAR&#40;255&#41; COLLATE utf8&#95;unicode&#95;ci DEFAULT NULL |
|  | <a name='trampolineworld.chat_logs_author_color_index'>author&#95;color&#95;index</a>| INT  DEFAULT NULL |
| Indexes |
| 🔑 | pk&#95;chat&#95;logs || ON id|
| Options |
| ENGINE&#61;InnoDB DEFAULT CHARSET&#61;utf8 COLLATE&#61;utf8&#95;unicode&#95;ci |


### Table trampoline_order 
| Idx | Field Name | Data Type |
|---|---|---|
| *🔑 | <a name='trampolineworld.trampoline_order_id'>id</a>| BIGINT AUTO_INCREMENT |
|  | <a name='trampolineworld.trampoline_order_complete'>complete</a>| TINYINT  DEFAULT NULL |
|  | <a name='trampolineworld.trampoline_order_first_name'>first&#95;name</a>| VARCHAR&#40;255&#41; COLLATE utf8&#95;unicode&#95;ci DEFAULT NULL |
|  | <a name='trampolineworld.trampoline_order_last_name'>last&#95;name</a>| VARCHAR&#40;255&#41; COLLATE utf8&#95;unicode&#95;ci DEFAULT NULL |
|  | <a name='trampolineworld.trampoline_order_phone_number'>phone&#95;number</a>| VARCHAR&#40;255&#41; COLLATE utf8&#95;unicode&#95;ci DEFAULT NULL |
|  | <a name='trampolineworld.trampoline_order_email'>email</a>| VARCHAR&#40;255&#41; COLLATE utf8&#95;unicode&#95;ci DEFAULT NULL |
|  | <a name='trampolineworld.trampoline_order_order_description'>order&#95;description</a>| TEXT COLLATE utf8&#95;unicode&#95;ci |
|  | <a name='trampolineworld.trampoline_order_measurements'>measurements</a>| VARCHAR&#40;255&#41; COLLATE utf8&#95;unicode&#95;ci DEFAULT NULL |
|  | <a name='trampolineworld.trampoline_order_subtotal'>subtotal</a>| DOUBLE  DEFAULT NULL |
|  | <a name='trampolineworld.trampoline_order_total'>total</a>| DOUBLE  DEFAULT NULL |
|  | <a name='trampolineworld.trampoline_order_date'>date</a>| DATE  DEFAULT NULL |
|  | <a name='trampolineworld.trampoline_order_deleted'>deleted</a>| TINYINT  DEFAULT '0' |
| Indexes |
| 🔑 | pk&#95;trampoline&#95;order || ON id|
| Options |
| ENGINE&#61;InnoDB AUTO&#95;INCREMENT&#61;70039 DEFAULT CHARSET&#61;utf8 COLLATE&#61;utf8&#95;unicode&#95;ci |


### Table user_roles 
| Idx | Field Name | Data Type |
|---|---|---|
|  | <a name='trampolineworld.user_roles_user_id'>user&#95;id</a>| VARCHAR&#40;200&#41; COLLATE utf8&#95;unicode&#95;ci DEFAULT NULL |
|  | <a name='trampolineworld.user_roles_roles'>roles</a>| VARCHAR&#40;255&#41; COLLATE utf8&#95;unicode&#95;ci DEFAULT NULL |
| Options |
| ENGINE&#61;InnoDB DEFAULT CHARSET&#61;utf8 COLLATE&#61;utf8&#95;unicode&#95;ci |


### Table webhooks 
| Idx | Field Name | Data Type |
|---|---|---|
| *🔑 | <a name='trampolineworld.webhooks_id'>id</a>| VARCHAR&#40;200&#41; COLLATE utf8&#95;unicode&#95;ci |
| *| <a name='trampolineworld.webhooks_webhook_name'>webhook&#95;name</a>| VARCHAR&#40;255&#41; COLLATE utf8&#95;unicode&#95;ci |
| *| <a name='trampolineworld.webhooks_webhook_url'>webhook&#95;url</a>| VARCHAR&#40;255&#41; COLLATE utf8&#95;unicode&#95;ci |
| Indexes |
| 🔑 | pk&#95;webhooks || ON id|
| Options |
| ENGINE&#61;InnoDB DEFAULT CHARSET&#61;utf8 COLLATE&#61;utf8&#95;unicode&#95;ci |


## Collaboration Engine & The Universal License File

The universal license file only authorizes 20 unique users per month to use the CollaborationEngine. Each time a new user is created, a UUID (universally unique identifier) is generated for them. If they then interact with any CE features, such as order editing or live chat, their UUID is registered with the CollaborationEngine and counts towards the monthly 20 user quota. Each user is only counted once per month, and the counter is reset back to 0 at the beginning of each new month.

This application is coded to recycle UUIDs. It has a list of 20 priority UUIDs that have already been registered with the engine. If a new user is created, the system will attempt to assign it a priority ID first. If none are available (as they're all taken) they'll be left with one auto-generated by JPA.

The first time you surpass the 20 user limit, you enter a 30 day grace period in which the limit is raised to 200. After 30 days it goes back down to 20. From then, if you surpass the limit again, CollabrationEngine features are disabled for anyone past the first 20 users. The system still works as intended and will operate normally, but users beyond the original 20 will not have fields they're editing highlighted in their profile color, and they'll only be able to see their own avatar in avatar groups. Basically, there'll be some aesthetic differences for additional users, but things should still work fine from a fucntionality standpoint.

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


