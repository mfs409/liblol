package edu.lehigh.cse.ale;

// TODO: clean code; make help work

import com.badlogic.gdx.Gdx;
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
import edu.lehigh.cse.ale.Media;

public class Splash implements MyScreen {
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
         * A rectangle for tracking when the Play button is pressed
         */
        Rectangle _play;

        /**
         * A rectangle for tracking when the Help button is pressed
         */
        Rectangle _help;

        /**
         * A rectangle for tracking when the Quit button is pressed
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

        /**
         * The splash screen texture region
         * 
         * TODO: use Media so we don't need to track this... it's lazy, but for the simple splash screen it will do
         */
        TextureRegion _tr;

        /**
         * Set up the splash screen
         *
         * @param game
         *            The main game object
         */
        public Splash(ALE game) {
                // save a reference to the game
                _game = game;

                int CAMERA_WIDTH = _game._config.getScreenWidth();
                int CAMERA_HEIGHT = _game._config.getScreenHeight();

                // configure the camera, center it on the screen
                _camera = new OrthographicCamera(CAMERA_WIDTH, CAMERA_HEIGHT);
                _camera.position.set(CAMERA_WIDTH / 2, CAMERA_HEIGHT / 2, 0);

                // set up the play, help, and quit buttons
                // NB: voodoo on the Y due to peculiarity of font rendering
                _play = new Rectangle(_game._splashConfig.getPlayX(), _game._splashConfig.getPlayY() - _game._splashConfig.getPlayHeight(), _game._splashConfig.getPlayWidth(),
                                      _game._splashConfig.getPlayHeight());
                _help = new Rectangle(_game._splashConfig.getHelpX(), _game._splashConfig.getHelpY() - _game._splashConfig.getHelpHeight(), _game._splashConfig.getHelpWidth(),
                                      _game._splashConfig.getHelpHeight());
                _quit = new Rectangle(_game._splashConfig.getQuitX(), _game._splashConfig.getQuitY() - _game._splashConfig.getQuitHeight(), _game._splashConfig.getQuitWidth(),
                                      _game._splashConfig.getQuitHeight());

                // prepare for touches
                _touchVec = new Vector3();

                // set up our images
                _tr = new TextureRegion(new Texture(Gdx.files.internal("data/"
                                + _game._config.getSplashBackground())));

                // and our sprite batcher
                _batcher = new SpriteBatch();

                // config music?
                // TODO: put string into config
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

                int width = _game._config.getScreenWidth();
                int height = _game._config.getScreenHeight();

                _batcher.begin();
                _batcher.enableBlending();
                _batcher.draw(_tr, 0, 0, width, height);
                _batcher.end();

                // Render some text... for this we have to set the projection matrix
                // again, so we work in pixel coordinates
                _batcher.setProjectionMatrix(_camera.combined);
                _batcher.begin();

                // [TODO]: these need to be externalized... it also wouldn't hurt to
                // have objects to store these text entities, so that we can precompute
                // more and set up the rectangles correctly...
                BitmapFont f = Media.getFont("data/arial.ttf", 30);
                float w = f.getBounds("Demo Game").width;
                float h = f.getBounds("test").height;
                f.draw(_batcher, "Demo Game", width / 2 - w / 2,
                                height - 5 - h);
                w = f.getBounds("Play").width;
                f.draw(_batcher, "Play", _game._splashConfig.getPlayX(), _game._splashConfig.getPlayY());
                w = f.getBounds("Help").width;
                f.draw(_batcher, "Help", width / 2 - w / 2, height
                                - 5 - h - 30 - h - 30 - h);
                w = f.getBounds("Quit").width;
                f.draw(_batcher, "Quit", width / 2 - w / 2, height
                                - 5 - h - 30 - h - 30 - h - 30 - h);
                _batcher.end();
        }

        @Override
        public void dispose() {

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
