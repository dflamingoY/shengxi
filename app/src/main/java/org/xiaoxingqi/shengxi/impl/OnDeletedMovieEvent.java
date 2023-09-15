package org.xiaoxingqi.shengxi.impl;

public class OnDeletedMovieEvent {
    private String deletedId;
    private int resourceType;

    public OnDeletedMovieEvent(String deletedId, int resourceType) {
        this.deletedId = deletedId;
        this.resourceType = resourceType;
    }

    public String getDeletedId() {
        return deletedId;
    }

    public int getResourceType() {
        return resourceType;
    }
}
