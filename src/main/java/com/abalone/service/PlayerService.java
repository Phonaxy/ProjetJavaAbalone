package com.abalone.service;

import com.abalone.dto.PlayerRequest;
import com.abalone.exception.PlayerNotFoundException;
import com.abalone.model.Player;
import com.abalone.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    public Player getPlayer(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new PlayerNotFoundException("Joueur non trouve avec l'id : " + id));
    }

    @Transactional
    public Player createPlayer(PlayerRequest request) {
        if (playerRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Le username '" + request.getUsername() + "' est deja pris.");
        }
        Player player = new Player(request.getUsername(), request.getDisplayName());
        return playerRepository.save(player);
    }

    @Transactional
    public Player updatePlayer(Long id, PlayerRequest request) {
        Player player = getPlayer(id);

        if (request.getUsername() != null && !request.getUsername().equals(player.getUsername())) {
            if (playerRepository.existsByUsername(request.getUsername())) {
                throw new IllegalArgumentException("Le username '" + request.getUsername() + "' est deja pris.");
            }
            player.setUsername(request.getUsername());
        }

        if (request.getDisplayName() != null) {
            player.setDisplayName(request.getDisplayName());
        }

        return playerRepository.save(player);
    }

    @Transactional
    public void deletePlayer(Long id) {
        Player player = getPlayer(id);
        playerRepository.delete(player);
    }
}
