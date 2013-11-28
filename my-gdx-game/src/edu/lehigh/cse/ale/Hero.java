package edu.lehigh.cse.ale;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.joints.WeldJoint;

public class Hero extends PhysicsSprite
{

    Hero(TextureRegion tr, float width, float height)
    {
        super(tr, SpriteId.HERO, width, height);
        Score._heroesCreated++;
    }

    public static Hero makeAsBox(float x, float y, float width, float height, String imgName)
    {
        TextureRegion tr = Media.getImage(imgName);
        Hero h = new Hero(tr, width, height);
        h.setBoxPhysics(0, 0, 0, BodyType.DynamicBody, false, x, y);
        Level._current._sprites.add(h);
        return h;
    }

    public static Hero makeAsCircle(float x, float y, float width, float height, String imgName)
    {
        TextureRegion tr = Media.getImage(imgName);
        float radius = (width > height) ? width : height;
        Hero h = new Hero(tr, radius * 2, radius * 2);
        h.setCirclePhysics(0, 0, 0, BodyType.DynamicBody, false, x, y, radius);
        Level._current._sprites.add(h);
        return h;
    }

    @Override
    void onCollide(PhysicsSprite other)
    {
        // NB: we currently ignore (other._psType == SpriteId.PROJECTILE)
        if (other._psType == SpriteId.ENEMY)
         onCollideWithEnemy((Enemy) other);
        else
        if (other._psType == SpriteId.DESTINATION)
            onCollideWithDestination((Destination) other);
        else if (other._psType == SpriteId.OBSTACLE)
            onCollideWithObstacle((Obstacle) other);
         else if (other._psType == SpriteId.SVG)
         onCollideWithSVG(other);
         else if (other._psType == SpriteId.GOODIE)
             onCollideWithGoodie((Goodie) other);

        // one last thing: if the hero was "norotate", then patch up any
        // rotation that happened to its _physics body by
        // mistake:
        // TODO: do we still need this?
        // if (!_canRotate)
        // _physBody.setTransform(_physBody.getPosition(), 0);
    }

    /**
     * Dispatch method for handling Hero collisions with Destinations
     * 
     * @param d
     *            The destination with which this hero collided
     */
    private void onCollideWithDestination(Destination d)
    {
        // only do something if the hero has enough goodies of each type and
        // there's room in the destination
        if ((Score._goodiesCollected1 >= d._activationScore1) && (Score._goodiesCollected2 >= d._activationScore2)
                && (Score._goodiesCollected3 >= d._activationScore3)
                && (Score._goodiesCollected4 >= d._activationScore4) && (d._holding < d._capacity) && _visible)
        {
            // hide the hero quietly, since the destination might make a sound
            remove(true);
            d._holding++;
            if (d._arrivalSound != null)
                d._arrivalSound.play();
            Score.onDestinationArrive();
        }
    }

    /**
     * Dispatch method for handling Hero collisions with Enemies
     * 
     * @param e
     *            The enemy with which this hero collided
     */
    private void onCollideWithEnemy(Enemy e)
    {
        // if the enemy always defeats the hero, no matter what, then defeat the hero
        if (e._alwaysDoesDamage) {
            remove(false);
            Score.defeatHero(e);
            return;
        }
        // TODO
        /*
        // handle hero invincibility
        if (_invincibleUntil > ALE._self.getEngine().getSecondsElapsedTotal()) {
            // if the enemy is immune to invincibility, do nothing
            if (e._immuneToInvincibility)
                return;
            e.defeat(true);
        }
        */
        // defeat by _crawling?
        // else 
        if (_crawling && e._defeatByCrawl) {
            e.defeat(true);
        }
        // when we can't defeat it by losing strength, remove the hero
        else if (e._damage >= _strength) {
            remove(false);
            Score.defeatHero(e);
        }
        // when we can defeat it by losing strength
        else {
            _strength -= e._damage;
            e.defeat(true);
        }
    }

