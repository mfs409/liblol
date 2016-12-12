package com.me.mylolgame.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.me.mylolgame.MyConfig;

import edu.lehigh.cse.lol.Lol;

public class DesktopLauncher {
    public static void main (String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        MyConfig game = new MyConfig();
        config.title = game.mGameTitle;
        config.width = game.mWidth;
        config.height = game.mHeight;
        new LwjglApplication(new Lol(game), config);
    }
}
