package com.abalone.dto;

import com.abalone.model.Player;
import java.time.LocalDateTime;

public class PlayerResponse {

    private Long id;
    private String username;
    private String displayName;
    private LocalDateTime createdAt;

    public static PlayerResponse fromEntity(Player player) {
        PlayerResponse r = new PlayerResponse();
        r.setId(player.getId());
        r.setUsername(player.getUsername());
        r.setDisplayName(player.getDisplayName());
        r.setCreatedAt(player.getCreatedAt());
        return r;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
