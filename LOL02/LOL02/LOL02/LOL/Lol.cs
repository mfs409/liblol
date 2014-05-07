using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Xna.Framework.Input;
using Microsoft.Xna.Framework.Content;
using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Graphics;
using Microsoft.Devices;
using FarseerPhysics.Common;


namespace LOL
{
    public abstract class Lol: Game
    {
        public static ScreenManager Screen;
        public static GameTime GlobalGameTime;
        public static float FPS = 30;
        
        /**
         * The current mode of the program
         */
        private Modes mMode;

        /**
         * The current level being played
         */
        public int mCurrLevelNum;

        /**
         * Track the current help scene being displayed
         */
        public int mCurrHelpNum;

        /**
         * A reference to the game object
         */
        public static Lol sGame;

        /**
         * This variable lets us track whether the user pressed 'back' on an
         * android, or 'escape' on the desktop. We are using polling, so we swallow
         * presses that aren't preceded by a release. In that manner, holding 'back'
         * can't exit all the way out... you must press 'back' repeatedly, once for
         * each screen to revert.
         */
        public bool mKeyDown;

        /**
         * The configuration of the game is accessible through this
         */
        public LolConfiguration mConfig;

        /**
         * The configuratoin of the chooser screen is accessible through this
         */
        public ChooserConfiguration mChooserConfig;

        /**
         * Modes of the game: we can be showing the main screen, the help screens,
         * the level chooser, or a playable level
         */
        private enum Modes
        {
            SPLASH, HELP, CHOOSE, PLAY
        };

        /**
         * Use this to load the splash screen
         */
        public void doSplash() {
            // set the default display mode
            mCurrLevelNum = 0;
            mCurrHelpNum = 0;
            mMode = Modes.SPLASH;
            Screen.Display(new Splash());
        }

        /**
         * Use this to load the level-chooser screen. Note that when a game has only
         * one level, we'll never draw the level-picker screen, thereby mimicing the
         * behavior of "infinite" games.
         */
        public void doChooser() {
            if (mConfig.getNumLevels() == 1) {
                if (mCurrLevelNum == 1)
                    doSplash();
                else
                    doPlayLevel(1);
                return;
            }
            mCurrHelpNum = 0;
            mMode = Modes.CHOOSE;
            Screen.Display(new Chooser());
        }

        /**
         * Use this to load a playable level.
         * 
         * @param which The index of the level to load
         */
        public void doPlayLevel(int which) {
            mCurrLevelNum = which;
            mCurrHelpNum = 0;
            mMode = Modes.PLAY;
            configureLevel(which);
            Screen.Display(Level.sCurrent);
        }

        /**
         * Use this to load a help level.
         * 
         * @param which The index of the help level to load
         */
        public void doHelpLevel(int which) {
            mCurrHelpNum = which;
            mCurrLevelNum = 0;
            mMode = Modes.HELP;
            configureHelpScene(which);
            Screen.Display(HelpLevel.sCurrentLevel);
        }

        /**
         * Use this to quit the app
         */
        public void doQuit() {
            //getScreen().dispose();
            Exit();
        }

        /**
         * Vibrate the phone for a fixed amount of time. Note that this only
         * vibrates the phone if the configuration says that vibration should be
         * permitted.
         * 
         * @param millis The amount of time to vibrate
         */
        public void vibrate(int millis) {
            if (mConfig.getVibration())
            {
                // TODO: Figure out vibration
            }
        }

        /**
         * We can use this method from the render loop to poll for back presses
         */
        private void handleKeyDown() {
            // if neither BACK nor ESCAPE is being pressed, do nothing, but
            // recognize future presses
            if (GamePad.GetState(PlayerIndex.One).Buttons.Back != ButtonState.Pressed) {
                mKeyDown = false;
                return;
            }
            // if they key is being held down, ignore it
            if (mKeyDown)
                return;
            // recognize a new back press as being a 'down' press
            mKeyDown = true;
            handleBack();
        }

        /**
         * When the back key is pressed, or when we are simulating the back key
         * being pressed (e.g., a back button), this code runs.
         */
        public void handleBack() {
            // clear all timers, just in case...
            Timer.Instance.Clear();
            // if we're looking at main menu, then exit
            if (mMode == Modes.SPLASH) {
                dispose();
                Lol.sGame.Exit();
            }
            // if we're looking at the chooser or help, switch to the splash
            // screen
            else if (mMode == Modes.CHOOSE || mMode == Modes.HELP) {
                doSplash();
            }
            // ok, we're looking at a game scene... switch to chooser
            else {
                doChooser();
            }
        }

        /**
         * This is an internal method for initializing a game. User code should
         * never call this.
         */
        public void create() {
            sGame = this;
            // get configuration
            mConfig = lolConfig();
            mChooserConfig = chooserConfig();

            // for handling back presses
            // NOTE: Windows / XNA should do this automatically
            //Gdx.input.setCatchBackKey(true);

            // Load Resources
            nameResources();

            // show the splash screen
            doSplash();
        }

