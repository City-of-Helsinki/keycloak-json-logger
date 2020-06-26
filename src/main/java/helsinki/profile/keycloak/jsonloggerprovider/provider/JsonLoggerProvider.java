package helsinki.profile.keycloak.jsonloggerprovider.provider;

import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.sessions.AuthenticationSessionModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import javax.json.*;

public class JsonLoggerProvider implements EventListenerProvider {

    KeycloakSession session;
    Logger loggerLoginEvents;
	Logger loggerAdminEvents;
	SimpleDateFormat dateTimeFormatter;


	public JsonLoggerProvider(KeycloakSession session, Logger loggerLoginEvents, Logger loggerAdminEvents) {
        this.session = session;
		this.loggerLoginEvents = loggerLoginEvents;
		this.loggerAdminEvents = loggerAdminEvents;
		
		this.dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    }
	
    @Override
    public void onEvent(Event event) {

        StringBuilder sb = new StringBuilder();

        sb.append(toString(event));

		loggerLoginEvents.log(Logger.Level.INFO, sb.toString());
		
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean b) {

        StringBuilder sb = new StringBuilder();

        sb.append(toString(adminEvent));

		loggerAdminEvents.log(Logger.Level.INFO, sb.toString());
    }

    @Override
    public void close() {

    }

    private String toString(Event event) {
		
		JsonObjectBuilder obj = Json.createObjectBuilder();
		
        obj.add("date_time_epoch", event.getTime());
		
		obj.add("date_time", dateTimeFormatter.format(new java.util.Date(event.getTime())));
		
		if (event.getType() != null) {
			obj.add("type", event.getType().toString());
		}

        if (event.getRealmId() != null) {
            obj.add("realm", event.getRealmId().toString());
        }

        if (event.getClientId() != null) {
            obj.add("client_id", event.getClientId().toString());
        }

        if (event.getUserId() != null) {
            obj.add("user_id", event.getUserId().toString());
        }

        if (event.getIpAddress() != null) {
            obj.add("ip_address", event.getIpAddress().toString());
        }
		
		 if (event.getSessionId() != null) {
            obj.add("session_id", event.getSessionId().toString());
        }

        if (event.getError() != null) {
            obj.add("error", event.getError().toString());
        }

        if (event.getDetails() != null) {
			JsonObjectBuilder objDetails  = Json.createObjectBuilder();
            for (Map.Entry<String, String> e : event.getDetails().entrySet()) {
                objDetails.add(e.getKey(), e.getValue().toString());
            }
			obj.add("details", objDetails.build());
        }
		
		JsonObjectBuilder objRoot = Json.createObjectBuilder();
		
        return objRoot.add("keycloak_login_event", obj.build()).build().toString();

    }


    private String toString(AdminEvent adminEvent) {

		JsonObjectBuilder obj = Json.createObjectBuilder();
		
        obj.add("date_time_epoch", adminEvent.getTime());
		obj.add("date_time", dateTimeFormatter.format(new java.util.Date(adminEvent.getTime())));

        obj.add("type", "ADMIN_EVENT");

        if (adminEvent.getOperationType() != null) {
            obj.add("operation_type", adminEvent.getOperationType().toString());
        }

        if (adminEvent.getAuthDetails() != null) {
			
			JsonObjectBuilder objAuthDetails =  Json.createObjectBuilder();
			
            if (adminEvent.getAuthDetails().getRealmId() != null) {
                objAuthDetails.add("realm", adminEvent.getAuthDetails().getRealmId().toString());
            }

            if (adminEvent.getAuthDetails().getClientId() != null) {
                objAuthDetails.add("client_id", adminEvent.getAuthDetails().getClientId().toString());
            }

            if (adminEvent.getAuthDetails().getUserId() != null) {
                objAuthDetails.add("user_id", adminEvent.getAuthDetails().getUserId().toString());
            }

            if (adminEvent.getAuthDetails().getIpAddress() != null) {
                objAuthDetails.add("ip_address", adminEvent.getAuthDetails().getIpAddress().toString());
            }
			
			obj.add("auth_details", objAuthDetails.build());

        }

        if (adminEvent.getResourceType() != null) {
            obj.add("resource_type", adminEvent.getResourceType().toString());
        }

        if (adminEvent.getResourcePath() != null) {
            obj.add("resource_path", adminEvent.getResourcePath().toString());
        }

        if (adminEvent.getError() != null) {
            obj.add("error", adminEvent.getError().toString());
        }

		JsonObjectBuilder objRoot = Json.createObjectBuilder();
		
        return objRoot.add("keycloak_admin_event", obj.build()).build().toString();
    }

}
