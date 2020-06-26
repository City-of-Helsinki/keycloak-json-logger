# Keycloak-json-logger
Custom Keycloak Event Listener for logging (outputting) Keycloak Login events and Admin events in JSON format

PROTOTYPE ONLY at this point.

**Requirements**
- Java JDK
- Maven

**Commands**
| Name | Description |
|:-|:-|
| `mvn package` | Build and package keycloack-json-logger.jar file into /target folder |

**Keycloak deployment (standalone mode)**

Step 1: Copy keycloack-json-logger.jar into $KEYCLOAK_DIR/standalone/deployments

Step 2: In Keycloak Admin UI, go to the Events left menu item and select the Config tab, and add 'keycloak_json_logger' to Event listeners config
- Note, that settings for saving login events and admin events to Keycloak database have no dependecy to Event Listeners config.

**Logger settings**

By default, after deployment, Keycloak json logger uses the root-logger defined in Keycloak's standalone.xml (see <root-logger> there).
  
MORE TO BE WRITTEN (UNDER CONSTRUCTION)
