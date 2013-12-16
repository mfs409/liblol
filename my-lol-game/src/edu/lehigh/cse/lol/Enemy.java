package edu.lehigh.cse.lol;

// TODO: clean up comments

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

public class Enemy extends PhysicsSprite
{
    /**
     * Create a destination
     * 
     * This should never be called directly.
     * 
     * @param x
     *            X coordinate of top left corner of this destination
     * @param y
     *            X coordinate of top left corner of this destination
     * @param width
     *            Width of this destination
     * @param height
     *            Height of this destination
     * @param ttr
     *            Image to display
     * @param isStatic
     *            Can this destination move, or is it at a fixed location
     * @param isCircle
     *            true if this should use a circle underneath for its collision
     *            detection, and false if a box should be used
     */
    private Enemy(float width, float height, String imgName)
    {
        super(imgName, SpriteId.ENEMY, width, height);
        Level._currLevel._score._enemiesCreated++;
    }

    public static Enemy makeAsBox(float x, float y, float width, float height, String imgName)
    {
        Enemy e = new Enemy(width, height, imgName);
        e.setBoxPhysics(0, 0, 0, BodyType.StaticBody, false, x, y);
        Level._currLevel._sprites.add(e);
        return e;
    }

    public static Enemy makeAsCircle(float x, float y, float width, float height, String imgName)
    {
        float radius = (width > height) ? width : height;
        Enemy e = new Enemy(radius, radius, imgName);
        e.setCirclePhysics(0, 0, 0, BodyType.StaticBody, false, x, y, radius / 2);
        Level._currLevel._sprites.add(e);
        return e;
    }

    /**
     * Enemies are things to be avoided or defeated by the hero.
     * 
     * Every enemy can be defeated via bullets. They can also be defeated by colliding with invincible _heroes, or by
     * colliding with a hero whose _strength is >= the enemy's _strength, though that case results in the hero losing
     * _strength.
     * 
     * A level can require all enemies to be defeated before the level can be won.
     * 
     * Note that goodies can move, using the standard Route interface of PhysicsSprites, or by using tilt
     * 
     * TODO: add support for enemies that can be defeated by jumping on them
     */

    /*
     * BASIC FUNCTIONALITY
     */

    /*
     * SCORE AND DAMAGE
     */

    /**
     * Amount of _damage this enemy does to a hero on a collision. The default is 2, so that an enemy will defeat a hero
     * and not disappear.
     */
    int    _damage           = 2;

    /**
     * Message to display when this enemy defeats the last hero
     */
    String _onDefeatHeroText = "";

    /**
     * Set the amount of _damage that this enemy does to a hero
     * 
     * @param amount
     *            Amount of _damage. Default is 2, since _heroes have a default _strength of 1, so that the enemy
     *            defeats the hero but does not disappear.
     */
    public void setDamage(int amount)
    {
        _damage = amount;
    }

    /**
     * If this enemy defeats the last hero of the board, this is the message that will be displayed
     * 
     * @param message
     *            The message to display
     */
    public void setDefeatHeroText(String message)
    {
        _onDefeatHeroText = message;
    }

    /*
     * SUPPORT FOR DEFEATING ENEMIES
     */

    /**
     * Does a crawling hero avoid being damaged by this enemy?
     */
    boolean         _defeatByCrawl         = false;

    /**
     * Is this enemy immune to invincibility? That means it won't hurt the enemy, but it won't disappear
     */
    boolean         _immuneToInvincibility = false;

    /**
     * Does the enemy do _damage even to an invincible hero?
     */
    boolean         _alwaysDoesDamage      = false;

    /**
     * Indicates that touching this enemy will remove it from the level
     */
    private boolean _disappearOnTouch      = false;

    /**
     * When an enemy is defeated, this is the code sequence we run to figure out how gameplay should change.
     * 
     * @param increaseScore
     *            Indicate if we should increase the score when this enemy is defeated
     */
    public void defeat(boolean increaseScore)
    {
        // remove the enemy from the screen
        remove(false);

        // possibly update score
        if (increaseScore)
            Level._currLevel._score.onDefeatEnemy();

        // handle defeat triggers
        if (_isTrigger)
            LOL._game.onEnemyDefeatTrigger(_defeatTriggerID, LOL._game._currLevel, this);
    }

    /**
     * Indicate that this enemy can be defeated by crawling into it
     */
    public void setDefeatByCrawl()
    {
        _defeatByCrawl = true;
        // make the enemy's _physics body a sensor to prevent ricochets when the hero defeats this
        _physBody.getFixtureList().get(0).setSensor(true);
    }

