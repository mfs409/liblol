package edu.lehigh.cse.ale;

import com.badlogic.gdx.Gdx;

// this will probably roll into score

public class MenuManager {

    /*
     * INTERNAL CLASSES
     */


    
    /*
     * HELP LEVELS
     */


    
    /*
     * MANAGE WINNING AND LOSING LEVELS
     */

    /**
     * When a level ends in failure, this is how we shut it down, print a message, and then let the user resume it
     * 
     * @param loseText
     *            Text to print when the level is lost
     */
    static void loseLevel(String loseText) 
    {
    	/*
        ALE._self.levelCompleteTrigger(false);
        if (Level._gameOver)
            return;
        Level._gameOver = true;
        if (Level._loseSound != null)
            Level._loseSound.play();

        Controls.resetHUD();

        Level.hideAllHeroes();
        // dim out the screen by putting a slightly transparent black rectangle
        // on the HUD
        Rectangle r = new Rectangle(0, 0, Configuration.getCameraWidth(), Configuration.getCameraHeight(),
                ALE._self.getVertexBufferObjectManager())
        {
            // When this _sprite is pressed, we re-create the level
            @Override
            public boolean onAreaTouched(TouchEvent e, float x, float y)
            {
                if (e.getAction() != TouchEvent.ACTION_DOWN)
                    return false;

                // now draw the chooser screen
                ALE._self.getEngine().clearUpdateHandlers();
                ALE._self._camera.setHUD(new HUD());
                ALE._self.configureLevel(_currLevel);
                ALE._self.getEngine().setScene(Level._current);
                if (Level._music != null)
                    Level._music.play();

                // NB: we return true because we are acting on account of the
                // touch, so we don't want to propoagate the touch to an
                // underlying entity
                return true;
            }
        };
        r.setColor(0, 0, 0);
        r.setAlpha(0.9f);
        Controls._hud.registerTouchArea(r);
        Controls._hud.attachChild(r);
        Controls._timerActive = false;

        // draw a background image?
        if (Level._backgroundYouLost != null) {
            AnimatedSprite as = new AnimatedSprite(0, 0, Configuration.getCameraWidth(),
                    Configuration.getCameraHeight(), Media.getImage(Level._backgroundYouLost),
                    ALE._self.getVertexBufferObjectManager());
            Controls._hud.attachChild(as);
        }

        Text t = new Text(100, 100, _menuFont, loseText, ALE._self.getVertexBufferObjectManager());
        float w = t.getWidth();
        float h = t.getHeight();
        t.setPosition(Configuration.getCameraWidth() / 2 - w / 2, Configuration.getCameraHeight() / 2 - h / 2);
        Controls._hud.attachChild(t);
*/
    }
    
    /**
     * When a level is won, this is how we end the scene and allow a transition to the next level
     */
    static void winLevel()
    {
    	Gdx.app.log("Win", "You Won!");
    	// set game over, and the renderer will handle the rest...
    	GameLevel._currLevel._gameOver = true;
    	
    	/*
        ALE._currLevel._game.levelCompleteTrigger(true);
        if (Level._gameOver)
            return;
        Level._gameOver = true;
        if (Level._winSound != null)
            Level._winSound.play();

        if (_unlocklevel == _currLevel) {
            _unlocklevel++;
            saveUnlocked();
        }

        Level.hideAllHeroes();
        // dim out the screen by putting a slightly transparent black rectangle on the HUD
        Rectangle r = new Rectangle(0, 0, Configuration.getCameraWidth(), Configuration.getCameraHeight(),
                ALE._self.getVertexBufferObjectManager())
        {
            // When the rectangle is pressed, we change the level to 1 and
            // switch to the level picker mode, then we display the appropriate scene
            @Override
            public boolean onAreaTouched(TouchEvent e, float x, float y)
            {
                if (e.getAction() != TouchEvent.ACTION_DOWN)
                    return false;

                // if we're out of levels, switch to the chooser
                if (_currLevel == Configuration.getNumLevels()) {
                    _mode = Modes.CHOOSE;
                    if (Level._music != null && Level._music.isPlaying())
                        Level._music.pause();
                    ALE._self.getEngine().clearUpdateHandlers();
                    ALE._self.getEngine().setScene(Chooser.draw(_menuFont));
                }
                else {
                    _currLevel++;
                    if (Level._music != null && Level._music.isPlaying())
                        Level._music.pause();
                    ALE._self.getEngine().clearUpdateHandlers();
                    ALE._self._camera.setHUD(new HUD());
                    ALE._self.configureLevel(_currLevel);
                    ALE._self.getEngine().setScene(Level._current);
                    if (Level._music != null)
                        Level._music.play();
                }
                // NB: we return true because we are acting on account of the
                // touch, so we don't want to propoagate the touch to an
                // underlying entity
                return true;
            }
        };
        r.setColor(0, 0, 0);
        r.setAlpha(0.9f);
        Controls.resetHUD();
        Controls._hud.attachChild(r);
        Controls._hud.registerTouchArea(r);
        Controls._timerActive = false;

        // draw a background image?
        if (Level._backgroundYouWon != null) {
            AnimatedSprite as = new AnimatedSprite(0, 0, Configuration.getCameraWidth(),
                    Configuration.getCameraHeight(), Media.getImage(Level._backgroundYouWon),
                    ALE._self.getVertexBufferObjectManager());
            Controls._hud.attachChild(as);
        }

        Text t = new Text(100, 100, _menuFont, Level._textYouWon, ALE._self.getVertexBufferObjectManager());
        float w = t.getWidth();
        float h = t.getHeight();
        t.setPosition(Configuration.getCameraWidth() / 2 - w / 2, Configuration.getCameraHeight() / 2 - h / 2);
        Controls._hud.attachChild(t);
*/
    }



}
