using System;
using System.Collections.Generic;

using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Graphics;

namespace LibLOL
{
    public static class Controls
    {
        /**
         * This is for handling everything that gets drawn on the HUD, whether it is
         * pressable or not
         */
        internal class HudEntity
        {
            /**
             * Should we run code when this HudEntity is touched?
             */
            internal bool mIsTouchable;

            /**
             * For touchable HudEntities, this is the rectangle on the screen that
             * is touchable
             */
            internal Rectangle mRange;

            /**
             * What color should we use to draw text, if this HudEntity is a text
             * entity?
             */
            internal Color mColor = new Color(0, 0, 0, 1);

            /**
             * What image should we display, if this HudEntity has an image
             * associated with it?
             */
            internal Texture2D mImage;

            internal RenderDelegate Render = null;

            /**
             * Run this code when this HUD entity is down-pressed
             * 
             * @param vec The coordinates of the touch
             */
            internal PressDelegate OnDownPress = null;

            /**
             * Use this constructor for controls that provide pressable images
             * 
             * @param imgName The name of the image to display. If "" is given as
             *            the name, it will not crash.
             * @param x The X coordinate (in pixels) of the bottom left corner.
             * @param y The Y coordinate (in pixels) of the bottom left corner.
             * @param width The width of the Hud Entity
             * @param height The height of the Hud Entity
             */
            internal HudEntity(String imgName, int x, int y, int width, int height)
            {
                // set up the image to display
                //
                // NB: this will fail gracefully (no crash) for invalid file names
                Texture2D[] trs = Media.GetImage(imgName);
                if (trs != null)
                    mImage = trs[0];

                // set up the touchable range for the image
                mRange = new Rectangle(x, y, width, height);
                mIsTouchable = true;
            }

            /**
             * Use this constructor for controls that are simply for displaying text
             * 
             * @param red The red portion of text color (0-255)
             * @param green The green portion of text color (0-255)
             * @param blue The blue portion of text color (0-255)
             */
            internal HudEntity(int red, int green, int blue)
            {
                mColor.R = (byte)(((float)red) / 256);
                mColor.G = (byte)(((float)green) / 256);
                mColor.B = (byte)(((float)blue) / 256);
                mIsTouchable = false;
            }

            internal delegate void RenderDelegate(SpriteBatch sb);
            internal delegate void PressDelegate(Vector3 vv);
            internal delegate void UpPressDelegate();



            /**
             * Run this code when this HUD entity is still being pressed, after a
             * down press has already been observed.
             * 
             * @param vec The coordinates of the touch
             */
            internal PressDelegate OnHold;

            /**
             * Run this code when this HUD entity is released
             */
            internal UpPressDelegate OnUpPress;

            /**
             * This is the render method when we've got a valid TR. When we don't,
             * we're displaying text, which probably means we're also dynamically
             * updating the text to display on every render, so it makes sense to
             * overload the render() call for those HUD entities
             * 
             * @param sb The SpriteBatch to use to draw the image
             */
            public void Draw(SpriteBatch sb)
            {
                if (Render != null)
                {
                    Render(sb);
                }
                else
                    if (mImage != null)
                        sb.Draw(mImage, new Vector2(mRange.X, mRange.Y), mRange, Color.White, 0, new Vector2(0, 0), 1, SpriteEffects.None, 0);
            }
        }
 

        /**
         * A helper method to draw text nicely. In GDX, we draw everything by giving
         * the bottom left corner, except text, which takes the top left corner.
         * This function handles the conversion, so that we can use bottom-left.
         * 
         * @param x The X coordinate of the bottom left corner (in pixels)
         * @param y The Y coordinate of the bottom left corner (in pixels)
         * @param message The text to display
         * @param bf The BitmapFont object to use for the text's font
         * @param sb The SpriteBatch used to render the text
         */
        internal static void DrawTextTransposed(int x, int y, String message, Font bf, SpriteBatch sb)
        {
            sb.DrawString(bf.Face, message, new Vector2(x, y), bf.Color);
        }

        /*
         * PUBLIC INTERFACE
         */

        /**
         * Add a countdown timer to the screen. When time is up, the level ends in
         * defeat
         * 
         * @param timeout Starting value of the timer
         * @param text The text to display when the timer expires
         * @param x The X coordinate of the bottom left corner (in pixels)
         * @param y The Y coordinate of the bottom left corner (in pixels)
         */
        public static void AddCountdown(float timeout, String text, int x, int y)
        {
            AddCountdown(timeout, text, x, y, Lol.sGame.mConfig.GetDefaultFontFace(),
                    Lol.sGame.mConfig.GetDefaultFontRed(), Lol.sGame.mConfig.GetDefaultFontGreen(),
                    Lol.sGame.mConfig.GetDefaultFontBlue(), Lol.sGame.mConfig.GetDefaultFontSize());
        }

