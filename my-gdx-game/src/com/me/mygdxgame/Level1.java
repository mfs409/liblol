package com.me.mygdxgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import edu.lehigh.cse.ale.MyScreen;

public class Level1 implements MyScreen {

	// set up the rendering...
	static final float BOX_STEP = 1 / 60f;
	static final int BOX_VELOCITY_ITERATIONS = 6;
	static final int BOX_POSITION_ITERATIONS = 2;
	Box2DDebugRenderer _debugRenderer;

	// we need a camera
	OrthographicCamera _camera;

	public Level1() {
		// make the camera, center it, then update it with latest changes
		_camera = new OrthographicCamera();
		_camera.viewportHeight = 320;
		_camera.viewportWidth = 480;
		_camera.position.set(_camera.viewportWidth * .5f,
				_camera.viewportHeight * .5f, 0f);
		_camera.update();

		// Tempting though it is, we can't just put the body in the middle of a
		// box or it will not collide with the borders. Nonetheless, this is a
		// good thing to see, for now, so it's in the comments
		BodyDef bdGround = new BodyDef();
		// bdGround.position.set(camera.viewportWidth / 2.0f,
		// camera.viewportHeight / 2.0f);
		bdGround.position.set(_camera.viewportWidth / 2.0f, 5);
		Body bGround = _world.createBody(bdGround);
		PolygonShape psGround = new PolygonShape();
		// setAsBox draws a box centered around the position
		// psGround.setAsBox(-10+camera.viewportWidth / 2.0f,
		// -10+camera.viewportHeight / 2.0f);
		psGround.setAsBox(-10 + _camera.viewportWidth / 2.0f, 1);
		bGround.createFixture(psGround, 0.0f);
		// since we don't need to do fancy stuff...
		psGround.dispose();

		// Dynamic Body... note that bottom left appears to be (0,0)
		BodyDef bdBall = new BodyDef();
		bdBall.type = BodyType.DynamicBody;
		bdBall.position.set(_camera.viewportWidth / 2, _camera.viewportHeight);
		Body bBall = _world.createBody(bdBall);
		CircleShape csBall = new CircleShape();
		csBall.setRadius(5f);
		FixtureDef fdBall = new FixtureDef();
		fdBall.shape = csBall;
		fdBall.density = 1.0f;
		fdBall.friction = 0.0f;
		fdBall.restitution = 1;
		bBall.createFixture(fdBall);
		csBall.dispose();

		_debugRenderer = new Box2DDebugRenderer();
	}

	// -100 seems high, but looks good as the downward gravity force...
	World _world = new World(new Vector2(0, -100), true);

	@Override
	public void render(float delta) {
		GLCommon gl = Gdx.gl;
		gl.glClearColor(0, 0, 0, 1);
		gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		_camera.update();
		_debugRenderer.render(_world, _camera.combined);
		_world.step(BOX_STEP, BOX_VELOCITY_ITERATIONS, BOX_POSITION_ITERATIONS);
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void show() {
	}

	@Override
	public void hide() {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int newParam) {
		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		return false;
	}
}
