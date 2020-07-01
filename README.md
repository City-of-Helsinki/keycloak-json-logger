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

Keycloak_json_logger use org.jboss.loggin.Logger, configured in Keycloak's standalone.xml )or standalone-ha.xml) configuration file. By default, after deployment, it uses "root-logger" and logs to both to console (stdout) and to  'server.log' file in $KEYCLOAK_DIR/standalone/logs folder.

Furthermore, by default, the root-logger formats each log line so that there are timestamp, loglevel, etc. information in front of actual json message, which means that Filebeat's automatic JSON decoding cannot be used.

Example default logging into server.log -file (and to console)
``` 
2020-06-29 09:08:44,457 INFO  [org.keycloak.events.Event] (default task-1) {"keycloak_login_event":{"date_time_epoch":1593410924440,"date_time":"2020-06-29T09:08:44.440Z","type":"LOGIN","realm":"local-dev","client_id":"account","user_id":"ccad2aaa-d7ea-46fc-b91d-d37c95c4f9bb","ip_address":"127.0.0.1","session_id":"8b0123aa-b223-4f93-a415-6fa29e5beede","details":{"identity_provider":"github","redirect_uri":"http://localhost:8080/auth/realms/local-dev/account/login-redirect","consent":"no_consent_required","identity_provider_identity":"Username not logged on purpose","code_id":"8b0123aa-b223-4f93-a415-6fa29e5beede","username":"Username not logged on purpose"},"authentication_session_parent_id":"8b0123aa-b223-4f93-a415-6fa29e5beede","authentication_session_tab_id":"4cISlfSEfwI"}}
```


***Logging events as json only***

Example standalone.xml configuration to log JSON only and both Login events and Admin events to their own files
```
<subsystem xmlns="urn:jboss:domain:logging:8.0">
  <periodic-rotating-file-handler name="KEYCLOAK-LOGIN-EVENTS-FILE" autoflush="true">
    <formatter>
      <named-formatter name="MESSAGE-ONLY"/>
    </formatter>
    <file relative-to="jboss.server.log.dir" path="keycloak-login-events.log"/>
    <suffix value=".yyyy-MM-dd"/>
    <append value="false"/>
  </periodic-rotating-file-handler>
  <periodic-rotating-file-handler name="KEYCLOAK-ADMIN-EVENTS-FILE" autoflush="true">
    <formatter>
      <named-formatter name="MESSAGE-ONLY"/>
    </formatter>
    <file relative-to="jboss.server.log.dir" path="keycloak-admin-events.log"/>
     <suffix value=".yyyy-MM-dd"/>
    <append value="false"/>
  </periodic-rotating-file-handler>
  <logger category="org.keycloak.events.Event" use-parent-handlers="false">
    <level name="INFO"/>
      <handlers>
        <handler name="KEYCLOAK-LOGIN-EVENTS-FILE"/>
      </handlers>
    </logger>
  <logger category="org.keycloak.events.admin.AdminEvent" use-parent-handlers="false">
    <level name="INFO"/>
    <handlers>
      <handler name="KEYCLOAK-ADMIN-EVENTS-FILE"/>
    </handlers>
  </logger>
  <formatter name="MESSAGE-ONLY">
    <pattern-formatter pattern="%s%e%n"/>
  </formatter>
```

