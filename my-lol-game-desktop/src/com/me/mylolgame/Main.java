package com.me.mylolgame;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class Main {
    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        LolConfig c = new LolConfig();
        cfg.title = c.getGameTitle();
        cfg.width = c.getScreenWidth();
        cfg.height = c.getScreenHeight();
        new LwjglApplication(new MyLolGame(), cfg);
    }
}
