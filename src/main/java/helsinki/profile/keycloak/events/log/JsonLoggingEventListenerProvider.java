package helsinki.profile.keycloak.events.log;

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
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

public class JsonLoggingEventListenerProvider implements EventListenerProvider {

    private final KeycloakSession session;
    private final Logger loggerLoginEvents;
    private final Logger loggerAdminEvents;
    private final SimpleDateFormat dateTimeFormatter;


    public JsonLoggingEventListenerProvider(KeycloakSession session, Logger loggerLoginEvents, Logger loggerAdminEvents) {
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
        
        JsonObjectBuilder objAuditEvent = Json.createObjectBuilder();
        
        objAuditEvent.add("origin", "KEYCLOAK");
        
        objAuditEvent.add("operation", event.getType() != null ? event.getType().toString() : "");
        
        objAuditEvent.add("date_time_epoch", event.getTime());
        
        objAuditEvent.add("date_time", dateTimeFormatter.format(new java.util.Date(event.getTime())));
        
        JsonObjectBuilder objActor= Json.createObjectBuilder();
        objActor.add("role", "OWNER");
        String userId = "";
        String userName = "";
        if (event.getUserId() != null) {
            userId = event.getUserId().toString();
            userName = "not_logged_on_purpose";
        }
        objActor.add("user_id", userId);
        objActor.add("user_name", userName);
        String realm = "";
        if (event.getRealmId() != null)
            realm = event.getRealmId();
        objActor.add("realm", realm);
        objAuditEvent.add("actor", objActor.build());
        
        JsonObjectBuilder objTarget = Json.createObjectBuilder();
        userId = "";
        if (event.getUserId() != null) {
            userId = event.getUserId().toString();
            userName = "not_logged_on_purpose";
        }
        objTarget.add("user_id", userId);
        objTarget.add("user_name", userName);
        realm = "";
        if (event.getRealmId() != null)
            realm = event.getRealmId();
        objTarget.add("realm", realm);
        objAuditEvent.add("target", objTarget.build());
        
        JsonObjectBuilder objKeycloakEvent = Json.createObjectBuilder();
        
        objKeycloakEvent.add("category", "LOGIN_EVENT");
        
        if (event.getType() != null) {
            objKeycloakEvent.add("type", event.getType().toString());
        }

        if (event.getRealmId() != null) {
            objKeycloakEvent.add("realm", event.getRealmId().toString());
        }

        if (event.getClientId() != null) {
            objKeycloakEvent.add("client_id", event.getClientId().toString());
        }

        if (event.getUserId() != null) {
            objKeycloakEvent.add("user_id", event.getUserId().toString());
        }

        if (event.getIpAddress() != null) {
            objKeycloakEvent.add("ip_address", event.getIpAddress().toString());
        }
        
         if (event.getSessionId() != null) {
            objKeycloakEvent.add("session_id", event.getSessionId().toString());
        }

        if (event.getError() != null) {
            objKeycloakEvent.add("error", event.getError().toString());
        }

        if (event.getDetails() != null) {
            JsonObjectBuilder objDetails  = Json.createObjectBuilder();
            for (Map.Entry<String, String> e : event.getDetails().entrySet()) {
                
                // Because of GDPR, we may not want to log username (=email) to Centralized audit log (Elastic cloud)
                if (e.getKey() == "username" || e.getKey() == "identity_provider_identity")
                    objDetails.add(e.getKey(), "not_logged_on_purpose");
                else
                    objDetails.add(e.getKey(), e.getValue().toString());
            }
            objKeycloakEvent.add("details", objDetails.build());
        }
        
        AuthenticationSessionModel authSession = session.getContext().getAuthenticationSession(); 
        if(authSession!=null) {
            objKeycloakEvent.add("authentication_session_parent_id", authSession.getParentSession().getId());
            objKeycloakEvent.add("authentication_session_tab_id", authSession.getTabId());
        }
        
        //setKeycloakContext(objKeycloakEvent);
        
        objAuditEvent.add("keycloak", objKeycloakEvent.build());
        
        JsonObjectBuilder objRoot = Json.createObjectBuilder();
        
        return objRoot.add("audit_event", objAuditEvent.build()).build().toString();

    }


