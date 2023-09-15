package org.xiaoxingqi.shengxi.impl;

import org.xiaoxingqi.shengxi.model.SocketData;

public class UpdateInteraction {
    private String interactionsType = null;
    private SocketData.SocketBean bean = null;

    public UpdateInteraction(String type) {
        this.interactionsType = type;
    }

    public UpdateInteraction(SocketData.SocketBean bean) {
        this.bean = bean;
    }

    public SocketData.SocketBean getBean() {
        return bean;
    }

    public String getInteractionsType() {
        return interactionsType;
    }
}