        /**
         * Add a countdown timer to the screen, with extra features for describing
         * the appearance of the font. When time is up, the level ends in defeat.
         * 
         * @param timeout Starting value of the timer
         * @param text The text to display when the timer expires
         * @param x The X coordinate of the bottom left corner (in pixels)
         * @param y The Y coordinate of the bottom left corner (in pixels)
         * @param fontname The name of the font file to use
         * @param red The red portion of text color (0-255)
         * @param green The green portion of text color (0-255)
         * @param blue The blue portion of text color (0-255)
         * @param size The font size to use (20 is usually a good value)
         */
        public static void AddCountdown(float timeout, String text, int x,
                int y, String fontName, int red, int green, int blue, int size)
        {
            Level.sCurrent.mScore.mCountDownRemaining = timeout;
            Font bf = Media.GetFont(fontName, size);

            HudEntity hud = new HudEntity(red, green, blue);
            hud.Render = delegate(SpriteBatch sb)
            {
                bf.Color = hud.mColor;
                // TODO: Calculate time between calls to Draw OR use a timer and use this for the count Down
                Level.sCurrent.mScore.mCountDownRemaining -= 0.01F;
                if (Level.sCurrent.mScore.mCountDownRemaining > 0)
                {
                    DrawTextTransposed(x, y, "" + (int)Level.sCurrent.mScore.mCountDownRemaining,
                            bf, sb);
                }
                else
                {
                    PostScene.SetDefaultLoseText(text);
                    Level.sCurrent.mScore.EndLevel(false);
                }
            };
            Level.sCurrent.mControls.Add(hud);
        }

        /**
         * Print the frames per second
         * 
         * @param x The X coordinate of the bottom left corner (in pixels)
         * @param y The Y coordinate of the bottom left corner (in pixels)
         * @param fontname The name of the font file to use
         * @param red The red portion of text color (0-255)
         * @param green The green portion of text color (0-255)
         * @param blue The blue portion of text color (0-255)
         * @param size The font size to use (20 is usually a good value)
         */
        public static void AddFPS(int x, int y, String fontName, int red,
                int green, int blue, int size)
        {
            Font bf = Media.GetFont(fontName, size);

            HudEntity hud = new HudEntity(red, green, blue);
            hud.Render = delegate(SpriteBatch sb)
            {
                bf.Color = hud.mColor;
                // TODO: Calculate FPS
                float fps = 30;
                DrawTextTransposed(x, y, "fps: " + fps, bf, sb);
            };
            Level.sCurrent.mControls.Add(hud);
        }

        /**
         * Add a countdown timer to the screen. When time is up, the level ends in
         * victory
         * 
         * @param timeout Starting value of the timer
         * @param x The X coordinate of the bottom left corner (in pixels)
         * @param y The Y coordinate of the bottom left corner (in pixels)
         */
        public static void AddWinCountdown(float timeout, int x, int y)
        {
            AddWinCountdown(timeout, x, y, Lol.sGame.mConfig.GetDefaultFontFace(),
                    Lol.sGame.mConfig.GetDefaultFontRed(), Lol.sGame.mConfig.GetDefaultFontGreen(),
                    Lol.sGame.mConfig.GetDefaultFontBlue(), Lol.sGame.mConfig.GetDefaultFontSize());
        }

        /**
         * Add a countdown timer to the screen, with extra features for describing
         * the appearance of the font. When time is up, the level ends in victory
         * 
         * @param timeout Starting value of the timer
         * @param x The X coordinate of the bottom left corner (in pixels)
         * @param y The Y coordinate of the bottom left corner (in pixels)
         * @param fontname The name of the font file to use
         * @param red The red portion of text color (0-255)
         * @param green The green portion of text color (0-255)
         * @param blue The blue portion of text color (0-255)
         * @param size The font size to use (20 is usually a good value)
         */
        public static void AddWinCountdown(float timeout, int x, int y,
                String fontName, int red, int green, int blue, int size)
        {
            Level.sCurrent.mScore.mWinCountRemaining = timeout;
            Font bf = Media.GetFont(fontName, size);
            HudEntity hud = new HudEntity(red, green, blue);
            hud.Render = delegate(SpriteBatch sb)
            {
                bf.Color = hud.mColor;
                // TODO: Calculate correct time like countdown
                Level.sCurrent.mScore.mWinCountRemaining -= 0.01F;
                if (Level.sCurrent.mScore.mWinCountRemaining > 0)
                    // get elapsed time for this level
                    DrawTextTransposed(x, y, "" + (int)Level.sCurrent.mScore.mWinCountRemaining,
                            bf, sb);
                else
                    Level.sCurrent.mScore.EndLevel(true);
            };
            Level.sCurrent.mControls.Add(hud);
        }

