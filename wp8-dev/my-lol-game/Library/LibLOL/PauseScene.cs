using System;
using System.Collections.Generic;

using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Graphics;
using Microsoft.Xna.Framework.Input.Touch;

namespace LibLOL
{
    class PauseScene
    {
        /**
         * All text and images that go on the PauseScreen are stored here
         */
        private List<Renderable> mSprites = new List<Renderable>();

        /**
         * Track if the PauseScene is visible. Initially it is not.
         */
        private bool mVisible;

        /**
         * A PauseScene can have a back button, which we represent with this
         * Rectangle
         */
        private Rectangle mBackRectangle;

        /**
         * For handling touches
         */
        private Vector3 mV = new Vector3();

        /**
         * Time that the PauseScene started being shown, so we can update timers
         */
        private DateTime showingAt;

        /**
         * Get the PauseScene that is configured for the current level, or create a
         * blank one if none exists. We use this as a convenience since the LOL
         * paradigm is that the game designer calls static methods on PauseScene to
         * configure an existing object.
         * 
         * @return The current PauseScene
         */
        private static PauseScene GetCurrPauseScene()
        {
            PauseScene ps = Level.sCurrent.mPauseScene;
            if (ps != null)
                return ps;
            ps = new PauseScene();
            Level.sCurrent.mPauseScene = ps;
            return ps;
        }

        /**
         * Internal method to draw a PauseScene
         * 
         * @param sb The SpriteBatch used to draw the text and pictures
         * @return true if the PauseScene was drawn, false otherwise
         */
        internal bool Draw(SpriteBatch sb)
        {
            // if the pop-up scene is not visible, do nothing
            if (!mVisible)
                return false;
            // handle touches
            Vector2 loc;
            if (Util.JustTouched(out loc))
            {
                // check if it's to the 'back to chooser' button
                mV = new Vector3(loc.X, loc.Y, 0);
                Level.sCurrent.mHudCam.Unproject(mV);
                if (mBackRectangle != null && mBackRectangle.Contains((int)mV.X, (int)mV.Y))
                {
                    Lol.sGame.HandleBack();
                    mVisible = false;
                    return false;
                }
                // otherwise, just clear the pauseScene (be sure to resume timers)
                mVisible = false;
                TimeSpan showTime = DateTime.Now - showingAt;
                Timer.Instance.Delay((long)showTime.TotalMilliseconds);
                Timer.Instance.Start();
                return false;
            }
            // clear screen and draw sprites via HudCam
            Lol.sGame.GraphicsDevice.Clear(Color.Black);
            Level.sCurrent.mHudCam.Update();
            // NOTE: Uncomment
            //sb.setProjectionMatrix(Level.sCurrent.mHudCam.combined);
            sb.Begin();
            foreach (Renderable r in mSprites)
                r.Draw(sb, new GameTime());
            sb.End();
            return true;
        }

        /*
         * PUBLIC INTERFACE
         */

        /**
         * Draw text on the PauseScene
         * 
         * @param text The text to display
         * @param x The X coordinate of the bottom left corner
         * @param y The Y coordinate of the bottom left corner
         * @param red The red component of the color (0-255)
         * @param green The green component of the color (0-255)
         * @param blue The blue component of the color (0-255)
         * @param fontName The name of the font to use
         * @param size The font size to use
         */
        public static void AddText(String text, int x, int y, int red, int green, int blue,
                String fontName, int size)
        {
            GetCurrPauseScene().mSprites.Add(Util
                    .MakeText(x, y, text, red, green, blue, fontName, size));
        }

        /**
         * Draw text on the PauseScene, and center the text vertically and
         * horizontally
         * 
         * @param text The text to display
         * @param red The red component of the color (0-255)
         * @param green The green component of the color (0-255)
         * @param blue The blue component of the color (0-255)
         * @param fontName The name of the font to use
         * @param size The font size to use
         */
        public static void AddText(String text, int red, int green, int blue, String fontName, int size)
        {
            GetCurrPauseScene().mSprites.Add(Util.MakeText(text, red, green, blue, fontName, size));
        }

        /**
         * Draw a picture on the PauseScene
         * 
         * @param imgName The name of the image file that should be displayed
         * @param x The X coordinate of the bottom left corner
         * @param y The Y coordinate of the bottom left corner
         * @param width The width of the image
         * @param height The height of the image
         */
        public static void AddImage(String imgName, int x, int y, int width, int height)
        {
            GetCurrPauseScene().mSprites.Add(Util.MakePicture(x, y, width, height, imgName));
        }

        /**
         * Draw a picture on the PauseScene, but indicate that touching the picture
         * will cause the level to stop playing, and control to return to the
         * chooser.
         * 
         * @param imgName The name of the image file that should be displayed
         * @param x The X coordinate of the bottom left corner
         * @param y The Y coordinate of the bottom left corner
         * @param width The width of the image
         * @param height The height of the image
         */
        public static void AddBackButton(String imgName, int x, int y, int width, int height)
        {
            GetCurrPauseScene().mBackRectangle = new Rectangle(x, y, width, height);
            GetCurrPauseScene().mSprites.Add(Util.MakePicture(x, y, width, height, imgName));
        }

        /**
         * Show the pause screen
         */
        public static void Show()
        {
            Timer.Instance.Stop();
            GetCurrPauseScene().mVisible = true;
            GetCurrPauseScene().showingAt = DateTime.Now;
        }

        /**
         * Clear everything off of the level's pause scene, so it can be reused
         */
        public static void Reset()
        {
            GetCurrPauseScene().mSprites.Clear();
            GetCurrPauseScene().mBackRectangle = Rectangle.Empty;
        }
    }
}
