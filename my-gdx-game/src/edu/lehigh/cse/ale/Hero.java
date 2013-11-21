package edu.lehigh.cse.ale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.PolygonShape;

public class Hero extends PhysicsSprite{

	Hero(TextureRegion tr, float width, float height) {
		super(tr, SpriteId.HERO, width, height);
	}

	public static Hero makeAsCircle(float x, float y, float width, float height, String imgName)
	{
		TextureRegion tr = new TextureRegion(new Texture(Gdx.files.internal("data/badlogicsmall.jpg")));
		Hero h = new Hero(tr, width, height);
		
		// NB: this is not a circle... it's a box...
		PolygonShape boxPoly = new PolygonShape();
		boxPoly.setAsBox(width/2, height/2);
		BodyDef boxBodyDef = new BodyDef();
		boxBodyDef.type = BodyType.DynamicBody;
		boxBodyDef.position.x = x;
		boxBodyDef.position.y = y;
		h._physBody = Level._current._world.createBody(boxBodyDef);
		h._physBody.createFixture(boxPoly, 1);
		// link the body to the sprite
		h._physBody.setUserData(h);
		boxPoly.dispose();
		Level._current._sprites.add(h);
		return h;
	}

	@Override
	void onCollide(PhysicsSprite other) {
		// TODO Auto-generated method stub
		
	}
}