        /**
         * Add a count of the current number of goodies of the specified type, with
         * extra features for describing the appearance of the font
         * 
         * @param type The type of goodie to show (1-4)
         * @param max If this is > 0, then the message wil be of the form XX/max
         *            instead of just XX
         * @param text The text to display after the number of goodies
         * @param x The X coordinate of the bottom left corner (in pixels)
         * @param y The Y coordinate of the bottom left corner (in pixels)
         * @param fontname The name of the font file to use
         * @param red The red portion of text color (0-255)
         * @param green The green portion of text color (0-255)
         * @param blue The blue portion of text color (0-255)
         * @param size The font size to use (20 is usually a good value)
         */
        public static void AddGoodieCount(int type, int max, String text, int x,
                int y, String fontName, int red, int green, int blue, int size)
        {
            // The suffix to display after the goodie count:
            String suffix = (max > 0) ? "/" + max + " " + text : " " + text;
            Font bf = Media.GetFont(fontName, size);
            HudEntity he = new HudEntity(red, green, blue);
            he.Render = delegate(SpriteBatch sb)
            {
                bf.Color = he.mColor;
                DrawTextTransposed(x, y, "" + Level.sCurrent.mScore.mGoodiesCollected[type - 1]
                        + suffix, bf, sb);
            };
            Level.sCurrent.mControls.Add(he);
        }

        /**
         * Add a count of the number of enemies who have been defeated
         * 
         * @param max If this is > 0, then the message will be of the form XX/max
         *            instead of just XX
         * @param text The text to display after the number of goodies
         * @param x The X coordinate of the bottom left corner (in pixels)
         * @param y The Y coordinate of the bottom left corner (in pixels)
         */
        public static void AddDefeatedCount(int max, String text, int x, int y)
        {
            AddDefeatedCount(max, text, x, y, Lol.sGame.mConfig.GetDefaultFontFace(),
                    Lol.sGame.mConfig.GetDefaultFontRed(), Lol.sGame.mConfig.GetDefaultFontGreen(),
                    Lol.sGame.mConfig.GetDefaultFontBlue(), Lol.sGame.mConfig.GetDefaultFontSize());
        }

        /**
         * Add a count of the number of enemies who have been defeated, with extra
         * features for describing the appearance of the font
         * 
         * @param max If this is > 0, then the message wil be of the form XX/max
         *            instead of just XX
         * @param text The text to display after the number of goodies
         * @param x The X coordinate of the bottom left corner (in pixels)
         * @param y The Y coordinate of the bottom left corner (in pixels)
         * @param fontname The name of the font file to use
         * @param red The red portion of text color (0-255)
         * @param green The green portion of text color (0-255)
         * @param blue The blue portion of text color (0-255)
         * @param size The font size to use (20 is usually a good value)
         */
        public static void AddDefeatedCount(int max, String text, int x, int y,
                String fontName, int red, int green, int blue, int size)
        {
            // The suffix to display after the goodie count:
            String suffix = (max > 0) ? "/" + max + " " + text : " " + text;
            Font bf = Media.GetFont(fontName, size);
            HudEntity he = new HudEntity(red, green, blue);
            he.Render = delegate(SpriteBatch sb)
            {
                bf.Color = he.mColor;
                DrawTextTransposed(x, y, "" + Level.sCurrent.mScore.mEnemiesDefeated + suffix, bf,
                        sb);
            };
            Level.sCurrent.mControls.Add(he);
        }

        /**
         * Add a stopwatch for tracking how long a level takes
         * 
         * @param x The X coordinate of the bottom left corner (in pixels)
         * @param y The Y coordinate of the bottom left corner (in pixels)
         */
        static public void AddStopwatch(int x, int y)
        {
            AddStopwatch(x, y, Lol.sGame.mConfig.GetDefaultFontFace(),
                    Lol.sGame.mConfig.GetDefaultFontRed(), Lol.sGame.mConfig.GetDefaultFontGreen(),
                    Lol.sGame.mConfig.GetDefaultFontBlue(), Lol.sGame.mConfig.GetDefaultFontSize());
        }