    /**
     * Dispatch method for handling Hero collisions with Obstacles
     * 
     * @param o
     *            The obstacle with which this hero collided
     */
    private void onCollideWithObstacle(Obstacle o)
    {
        // do we need to play a sound?
        o.playCollideSound();

        // TODO
        // reset rotation of hero if this obstacle is not a sensor
        //if ((_currentRotation != 0) && !o._physBody.getFixtureList().get(0).isSensor())
        //    increaseRotation(-_currentRotation);

        // Handle callback if this obstacle is a trigger
        if (o._isHeroCollideTrigger) {
            // check if trigger is activated, if so, disable it and run code
            if ((o._heroTriggerActivation1 <= Score._goodiesCollected1)
                    && (o._heroTriggerActivation2 <= Score._goodiesCollected2)
                    && (o._heroTriggerActivation3 <= Score._goodiesCollected3)
                    && (o._heroTriggerActivation4 <= Score._goodiesCollected4)) 
            {
                // TODO:
                /*
                ALE._self.onHeroCollideTrigger(o._heroTriggerID, MenuManager._currLevel, o,
                        this);
                return;
                */
            }
        }

        // damp obstacles change the hero _physics by causing slowdown/speedup
        if (o._isDamp) {
            Vector2 v = _physBody.getLinearVelocity();
            v.x *= o._dampFactor;
            v.y *= o._dampFactor;
            updateVelocity(v);
        }

        // speed boost obstacles also change the hero speed, but can be put on a timer
        if (o._isSpeedBoost) {
            // boost the speed
            Vector2 v = _physBody.getLinearVelocity();
            v.x += o._speedBoostX;
            v.y += o._speedBoostY;
            updateVelocity(v);
            // now set a timer to un-boost the speed
            if (o._speedBoostDuration > 0) {
                final Obstacle oo = o;
                // set up a timer to shut off the boost
                // TODO:
                /*
                TimerHandler t = new TimerHandler(o._speedBoostDuration, false, new ITimerCallback()
                {
                    @Override
                    public void onTimePassed(TimerHandler th)
                    {
                        Vector2 v = _physBody.getLinearVelocity();
                        v.x -= oo._speedBoostX;
                        v.y -= oo._speedBoostY;
                        updateVelocity(v);
                    }
                });
                Level._current.registerUpdateHandler(t);
                */
            }
        }

        // If this is a wall, then mark us not in the air so we can do more jumps. Note that sensors should not enable
        // jumps for the hero.
        // TODO:
        /*
        if ((_inAir || _allowMultiJump) && !o._physBody.getFixtureList().get(0).isSensor() && !o._noJumpReenable)
            stopJump();
    */
        
        // handle sticky obstacles
        if (o.isSticky) {
            // TODO:
            /*
            WeldJointDef w = new WeldJointDef();
            w.initialize(o._physBody, this._physBody, o._physBody.getWorldCenter());
            _weldJoint = (WeldJoint) Level._physics.createJoint(w);
            */
        }
    }

    /**
     * Dispatch method for handling Hero collisions with SVG lines
     * 
     * @param s
     *            The svg line with which this hero collided
     */
    private void onCollideWithSVG(PhysicsSprite s)
    {
        // all we do is record that the hero is not in the air anymore, and is
        // not in a jump animation anymore
        // TODO:
        // stopJump();
    }

