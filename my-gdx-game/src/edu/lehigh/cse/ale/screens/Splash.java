package edu.lehigh.cse.ale.screens;

// TODO: clean code; fix button rectangles; make help work; test sound once we fix back buttons

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

import edu.lehigh.cse.ale.ALE;

public class Splash implements MyScreen
{
	/**
	 * Since we're going to create other screens via this screen, we need a
	 * reference to the game...
	 */
	ALE _game;

	/**
	 * The camera we will use
	 */
	OrthographicCamera _camera;

	/**
	 * Camera Dimensions
	 */
	int CAMERA_WIDTH;
	int CAMERA_HEIGHT;

	/**
	 * The Play Button
	 */
	Rectangle _play;

	/**
	 * The Quit Button
	 */
	Rectangle _quit;

	/**
	 * For handling touches
	 */
	Vector3 _touchVec;

	/**
	 * For rendering
	 */
	SpriteBatch _batcher;

	TextureRegion _tr;

	BitmapFont _font;
	
	/**
	 * Set up the splash screen
	 * 
	 * @param game
	 *            The main game object
	 */
	public Splash(ALE game) {
		// save a reference to the game
		_game = game;

		CAMERA_WIDTH = _game._config.getScreenWidth();
		CAMERA_HEIGHT = _game._config.getScreenHeight();

		// configure the camera
		_camera = new OrthographicCamera(CAMERA_WIDTH, CAMERA_HEIGHT);
		_camera.position.set(CAMERA_WIDTH / 2, CAMERA_HEIGHT / 2, 0);

		// set up the play button
		_play = new Rectangle(0, CAMERA_HEIGHT / 2, CAMERA_WIDTH,
				CAMERA_HEIGHT / 2);

		// set up the quit button
		_quit = new Rectangle(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT / 2);

		// prepare for touches
		_touchVec = new Vector3();

		// set up our images
		_tr = new TextureRegion(new Texture(
				Gdx.files.internal("data/splash.png")));

		_font = new BitmapFont(Gdx.files.internal("data/arial-15.fnt"), false);
		_font.setColor(Color.WHITE);
		_font.setScale(2);

		// and our sprite batcher
		_batcher = new SpriteBatch();
		
		// config music?
		if (_game._config.getSplashMusic() != null) {
			_game.setMusic("data/tune.ogg", true);
		}
	}

	@Override
	public void render(float delta) {
		// for now, stick everything in here...

		_game.playMusic();
		
		// was there a touch?
		if (Gdx.input.justTouched()) {
			// translate the touch into _touchVec
			_camera.unproject(_touchVec.set(Gdx.input.getX(), Gdx.input.getY(),
					0));
			if (_quit.contains(_touchVec.x, _touchVec.y)) {
				_game.doQuit();
			}
			if (_play.contains(_touchVec.x, _touchVec.y)) {
				_game.doChooser();
			}
		}

		// now draw the screen...
		GLCommon gl = Gdx.gl;
		gl.glClearColor(1, 0, 0, 1);
		gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		_camera.update();
		_batcher.setProjectionMatrix(_camera.combined);

		_batcher.enableBlending();
		_batcher.begin();
		_batcher.draw(_tr, 0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		_batcher.end();

		// Render some text... for this we have to set the projection matrix
		// again, so we work in pixel coordinates
		_batcher.getProjectionMatrix().setToOrtho2D(0, 0,
				Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		_batcher.begin();

		// [TODO]: these need to be externalized... it also wouldn't hurt to
		// have objects to store these text entities, so that we can precompute
		// more and set up the rectangles correctly...
		float w = _font.getBounds("Demo Game").width;
		float h = _font.getBounds("test").height;
		_font.draw(_batcher, "Demo Game", CAMERA_WIDTH / 2 - w / 2,
				CAMERA_HEIGHT - 5 - h);

		w = _font.getBounds("Play").width;
		_font.draw(_batcher, "Play", CAMERA_WIDTH / 2 - w / 2, CAMERA_HEIGHT
				- 5 - h - 30 - h);

		w = _font.getBounds("Help").width;
		_font.draw(_batcher, "Help", CAMERA_WIDTH / 2 - w / 2, CAMERA_HEIGHT
				- 5 - h - 30 - h - 30 - h);

		w = _font.getBounds("Quit").width;
		_font.draw(_batcher, "Quit", CAMERA_WIDTH / 2 - w / 2, CAMERA_HEIGHT
				- 5 - h - 30 - h - 30 - h - 30 - h);
		_batcher.end();
	}

	@Override
	public void dispose() {
		_font.dispose();
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
