package com.me.mygdxgame;

import edu.lehigh.cse.ale.*;

public class MyGdxGame extends ALE {

	@Override
	public void nameResources() {
		Media.registerImage("greenball.png");
		Media.registerImage("mustardball.png");
	}

	@Override
	public void configureLevel(int whichLevel) {
		if (whichLevel == 1) {
			// set the screen to 48 meters by 32 meters
			Level.configure(48, 32);
			// there is no default force pushing everything
			Physics.configure(0, 0);

            // in this level, we'll use tilt to move some things around. The
            // maximum force that tilt can exert on anything is 10 in the X
            // dimension, and 10 in the Y dimension
            Tilt.enable(10, 10);

            // now let's create a hero, and indicate that the hero can move by
            // tilting the phone.
            Hero h = Hero.makeAsCircle(4, 17, 3, "greenball.png");
            h.setMoveByTilting();

            // finally, let's draw a circular destination
            Destination.makeAsCircle(29, 26, 1, "mustardball.png");
            Level.setVictoryDestination(1);
		}
	}

	@Override
	public void configureHelpScene(int whichScene) {
	}

	/**
	 * Mandatory method.  Don't change this.
	 */
	@Override
	public ALEConfiguration config() {
		return new Config();
	}

	/**
	 * Mandatory method.  Don't change this.
	 */
	@Override
	public SplashConfiguration splashConfig() {
		return new SplashConfig();
	}

}