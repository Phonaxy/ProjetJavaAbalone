package com.abalone.dto;

import com.abalone.model.enums.Direction;

import java.util.List;

public class MoveRequest {

    private List<int[]> marbles;
    private Direction direction;

    public MoveRequest() {
    }

    public MoveRequest(List<int[]> marbles, Direction direction) {
        this.marbles = marbles;
        this.direction = direction;
    }

    public List<int[]> getMarbles() {
        return marbles;
    }

    public void setMarbles(List<int[]> marbles) {
        this.marbles = marbles;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }
}
