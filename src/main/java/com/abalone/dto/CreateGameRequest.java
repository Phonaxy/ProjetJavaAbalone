package com.abalone.dto;

public class CreateGameRequest {

    private Long playerBlackId;
    private Long playerWhiteId;
    private int turnTimeLimitSeconds;

    public CreateGameRequest() {}

    public Long getPlayerBlackId() { return playerBlackId; }
    public void setPlayerBlackId(Long playerBlackId) { this.playerBlackId = playerBlackId; }

    public Long getPlayerWhiteId() { return playerWhiteId; }
    public void setPlayerWhiteId(Long playerWhiteId) { this.playerWhiteId = playerWhiteId; }

    public int getTurnTimeLimitSeconds() { return turnTimeLimitSeconds; }
    public void setTurnTimeLimitSeconds(int turnTimeLimitSeconds) { this.turnTimeLimitSeconds = turnTimeLimitSeconds; }
}