        /**
         * Add a stopwatch for tracking how long a level takes, with extra features
         * for describing the appearance of the font
         * 
         * @param x The X coordinate of the bottom left corner (in pixels)
         * @param y The Y coordinate of the bottom left corner (in pixels)
         * @param fontname The name of the font file to use
         * @param red The red portion of text color (0-255)
         * @param green The green portion of text color (0-255)
         * @param blue The blue portion of text color (0-255)
         * @param size The font size to use (20 is usually a good value)
         */
        static public void AddStopwatch(int x, int y, String fontName, int red,
                int green, int blue, int size)
        {
            Font bf = Media.GetFont(fontName, size);
            HudEntity he = new HudEntity(red, green, blue);
            he.Render = delegate(SpriteBatch sb)
            {
                bf.Color = he.mColor;
                // TODO: Again replace that Gdx.graphics.getDeltaTime method
                Level.sCurrent.mScore.mStopWatchProgress += 0.01F;
                DrawTextTransposed(x, y, "" + (int)Level.sCurrent.mScore.mStopWatchProgress, bf, sb);
            };
            Level.sCurrent.mControls.Add(he);
        }

        /**
         * Display a strength meter for a specific hero
         * 
         * @param text The text to display after the remaining strength value
         * @param x The X coordinate of the bottom left corner (in pixels)
         * @param y The Y coordinate of the bottom left corner (in pixels)
         * @param h The Hero whose strength should be displayed
         */
        static public void AddStrengthMeter(String text, int x, int y, Hero h)
        {
            // forward to the more powerful method...
            AddStrengthMeter(text, x, y, Lol.sGame.mConfig.GetDefaultFontFace(),
                    Lol.sGame.mConfig.GetDefaultFontRed(), Lol.sGame.mConfig.GetDefaultFontGreen(),
                    Lol.sGame.mConfig.GetDefaultFontBlue(), Lol.sGame.mConfig.GetDefaultFontSize(), h);
        }

        /**
         * Display a strength meter for a specific hero, with extra features for
         * describing the appearance of the font
         * 
         * @param text The text to display after the remaining strength value
         * @param x The X coordinate of the bottom left corner (in pixels)
         * @param y The Y coordinate of the bottom left corner (in pixels)
         * @param fontname The name of the font file to use
         * @param red The red portion of text color (0-255)
         * @param green The green portion of text color (0-255)
         * @param blue The blue portion of text color (0-255)
         * @param size The font size to use (20 is usually a good value)
         * @param h The Hero whose strength should be displayed
         */
        static public void AddStrengthMeter(String text, int x, int y,
                String fontName, int red, int green, int blue, int size, Hero h)
        {
            Font bf = Media.GetFont(fontName, size);
            HudEntity he = new HudEntity(red, green, blue);
            he.Render = delegate(SpriteBatch sb)
            {
                bf.Color = he.mColor;
                // NOTE: UNCOMMENT
                DrawTextTransposed(x, y, "" + h.Strength + " " + text, bf, sb);
            };
            Level.sCurrent.mControls.Add(he);
        }

        /**
         * Display a meter showing how far a hero has traveled
         * 
         * @param text The text to display after the remaining strength value
         * @param x The X coordinate of the bottom left corner (in pixels)
         * @param y The Y coordinate of the bottom left corner (in pixels)
         * @param fontname The name of the font file to use
         * @param red The red portion of text color (0-255)
         * @param green The green portion of text color (0-255)
         * @param blue The blue portion of text color (0-255)
         * @param size The font size to use (20 is usually a good value)
         * @param h The Hero whose strength should be displayed
         */
        static public void AddDistanceMeter(String text, int x, int y,
                String fontName, int red, int green, int blue, int size, Hero h)
        {
            Font bf = Media.GetFont(fontName, size);
            HudEntity he = new HudEntity(red, green, blue);
            he.Render = delegate(SpriteBatch sb)
            {
                bf.Color = he.mColor;
                Level.sCurrent.mScore.mDistance = (int)h.XPosition;
                DrawTextTransposed(x, y, "" + Level.sCurrent.mScore.mDistance + " " + text, bf, sb);
            };
            Level.sCurrent.mControls.Add(he);
        }

        /**
         * Display the number of remaining projectiles
         * 
         * @param text The text to display after the number of goodies
         * @param x The X coordinate of the bottom left corner (in pixels)
         * @param y The Y coordinate of the bottom left corner (in pixels)
         * @param fontname The name of the font file to use
         * @param red The red portion of text color (0-255)
         * @param green The green portion of text color (0-255)
         * @param blue The blue portion of text color (0-255)
         * @param size The font size to use (20 is usually a good value)
         */
        public static void AddProjectileCount(String text, int x, int y,
                String fontName, int red, int green, int blue, int size)
        {
            Font bf = Media.GetFont(fontName, size);
            HudEntity he = new HudEntity(red, green, blue);
            he.Render = delegate(SpriteBatch sb)
            {
                bf.Color = he.mColor;
                DrawTextTransposed(x, y, "" + Level.sCurrent.mProjectilePool.mProjectilesRemaining
                        + " " + text, bf, sb);
            };
            Level.sCurrent.mControls.Add(he);
        }

