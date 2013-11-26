package edu.lehigh.cse.ale;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class Util {

	/**
	 * Draw a box on the scene
	 * 
	 * Note: the box is actually four narrow rectangles
	 * 
	 * @param x0
	 *            X coordinate of top left corner
	 * @param y0
	 *            Y coordinate of top left corner
	 * @param x1
	 *            X coordinate of bottom right corner
	 * @param y1
	 *            Y coordinate of bottom right corner
	 * @param imgName
	 *            name of the image file to use when drawing the rectangles
	 * @param density
	 *            Density of the obstacle. When in doubt, use 1
	 * @param elasticity
	 *            Elasticity of the obstacle. When in doubt, use 0
	 * @param friction
	 *            Friction of the obstacle. When in doubt, use 1
	 */
	static public void drawBoundingBox(int x0, int y0, int x1, int y1,
			String imgName, float density, float elasticity, float friction) {

		// TODO: Why do we need the factor of 2 everywhere?
		
		// get the image by name. Note that we could animate it ;)
		TextureRegion ttr = Media.getImage(imgName);
		// draw four rectangles, give them physics and attach them to the scene
		Obstacle b = new Obstacle(2 * Math.abs(x0 - x1), 0.5f, ttr);
		b.setBoxPhysics(density, elasticity, friction, BodyType.StaticBody,
				false, x0, y0);
		//b.disableRotation();
		Level._current._sprites.add(b);
		Obstacle t = new Obstacle(2 * Math.abs(x0 - x1), 0.5f, ttr);
		t.setBoxPhysics(density, elasticity, friction, BodyType.StaticBody,
				false, x0, y1);
		//t.disableRotation();
		Level._current._sprites.add(t);
		Obstacle l = new Obstacle(0.5f, 2 * Math.abs(y0 - y1), ttr);
		l.setBoxPhysics(density, elasticity, friction, BodyType.StaticBody,
				false, x0, y0);
		//l.disableRotation();
		Level._current._sprites.add(l);
		Obstacle r = new Obstacle(0.5f, 2 * Math.abs(y0 - y1), ttr);
		r.setBoxPhysics(density, elasticity, friction, BodyType.StaticBody,
				false, x1, y0);
		//r.disableRotation();
		Level._current._sprites.add(r);
	}

}
