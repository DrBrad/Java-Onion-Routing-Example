package org.theanarch.onionrouting.Network;

import javax.crypto.SecretKey;
import java.util.HashMap;

public class ServerSessionManager {

    private static int MAX_SESSION_COUNT = 60;
    private static long SESSION_TIMEOUT_NS = 60*1000000000L;
    private static HashMap<String, Session> sessions = new HashMap<>();

    public static Session getSession(String sessionId){
        return sessions.get(sessionId);
    }

    public static void createSession(String sessionId, SecretKey secret){ //COULD BE VOID...
        if(sessions.size() > MAX_SESSION_COUNT){
            clearExpiredSessions();
        }

        if(!sessions.containsKey(sessionId)){
            Session session = new Session();
            session.sessionId = sessionId;
            session.secret = secret;
            session.startTime = System.nanoTime();

            sessions.put(sessionId, session);
        }
    }

    public static void clearExpiredSessions(){
        long now = System.nanoTime();
        for(String nodeId : sessions.keySet()){
            Session session = sessions.get(nodeId);
            if(now-session.startTime > SESSION_TIMEOUT_NS){
                sessions.remove(session);
            }
        }
    }

    public static class Session {

        public long startTime;
        public String sessionId;
        public SecretKey secret;
    }
}
