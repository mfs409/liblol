using System;
using System.Collections.Generic;

using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Graphics;
using Microsoft.Xna.Framework.Input.Touch;

namespace LibLOL
{
    class PreScene
    {
        /**
         * The text and pictures to display
         */
        private List<Renderable> mSprites = new List<Renderable>();

        /**
         * True if we must click in order to clear the PreScene
         */
        private bool mClickToClear = true;

        /**
         * True when the scene is being displayed
         */
        private bool mVisible = true;

        /**
         * Time that the PauseScene started being shown, so we can update timers
         */
        private DateTime showingAt;

        /**
         * Get the PreScene that is configured for the current level, or create a
         * blank one if none exists. We use this as a convenience since the LOL
         * paradigm is that the game designer calls static methods on PreScene to
         * configure an existing object.
         * 
         * @return The current PreScene
         */
        private static PreScene GetCurrPreScene()
        {
            PreScene ps = Level.sCurrent.mPreScene;
            if (ps != null)
                return ps;
            ps = new PreScene();
            Level.sCurrent.SuspendTouch();
            Level.sCurrent.mPreScene = ps;
            // pause the timer
            Timer.Instance.Stop();
            ps.showingAt = DateTime.Now;
            return ps;
        }

        /**
         * Hide the PreScene, and resume any timers.
         */
        private void Hide()
        {
            // resume timers only on clickToClear, because that's the only time
            // they're suspended
            if (mClickToClear)
            {
                TimeSpan showTime = DateTime.Now - showingAt;
                Timer.Instance.Delay((long)showTime.TotalMilliseconds);
                Timer.Instance.Start();
            }
            //Level.sCurrent.mPreScene.mVisible = false;
        }

        /**
         * Render this PreScene
         * 
         * @param sb The SpriteBatch to use when rendering
         * @return true if we drew something, false otherwise
         */
        internal bool Draw(SpriteBatch sb)
        {
            // if the scene is not visible, do nothing
            if (!mVisible)
                return false;
            // if we're supposed to be listening for clicks, and we get one, then
            // disable the scene
            if (mClickToClear)
            {
                Vector2 loc;
                if (Util.JustTouched(out loc))
                {
                    Hide();
                    return false;
                }
            }
            // OK, we should render the scene...

            // clear screen and draw sprites... we can use the level's hudCam
            Lol.sGame.GraphicsDevice.Clear(Color.Black);
            Level.sCurrent.mHudCam.Update();
            // NOTE: UNCOMMENT
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
         * Add some text to the PreScene
         * 
         * @param text The text to display
         * @param x X coordinate of the text
         * @param y Y coordinate of the text
         * @param red Redness of the text color
         * @param green Greenness of the text color
         * @param blue Blueness of the text color
         * @param fontName The font file to use
         * @param size The size of the text
         */
        public static void AddText(String text, int x, int y, int red, int green, int blue,
                String fontName, int size)
        {
            GetCurrPreScene().mSprites.Add(Util.MakeText(x, y, text, red, green, blue, fontName, size));
        }

        /**
         * Add some text to the PreScene, and center it vertically and horizontally
         * 
         * @param text The text to display
         * @param red Redness of the text color
         * @param green Greenness of the text color
         * @param blue Blueness of the text color
         * @param fontName The font file to use
         * @param size The size of the text
         */
        public static void AddText(String text, int red, int green, int blue, String fontName, int size)
        {
            GetCurrPreScene().mSprites.Add(Util.MakeText(text, red, green, blue, fontName, size));
        }

        /**
         * Add an image to the PreScene
         * 
         * @param imgName The file name for the image to display
         * @param x X coordinate of the bottom left corner
         * @param y Y coordinate of the bottom left corner
         * @param width Width of the image
         * @param height Height of the image
         */
        public static void AddImage(String imgName, int x, int y, int width, int height)
        {
            GetCurrPreScene().mSprites.Add(Util.MakePicture(x, y, width, height, imgName));
        }

        /**
         * The default is for a PreScene to show until the user touches it to
         * dismiss it. To have the PreScene disappear after a fixed time instead,
         * use this.
         * 
         * @param duration The time, in seconds, before the PreScene should
         *            disappear.
         */
        public static void SetExpire(float duration)
        {
            if (duration > 0)
            {
                GetCurrPreScene().mClickToClear = false;
                // resume timers, or this won't work
                Timer.Instance.Start();
                Timer.Schedule(delegate()
                {
                    GetCurrPreScene().Hide();
                }, duration);
            }
        }
    }
}
