using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Graphics;
using Microsoft.Xna.Framework.Input.Touch;

namespace LOL
{
    public class Splash: GameScreen
    {
        /**
         * A static reference to the current splash screen, so that we can use a
         * static context to configure a Splash screen from the game developer's
         * main file.
         */
        private static Splash sCurrent;

        /**
         * The camera for displaying the scene
         */
        private OrthographicCamera mCamera;

        /**
         * A rectangle for tracking the location of the play button
         */
        private Rectangle mPlay;

        /**
         * A rectangle for tracking the location of the help button
         */
        private Rectangle mHelp;

        /**
         * A rectangle for tracking the location of the quit button
         */
        private Rectangle mQuit;

        /**
         * For handling touches
         */
        private Vector3 mV = new Vector3();

        /**
         * For rendering
         */
        private SpriteBatch mSpriteBatch = new SpriteBatch(Lol.sGame.GraphicsDevice);

        /**
         * For debug rendering
         */
        private ShapeRenderer mShapeRender = new ShapeRenderer();

        /**
         * The image to display
         */
        private Texture2D[] mImage;

        /**
         * The music to play
         */
        public Music mMusic;

        /**
         * Track if the music is actually playing
         */
        public bool mMusicPlaying;

        /**
         * Basic configuration: get the image and music, configure the locations of
         * the play/help/quit buttons
         */
        public Splash() {
            // configure the camera, center it on the screen
            int w = Lol.sGame.mConfig.getScreenWidth();
            int h = Lol.sGame.mConfig.getScreenHeight();
            mCamera = new OrthographicCamera(w, h);
            // save a reference
            sCurrent = this;
            // call user code to configure the objects
            Lol.sGame.configureSplash();
        }

        /**
         * Start the music if it's not already playing
         */
        public void playMusic() {
            if (!mMusicPlaying && mMusic != null) {
                mMusicPlaying = true;
                mMusic.play();
            }
        }

        /**
         * Pause the music if it's playing
         */
        public void pauseMusic() {
            if (mMusicPlaying) {
                mMusicPlaying = false;
                mMusic.pause();
            }
        }

        /**
         * Stop the music if it's playing
         */
        public void stopMusic() {
            if (mMusicPlaying) {
                mMusicPlaying = false;
                mMusic.stop();
            }
        }

        /**
         * Draw the splash screen
         * 
         * @param delta time since the screen was last displayed
         */
        public override void Draw(GameTime gameTime) {
            // make sure the music is playing
            playMusic();

            // If there is a new down-touch, figure out if it was to a button
            //TouchCollection tc = TouchPanel.GetState();
            //if (tc.Count > 0) {
            if (Util.justTouched())
            {
                // translate the touch into camera coordinates
                mV = new Vector3(Util.touch.X, Util.touch.Y, 0);
                
                // DEBUG: print the location of the touch... this is really useful
                // when trying to figure out the coordinates of the rectangles
                if (Lol.sGame.mConfig.showDebugBoxes()) {
                    Util.log("touch", "(" + mV.X + ", " + mV.Y + ")");
                }
                // check if the touch was inside one of our buttons, and act
                // accordingly
                if (mQuit != null && mQuit.Contains((int) mV.X, (int) mV.Y)) {
                    stopMusic();
                    Lol.sGame.doQuit();
                }
                if (mPlay != null && mPlay.Contains((int) mV.X, (int) mV.Y)) {
                    stopMusic();
                    Lol.sGame.doChooser();
                }
                if (mHelp != null && mHelp.Contains((int) mV.X, (int) mV.Y)) {
                    stopMusic();
                    Lol.sGame.doHelpLevel(1);
                }
            }

            // now draw the screen...
            Lol.sGame.GraphicsDevice.Clear(Color.Black);
            mCamera.update();
            // NOTE: UNCOMMENT
            //mSpriteBatch.setProjectionMatrix(mCamera.combined);
            // NOTE: BlendState may need to be changed (replaces enableBlending() in LibGDX)
            mSpriteBatch.Begin(SpriteSortMode.Immediate, BlendState.Additive);
            if (mImage != null)
                mSpriteBatch.Draw(mImage[0], new Rectangle(0, 0, Lol.sGame.mConfig.getScreenWidth(),
                        Lol.sGame.mConfig.getScreenHeight()), Color.White);
            mSpriteBatch.End();

            // DEBUG: show where the buttons' boxes are
            if (Lol.sGame.mConfig.showDebugBoxes()) {
                //mShapeRender.setProjectionMatrix(mCamera.combined);
                //mShapeRender.begin(ShapeType.Line);
                //mShapeRender.setColor(Color.Red);
                if (mPlay != null)
                    mShapeRender.rect(mPlay.X, mPlay.Y, mPlay.Width, mPlay.Height);
                if (mHelp != null)
                    mShapeRender.rect(mHelp.X, mHelp.Y, mHelp.Width, mHelp.Height);
                if (mQuit != null)
                    mShapeRender.rect(mQuit.X, mQuit.Y, mQuit.Width, mQuit.Height);
               // mShapeRender.end();
            }
        }

        /**
         * When this scene goes away, make sure the music gets turned off
         */
        public void dispose() {
            stopMusic();
        }

        /**
         * When this scene goes away, make sure the music gets turned off
         */
        public void hide() {
            pauseMusic();
        }

        /*
         * PUBLIC INTERFACE
         */

        /**
         * Describe the coordinates of the Play button, so that clicks to the
         * correct region of the splash screen will cause the chooser to be drawn or
         * the only level of the game to start playing
         * 
         * @param x The X coordinate of the bottom left corner of the button, in
         *            pixels
         * @param y The Y coordinate of the bottom left corner of the button, in
         *            pixels
         * @param width The width of the button, in pixels
         * @param height The height of the button, in pixels
         */
        public static void drawPlayButton(int x, int y, int width, int height) {
            sCurrent.mPlay = new Rectangle(x, y, width, height);
        }

        /**
         * Describe the coordinates of the Help button, so that clicks to the
         * correct region of the splash screen will cause the first help scene to be
         * drawn
         * 
         * @param x The X coordinate of the bottom left corner of the button, in
         *            pixels
         * @param y The Y coordinate of the bottom left corner of the button, in
         *            pixels
         * @param width The width of the button, in pixels
         * @param height The height of the button, in pixels
         */
        public static void drawHelpButton(int x, int y, int width, int height) {
            sCurrent.mHelp = new Rectangle(x, y, width, height);
        }

        /**
         * Describe the coordinates of the Quit button, so that clicks to the
         * correct region of the splash screen will cause the app to terminate
         * 
         * @param x The X coordinate of the bottom left corner of the button, in
         *            pixels
         * @param y The Y coordinate of the bottom left corner of the button, in
         *            pixels
         * @param width The width of the button, in pixels
         * @param height The height of the button, in pixels
         */
        public static void drawQuitButton(int x, int y, int width, int height) {
            sCurrent.mQuit = new Rectangle(x, y, width, height);
        }

        /**
         * Configure the music to play when the splash screen is showing
         * 
         * @param soundName The music file name. Be sure that it is registered!
         */
        public static void setMusic(String soundName) {
            sCurrent.mMusic = Media.getMusic(soundName);
        }

        /**
         * Configure the image to display as the background of the splash screen. It
         * should include your game name and text regions for Play and Quit, as well
         * as optional Help.
         * 
         * @param imgName The image file name. Be sure that it is registered!
         */
        public static void setBackground(String imgName) {
            sCurrent.mImage = Media.getImage(imgName);
        }

    }
}
