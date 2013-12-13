package com.me.mylolgame;

// TODO: reference file... delete this

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.utils.TimeUtils;

public class Level2 implements Screen {

	public void pause() {
	}

	public void resize(int width, int height) {
	}

	public void resume() {
	}

	// the camera
	private OrthographicCamera camera;

	// box2d debug renderer
	private Box2DDebugRenderer debugRenderer;

	// a spritebatch and a font for text rendering and a Texture to draw our
	// boxes
	private SpriteBatch batch;
	private BitmapFont font;
	private TextureRegion textureRegion;

	// box2d world
	private World world;

	// the boxes we draw
	private ArrayList<Body> boxes = new ArrayList<Body>();

	// ground
	Body groundBody;

	// mouse joint
	private MouseJoint mouseJoint = null;

	// the body that was touched
	Body hitBody = null;

	private void createPhysicsWorld() {
		// we instantiate a new World with a proper gravity vector
		// and tell it to sleep when possible.
		world = new World(new Vector2(0, -10), true);

		float[] vertices = { -0.07421887f, -0.16276085f, -0.12109375f,
				-0.22786504f, -0.157552f, -0.7122401f, 0.04296875f,
				-0.7122401f, 0.110677004f, -0.6419276f, 0.13151026f,
				-0.49869835f, 0.08984375f, -0.3190109f };

		PolygonShape shape = new PolygonShape();
		shape.set(vertices);

		// next we create a static ground platform. This platform
		// is not moveable and will not react to any influences from
		// outside. It will however influence other bodies. First we
		// create a PolygonShape that holds the form of the platform.
		// it will be 100 meters wide and 2 meters high, centered
		// around the origin
		PolygonShape groundPoly = new PolygonShape();
		groundPoly.setAsBox(50, 1);

		// next we create the body for the ground platform. It's
		// simply a static body.
		BodyDef groundBodyDef = new BodyDef();
		groundBodyDef.type = BodyType.StaticBody;
		groundBody = world.createBody(groundBodyDef);

		// finally we add a fixture to the body using the polygon
		// defined above. Note that we have to dispose PolygonShapes
		// and CircleShapes once they are no longer used. This is the
		// only time you have to care explicitely for memomry managment.
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = groundPoly;
		fixtureDef.filter.groupIndex = 0;
		groundBody.createFixture(fixtureDef);
		groundPoly.dispose();

		// We also create a simple ChainShape we put above our
		// ground polygon for extra funkyness.
		ChainShape chainShape = new ChainShape();
		chainShape
				.createLoop(new Vector2[] { new Vector2(-10, 10),
						new Vector2(-10, 5), new Vector2(10, 5),
						new Vector2(10, 11), });
		BodyDef chainBodyDef = new BodyDef();
		chainBodyDef.type = BodyType.StaticBody;
		Body chainBody = world.createBody(chainBodyDef);
		chainBody.createFixture(chainShape, 0);
		chainShape.dispose();

		createBoxes();

		// You can safely ignore the rest of this method :)
		world.setContactListener(new ContactListener() {

			// quick demo to show that we can manage collisions just like in ALE
			
			@Override
			public void beginContact(Contact contact) {
				Object a = contact.getFixtureA().getBody().getUserData();
				Object b = contact.getFixtureB().getBody().getUserData();
				if (a == null) return;
				if (b == null) return;
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

	private void createBoxes() {
		// next we create 50 boxes at random locations above the ground
		// body. First we create a nice polygon representing a box 2 meters
		// wide and high.
		PolygonShape boxPoly = new PolygonShape();
		boxPoly.setAsBox(1, 1);

		// next we create the 50 box bodies using the PolygonShape we just //
		// defined. This process is similar to the one we used for the ground //
		// body. Note that we reuse the polygon for each body fixture.
		for (int i = 0; i < 20; i++) {
			// Create the BodyDef, set a random position above the
			// ground and create a new body
			BodyDef boxBodyDef = new BodyDef();
			boxBodyDef.type = BodyType.DynamicBody;
			boxBodyDef.position.x = -24 + (float) (Math.random() * 48);
			boxBodyDef.position.y = 10 + (float) (Math.random() * 100);
			Body boxBody = world.createBody(boxBodyDef);

			boxBody.createFixture(boxPoly, 1);
			boxBody.setUserData(""+i);
			// add the box to our list of boxes
			boxes.add(boxBody);
		}

		// we are done, all that's left is disposing the boxPoly
		boxPoly.dispose();
	}

	// instantiate the vector and callback here, so we don't irritate the GC
	Vector3 testPoint = new Vector3();
	QueryCallback callback = new QueryCallback() {

		@Override
		public boolean reportFixture(Fixture fixture) { // if the hit fixture's
														// body is the ground
														// body // we ignore it
			if (fixture.getBody() == groundBody)
				return true;

			// if the hit point is inside the fixture of the body // we report
			// it
			if (fixture.testPoint(testPoint.x, testPoint.y)) {
				hitBody = fixture.getBody();
				return false;
			} else
				return true;
		}
	};

	public boolean touchDown(int x, int y, int pointer, int newParam) {
		// translate the mouse coordinates to world coordinates
		testPoint.set(x, y, 0);
		camera.unproject(testPoint);

		// ask the world which bodies are within the given // bounding box
		// around the mouse pointer
		hitBody = null;
		world.QueryAABB(callback, testPoint.x - 0.1f, testPoint.y - 0.1f,
				testPoint.x + 0.1f, testPoint.y + 0.1f);

		// if we hit something we create a new mouse joint
		// and attach it to the hit body.
		if (hitBody != null) {
			MouseJointDef def = new MouseJointDef();
			def.bodyA = groundBody;
			def.bodyB = hitBody;
			def.collideConnected = true;
			def.target.set(testPoint.x, testPoint.y);
			def.maxForce = 1000.0f * hitBody.getMass();
			
			mouseJoint = (MouseJoint) world.createJoint(def);
			hitBody.setAwake(true);
		} else {
			for (Body box : boxes)
				world.destroyBody(box);
			boxes.clear();
			createBoxes();
		}

		return false;
	}

	// another temporary vector
	Vector2 target = new Vector2();

	public boolean touchDragged(int x, int y, int pointer) {
		// if a mouse joint exists we simply update the target of the joint
		// based on the new mouse coordinates
		if (mouseJoint != null) {
			camera.unproject(testPoint.set(x, y, 0));
			mouseJoint.setTarget(target.set(testPoint.x, testPoint.y));
		}
		return false;
	}

	public boolean touchUp(int x, int y, int pointer, int button) {
		// if a mouse joint exists we simply destroy it
		if (mouseJoint != null) {
			world.destroyJoint(mouseJoint);
			mouseJoint = null;
		}
		return false;
	}

	@Override
	public void dispose() {
		world.dispose();
		debugRenderer.dispose();
		font.dispose();
		textureRegion.getTexture().dispose();
	}

	@Override
	public void render(float delta) {
		// first we update the world. For simplicity we use the delta time //
		// provided by the Graphics instance. Normally you'll want to fix the //
		// time step.
		long start = TimeUtils.nanoTime();
		world.step(Gdx.graphics.getDeltaTime(), 8, 3);
		float updateTime = (TimeUtils.nanoTime() - start) / 1000000000.0f;

		// next we clear the color buffer and set the camera matrices
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		camera.update();

		// next we render each box via the SpriteBatch. for this we have to set
		// the projection matrix of the spritebatch to the camera's combined
		// matrix. This will make the spritebatch work in world coordinates
		batch.getProjectionMatrix().set(camera.combined);
		batch.begin();
		for (int i = 0; i < boxes.size(); i++) {
			Body box = boxes.get(i);
			// get box center
			Vector2 position = box.getPosition();
			// get rotation angle around the center
			float angle = MathUtils.radiansToDegrees * box.getAngle();
			batch.draw(textureRegion, position.x - 1, position.y - 1, 1f, 1f,
					2, 2, 1, 1, angle);
		}
		batch.end();

		// next we use the debug renderer. Note that we simply apply the camera
		// again and then call the renderer. the camera.apply() call is actually
		// not needed as the opengl matrices are already set by the spritebatch
		// which in turn uses the camera matrices :)
		debugRenderer.render(world, camera.combined);

		// finally we render the time it took to update the world
		// for this we
		// have to set the projection matrix again, so
		// we work in pixel coordinates
		batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(),
				Gdx.graphics.getHeight());
		batch.begin();
		font.draw(batch, "fps: " + Gdx.graphics.getFramesPerSecond()
				+ " update time: " + updateTime, 0, 20);
		batch.end();
	}

	@Override
	public void show() {
	}

	@Override
	public void hide() {
	}
}
