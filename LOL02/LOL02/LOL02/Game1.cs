using System;
using System.Collections.Generic;
using System.Linq;
using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Audio;
using Microsoft.Xna.Framework.Content;
using Microsoft.Xna.Framework.GamerServices;
using Microsoft.Xna.Framework.Graphics;
using Microsoft.Xna.Framework.Input;
using Microsoft.Xna.Framework.Input.Touch;
using Microsoft.Xna.Framework.Media;
using LOL;

namespace LOL02
{
    /// <summary>
    /// This is the main type for your game
    /// </summary>
    public class Game1 : Game
    {
        protected GraphicsDeviceManager graphics;
        protected SpriteBatch spriteBatch;
        public ScreenManager mgr;

        public Game1()
        {
            mgr = new ScreenManager();

            // TODO: Move to Lol class init code
            LOL.Lol.Content = Content;
            LOL.Lol.Window = Window;
            LOL.Lol.GD = GraphicsDevice;
            LOL.Lol.Screen = mgr;
            LOL.Lol.Game = this;

            graphics = new GraphicsDeviceManager(this);
            Content.RootDirectory = "Content";

            // Frame rate is 30 fps by default for Windows Phone.
            TargetElapsedTime = TimeSpan.FromTicks(333333);

            // Extend battery life under lock.
            InactiveSleepTime = TimeSpan.FromSeconds(1);
        }

        // WARNING: DO NOT TOUCH THIS METHOD!
        protected override void Update(GameTime gameTime)
        {
            // Allows the game to exit
            if (GamePad.GetState(PlayerIndex.One).Buttons.Back == ButtonState.Pressed)
                this.Exit();
            mgr.Update(gameTime);
            base.Update(gameTime);
        }

        // WARNING: DO NOT TOUCH THIS METHOD!
        protected override void Draw(GameTime gameTime)
        {
            mgr.Draw(gameTime);
            base.Draw(gameTime);
        }

        /// <summary>
        /// Allows the game to perform any initialization it needs to before starting to run.
        /// This is where it can query for any required services and load any non-graphic
        /// related content.  Calling base.Initialize will enumerate through any components
        /// and initialize them as well.
        /// </summary>
        protected override void Initialize()
        {
            // TODO: Add your initialization logic here
            SpriteFont sf = Content.Load<SpriteFont>("Default");
            GameScreen s1 = new GameScreen();
            s1.fDraw = delegate(GameTime gameTime)
            {
                GraphicsDevice.Clear(Color.Red);
                spriteBatch.Begin();
                spriteBatch.DrawString(sf, "ONE", new Vector2(100,100), Color.White);
                spriteBatch.End();
            };
            GameScreen s2 = new GameScreen();
            s2.fDraw = delegate(GameTime gameTime)
            {
                GraphicsDevice.Clear(Color.Green);
                spriteBatch.Begin();
                spriteBatch.DrawString(sf, "TWO", new Vector2(100,100), Color.White);
                spriteBatch.End();
            };
            GameScreen s3 = new GameScreen();
            s3.fDraw = delegate(GameTime gameTime)
            {
                GraphicsDevice.Clear(Color.Blue);
                spriteBatch.Begin();
                spriteBatch.DrawString(sf, "THREE", new Vector2(100,100), Color.White);
                spriteBatch.End();
            };
            int screen = 0;
            GameScreen.UpdateDelegate update1 = delegate(GameTime gameTime)
            {
                if (TouchPanel.GetState().Count > 0)
                {
                    mgr.Display((screen++) % 3);
                }
            };
            s1.fUpdate = update1;
            s2.fUpdate = update1;
            s3.fUpdate = update1;

            mgr.Add(s1);
            mgr.Add(s2);
            mgr.Add(s3);
            mgr.Display(0);
            base.Initialize();
        }

        /// <summary>
        /// LoadContent will be called once per game and is the place to load
        /// all of your content.
        /// </summary>
        protected override void LoadContent()
        {
            // Create a new SpriteBatch, which can be used to draw textures.
            spriteBatch = new SpriteBatch(GraphicsDevice);

            // TODO: use this.Content to load your game content here
        }

        /// <summary>
        /// UnloadContent will be called once per game and is the place to unload
        /// all content.
        /// </summary>
        protected override void UnloadContent()
        {
            // TODO: Unload any non ContentManager content here
        }
    }
}
