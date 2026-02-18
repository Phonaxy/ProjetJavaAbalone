package com.abalone.dto;

public class CreateGameRequest {

    private Long playerBlackId;
    private Long playerWhiteId;

    public CreateGameRequest() {}

    public Long getPlayerBlackId() { return playerBlackId; }
    public void setPlayerBlackId(Long playerBlackId) { this.playerBlackId = playerBlackId; }

    public Long getPlayerWhiteId() { return playerWhiteId; }
    public void setPlayerWhiteId(Long playerWhiteId) { this.playerWhiteId = playerWhiteId; }
}
