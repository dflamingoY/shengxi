package org.xiaoxingqi.shengxi.impl;

/**
 * 更新灵魂画手的数据,  2个页面之间数据的互相更新   发布数据之后, 2个页面的刷新数据
 */
public class ImpUpdatePaint {
    private int type; //1删除,2 设为隐藏 3 设为显示 4 需要刷新数据  5 6 更改了资源是否可以涂鸦 5禁止 6允许  7增加涂鸦  8 删除涂鸦
    private int id;//资源id

    public ImpUpdatePaint(int type, int id) {
        this.type = type;
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
