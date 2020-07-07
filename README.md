# Keycloak-json-logger
Custom Keycloak Event Listener for logging (outputting) Keycloak Login events and Admin events in JSON format

PROTOTYPE at this point (to be reviewd).

**Requirements**
- Java JDK
- Maven

**Commands**
| Name | Description |
|:-|:-|
| `mvn package` | Build and package keycloack-json-logger.jar file into /target folder |

**Keycloak deployment (standalone mode)**

Step 1: Copy keycloack-json-logger.jar into $KEYCLOAK_DIR/standalone/deployments

Step 2: In Keycloak Admin UI, go to the Events left menu item and select the Config tab, and add "keycloak_json_logger" to Event listeners config
- Note, that settings for saving login events and admin events to Keycloak database have no dependecy to Event Listeners config.

**Logger settings**

***Logging with default settings***

Keycloak_json_logger uses org.jboss.loggin.Logger and logging confuguration (loggers, handlers, formatters) are defined in Keycloak's (WildFly's) standalone.xml or standalone-ha.xml configuration file. By default, `root-logger` is used and it logs to both to console (stdout) and to  server.log file.

Furthermore, by default, the root-logger uses such formatters, that each log line starts with timestamp, loglevel, etc. information and only then the actual log message JSON. This means, that Filebeat's automatic JSON decoding cannot be used.

Example default logging into server.log -file (and to console)
``` 
2020-06-29 09:08:44,457 INFO  [org.keycloak.events.Event] (default task-1) {"keycloak_login_event":{"date_time_epoch":1593410924440,"date_time":"2020-06-29T09:08:44.440Z","type":"LOGIN","realm":"local-dev","client_id":"account","user_id":"ccad2aaa-d7ea-46fc-b91d-d37c95c4f9bb","ip_address":"127.0.0.1","session_id":"8b0123aa-b223-4f93-a415-6fa29e5beede","details":{"identity_provider":"github","redirect_uri":"http://localhost:8080/auth/realms/local-dev/account/login-redirect","consent":"no_consent_required","identity_provider_identity":"Username not logged on purpose","code_id":"8b0123aa-b223-4f93-a415-6fa29e5beede","username":"Username not logged on purpose"},"authentication_session_parent_id":"8b0123aa-b223-4f93-a415-6fa29e5beede","authentication_session_tab_id":"4cISlfSEfwI"}}
```

***Log configuration for keycloak_json_logger to output to console (stdout) only and json message only***

This is valid, usable configuration if Keycloak runs in Docker container and Filebeat will be setup with `container` input (Filebeat reading container log files).
```
standalone.xml (standalone-ha.xml)
....
<subsystem xmlns="urn:jboss:domain:logging:8.0">
.....
  <console-handler name="CONSOLE-EVENTS">
    <level name="INFO"/>
    <formatter>
      <named-formatter name="MESSAGE-ONLY"/>
    </formatter>
  </console-handler>
  <logger category="org.keycloak.events.Event" use-parent-handlers="false">
    <level name="INFO"/>
    <handlers>
      <handler name="CONSOLE-EVENTS"/>
    </handlers>
  </logger>
  <logger category="org.keycloak.events.admin.AdminEvent" use-parent-handlers="false">
    <level name="INFO"/>
    <handlers>
      <handler name="CONSOLE-EVENTS"/>
    </handlers>
  </logger>
  ....
  <formatter name="MESSAGE-ONLY">
    <pattern-formatter pattern="%s%e%n"/>
  </formatter>
 ```
Example console (stdout) output with log configuration above
```
{"keycloak_event":{"category":"LOGIN_EVENT","date_time_epoch":1593775257302,"date_time":"2020-07-03T11:20:57.302Z","actor_user":{"role":"owner","user_id":"327f2cb1-0180-4a9e-9923-0347dbd31b25","user_name":"not_logged_on_purpose","realm":"local-dev"},"target_user":{"user_id":"327f2cb1-0180-4a9e-9923-0347dbd31b25","user_name":"not_logged_on_purpose","realm":"local-dev"},"type":"LOGIN","realm":"local-dev","client_id":"account","user_id":"327f2cb1-0180-4a9e-9923-0347dbd31b25","ip_address":"172.17.0.1","session_id":"1412873b-8830-4e83-ae46-4ca63512a3e2","details":{"auth_method":"openid-connect","auth_type":"code","redirect_uri":"http://localhost:9090/auth/realms/local-dev/account/login-redirect","consent":"no_consent_required","code_id":"1412873b-8830-4e83-ae46-4ca63512a3e2","username":"not_logged_on_purpose"},"authentication_session_parent_id":"1412873b-8830-4e83-ae46-4ca63512a3e2","authentication_session_tab_id":"OniFaL7lqpg"}}
```