    /**
     * Dispatch method for handling Hero collisions with Goodies
     * 
     * @param g
     *            The goodie with which this hero collided
     */
    private void onCollideWithGoodie(Goodie g)
    {
        // hide the goodie
        g.remove(false);

        // count this goodie
        Score.onGoodieCollected(g);

        // update _strength if the goodie is a _strength booster
        _strength += g._strengthBoost;

        // deal with invincibility
        // TODO:
        /*
        if (g._invincibilityDuration > 0) {
            float newExpire = ALE._self.getEngine().getSecondsElapsedTotal() + g._invincibilityDuration;
            if (newExpire > _invincibleUntil)
                _invincibleUntil = newExpire;
            if (_invincibleAnimateDurations != null) {
                _sprite.animate(_invincibleAnimateDurations, _invincibleAnimateCells, true);
                _glowing = true;
            }
        }
        */

        // deal with animation changes due to goodie count
        // TODO:
        /*
        if (_isAnimateByGoodieCount) {
            int goodies = Score._goodiesCollected1;
            for (int i = 0; i < _animateByGoodieCountCounts.length; ++i) {
                if (_animateByGoodieCountCounts[i] == goodies) {
                    _sprite.setCurrentTileIndex(_animateByGoodieCountCells[i]);
                    break;
                }
            }
        }
        */
    }

    
    
    
    
    /*
     * BASIC FUNCTIONALITY
     */

    /**
     * Strength of the hero
     * 
     * This determines how many collisions with enemies the hero can sustain before it is defeated. The default is 1,
     * and the default enemy _damage amount is 2, so that the default behavior is for the hero to be defeated on any
     * collision with an enemy
     */
    int _strength = 1;

    /**
     * Give the hero more _strength than the default, so it can survive more collisions with enemies
     * 
     * @param amount
     *            The new _strength of the hero
     */
    public void setStrength(int amount)
    {
        _strength = amount;
    }

    /*
     * DELAYED MOTION SUPPORT
     */

    /**
     * Does the hero only start moving when we touch it?
     */
    private boolean _isTouchAndGo     = false;

    /**
     * Velocity in X dimension for this hero when it is touched
     */
    private int     _xVelocityTouchGo = 0;

    /**
     * Velocity in Y dimension for this hero when it is touched
     */
    private int     _yVelocityTouchGo = 0;

    /**
     * Indicate that upon a touch, this hero should begin moving with a specific velocity
     * 
     * @param x
     *            Velocity in X dimension
     * @param y
     *            Velocity in Y dimension
     */
    public void setTouchAndGo(int x, int y)
    {
        _isTouchAndGo = true;
        _xVelocityTouchGo = x;
        _yVelocityTouchGo = y;
        // TODO: // Level._current.registerTouchArea(_sprite);
    }

    /*
     * JUMP SUPPORT
     */

    /**
     * Track if the hero is in the air, so that it can't jump when it isn't touching anything.
     * 
     * This does not quite work as desired, but is good enough for our demo
     */
    private boolean _inAir        = false;

    /**
     * When the hero jumps, this specifies the amount of jump impulse in the X dimension
     */
    private int     _xJumpImpulse = 0;

    /**
     * When the hero jumps, this specifies the amount of jump impulse in the Y dimension
     */
    private int     _yJumpImpulse = 0;

    /**
     * Indicate that the hero can jump while in the air
     */
    private boolean _allowMultiJump;

    /**
     * Does the hero jump when we touch it?
     */
    private boolean _isTouchJump  = false;

    /**
     * Animation support: cells involved in animation for jumping
     */
    private int[]   _jumpAnimateCells;

    /**
     * Animation support: durations for jumping animation
     */
    private long[]  _jumpAnimateDurations;

    /**
     * Sound to play when a jump occurs
     */
    // private Sound   _jumpSound;

