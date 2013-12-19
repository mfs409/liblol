package com.me.mylolgame;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class Main {
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		Config c = new Config();
		cfg.title = c.getGameTitle();
		cfg.width = c.getScreenWidth();
		cfg.height = c.getScreenHeight();
		cfg.useGL20 = true;
		new LwjglApplication(new MyLolGame(), cfg);
	}
}
