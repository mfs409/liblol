package edu.lehigh.cse.ale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;

public class Physics {

	public static void configure(float defaultXGravity, float defaultYGravity) {
		// we instantiate a new World with a proper gravity vector
		// and tell it to sleep when possible.
		Level._current._world = new World(new Vector2(defaultXGravity, defaultYGravity), true);

		// set up the collision handler
		Level._current._world.setContactListener(new ContactListener() {

			// quick demo to show that we can manage collisions just like in ALE

			@Override
			public void beginContact(Contact contact) {
				Object a = contact.getFixtureA().getBody().getUserData();
				Object b = contact.getFixtureB().getBody().getUserData();
				if (a == null)
					return;
				if (b == null)
					return;
				Gdx.app.log("collide", a.toString() + " hit " + b.toString());
			}

			@Override
			public void endContact(Contact contact) {
			}

			@Override
			public void preSolve(Contact contact, Manifold oldManifold) {
			}

			@Override
			public void postSolve(Contact contact, ContactImpulse impulse) {
			}
		});

	}
}