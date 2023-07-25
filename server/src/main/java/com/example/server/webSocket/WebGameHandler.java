package com.example.server.webSocket;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.*;

@Component
@EnableScheduling
public class WebGameHandler implements WebSocketHandler {
    private final Logger logger = LoggerFactory.getLogger(WebGameHandler.class);
    private final Map<String, Player > players = new HashMap<>();
    private final List<WebSocketSession> sessions = new ArrayList<>();
    private final JSONParser parser = new JSONParser();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        TextMessage exportId = new TextMessage(session.getId());
        session.sendMessage(exportId);
        logger.info("WebSocket 연결이 성립되었습니다. players={}",players);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String payload = message.getPayload().toString();
        logger.info("Received message: " + payload);

        JSONObject jsonObject = (JSONObject) parser.parse(payload);
        String messageType = (String) jsonObject.get("type");
        logger.info("Received message Type: " + messageType);

        switch (messageType) {
            case "ENROLL" :
                handleEnrollMessage(jsonObject,session);
                break;

            case "MOVE":
                handleMoveMessage(jsonObject, session);
                break;

            default:
                logger.warn("Unknown message type: " + messageType);
        }
    }

    private void handleEnrollMessage(JSONObject jsonObject, WebSocketSession session) throws IOException {
        String name = (String) jsonObject.get("name");
        sessions.add(session);
        players.put(session.getId(), new Player(name,0,0));
        WebSocketMessage<String> resp = new TextMessage("ENROLL SUCCESS");
        session.sendMessage(resp);
        logger.info("sessoins = {}", sessions);
        logger.info("players = {}", players);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("WebSocket 에러 발생: " + exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        logger.info("WebSocket 연결이 끊어졌습니다. {} 상태: "+ closeStatus,session.getId());
        players.remove(session.getId());
        sessions.remove(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private void handleMoveMessage(JSONObject jsonObject, WebSocketSession session) throws IOException {
        String id = session.getId();
        String direction = (String) jsonObject.get("direction");


        if (players.get(id) == null) {
            logger.warn("Player not found: " + session.getId());
            return;
        }
        int newX;
        int newY;
        switch (direction) {
            case "right":
                newX = players.get(id).getX() + 1;
                newY = players.get(id).getY();
                if (canMove(newX, newY)) {
                    setNewPosition(id, newX, newY);
                }
                break;
            case "left":
                newX = players.get(id).getX() - 1;
                newY = players.get(id).getY();
                if (canMove(newX, newY)) {
                    setNewPosition(id, newX, newY);
                }
                break;
            case "up":
                newX = players.get(id).getX();
                newY = players.get(id).getY() - 1;
                if (canMove(newX, newY)) {
                    setNewPosition(id, newX, newY);
                }
                break;
            case "down":
                newX = players.get(id).getX();
                newY = players.get(id).getY() + 1;
                if (canMove(newX, newY)) {
                    setNewPosition(id, newX, newY);
                }
                break;
            default:
                logger.warn("Unknown direction: " + direction);
        }
        logger.info(players.toString());
    }
    private boolean canMove(int newX, int newY) {
        return newX >= 0 && newY >= 0 && newX < 32 && newY < 18;
    }

    private void setNewPosition(String id, int x, int y) throws IOException {
        players.replace(id , new Player(players.get(id).getName() , x , y));
    }

    @Scheduled(fixedRate = 5) // 100ms 간격으로 실행
    public void sendPlayersInfoToAllSessions() {
        JSONObject playersInfo = createPlayersInfoJSON();
        broadcastMessage(playersInfo.toJSONString());
    }

    private JSONObject createPlayersInfoJSON() {
        JSONObject playersInfo = new JSONObject();
        playersInfo.put("broadcasting","position");
        for (Map.Entry<String, Player> entry : players.entrySet()) {
            Player player = entry.getValue();
            JSONObject playerInfo = new JSONObject();
            playerInfo.put("x", player.getX());
            playerInfo.put("y", player.getY());
            playersInfo.put(player.getName(), playerInfo);
        }
        return playersInfo;
    }

    public void broadcastMessage(String message) {
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    logger.error("Error broadcasting message: " + e.getMessage());
                }
            }
        }
    }
}