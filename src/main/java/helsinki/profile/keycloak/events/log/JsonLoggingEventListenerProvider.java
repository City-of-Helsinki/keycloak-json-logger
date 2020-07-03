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
        
        JsonObjectBuilder obj = Json.createObjectBuilder();
        
        obj.add("category", "LOGIN_EVENT");
        
        obj.add("date_time_epoch", event.getTime());
        
        obj.add("date_time", dateTimeFormatter.format(new java.util.Date(event.getTime())));
        
        JsonObjectBuilder objActorUser = Json.createObjectBuilder();
        objActorUser.add("role", "owner");
        String userId = "";
        String userName = "";
        if (event.getUserId() != null) {
            userId = event.getUserId().toString();
            userName = "not_logged_on_purpose";
        }
        objActorUser.add("user_id", userId);
        objActorUser.add("user_name", userName);
        String realm = "";
        if (event.getRealmId() != null)
            realm = event.getRealmId();
        objActorUser.add("realm", realm);
        obj.add("actor_user", objActorUser.build());
        
        JsonObjectBuilder objTargetUser = Json.createObjectBuilder();
        userId = "";
        if (event.getUserId() != null) {
            userId = event.getUserId().toString();
            userName = "not_logged_on_purpose";
        }
        objTargetUser.add("user_id", userId);
        objTargetUser.add("user_name", userName);
        realm = "";
        if (event.getRealmId() != null)
            realm = event.getRealmId();
        objTargetUser.add("realm", realm);
        obj.add("target_user", objTargetUser.build());
        
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
                
                // Because of GDPR, we may not want to log username (=email) to Centralized audit log (Elastic cloud)
                if (e.getKey() == "username" || e.getKey() == "identity_provider_identity")
                    objDetails.add(e.getKey(), "not_logged_on_purpose");
                else
                    objDetails.add(e.getKey(), e.getValue().toString());
            }
            obj.add("details", objDetails.build());
        }
        
        AuthenticationSessionModel authSession = session.getContext().getAuthenticationSession(); 
        if(authSession!=null) {
            obj.add("authentication_session_parent_id", authSession.getParentSession().getId());
            obj.add("authentication_session_tab_id", authSession.getTabId());
        }
        
        //setKeycloakContext(obj);
        
        JsonObjectBuilder objRoot = Json.createObjectBuilder();
        
        return objRoot.add("keycloak_event", obj.build()).build().toString();

    }


    private String toString(AdminEvent adminEvent) {

        JsonObjectBuilder obj = Json.createObjectBuilder();
        
        if (adminEvent.getResourcePath() != null && adminEvent.getResourcePath().startsWith("users/"))
            obj.add("category", "ADMIN_EVENT_USER"); // Keycloak admin event whose target is a user
        else
            obj.add("category", "ADMIN_EVENT_OTHER");  // Keycloak admin event whose target is not a user
        
        obj.add("date_time_epoch", adminEvent.getTime());
        obj.add("date_time", dateTimeFormatter.format(new java.util.Date(adminEvent.getTime())));
        
        JsonObjectBuilder objActorUser = Json.createObjectBuilder();
        objActorUser.add("role", "admin");
        String userId = "";
        String userName = "";
        if (adminEvent.getAuthDetails().getUserId() != null) {
            userId = adminEvent.getAuthDetails().getUserId().toString();
            userName = "not_logged_on_purpose";
        }
        objActorUser.add("user_id", userId);
        objActorUser.add("user_name", userName);
        String realm = "";
        if (adminEvent.getAuthDetails().getRealmId() != null)
            realm = adminEvent.getAuthDetails().getRealmId();
        objActorUser.add("realm", realm);
        obj.add("actor_user", objActorUser.build());
        
        JsonObjectBuilder objTargetUser = Json.createObjectBuilder();
        userId = "";
        userName = "";
        if (adminEvent.getResourcePath() != null && adminEvent.getResourcePath().startsWith("users/")) { 
             try {
                 String[] resourcePathSplit = adminEvent.getResourcePath().split("/");
                 userId = resourcePathSplit[1];
                 userName = "not_logged_on_purpose";
             } catch (Exception e) {}
        }
        objTargetUser.add("user_id", userId);
        objTargetUser.add("user_name", userName);
        realm = "";
        if (adminEvent.getRealmId() != null)
            realm =  adminEvent.getRealmId();
        objTargetUser.add("realm", realm);
        obj.add("target_user", objTargetUser.build());

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
        
        if (adminEvent.getRealmId() != null) {
            obj.add("resource_realm", adminEvent.getRealmId().toString());
        }

        if (adminEvent.getError() != null) {
            obj.add("error", adminEvent.getError().toString());
        }
        
        //setKeycloakContext(obj);

        JsonObjectBuilder objRoot = Json.createObjectBuilder();
        
        return objRoot.add("keycloak_event", obj.build()).build().toString();
    }
    
    private void setKeycloakContext(JsonObjectBuilder obj) {
        KeycloakContext context = session.getContext();
        UriInfo uriInfo = context.getUri();
        HttpHeaders headers = context.getRequestHeaders();
        if (uriInfo != null) {
            obj.add("request_uri", uriInfo.getRequestUri().toString());
        }

        if (headers != null) {        
            JsonArrayBuilder cookieArray = Json.createArrayBuilder();
            for (Map.Entry<String, Cookie> e : headers.getCookies().entrySet()) {
                cookieArray.add(e.getValue().toString());
            }    
            obj.add("cookies", cookieArray.build());
        }  
    }

}