        /**
         * Add a button that pauses the game by causing a PauseScene to be
         * displayed. Note that you must configure a PauseScene, or pressing this
         * button will cause your game to crash.
         * 
         * @param x The X coordinate of the bottom left corner (in pixels)
         * @param y The Y coordinate of the bottom left corner (in pixels)
         * @param width The width of the image
         * @param height The height of the image
         * @param imgName The name of the image to display. Use "" for an invisible
         *            button
         */
        public static void addPauseButton(int x, int y, int width, int height, String imgName)
        {
            HudEntity he = new HudEntity(imgName, x, y, width, height);
            he.OnDownPress = delegate(Vector3 vv)
            {
                PauseScene.Show();
            };
            Level.sCurrent.mControls.Add(he);
        }

        /**
         * Add a button that moves an entity
         * 
         * @param x The X coordinate of the bottom left corner (in pixels)
         * @param y The Y coordinate of the bottom left corner (in pixels)
         * @param width The width of the image
         * @param height The height of the image
         * @param imgName The name of the image to display. Use "" for an invisible
         *            button
         * @param entity The entity to move downward
         * @param dx The new X velocity
         * @param dy The new Y velocity
         */
        public static void AddMoveButton(int x, int y, int width, int height, String imgName,
                PhysicsSprite entity, float dx, float dy)
        {
            HudEntity he = new HudEntity(imgName, x, y, width, height);
            he.OnDownPress = delegate(Vector3 vv)
            {
                // NOTE: UNCOMMENT
                Vector2 v = entity.mBody.LinearVelocity;
                if (dx != 0)
                    v.X = dx;
                if (dy != 0)
                    v.Y = dy;
                entity.UpdateVelocity(v.X, v.Y);
            };

            he.OnHold = delegate(Vector3 vv)
            {
                he.OnDownPress(vv);
            };

            he.OnUpPress = delegate()
            {
                // NOTE: UNCOMMENT
                Vector2 v = entity.mBody.LinearVelocity;
                if (dx != 0)
                    v.X = 0;
                if (dy != 0)
                    v.Y = 0;
                entity.UpdateVelocity(v.X, v.Y);
            };
            Level.sCurrent.mControls.Add(he);
        }

        /**
         * Add a button that moves an entity downward
         * 
         * @param x The X coordinate of the bottom left corner (in pixels)
         * @param y The Y coordinate of the bottom left corner (in pixels)
         * @param width The width of the image
         * @param height The height of the image
         * @param imgName The name of the image to display. Use "" for an invisible
         *            button
         * @param rate Rate at which the entity moves
         * @param entity The entity to move downward
         */
        public static void AddDownButton(int x, int y, int width, int height, String imgName,
                float rate, PhysicsSprite entity)
        {
            AddMoveButton(x, y, width, height, imgName, entity, 0, -rate);
        }

        /**
         * Add a button that moves an entity upward
         * 
         * @param x The X coordinate of the bottom left corner (in pixels)
         * @param y The Y coordinate of the bottom left corner (in pixels)
         * @param width The width of the image
         * @param height The height of the image
         * @param imgName The name of the image to display. Use "" for an invisible
         *            button
         * @param rate Rate at which the entity moves
         * @param entity The entity to move upward
         */
        public static void AddUpButton(int x, int y, int width, int height, String imgName, float rate,
                PhysicsSprite entity)
        {
            AddMoveButton(x, y, width, height, imgName, entity, 0, rate);
        }

        /**
         * Add a button that moves the given entity left
         * 
         * @param x The X coordinate of the bottom left corner (in pixels)
         * @param y The Y coordinate of the bottom left corner (in pixels)
         * @param width The width of the image
         * @param height The height of the image
         * @param imgName The name of the image to display. Use "" for an invisible
         *            button
         * @param rate Rate at which the entity moves
         * @param entity The entity that should move left when the button is pressed
         */
        public static void AddLeftButton(int x, int y, int width, int height, String imgName,
                float rate, PhysicsSprite entity)
        {
            AddMoveButton(x, y, width, height, imgName, entity, -rate, 0);
        }

        /**
         * Add a button that moves the given entity to the right
         * 
         * @param x The X coordinate of the bottom left corner (in pixels)
         * @param y The Y coordinate of the bottom left corner (in pixels)
         * @param width The width of the image
         * @param height The height of the image
         * @param imgName The name of the image to display. Use "" for an invisible
         *            button
         * @param rate Rate at which the entity moves
         * @param entity The entity that should move right when the button is
         *            pressed
         */
        public static void AddRightButton(int x, int y, int width, int height, String imgName,
                float rate, PhysicsSprite entity)
        {
            AddMoveButton(x, y, width, height, imgName, entity, rate, 0);
        }

