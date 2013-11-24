package com.me.mygdxgame;

import edu.lehigh.cse.ale.ALEConfiguration;

// TODO: comment/explain

public class Config extends ALEConfiguration {
	@Override
	public int getNumLevels() {
		return 73;
	}

	@Override
	public int getScreenWidth() {
		return 480;
	}

	@Override
	public int getScreenHeight() {
		return 320;
	}

	@Override
	public int getNumHelpScenes() {
		return 0;
	}

	@Override
	public String getSplashBackground() {
		return "splash.png";
	}

	@Override
	public String getSplashMusic() {
		return "tune.ogg";
	}

	@Override
	public boolean getVibration() {
		return false;
	}

	@Override
	public boolean getDeveloperUnlock() {
		return true;
	}

	@Override
	public boolean showDebugBoxes() {
		return true;
	}

}
