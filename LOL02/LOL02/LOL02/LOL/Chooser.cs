using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Graphics;
using Microsoft.Xna.Framework.Input.Touch;

namespace LOL
{
    public class Chooser: GameScreen
    {
        /**
         * The "Previous Chooser Screen" button
         */
        private Button mPrev;

        /**
         * The "Next Chooser Screen" button
         */
        private Button mNext;

        /**
         * The "Back To Splash" button
         */
        private Button mBack;

        /**
         * The image to display as the background
         */
        private Texture2D[] mImage;

        /**
         * The music to play
         */
        private Music mMusic;

        /**
         * Track if the music is actually playing
         */
        private bool mMusicPlaying;

        /**
         * For tracking touches
         */
        private Vector3 mV = new Vector3();

        /**
         * All the level boxes we drew
         */
        private Button[] levels;

        /**
         * The camera we will use
         */
        private OrthographicCamera mCamera;

        /**
         * For rendering
         */
        private SpriteBatch mSpriteBatch;

        /**
         * For making text
         */
        private Font mFont;

        /**
         * For debug rendering
         */
        private ShapeRenderer mShapeRender;

        /**
         * A helper class for tracking where the buttons are
         */
        private class Button {
            /**
             * The rectangle that describes the coordinates of this button
             */
            public Rectangle mRect;

            /**
             * The level to run when this is pressed (use 0 for prev/next/back)
             */
            public int mLevel;

            /**
             * The image to display for this button
             */
            public Texture2D[] mTr;

            /**
             * Construct by defining the rectangle, level, and image
             * 
             * @param x The X coordinate of the bottom left corner
             * @param y The Y coordinate of the bottom left corner
             * @param w The width of the button
             * @param h The height of the button
             * @param level The level to play when this is pressed
             * @param imgName The image to display behind the text for this button
             */
            public Button(int x, int y, int w, int h, int level, String imgName) {
                mRect = new Rectangle(x, y, w, h);
                mLevel = level;
                mTr = Media.getImage(imgName);
            }
        }

        /**
         * Construct a chooser for the currently selected set of levels, and prepare
         * all the buttons
         */
        public Chooser() {
            // start by getting the pieces of configuration that we use over and
            // over again
            ChooserConfiguration cc = Lol.sGame.mChooserConfig;
            int levelsPerChooser = cc.getColumns() * cc.getRows();
            int totalLevels = Lol.sGame.mConfig.getNumLevels();

            // set up the background image and music
            mImage = Media.getImage(cc.getBackgroundName());
            if (cc.getMusicName() != null)
                mMusic = Media.getMusic(cc.getMusicName());

            // always make the back button
            mBack = new Button(cc.getBackButtonX(), cc.getBackButtonY(), cc.getBackButtonWidth(),
                    cc.getBackButtonHeight(), 0, cc.getBackButtonName());

            // make the previous button if we aren't drawing the first set of
            // choices
            if (Lol.sGame.mCurrLevelNum > levelsPerChooser) {
                mPrev = new Button(cc.getPrevButtonX(), cc.getPrevButtonY(), cc.getPrevButtonWidth(),
                        cc.getPrevButtonHeight(), 0, cc.getPrevButtonName());
            }

            // make the next button if we aren't drawing the last set of choices
            if (Lol.sGame.mCurrLevelNum + levelsPerChooser - 1 < totalLevels) {
                mNext = new Button(cc.getNextButtonX(), cc.getNextButtonY(), cc.getNextButtonWidth(),
                        cc.getNextButtonHeight(), 0, cc.getNextButtonName());
            }

            // figure out the first level to draw on this chooser. Note that '0' is
            // a possible value of mCurrLevelNum when we come straight from Splash,
            // so we must handle it
            int first = Lol.sGame.mCurrLevelNum;
            if (first > 0)
                first--;
            first = first - (first % levelsPerChooser) + 1;
            // figure out the last level to draw on this chooser
            int last = Math.Min(totalLevels, first + levelsPerChooser - 1);

            // get screen dimensions, and figure out the *top* left corner of the
            // first level button
            int camWidth = Lol.sGame.mConfig.getScreenWidth();
            int camHeight = Lol.sGame.mConfig.getScreenHeight();
            int top = cc.getTopMargin();//camHeight - cc.getTopMargin();
            int left = cc.getLeftMargin();

            // button dimensions
            int bWidth = cc.getLevelButtonWidth();
            int bHeight = cc.getLevelButtonHeight();
            // padding
            int hGutter = cc.getHPadding();
            int vGutter = cc.getBPadding();
            // position of next button
            int mytop = top;
            int myleft = left;
            // now let's make buttons for the levels
            levels = new Button[levelsPerChooser];
            int index = 0;
            for (int i = first; i <= last; ++i) {
                // move down a row?
                if (i % cc.getColumns() == 1 && i != first) {
                    mytop = mytop + bHeight + vGutter;//- bHeight - vGutter;
                    myleft = left;
                }
                levels[index] = new Button(myleft, mytop, bWidth, bHeight, i, cc.getLevelButtonName());
                myleft = myleft + bWidth + hGutter;
                index++;
            }

            // configure the camera
            mCamera = new OrthographicCamera(camWidth, camHeight);
            
            // create a font
            mFont = Media.getFont(cc.getLevelFont(), cc.getLevelFontSize());
            mFont.Color = new Color(((float)cc.getLevelFontRed()) / 255, ((float)cc.getLevelFontGreen()) / 255,
                    ((float)cc.getLevelFontBlue()) / 255, 1);
            // and create our renderers
            mSpriteBatch = new SpriteBatch(Lol.sGame.GraphicsDevice);
            mShapeRender = new ShapeRenderer();
        }