        /**
         * Add a button that moves the given entity at one speed when it is
         * depressed, and at another otherwise
         * 
         * @param x The X coordinate of the bottom left corner (in pixels)
         * @param y The Y coordinate of the bottom left corner (in pixels)
         * @param width The width of the image
         * @param height The height of the image
         * @param imgName The name of the image to display. Use "" for an invisible
         *            button
         * @param rateDownX Rate (X) at which the entity moves when the button is
         *            pressed
         * @param rateDownY Rate (Y) at which the entity moves when the button is
         *            pressed
         * @param rateUpX Rate (X) at which the entity moves when the button is not
         *            pressed
         * @param rateUpY Rate (Y) at which the entity moves when the button is not
         *            pressed
         * @param entity The entity that the button controls
         */
        public static void AddTurboButton(int x, int y, int width, int height, String imgName,
                int rateDownX, int rateDownY, int rateUpX, int rateUpY,
                PhysicsSprite entity)
        {
            HudEntity he = new HudEntity(imgName, x, y, width, height);
            he.OnDownPress = delegate(Vector3 vv)
            {
                // NOTE: UNCOMMENT
                Vector2 v = entity.mBody.LinearVelocity;
                v.X = rateDownX;
                v.Y = rateDownY;
                entity.UpdateVelocity(v.X, v.Y);
            };

            he.OnHold = delegate(Vector3 vv)
            {
                he.OnDownPress(vv);
            };

            he.OnUpPress = delegate()
            {
                // NOTE: UNCOMMENT
                Vector2 v = entity.mBody.LinearVelocity;
                v.X = rateUpX;
                v.Y = rateUpY;
                entity.UpdateVelocity(v.X, v.Y);
            };

            Level.sCurrent.mControls.Add(he);
        }

        /**
         * Add a button that moves the given entity at one speed, but doesn't stop
         * the entity when the button is released
         * 
         * @param x The X coordinate of the bottom left corner (in pixels)
         * @param y The Y coordinate of the bottom left corner (in pixels)
         * @param width The width of the image
         * @param height The height of the image
         * @param imgName The name of the image to display. Use "" for an invisible
         *            button
         * @param rateX Rate (X) at which the entity moves when the button is
         *            pressed
         * @param rateY Rate (Y) at which the entity moves when the button is
         *            pressed
         * @param entity The entity that the button controls
         */
        public static void AddDampenedMotionButton(int x, int y, int width, int height, String imgName,
                float rateX, float rateY, float dampening, PhysicsSprite entity)
        {
            HudEntity he = new HudEntity(imgName, x, y, width, height);
            he.OnDownPress = delegate(Vector3 vv)
            {
                // NOTE: UNCOMMENT
                Vector2 v = entity.mBody.LinearVelocity;
                v.X = rateX;
                v.Y = rateY;
                entity.mBody.LinearDamping = 0;
                entity.UpdateVelocity(v.X, v.Y);
            };

            he.OnUpPress = delegate()
            {
                // NOTE: UNCOMMENT
                entity.mBody.LinearDamping = dampening;
            };
            Level.sCurrent.mControls.Add(he);
        }

        /**
         * Add a button that puts the hero into crawl mode when depressed, and
         * regular mode when released
         * 
         * @param x The X coordinate of the bottom left corner (in pixels)
         * @param y The Y coordinate of the bottom left corner (in pixels)
         * @param width The width of the image
         * @param height The height of the image
         * @param imgName The name of the image to display. Use "" for an invisible
         *            button
         * @param h The hero to control
         */
        public static void AddCrawlButton(int x, int y, int width, int height, String imgName,
                Hero h)
        {
            HudEntity he = new HudEntity(imgName, x, y, width, height);
            he.OnDownPress = delegate(Vector3 vv)
            {
                // NOTE: UNCOMMENT
                h.CrawlOn();
            };

            he.OnUpPress = delegate()
            {
                // NOTE: UNCOMMENT
                h.CrawlOff();
            };
            Level.sCurrent.mControls.Add(he);
        }

        /**
         * Add a button to make the hero jump
         * 
         * @param x The X coordinate of the bottom left corner (in pixels)
         * @param y The Y coordinate of the bottom left corner (in pixels)
         * @param width The width of the image
         * @param height The height of the image
         * @param imgName The name of the image to display. Use "" for an invisible
         *            button
         * @param h The hero to control
         */
        public static void AddJumpButton(int x, int y, int width, int height, String imgName,
                Hero h)
        {
            HudEntity he = new HudEntity(imgName, x, y, width, height);
            he.OnDownPress = delegate(Vector3 vv)
            {
                // NOTE: UNCOMMENT
                h.Jump();
            };
            Level.sCurrent.mControls.Add(he);
        }