        /**
         * This is an internal method for quitting a game. User code should never
         * call this.
         */
        public void dispose() {
            // TODO: Tie into destructor / garbage collector
            //base.dispose();

            // dispose of all fonts, textureregions, etc...
            //
            // It appears that GDX manages all textures for images and fonts, as
            // well as all sounds and music files. That
            // being the case, the only thing we need to be careful about is that we
            // get rid of any references to fonts that
            // might be hanging around
            Media.onDispose();
        }

        /**
         * This is an internal method for drawing game levels. User code should
         * never call this.
         */
        public void render() {
            // Check for back press
            handleKeyDown();
            
        }

        /*
         * PUBLIC INTERFACE
         */

        /**
         * The programmer configures the game by implementing this method, and
         * returning a LolConfiguration object
         */
        abstract public LolConfiguration lolConfig();

        /**
         * The programmer configures the chooser screen by implementing this method,
         * and returning a ChooserConfiguration object
         */
        abstract public ChooserConfiguration chooserConfig();

        /**
         * Register any sound or image files to be used by the game
         */
        abstract public void nameResources();

        /**
         * Describe how to draw the levels of the game
         * 
         * @param whichLevel The number of the level being drawn
         */
        abstract public void configureLevel(int whichLevel);

        /**
         * Describe how to draw the help scenes
         * 
         * @param whichScene The number of the help scene being drawn
         */
        abstract public void configureHelpScene(int whichScene);

        /**
         * Describe how to draw the splash scene
         */
        abstract public void configureSplash();

        /**
         * When a Hero collides with an Obstacle for which a HeroCollideTrigger has
         * been set, this code will run
         * 
         * @param id The number assigned to the Obstacle's HeroCollideTrigger
         * @param whichLevel The current level
         * @param o The obstacle involved in the collision
         * @param h The hero involved in the collision
         */
        abstract public void onHeroCollideTrigger(int id, int whichLevel, Obstacle o, Hero h);

        /**
         * When the player touches an entity that has a TouchTrigger attached to it,
         * this code will run
         * 
         * @param id The number assigned to the entity's TouchTrigger
         * @param whichLevel The current level
         * @param o The entity involved in the collision
         */
        abstract public void onTouchTrigger(int id, int whichLevel, PhysicsSprite o);

        /**
         * When the player requests a TimerTrigger, and the required time passes,
         * this code will run
         * 
         * @param id The number assigned to the TimerTrigger
         * @param whichLevel The current level
         */
        abstract public void onTimerTrigger(int id, int whichLevel);

        /**
         * When a player requests an EnemyTimerTrigger, and the required time
         * passes, and the enemy is still visible, this code will run
         * 
         * @param id The number assigned to the EnemyTimerTrigger
         * @param whichLevel The current level
         * @param e The enemy to which the timer was attached
         */
        abstract public void onEnemyTimerTrigger(int id, int whichLevel, Enemy e);

        /**
         * When an enemy is defeated, this code will run if the enemy has an
         * EnemyDefeatTrigger
         * 
         * @param id The number assigned to this trigger
         * @param whichLevel The current level
         * @param e The enemy who was defeated
         */
        abstract public void onEnemyDefeatTrigger(int id, int whichLevel, Enemy e);

        /**
         * When an obstacle collides with an enemy, if the obstacle has an
         * EnemyCollideTrigger, then this code will run.
         * 
         * @param id The number assigned to this trigger
         * @param whichLevel The current level
         * @param o The obstacle involved in the collision
         * @param e The enemy involved in the collision
         */
        abstract public void onEnemyCollideTrigger(int id, int whichLevel, Obstacle o, Enemy e);

        /**
         * When a projectile collides with an obstacle, if the obstacle has a
         * ProjectileCollideTrigger, then this code will run
         * 
         * @param id The number assigned to this trigger
         * @param whichLevel The current level
         * @param o The obstacle involved in the collision
         * @param p The projectile involved in the collision
         */
        abstract public void onProjectileCollideTrigger(int id, int whichLevel, Obstacle o, Projectile p);

        /**
         * When a level finishes, this code will run
         * 
         * @param whichLevel The current level
         * @param win True if the level was won, false otherwise
         */
        abstract public void levelCompleteTrigger(int whichLevel, bool win);

        /**
         * When a Control is pressed, for which there is a ControlTrigger, this code
         * will run.
         * 
         * @param id The number assigned to this trigger
         * @param whichLevel The current level
         */
        abstract public void onControlPressTrigger(int id, int whichLevel);

        /**
         * When a hero collides with a goodie or enemy, and it leads to the hero's
         * strength changing, we can opt to run this code.
         * 
         * @param whichLevel The current level
         * @param h The hero whose strength just changed
         */
        abstract public void onStrengthChangeTrigger(int whichLevel, Hero h);

    }
}