    /**
     * Make this enemy resist invincibility
     */
    public void setResistInvincibility()
    {
        _immuneToInvincibility = true;
    }

    /**
     * Make this enemy _damage the hero even when the hero is invincible
     */
    public void setImmuneToInvincibility()
    {
        _alwaysDoesDamage = true;
    }

    /**
     * Indicate that if the player touches this enemy, the enemy will be removed from the game
     */
    public void setDisappearOnTouch()
    {
        _disappearOnTouch = true;
    }

    /*
     * CALLBACK SUPPORT
     */

    /**
     * An ID for each enemy who has a callback that runs upon defeat
     */
    private int _defeatTriggerID;

    /**
     * Track if defeating this enemy should cause special code to run
     */
    boolean     _isTrigger;

    /**
     * Make the enemy a "defeat trigger" enemy, so that custom code will run when this enemy is defeated
     * 
     * @param id
     *            The id of this enemy, so that we can disambiguate enemy collisions in the onEnemyTrigger code
     */
    public void setDefeatTrigger(int id)
    {
        _defeatTriggerID = id;
        _isTrigger = true;
    }

    /**
     * Mark this enemy as no longer being a defeat trigger enemy
     */
    public void clearDefeatTrigger()
    {
        _isTrigger = false;
    }

    /*
     * COLLISION SUPPORT
     */

    /**
     * Collision behavior of enemies. Based on our PhysicsSprite numbering scheme, the only concerns are to ensure that
     * when a bullet hits this enemy, we remove the enemy and hide the bullet, and to handle collisions with SubClass
     * obstacles
     * 
     * @param other
     *            The other entity involved in the collision
     */
    @Override
    void onCollide(PhysicsSprite other, Contact contact)
    {
        // collision with obstacles
        if (other._psType == SpriteId.OBSTACLE)
            onCollideWithObstacle((Obstacle) other);
        // collision with projectiles
        if (other._psType == SpriteId.PROJECTILE)
            onCollideWithProjectile((Projectile) other);
    }

    /**
     * Dispatch method for handling Enemy collisions with Obstacles
     * 
     * @param o
     *            The obstacle with which this Enemy collided
     */
    private void onCollideWithObstacle(final Obstacle o)
    {
        // is there a callback when this obstacle collides with enemies?
        boolean match = true;
        for (int i = 0; i < 4; ++i)
            match &= o._enemyTriggerActivation[i] <= Level._currLevel._score._goodiesCollected[i];
        if (o._isEnemyCollideTrigger && match) {
            // run the callback after a delay, or immediately?
            if (o._enemyCollideTriggerDelay > 0) {
                final Enemy e = this;
                Timer.schedule(new Task()
                {
                    @Override
                    public void run()
                    {
                        LOL._game.onEnemyCollideTrigger(o._enemyTriggerID, LOL._game._currLevel, o, e);
                    }
                }, o._enemyCollideTriggerDelay);
            }
            else {
                LOL._game.onEnemyCollideTrigger(o._enemyTriggerID, LOL._game._currLevel, o, this);
            }
        }

        // handle obstacles that make the enemy jump.
        if (o._isEnemyJump) {
            Vector2 v = _physBody.getLinearVelocity();
            v.y += o._enemyYJumpImpulse;
            v.x += o._enemyXJumpImpulse;
            updateVelocity(v);
        }
    }

    /**
     * Dispatch method for handling Enemy collisions with projectiles
     * 
     * @param p
     *            The projectile with which this hero collided
     */
    private void onCollideWithProjectile(Projectile p)
    {
        // only work with active projectiles
        if (!p._visible)
            return;
        // compute damage to determine if the enemy is defeated
        _damage -= Projectile._strength;
        if (_damage <= 0) {
            // hide the projectile quietly, so that the sound of the enemy can be heard
            p.remove(true);
            // remove this enemy
            defeat(true);
        }
        else {
            // hide the projectile
            p.remove(false);
        }
    }

    /*
     * INTERNAL FUNCTIONALITY
     */

    /**
     * Whenever an Enemy is touched, this code runs automatically.
     * 
     * @param e
     *            Nature of the touch (down, up, etc)
     * @param x
     *            X position of the touch
     * @param y
     *            Y position of the touch
     */
    @Override
    void handleTouchDown()
    {
        if (_disappearOnTouch) {
            // if (Configuration.isVibrationOn())
            // ALE._self.getEngine().vibrate(100);
            defeat(true);
            return;
        }
        super.handleTouchDown();
    }
}