        /**
         * Add a button to make the hero throw a projectile
         * 
         * @param x The X coordinate of the bottom left corner (in pixels)
         * @param y The Y coordinate of the bottom left corner (in pixels)
         * @param width The width of the image
         * @param height The height of the image
         * @param imgName The name of the image to display. Use "" for an invisible
         *            button
         * @param h The hero who should throw the projectile
         * @param milliDelay A delay between throws, so that holding doesn't lead to
         *            too many throws at once
         */
        public static void AddThrowButton(int x, int y, int width, int height, String imgName,
                Hero h, int milliDelay)
        {
            HudEntity he = new HudEntity(imgName, x, y, width, height);
            DateTime mLastThrow = DateTime.Now;

            he.OnDownPress = delegate(Vector3 vv)
            {
                Level.sCurrent.mProjectilePool.ThrowFixed(h);
                mLastThrow = DateTime.Now;
            };

            he.OnHold = delegate(Vector3 vv)
            {
                DateTime delayed = mLastThrow.AddMilliseconds(milliDelay);
                if (delayed < DateTime.Now)
                {
                    mLastThrow = DateTime.Now;
                    Level.sCurrent.mProjectilePool.ThrowFixed(h);
                }
            };
            Level.sCurrent.mControls.Add(he);
        }

        /**
         * Add a button to make the hero throw a projectile, but holding doesn't
         * make it throw more often
         * 
         * @param x The X coordinate of the bottom left corner (in pixels)
         * @param y The Y coordinate of the bottom left corner (in pixels)
         * @param width The width of the image
         * @param height The height of the image
         * @param imgName The name of the image to display. Use "" for an invisible
         *            button
         * @param h The hero who should throw the projectile
         */
        public static void AddSingleThrowButton(int x, int y, int width, int height, String imgName,
                Hero h)
        {
            HudEntity he = new HudEntity(imgName, x, y, width, height);
            he.OnDownPress = delegate(Vector3 vv)
            {
                Level.sCurrent.mProjectilePool.ThrowFixed(h);
            };
            Level.sCurrent.mControls.Add(he);
        }

        /**
         * The default behavior for throwing is to throw in a straight line. If we
         * instead desire that the projectiles have some sort of aiming to them, we
         * need to use this method, which throws toward where the screen was pressed
         * Note: you probably want to use an invisible button that covers the
         * screen...
         * 
         * @param x The X coordinate of the bottom left corner (in pixels)
         * @param y The Y coordinate of the bottom left corner (in pixels)
         * @param width The width of the image
         * @param height The height of the image
         * @param imgName The name of the image to display. Use "" for an invisible
         *            button
         * @param h The hero who should throw the projectile
         * @param milliDelay A delay between throws, so that holding doesn't lead to
         *            too many throws at once
         */
        public static void AddVectorThrowButton(int x, int y, int width, int height, String imgName,
                Hero h, long milliDelay)
        {
            HudEntity he = new HudEntity(imgName, x, y, width, height);
            DateTime mLastThrow = DateTime.Now;
            he.OnDownPress = delegate(Vector3 vv)
            {
                // NOTE: UNCOMMENT
                Level.sCurrent.mProjectilePool.ThrowAt(h.mBody.Position.X,
                        h.mBody.Position.Y, vv.X, vv.Y, h);
                mLastThrow = DateTime.Now;
            };

            he.OnHold = delegate(Vector3 vv)
            {
                DateTime delayed = mLastThrow.AddMilliseconds(milliDelay);
                if (delayed < DateTime.Now)
                {
                    mLastThrow = DateTime.Now;
                    // NOTE: UNCOMMENT
                    Level.sCurrent.mProjectilePool.ThrowAt(h.mBody.Position.X,
                            h.mBody.Position.Y, vv.X, vv.Y, h);
                }
            };
            Level.sCurrent.mControls.Add(he);
        }

        /**
         * This is almost exactly like addVectorThrowButton. The only difference is
         * that holding won't cause the hero to throw more projectiles
         * 
         * @param x The X coordinate of the bottom left corner (in pixels)
         * @param y The Y coordinate of the bottom left corner (in pixels)
         * @param width The width of the image
         * @param height The height of the image
         * @param imgName The name of the image to display. Use "" for an invisible
         *            button
         * @param h The hero who should throw the projectile
         */
        public static void AddVectorSingleThrowButton(int x, int y, int width, int height,
                String imgName, Hero h)
        {
            HudEntity he = new HudEntity(imgName, x, y, width, height);
            he.OnDownPress = delegate(Vector3 vv)
            {
                // NOTE: UNCOMMENT
                Level.sCurrent.mProjectilePool.ThrowAt(h.mBody.Position.X,
                        h.mBody.Position.Y, vv.X, vv.Y, h);
            };
            Level.sCurrent.mControls.Add(he);
        }

