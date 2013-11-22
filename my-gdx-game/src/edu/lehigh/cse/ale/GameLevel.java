package edu.lehigh.cse.ale;

// TODO: this is going to be the hardest part...

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;


// TODO: add support for multiple z indices: -2, -1, 0, 1, 2 (0 is hero)

public class GameLevel implements MyScreen {

	// for now, just one list of everything we need to render...
	public ArrayList<PhysicsSprite> _sprites;

	/*
	 * INTERNAL CLASSES
	 */

	/**
	 * these are the ways you can complete a level: you can reach the
	 * destination, you can collect enough stuff, or you can get the number of
	 * enemies down to 0
	 */
	enum VictoryType {
		DESTINATION, GOODIECOUNT, ENEMYCOUNT
	};

	/*
	 * BASIC LEVEL CONFIGURATION
	 */

	/**
	 * Width of this level
	 */
	static int _width;

	/**
	 * Height of this level
	 */
	static int _height;

	// the camera
	private OrthographicCamera _camera;

	// box2d debug renderer
	private Box2DDebugRenderer _debugRender;

	// a spritebatch and a font for text rendering and a Texture to draw our
	// boxes
	private SpriteBatch _spriteRender;

	// box2d world
	public World _world;

	public GameLevel(int width, int height) {
		// setup the camera. In Box2D we operate on a
		// meter scale, pixels won't do it. So we use
		// an orthographic camera with a viewport of
		// 48 meters in width and 32 meters in height.
		// We also position the camera so that it
		// looks at (0,16) (that's where the middle of the
		// screen will be located).
		_camera = new OrthographicCamera(width, height);
		_camera.position.set(0, 16, 0); // TODO: is this what we need?

		// next we create the box2d debug renderer
		_debugRender = new Box2DDebugRenderer();

		// next we create a SpriteBatch and a font
		_spriteRender = new SpriteBatch();
	
		_sprites  = new ArrayList<PhysicsSprite>();
	}

	@Override
	public void render(float delta) {

		// first we update the world. For simplicity we use the delta time //
		// provided by the Graphics instance. Normally you'll want to fix the //
		// time step.
		// long start = TimeUtils.nanoTime();
		_world.step(Gdx.graphics.getDeltaTime(), 8, 3);
		// float updateTime = (TimeUtils.nanoTime() - start) / 1000000000.0f;

		// next we clear the color buffer and set the camera matrices
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		_camera.update();

		// next we render each box via the SpriteBatch. for this we have to set
		// the projection matrix of the spritebatch to the camera's combined
		// matrix. This will make the spritebatch work in world coordinates
		
		_spriteRender.getProjectionMatrix().set(_camera.combined);
		_spriteRender.begin();
		for (PhysicsSprite ps : _sprites) {
			Vector2 pos = ps._physBody.getPosition();
			float angle = MathUtils.radiansToDegrees * ps._physBody.getAngle();
			_spriteRender.draw(ps._tr, pos.x - ps._width/2, pos.y - ps._height/2, 0, 0, ps._width, ps._height, 1, 1, angle);
		}
		_spriteRender.end();
		 
		// next we use the debug renderer. Note that we simply apply the camera
		// again and then call the renderer. the camera.apply() call is actually
		// not needed as the opengl matrices are already set by the spritebatch
		// which in turn uses the camera matrices :)
		_debugRender.render(_world, _camera.combined);

	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void show() {
		// TODO Auto-generated method stub

	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int newParam) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

}