    private String toString(AdminEvent adminEvent) {

        JsonObjectBuilder objAuditEvent = Json.createObjectBuilder();
		
		objAuditEvent.add("origin", "KEYCLOAK");
        
        objAuditEvent.add("operation", adminEvent.getOperationType() != null ? adminEvent.getOperationType().toString() : "");
        
        objAuditEvent.add("date_time_epoch", adminEvent.getTime());
        objAuditEvent.add("date_time", dateTimeFormatter.format(new java.util.Date(adminEvent.getTime())));
        
        JsonObjectBuilder objActor = Json.createObjectBuilder();
        objActor.add("role", "ADMIN");
        String userId = "";
        String userName = "";
        if (adminEvent.getAuthDetails().getUserId() != null) {
            userId = adminEvent.getAuthDetails().getUserId().toString();
            userName = "not_logged_on_purpose";
        }
        objActor.add("user_id", userId);
        objActor.add("user_name", userName);
        String realm = "";
        if (adminEvent.getAuthDetails().getRealmId() != null)
            realm = adminEvent.getAuthDetails().getRealmId();
        objActor.add("realm", realm);
        objAuditEvent.add("actor", objActor.build());
        
        JsonObjectBuilder objTarget = Json.createObjectBuilder();
        userId = "";
        userName = "";
        if (adminEvent.getResourcePath() != null && adminEvent.getResourcePath().startsWith("users/")) { 
             try {
                 String[] resourcePathSplit = adminEvent.getResourcePath().split("/");
                 userId = resourcePathSplit[1];
                 userName = "not_logged_on_purpose";
             } catch (Exception e) {}
        }
        objTarget.add("user_id", userId);
        objTarget.add("user_name", userName);
        realm = "";
        if (adminEvent.getRealmId() != null)
            realm =  adminEvent.getRealmId();
        objTarget.add("realm", realm);
        objAuditEvent.add("target", objTarget.build());
		
		JsonObjectBuilder objKeycloakEvent = Json.createObjectBuilder();
		
        if (adminEvent.getResourcePath() != null && adminEvent.getResourcePath().startsWith("users/"))
            objKeycloakEvent.add("category", "ADMIN_EVENT_USER"); // Keycloak admin event whose target is a user
        else
            objKeycloakEvent.add("category", "ADMIN_EVENT_OTHER");  // Keycloak admin event whose target is not a user
        

        if (adminEvent.getOperationType() != null) {
            objKeycloakEvent.add("operation_type", adminEvent.getOperationType().toString());
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
            
            objKeycloakEvent.add("auth_details", objAuthDetails.build());

        }

        if (adminEvent.getResourceType() != null) {
            objKeycloakEvent.add("resource_type", adminEvent.getResourceType().toString());
        }

        if (adminEvent.getResourcePath() != null) {
            objKeycloakEvent.add("resource_path", adminEvent.getResourcePath().toString());
        }
        
        if (adminEvent.getRealmId() != null) {
            objKeycloakEvent.add("resource_realm", adminEvent.getRealmId().toString());
        }

        if (adminEvent.getError() != null) {
            objKeycloakEvent.add("error", adminEvent.getError().toString());
        }
        
        //setKeycloakContext(obj);
		
		objAuditEvent.add("keycloak", objKeycloakEvent.build());

        JsonObjectBuilder objRoot = Json.createObjectBuilder();
        
        return objRoot.add("audit_event", objAuditEvent.build()).build().toString();
    }
    
    private void setKeycloakContext(JsonObjectBuilder objAuditEvent) {
        KeycloakContext context = session.getContext();
        UriInfo uriInfo = context.getUri();
        HttpHeaders headers = context.getRequestHeaders();
        if (uriInfo != null) {
            objAuditEvent.add("request_uri", uriInfo.getRequestUri().toString());
        }

        if (headers != null) {        
            JsonArrayBuilder cookieArray = Json.createArrayBuilder();
            for (Map.Entry<String, Cookie> e : headers.getCookies().entrySet()) {
                cookieArray.add(e.getValue().toString());
            }    
            objAuditEvent.add("cookies", cookieArray.build());
        }  
    }

}
