package edu.lehigh.cse.ale;

// STATUS: IN PROGRESS

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

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
        Score._enemiesCreated++;
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
        e.setCirclePhysics(0, 0, 0, BodyType.StaticBody, false, x, y, radius/2);
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
                Score.onDefeatEnemy();

            // handle defeat triggers
            if (_isTrigger)
                ALE._game.onEnemyDefeatTrigger(_defeatTriggerID, ALE._game._currLevel, this);
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
            // TODO
            //Level._current.registerTouchArea(_sprite);
            //Level._current.setTouchAreaBindingOnActionDownEnabled(true);
            //Level._current.setTouchAreaBindingOnActionMoveEnabled(true);
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
        void onCollide(PhysicsSprite other)
        {
            // collision with obstacles
            if (other._psType == SpriteId.OBSTACLE)
                onCollideWithObstacle((Obstacle) other);
            // collision with projectiles
            if (other._psType == SpriteId.PROJECTILE)
                onCollideWithProjectile((Projectile) other);

            // one last thing: if the enemy was "norotate", then patch up any rotation that happened to its _physics body by
            // mistake:
            
            // TODO: I don't think we need this anymore...
            //if (!_canRotate)
            //    _physBody.setTransform(_physBody.getPosition(), 0);
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
            if (o._isEnemyCollideTrigger 
                    && (o._enemyTriggerActivation1 <= Score._goodiesCollected1)
                    && (o._enemyTriggerActivation2 <= Score._goodiesCollected2)
                    && (o._enemyTriggerActivation3 <= Score._goodiesCollected3)
                    && (o._enemyTriggerActivation4 <= Score._goodiesCollected4)) 
            {
                // run the callback after a delay, or immediately?
                if (o._enemyCollideTriggerDelay > 0) {
                    // TODO
                    /*
                    final Enemy e = this;
                    TimerHandler t = new TimerHandler(o._enemyCollideTriggerDelay, false, new ITimerCallback()
                    {
                        @Override
                        public void onTimePassed(TimerHandler th)
                        {
                            ALE._self.onEnemyCollideTrigger(o._enemyTriggerID, MenuManager._currLevel, o, e);
                        }
                    });
                    Level._current.registerUpdateHandler(t);
                    */
                }
                else {
                    ALE._game.onEnemyCollideTrigger(o._enemyTriggerID, ALE._game._currLevel, o, this);
                }
            }

            // handle obstacles that make the enemy jump.
            if (o._isEnemyJump) {
                Vector2 v = _physBody.getLinearVelocity();
                v.y += o._enemyYJumpImpulse;
                v.x += o._enemyXJumpImpulse;
                // TODO
                // updateVelocity(v);
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
         * Game code should not call this directly. Its purpose is to support internal advanced features of the Enemy class
         */
        protected void onSpriteManagedUpdate()
        {
            // TODO
            /*
            // early exit if not a chase enemy...
            if (_chaseMultiplier == 0) {
                super.onSpriteManagedUpdate();
                return;
            }

            // early exit if enemy not visible
            if (!_sprite.isVisible()) {
                super.onSpriteManagedUpdate();
                return;
            }

            // get distance to hero, but exit if the hero has been removed from the
            // system
            Hero toChase = Level._lastHero;
            if (toChase == null) {
                super.onSpriteManagedUpdate();
                return;
            }

            // Don't run this too frequently...
            float now = ALE._self.getEngine().getSecondsElapsedTotal();
            if (now < _lastOSMU + 0.25) {
                super.onSpriteManagedUpdate();
                return;
            }
            _lastOSMU = now;

            // compute vector between hero and enemy
            _chaseVector.x = toChase._physBody.getPosition().x - _physBody.getPosition().x;
            _chaseVector.y = toChase._physBody.getPosition().y - _physBody.getPosition().y;

            // normalize it and then multiply by speed
            float len = FloatMath.sqrt(_chaseVector.x * _chaseVector.x + _chaseVector.y * _chaseVector.y);
            _chaseVector.x *= (_chaseMultiplier / len);

            // disable y position chasing for sidescrolling games.
            _chaseVector.y *= (_chaseMultiplier / len);

            // set Enemy velocity accordingly
            updateVelocity(_chaseVector);

            // dispatch to superclass
            super.onSpriteManagedUpdate();
            */
        }

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
        // TODO:
        /*
        @Override
        protected boolean onSpriteAreaTouched(TouchEvent e, float x, float y)
        {
            // if the enemy is supposed to disappear when we touch it, then hide it right here
            if (_disappearOnTouch) {
                if (Configuration.isVibrationOn())
                    ALE._self.getEngine().vibrate(100);
                defeat(true);
            }
            return super.onSpriteAreaTouched(e, x, y);
        }
        */
    }
