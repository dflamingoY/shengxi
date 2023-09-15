package org.xiaoxingqi.shengxi.impl;

public class UpdateMovieEvent {
    private int type;//3影评  4书评  5唱回忆

    public UpdateMovieEvent(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
