package com.cy.rvplaylist;

/**
 * Created by cy on 2018/12/9.
 */

public class PlayBean {
    private int state ;//播放状态   0播放，1停止

    public PlayBean(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
