package org.theanarch.onionrouting.Network;

import javax.crypto.SecretKey;
import java.util.HashMap;

public class ClientSessionManager {

    private static int MAX_SESSION_COUNT = 15;
    private static long SESSION_TIMEOUT_NS = 60*1000000000L;
    private static HashMap<String, Session> sessions = new HashMap<>();

    public static Session getSession(String nodeId){
        return sessions.get(nodeId);
    }

    public static void createSession(String nodeId, String sessionId, SecretKey secret){
        if(sessions.size() > MAX_SESSION_COUNT){
            clearExpiredSessions();
        }

        if(!sessions.containsKey(nodeId)){
            Session session = new Session();
            session.nodeId = nodeId;
            session.sessionId = sessionId;
            session.secret = secret;
            session.startTime = System.nanoTime();

            sessions.put(nodeId, session);
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
        public String nodeId, sessionId;
        public SecretKey secret;
    }
}