        /**
         * Start the music if it's not already playing
         */
        private void playMusic() {
            if (!mMusicPlaying && mMusic != null) {
                mMusicPlaying = true;
                mMusic.play();
            }
        }

        /**
         * Pause the music if it's playing
         */
        private void pauseMusic() {
            if (mMusicPlaying) {
                mMusicPlaying = false;
                mMusic.pause();
            }
        }

        /**
         * Stop the music if it's playing
         */
        private void stopMusic() {
            if (mMusicPlaying) {
                mMusicPlaying = false;
                mMusic.stop();
            }
        }

        /**
         * Render the chooser
         * 
         * @param delta The time since the last call to render
         */
        public override void Draw(GameTime gameTime) {
            // make sure music is playing, and check for touches
            playMusic();
            if (Util.justTouched())
            {
                touchDown((int)Util.touch.X, (int)Util.touch.Y);
            }

            // update the camera
            mCamera.update();

            // clear the screen
            Lol.sGame.GraphicsDevice.Clear(Color.Black);

            // NOTE: UNCOMMENT
            //mSpriteBatch.setProjectionMatrix(mCamera.combined);
            mSpriteBatch.Begin();
            // draw the background image
            // NOTE: Drawing with Color.White I assume for transparency
            if (mImage != null)
                mSpriteBatch.Draw(mImage[0], new Rectangle(0, 0, Lol.sGame.mConfig.getScreenWidth(),
                        Lol.sGame.mConfig.getScreenHeight()), Color.White);
            // draw back/prev/next
            mSpriteBatch.Draw(mBack.mTr[0], mBack.mRect, Color.Green);
            if (mPrev != null && mPrev.mTr != null)
                mSpriteBatch.Draw(mPrev.mTr[0], mPrev.mRect, Color.White);
            if (mNext != null && mNext.mTr != null)
                mSpriteBatch.Draw(mNext.mTr[0], mNext.mRect, Color.White);

            // draw the level buttons
            int unlocked = Score.readUnlocked();
            foreach (Button ls in levels) {
                if (ls != null) {
                    // draw picture
                    mSpriteBatch.Draw(ls.mTr[0], ls.mRect, Color.White);
                    // draw overlay text
                    String txt = ls.mLevel + "";
                    if (ls.mLevel > unlocked && !Lol.sGame.mConfig.getUnlockMode())
                        txt = Lol.sGame.mChooserConfig.getLevelLockText();
                    float x = mFont.Face.MeasureString(txt).X;
                    float y = mFont.Face.MeasureString(txt).Y;
                    mSpriteBatch.DrawString(mFont.Face, txt, new Vector2(ls.mRect.X + ls.mRect.Width / 2 - x / 2, ls.mRect.Y
                            + ls.mRect.Height / 2 - y / 2), mFont.Color);
                }
            }
            mSpriteBatch.End();

            // DEBUG: show the buttons' boxes
            if (Lol.sGame.mConfig.showDebugBoxes()) {
                // draw squares...
                mShapeRender.Color = Color.Gray;

                mShapeRender.rect(mBack.mRect.X, mBack.mRect.Y, mBack.mRect.Width, mBack.mRect.Height);
                if (mPrev != null)
                    mShapeRender.rect(mPrev.mRect.X, mPrev.mRect.Y, mPrev.mRect.Width,
                            mPrev.mRect.Height);
                if (mNext != null)
                    mShapeRender.rect(mNext.mRect.X, mNext.mRect.Y, mNext.mRect.Width,
                            mNext.mRect.Height);

                foreach (Button ls in levels) {
                    if (ls != null) {
                        mShapeRender.rect(ls.mRect.X, ls.mRect.Y, ls.mRect.Width, ls.mRect.Height);
                    }
                }
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

        /**
         * Handle a screen touch by figuring out what button was pressed, and then
         * taking action
         * 
         * @param x The X coordinate of the touch
         * @param y The Y coordinate of the touch
         */
        private void touchDown(int x, int y) {
            ChooserConfiguration cc = Lol.sGame.mChooserConfig;
            // get the coordinates of the touch
            mV = new Vector3(x, y, 0);
            
            // DEBUG: display touch coordinates
            if (Lol.sGame.mConfig.showDebugBoxes()) {
                Util.log("touch", "(" + mV.X + ", " + mV.Y + ")");
            }
            // handle 'back' presses
            if (mBack.mRect.Contains((int) mV.X, (int) mV.Y)) {
                Lol.sGame.handleBack();
                return;
            }
            // handle 'previous screen' requests
            if (mPrev != null && mPrev.mRect.Contains((int) mV.X, (int) mV.Y)) {
                Lol.sGame.mCurrLevelNum -= (cc.getColumns() * cc.getRows());
                Lol.sGame.doChooser();
                return;
            }
            // handle 'next screen' requests
            if (mNext != null && mNext.mRect.Contains((int) mV.X, (int) mV.Y)) {
                // special case for when we came straight from the Splash screen
                if (Lol.sGame.mCurrLevelNum == 0)
                    Lol.sGame.mCurrLevelNum = 1;
                Lol.sGame.mCurrLevelNum += (cc.getColumns() * cc.getRows());
                Lol.sGame.doChooser();
                return;
            }

            // check for press to an unlocked level
            int unlocked = Score.readUnlocked();
            foreach (Button ls in levels) {
                if (ls != null && (ls.mLevel <= unlocked || Lol.sGame.mConfig.getUnlockMode())) {
                    if (ls.mRect.Contains((int) mV.X, (int) mV.Y))
                        Lol.sGame.doPlayLevel(ls.mLevel);
                }
            }
        }
    }
}