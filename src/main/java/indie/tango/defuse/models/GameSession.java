package indie.tango.defuse.models;

import org.springframework.stereotype.Component;

import java.util.HashMap;
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

    private String generateRandomCode() {
        return "GAME-" + java.util.UUID.randomUUID().toString().substring(0, 8);
    }

    public boolean isGameExists(String gameCode) {
        return activeGames.containsKey(gameCode);
    }
}