        /**
         * Display a zoom out button. Note that zooming in and out does not work
         * well with elements that hover on the screen. Use with care.
         * 
         * @param x The X coordinate of the bottom left corner (in pixels)
         * @param y The Y coordinate of the bottom left corner (in pixels)
         * @param width The width of the image
         * @param height The height of the image
         * @param imgName The name of the image to display. Use "" for an invisible
         *            button
         * @param maxZoom Maximum zoom. 8 is usually a good default
         */
        public static void AddZoomOutButton(int x, int y, int width, int height, String imgName,
                float maxZoom)
        {
            HudEntity he = new HudEntity(imgName, x, y, width, height);
            he.OnDownPress = delegate(Vector3 v)
            {
                float curzoom = Level.sCurrent.mGameCam.zoom;
                if (curzoom < maxZoom)
                {
                    Level.sCurrent.mGameCam.zoom *= 2;
                    Level.sCurrent.mBgCam.zoom *= 2;
                }
            };
            Level.sCurrent.mControls.Add(he);
        }

        /**
         * Display a zoom in button
         * 
         * @param x The X coordinate of the bottom left corner (in pixels)
         * @param y The Y coordinate of the bottom left corner (in pixels)
         * @param width The width of the image
         * @param height The height of the image
         * @param imgName The name of the image to display. Use "" for an invisible
         *            button
         * @param minZoom Minimum zoom. 0.25f is usually a good default
         */
        public static void AddZoomInButton(int x, int y, int width, int height, String imgName,
                float minZoom)
        {
            HudEntity he = new HudEntity(imgName, x, y, width, height);
            he.OnDownPress = delegate(Vector3 v)
            {
                float curzoom = Level.sCurrent.mGameCam.zoom;
                if (curzoom > minZoom)
                {
                    Level.sCurrent.mGameCam.zoom /= 2;
                    Level.sCurrent.mBgCam.zoom /= 2;
                }
            };
            Level.sCurrent.mControls.Add(he);
        }

        /**
         * Add a button that rotates the hero
         * 
         * @param x The X coordinate of the bottom left corner (in pixels)
         * @param y The Y coordinate of the bottom left corner (in pixels)
         * @param width The width of the image
         * @param height The height of the image
         * @param imgName The name of the image to display. Use "" for an invisible
         *            button
         * @param rate Amount of rotation to apply to the hero on each press
         */
        public static void AddRotateButton(int x, int y, int width, int height, String imgName,
                float rate, Hero h)
        {
            HudEntity he = new HudEntity(imgName, x, y, width, height);
            he.OnDownPress = delegate(Vector3 vv)
            {
                // NOTE: UNCOMMENT
                h.IncreaseRotation(rate);
            };

            he.OnHold = delegate(Vector3 vv)
            {
                // NOTE: UNCOMMENT
                h.IncreaseRotation(rate);
            };

            Level.sCurrent.mControls.Add(he);
        }

        /**
         * Add an image to the heads-up display. Touching the image has no effect
         * 
         * @param x The X coordinate of the bottom left corner (in pixels)
         * @param y The Y coordinate of the bottom left corner (in pixels)
         * @param width The width of the image
         * @param height The height of the image
         * @param imgName The name of the image to display. Use "" for an invisible
         *            button
         */
        public static void AddImage(int x, int y, int width, int height, String imgName)
        {
            HudEntity he = new HudEntity(imgName, x, y, width, height);
            he.mIsTouchable = false;
            Level.sCurrent.mControls.Add(he);
        }

        /**
         * Add a button to the heads-up display that runs custom code via an
         * onControlPress trigger
         * 
         * @param x The X coordinate of the bottom left corner (in pixels)
         * @param y The Y coordinate of the bottom left corner (in pixels)
         * @param width The width of the image
         * @param height The height of the image
         * @param imgName The name of the image to display. Use "" for an invisible
         *            button
         * @param id An id to use for the trigger event
         */
        public static void AddTriggerControl(int x, int y, int width, int height, String imgName,
                int id)
        {
            HudEntity he = new HudEntity(imgName, x, y, width, height);
            he.OnDownPress = delegate(Vector3 vv)
            {
                Lol.sGame.OnControlPressTrigger(id, Lol.sGame.mCurrLevelNum);
            };
            Level.sCurrent.mControls.Add(he);
        }
    }
}
