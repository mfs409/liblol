using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Graphics;

using LibLOL;

namespace my_lol_game
{
    /// <summary>
    /// This is the main type for your game
    /// </summary>
    public class Game1 : Lol
    {
        

        public Game1()
        {
            
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

            base.Initialize();
        }

        /// <summary>
        /// LoadContent will be called once per game and is the place to load
        /// all of your content.
        /// </summary>
        protected override void LoadContent()
        {
            // Create a new SpriteBatch, which can be used to draw textures.

            base.LoadContent();

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

        /// <summary>
        /// Allows the game to run logic such as updating the world,
        /// checking for collisions, gathering input, and playing audio.
        /// </summary>
        /// <param name="gameTime">Provides a snapshot of timing values.</param>
        protected override void Update(GameTime gameTime)
        {
            // TODO: Add your update logic here

            base.Update(gameTime);
        }

        /// <summary>
        /// This is called when the game should draw itself.
        /// </summary>
        /// <param name="gameTime">Provides a snapshot of timing values.</param>
        protected override void Draw(GameTime gameTime)
        {
            GraphicsDevice.Clear(Color.CornflowerBlue);

            // TODO: Add your drawing code here

            base.Draw(gameTime);
        }

        public override ChooserConfiguration ChooserConfig()
        {
            return null;
        }

        public override LolConfiguration LolConfig()
        {
            return null;
        }

        public override void ConfigureHelpScene(int whichScene)
        {
            throw new System.NotImplementedException();
        }

        public override void ConfigureLevel(int whichLevel)
        {
            if (whichLevel == 1)
            {
                Level.Configure(0, 0);
                Physics.Configure(0, 0);
                Hero h = Hero.MakeAsBox(300, 300, 50, 50, "greenball");
                Route route = new Route(4).To(300, 300).To(400, 300).To(100, 100).To(300, 300);
                h.SetRoute(route, 25, true);
                Destination.MakeAsBox(400, 300, 50, 50, "blueball");
            }
            else
            {
                throw new System.NotImplementedException();
            }
        }

        public override void ConfigureSplash()
        {
            throw new System.NotImplementedException();
        }

        public override void LevelCompleteTrigger(int whichLevel, bool win)
        {
            throw new System.NotImplementedException();
        }

        public override void NameResources()
        {
            Media.RegisterImage("greenball");
            Media.RegisterImage("blueball");
        }

        public override void OnControlPressTrigger(int id, int whichLevel)
        {
            throw new System.NotImplementedException();
        }

        public override void OnEnemyCollideTrigger(int id, int whichLevel, Enemy e)
        {
            throw new System.NotImplementedException();
        }

        public override void OnEnemyDefeatTrigger(int id, int whichLevel, Enemy e)
        {
            throw new System.NotImplementedException();
        }

        public override void OnEnemyTimerTrigger(int id, int whichLevel, Enemy e)
        {
            throw new System.NotImplementedException();
        }

        public override void OnHeroCollideTrigger(int id, int whichLevel, Obstacle o, Hero h)
        {
            throw new System.NotImplementedException();
        }

        public override void OnProjectileCollideTrigger(int id, int whichLevel, Obstacle o, Projectile p)
        {
            throw new System.NotImplementedException();
        }

        public override void OnStrengthChangeTrigger(int whichLevel, Hero h)
        {
            throw new System.NotImplementedException();
        }

        public override void OnTimerTrigger(int id, int whichLevel)
        {
            throw new System.NotImplementedException();
        }

        public override void OnTouchTrigger(int id, int whichLevel, PhysicsSprite p)
        {
            throw new System.NotImplementedException();
        }
    }
}