    /**
     * Make the hero jump, unless it is in the air
     */
    /*
    void jump()
    {
        if (_weldJoint != null) {
            Level._physics.destroyJoint(_weldJoint);
            _weldJoint = null;
        }
        if (_inAir)
            return;
        Vector2 v = _physBody.getLinearVelocity();
        v.y += _yJumpImpulse;
        v.x += _xJumpImpulse;
        updateVelocity(v);
        if (!_allowMultiJump)
            _inAir = true;
        if (_jumpAnimateDurations != null)
            _sprite.animate(_jumpAnimateDurations, _jumpAnimateCells, true);
        if (_jumpSound != null)
            _jumpSound.play();
    }

    /**
     * Stop the jump animation for a hero, and make it eligible to jump again
     */
    /*
    void stopJump()
    {
        if (_inAir || _allowMultiJump) {
            _inAir = false;
            if (_defaultAnimateCells != null)
                _sprite.animate(_defaultAnimateDurations, _defaultAnimateCells, true);
            else
                _sprite.stopAnimation(0);
        }
    }

    /**
     * Specify the X and Y force to apply to the hero whenever it is instructed to jump
     * 
     * @param x
     *            Force in X direction
     * @param y
     *            Force in Y direction
     */
    public void setJumpImpulses(int x, int y)
    {
        _xJumpImpulse = x;
        _yJumpImpulse = y;
    }

    /**
     * Indicate that this hero can jump while it is in the air
     */
    public void setMultiJumpOn()
    {
        _allowMultiJump = true;
    }

    /**
     * Indicate that touching this hero should make it jump
     */
    /*
    public void setTouchToJump()
    {
        _isTouchJump = true;
        Level._current.registerTouchArea(_sprite);
    }

    /**
     * Register an animation sequence, so that this hero can have a custom animation while jumping
     * 
     * @param cells
     *            Which cells to show
     * @param durations
     *            How long to show each cell
     */
    public void setJumpAnimation(int[] cells, long[] durations)
    {
        _jumpAnimateCells = cells;
        _jumpAnimateDurations = durations;
    }

    /**
     * Set the sound to play when a jump occurs
     * 
     * @param soundName
     *            The name of the sound file to use
     */
    /*
    public void setJumpSound(String soundName)
    {
        _jumpSound = Media.getSound(soundName);
    }

    /*
     * THROW SUPPORT
     */

    /**
     * Does the hero throw a projectile when we touch it?
     */
    private boolean _isTouchThrow = false;

    /**
     * Animation support: cells involved in animation for throwing
     */
    private int[]   _throwAnimateCells;

    /**
     * Animation support: durations for jumping throwing
     */
    private long[]  _throwAnimateDurations;

    /**
     * Animation support: seconds that constitute a throw action
     */
    private float   _throwAnimateTotalLength;

    /**
     * Animation support: how long until we stop showing the throw animation
     */
    private float   _throwingUntil;

    /**
     * Indicate that touching this hero should make it throw a projectile
     */
    /*
    public void setTouchToThrow()
    {
        _isTouchThrow = true;
        Level._current.registerTouchArea(_sprite);
    }

    /**
     * Internal method to make the hero's throw animation play while it is throwing a projectile
     */
    /*
    void doThrowAnimation()
    {
        if (_throwAnimateDurations != null) {
            _sprite.animate(_throwAnimateDurations, _throwAnimateCells, false);
            _throwingUntil = ALE._self.getEngine().getSecondsElapsedTotal() + _throwAnimateTotalLength;
        }
    }

    /**
     * Register an animation sequence, so that this hero can have a custom animation while throwing
     * 
     * @param cells
     *            Which cells to show
     * @param durations
     *            How long to show each cell
     */
    public void setThrowAnimation(int cells[], long durations[])
    {
        _throwAnimateCells = cells;
        _throwAnimateDurations = durations;
        // compute the length of the throw sequence, so that we can get our
        // timer right for restoring the default animation
        _throwAnimateTotalLength = 0;
        for (long l : durations)
            _throwAnimateTotalLength += l;
        _throwAnimateTotalLength /= 1000; // convert to seconds
    }

    /*
     * CRAWL SUPPORT
     */

    /**
     * Is the hero currently in crawl mode?
     */
    private boolean _crawling = false;

    /**
     * Animation support: cells involved in animation for _crawling
     */
    private int[]   _crawlAnimateCells;

    /**
     * Animation support: durations for crawl animation
     */
    private long[]  _crawlAnimateDurations;

