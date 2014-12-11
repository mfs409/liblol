package com.me.mylolgame;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class Main {
    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        MyGame game = new MyGame();
        cfg.title = game.mGameTitle;
        cfg.width = game.mWidth;
        cfg.height = game.mHeight;
        new LwjglApplication(game, cfg);
    }
}
