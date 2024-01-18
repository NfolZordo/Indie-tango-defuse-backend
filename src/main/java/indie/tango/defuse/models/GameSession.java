package indie.tango.defuse.models;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GameSession {
    private Map<String, String> playerSessions = new HashMap<>();
    private Map<String, Boolean> activeGames = new HashMap<>();

    public String generateGameCode() {
        String gameCode = generateRandomCode();
        activeGames.put(gameCode, true); // Mark the game as active
        return gameCode;
    }

    public void addPlayerSession(String gameCode, String playerSessionId) {
        playerSessions.put(playerSessionId, gameCode);
    }

    public String getGameCodeForPlayer(String playerSessionId) {
        return playerSessions.get(playerSessionId);
    }

    public List<String> getPlayerSessions(String gameCode) {
        List<String> sessions = new ArrayList<>();
        for (Map.Entry<String, String> entry : playerSessions.entrySet()) {
            if (entry.getValue().equals(gameCode)) {
                sessions.add(entry.getKey());
            }
        }
        return sessions;
    }

    private String generateRandomCode() {
        return "GAME-" + java.util.UUID.randomUUID().toString().substring(0, 8);
    }

    public boolean isGameExists(String gameCode) {
        return activeGames.containsKey(gameCode);
    }
}