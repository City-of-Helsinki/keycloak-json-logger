package helsinki.profile.keycloak.events.log;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;



public class JsonLoggingEventListenerProviderFactory implements EventListenerProviderFactory {

    private static final Logger loggerLoginEvents = Logger.getLogger("org.keycloak.events.Event");
    private static final Logger loggerAdminEvents = Logger.getLogger("org.keycloak.events.admin.AdminEvent");


    @Override
    public EventListenerProvider create(KeycloakSession session) {

        return new JsonLoggingEventListenerProvider(session, loggerLoginEvents, loggerAdminEvents);
    }

    @Override
    public void init(Config.Scope scope) {
      
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return "keycloak_json_logger";
    }
}