    /**
     * Put the hero in crawl mode
     */
    /*
    void crawlOn()
    {
        _crawling = true;
        _physBody.setTransform(_physBody.getPosition(), 3.14159f / 2);
        _sprite.setRotation(90);
        if (_crawlAnimateDurations != null)
            _sprite.animate(_crawlAnimateDurations, _crawlAnimateCells, true);
    }

    /**
     * Take the hero out of crawl mode
     */
    /*
    void crawlOff()
    {
        _crawling = false;
        _physBody.setTransform(_physBody.getPosition(), 0);
        _sprite.setRotation(0);
        if (_defaultAnimateCells != null)
            _sprite.animate(_defaultAnimateDurations, _defaultAnimateCells, true);
        else
            _sprite.stopAnimation(0);
    }

    /**
     * Register an animation sequence, so that this hero can have a custom animation while _crawling
     * 
     * @param cells
     *            Which cells to show
     * @param durations
     *            How long to show each cell
     */
    public void setCrawlAnimation(int[] cells, long[] durations)
    {
        _crawlAnimateCells = cells;
        _crawlAnimateDurations = durations;
    }

    /*
     * INVINCIBILITY SUPPORT
     */

    /**
     * Time when the hero's invincibility runs out
     */
    private float   _invincibleUntil = 0;

    /**
     * Track whether there is a playing invincibility animation right now
     */
    private boolean _glowing         = false;

    /**
     * Animation support: cells involved in animation for invincibility
     */
    private int[]   _invincibleAnimateCells;

    /**
     * Animation support: durations for invincibility animation
     */
    private long[]  _invincibleAnimateDurations;

    /**
     * Register an animation sequence, so that this hero can have a custom animation while invincible
     * 
     * @param cells
     *            Which cells to show
     * @param durations
     *            How long to show each cell
     */
    public void setInvincibleAnimation(int[] cells, long[] durations)
    {
        _invincibleAnimateCells = cells;
        _invincibleAnimateDurations = durations;
    }

    /*
     * MANUAL ROTATION
     */

    /**
     * For tracking the _current amount of rotation of the hero
     */
    private float _currentRotation;

    /**
     * Change the rotation of the hero
     * 
     * @param delta
     *            How much to add to the _current rotation
     */
    /*
    void increaseRotation(float delta)
    {
        _currentRotation += delta;
        _physBody.setAngularVelocity(0);
        _physBody.setTransform(_physBody.getPosition(), _currentRotation);
        _sprite.setRotation(_currentRotation);
    }

    /*
     * CUSTOM ANIMATION VIA GOODIE COUNT
     */

    /**
     * Flag for tracking if we change the animation cell based on the goodie count
     */
    private boolean _isAnimateByGoodieCount       = false;

    /**
     * the goodie counts that correspond to image changes
     */
    private int     _animateByGoodieCountCounts[] = null;

    /**
     * The cell to show for the corresponding goodie count
     */
    private int     _animateByGoodieCountCells[]  = null;

    /**
     * Indicate that this hero should change its animation cell depending on how many (type-1) goodies have been collected
     * 
     * @param counts
     *            An array of the different goodie counts that cause changes in appearance
     * @param cells
     *            An array of the cells of the hero's animation sequence to display. These should correspond to the
     *            entries in counts
     */
    public void setAnimateByGoodieCount(int counts[], int cells[])
    {
        _isAnimateByGoodieCount = true;
        _animateByGoodieCountCounts = counts;
        _animateByGoodieCountCells = cells;
    }

    /*
     * ADVANCED POSITIONING FUNCTIONALITY
     */

    /**
     * A flag to indicate that the hero should rotate to always appear to be facing in the direction it is traveling
     */
    private boolean       _rotateByDirection;

    /**
     * A temporary vector for the _rotateByDirection computation
     */
    private final Vector2 _rotationVector = new Vector2();

    /**
     * Indicate that this hero's rotation should be determined by the direction in which it is traveling
     */
    public void setRotationByDirection()
    {
        _rotateByDirection = true;
    }

    /*
     * ADVANCED CAMERA SUPPORT
     */

    /**
     * When the camera follows the hero without centering the hero, this gives us the difference between the hero and
     * camera
     */
    private Vector2 _cameraOffset = new Vector2(0, 0);

    /**
     * Make the camera follow the hero, but without centering the hero on the screen
     * 
     * @param x
     *            Amount of x distance between hero and center
     * @param y
     *            Amount of y distance between hero and center
     */
    /*
    public void setCameraOffset(float x, float y)
    {
        ALE._self._camera.setChaseEntity(null);
        _cameraOffset.x = x;
        _cameraOffset.y = y;
        _sprite.registerUpdateHandler(new IUpdateHandler()
        {
            @Override
            public void onUpdate(float arg0)
            {
                ALE._self._camera.setCenter(_sprite.getX() + _cameraOffset.x, _sprite.getY() + _cameraOffset.y);
            }

            @Override
            public void reset()
            {
            }
        });
    }

    /*
     * COLLISION SUPPORT
     */

    /**
     * Internal field to track if this hero is connected to an obstacle
     */
    WeldJoint _weldJoint;


    /*
     * INTERNAL FUNCTIONALITY
     */

    /**
     * Code to run when the hero is touched
     * 
     * @param e
     *            The type of touch
     * @param x
     *            X coordinate of the touch
     * @param y
     *            Y coordinate of the touch
     */
    /*
    @Override
    protected boolean onSpriteAreaTouched(TouchEvent e, float x, float y)
    {
        // on a down press of a live-edit object, just run the editor and return
        if (e.isActionDown() && isLiveEdit) {
            ALE.launchLiveEditor(this);
            return true;
        }

        // if this isn't a down press, then don't do anything...
        if (!e.isActionDown())
            return false;
        // jump?
        if (_isTouchJump) {
            jump();
            return true;
        }
        // start moving?
        if (_isTouchAndGo) {
            _hover = false;
            makeMoveable(); // in case hero is hovering
            addVelocity(_xVelocityTouchGo, _yVelocityTouchGo);
            // turn off _isTouchAndGo, so we can't double-touch
            _isTouchAndGo = false;
            return true;
        }
        // throw a projectile?
        if (_isTouchThrow) {
            Projectile.throwFixed(_sprite.getX(), _sprite.getY());
            return true;
        }
        // forward to the PhysicsSprite handler
        return super.onSpriteAreaTouched(e, x, y);
    }

    /**
     * This override ensures that the hero doesn't have 'jitter' when it moves around. It also stops invincibility when
     * the timer expires
     */
    /*
    protected void onSpriteManagedUpdate()
    {
        ALE._self._camera.onUpdate(0.1f);
        float now = ALE._self.getEngine().getSecondsElapsedTotal();
        // handle invincibility animation
        if (_glowing && (_invincibleUntil < now)) {
            if (_defaultAnimateCells != null)
                _sprite.animate(_defaultAnimateDurations, _defaultAnimateCells, true);
            else
                _sprite.stopAnimation(0);
            _glowing = false;
        }

        // determine when to turn off throw animations
        if ((_throwingUntil != 0) && (_throwingUntil < now)) {
            if (_defaultAnimateCells != null)
                _sprite.animate(_defaultAnimateDurations, _defaultAnimateCells, true);
            else
                _sprite.stopAnimation(0);
            _throwingUntil = 0;
        }

        // handle rotating the hero based on the direction it faces
        if (_rotateByDirection) {
            _rotationVector.x = _physBody.getLinearVelocity().x;
            _rotationVector.y = _physBody.getLinearVelocity().y;
            double angle = Math.atan2(_rotationVector.y, _rotationVector.x) - Math.atan2(-1, 0);
            _sprite.setRotation(180 / (3.1415926f) * (float) angle);
        }
        super.onSpriteManagedUpdate();
    }
    */
}
