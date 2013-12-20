
package edu.lehigh.cse.lol;


import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;

public class Hero extends PhysicsSprite {
    /*
     * BASIC HERO CONFIGURATION
     */

    /**
     * Strength of the hero This determines how many collisions with enemies the
     * hero can sustain before it is defeated. The default is 1, and the default
     * enemy damage amount is 2, so that the default behavior is for the hero to
     * be defeated on any collision with an enemy, with the enemy *not*
     * disappearing
     */
    int _strength = 1;

    /**
     * Time until the hero's invincibility runs out
     */
    private float _invincibleRemaining;

    /**
     * Animation support: cells involved in animation for invincibility
     */
    private Animation _invincibleAnimation;

    /**
     * Is the hero currently in crawl mode?
     */
    private boolean _crawling;

    /**
     * Animation support: cells involved in animation for crawling
     */
    private Animation _crawlAnimation;

    /**
     * Animation support: cells involved in animation for throwing
     */
    private Animation _throwAnimation;

    /**
     * Animation support: seconds that constitute a throw action
     */
    private float _throwAnimateTotalLength;

    /**
     * Animation support: how long until we stop showing the throw animation
     */
    private float _throwAnimationTimeRemaining;

    /**
     * Animation support: an Animation sequence that correlates goodie counts to
     * specific frames of an animation
     */
    private Animation _goodieCountAnimation;

    /**
     * Track if the hero is in the air, so that it can't jump when it isn't
     * touching anything. This does not quite work as desired, but is good
     * enough for LOL
     */
    private boolean _inAir;

    /**
     * When the hero jumps, this specifies the amount of velocity to add to
     * simulate a jump
     */
    private Vector2 _jumpImpulses;

    /**
     * Indicate that the hero can jump while in the air
     */
    private boolean _allowMultiJump;

    /**
     * Does the hero jump when we touch it?
     */
    private boolean _isTouchJump;

    /**
     * Animation support: cells involved in animation for jumping
     */
    private Animation _jumpAnimation;

    /**
     * Sound to play when a jump occurs
     */
    private Sound _jumpSound;

    /**
     * Does the hero only start moving when we touch it?
     */
    private Vector2 _touchAndGo;

    /**
     * For tracking the current amount of rotation of the hero
     */
    private float _currentRotation;

    /**
     * Construct a Hero by creating a PhysicsSprite and incrementing the number
     * of heroes created.
     * 
     * @param width The width of the hero
     * @param height The height of the hero
     * @param imgName The name of the file that has the default image for this
     *            hero
     */
    Hero(float width, float height, String imgName) {
        super(imgName, SpriteId.HERO, width, height);
        Level._currLevel._score._heroesCreated++;
    }

    /*
     * INTERNAL INTERFACE: NON-COLLISION EVENTS
     */

    /**
     * We can't just use the basic PhysicsSprite renderer, because we might need
     * to adjust a one-off animation (invincibility or throw) first
     * 
     * @param sb The SpriteBatch to use for drawing this hero
     * @param delta The time since the last render
     */
    @Override
    public void render(SpriteBatch sb, float delta) {
        // determine when to turn off throw animations
        if (_throwAnimationTimeRemaining > 0) {
            _throwAnimationTimeRemaining -= delta;
            if (_throwAnimationTimeRemaining <= 0) {
                _throwAnimationTimeRemaining = 0;
                _animator.setCurrentAnimation(_defaultAnimation);
            }
        }

        // determine when to turn off invincibility. When we turn it off, if we
        // had an invincibility animation, turn it
        // off too
        if (_invincibleRemaining > 0) {
            _invincibleRemaining -= delta;
            if (_invincibleRemaining <= 0) {
                _invincibleRemaining = 0;
                if (_invincibleAnimation != null)
                    _animator.setCurrentAnimation(_defaultAnimation);
            }
        }

        super.render(sb, delta);
    }

    /**
     * Make the hero jump, unless it is in the air and not multijump
     */
    void jump() {
        if (_inAir)
            return;
        Vector2 v = _physBody.getLinearVelocity();
        v.add(_jumpImpulses);
        updateVelocity(v.x, v.y);
        if (!_allowMultiJump)
            _inAir = true;
        if (_jumpAnimation != null)
            _animator.setCurrentAnimation(_jumpAnimation);
        if (_jumpSound != null)
            _jumpSound.play();
        _stickyDelay = System.nanoTime() + 10000000;
    }

    /**
     * Stop the jump animation for a hero, and make it eligible to jump again
     */
    void stopJump() {
        if (_inAir || _allowMultiJump) {
            _inAir = false;
            _animator.setCurrentAnimation(_defaultAnimation);
        }
    }

    /**
     * When the hero is touched, we might need to take action. If so, we use
     * this to detemrine what to do.
     */
    @Override
    void handleTouchDown(float x, float y) {
        // if the hero is touch-to-jump, then try to jump
        if (_isTouchJump) {
            jump();
            return;
        }
        // if the hero is touch and go, make the hero start moving
        if (_touchAndGo != null) {
            _hoverVector = null;
            if (_physBody.getType() != BodyType.DynamicBody)
                _physBody.setType(BodyType.DynamicBody); // in case hero is
                                                         // hovering
            setAbsoluteVelocity(_touchAndGo.x, _touchAndGo.y, false);
            // turn off _isTouchAndGo, so we can't double-touch
            _touchAndGo = null;
            return;
        }
        super.handleTouchDown(x, y);
    }

    /**
     * Internal method to make the hero's throw animation play while it is
     * throwing a projectile
     */
    void doThrowAnimation() {
        if (_throwAnimation != null) {
            _animator.setCurrentAnimation(_throwAnimation);
            _throwAnimationTimeRemaining = _throwAnimateTotalLength;
        }
    }

    /**
     * Put the hero in crawl mode. Note that we make the hero rotate when it is
     * crawling
     */
    void crawlOn() {
        _crawling = true;
        _physBody.setTransform(_physBody.getPosition(), -3.14159f / 2);
        if (_crawlAnimation != null)
            _animator.setCurrentAnimation(_crawlAnimation);
    }

    /**
     * Take the hero out of crawl mode
     */
    void crawlOff() {
        _crawling = false;
        _physBody.setTransform(_physBody.getPosition(), 0);
        _animator.setCurrentAnimation(_defaultAnimation);
    }

    /**
     * Change the rotation of the hero
     * 
     * @param delta How much to add to the current rotation
     */
    void increaseRotation(float delta) {
        if (_inAir) {
            _currentRotation += delta;
            _physBody.setAngularVelocity(0);
            _physBody.setTransform(_physBody.getPosition(), _currentRotation);
        }
    }

    /*
     * INTERNAL INTERFACE: COLLISION DETECTION
     */

    /**
     * The Hero is the dominant participant in all collisions. Whenever the hero
     * collides with something, we need to figure out what to do
     * 
     * @param other Other object involved in this collision
     * @param contact A description of the contact that caused this collision
     */
    @Override
    void onCollide(PhysicsSprite other, Contact contact) {
        // NB: we currently ignore (other._psType == SpriteId.PROJECTILE)
        if (other._psType == SpriteId.ENEMY)
            onCollideWithEnemy((Enemy)other);
        else if (other._psType == SpriteId.DESTINATION)
            onCollideWithDestination((Destination)other);
        else if (other._psType == SpriteId.OBSTACLE)
            onCollideWithObstacle((Obstacle)other, contact);
        else if (other._psType == SpriteId.SVG)
            onCollideWithSVG(other);
        else if (other._psType == SpriteId.GOODIE)
            onCollideWithGoodie((Goodie)other);
    }

    /**
     * Dispatch method for handling Hero collisions with Destinations
     * 
     * @param d The destination with which this hero collided
     */
    private void onCollideWithDestination(Destination d) {
        // only do something if the hero has enough goodies of each type and
        // there's room in the destination
        boolean match = true;
        for (int i = 0; i < 4; ++i)
            match &= Level._currLevel._score._goodiesCollected[i] >= d._activationScore[i];
        if (match && (d._holding < d._capacity) && _visible) {
            // hide the hero quietly, since the destination might make a sound
            remove(true);
            d._holding++;
            if (d._arrivalSound != null)
                d._arrivalSound.play();
            Level._currLevel._score.onDestinationArrive();
        }
    }

    /**
     * Dispatch method for handling Hero collisions with Enemies
     * 
     * @param e The enemy with which this hero collided
     */
    private void onCollideWithEnemy(Enemy e) {
        // if the enemy always defeats the hero, no matter what, then defeat the
        // hero
        if (e._alwaysDoesDamage) {
            remove(false);
            Level._currLevel._score.defeatHero(e);
            return;
        }
        // handle hero invincibility
        if (_invincibleRemaining > 0) {
            // if the enemy is immune to invincibility, do nothing
            if (e._immuneToInvincibility)
                return;
            e.defeat(true);
        }
        // defeat by _crawling?
        else if (_crawling && e._defeatByCrawl) {
            e.defeat(true);
        }
        // when we can't defeat it by losing strength, remove the hero
        else if (e._damage >= _strength) {
            remove(false);
            Level._currLevel._score.defeatHero(e);
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
     * @param o The obstacle with which this hero collided
     */
    private void onCollideWithObstacle(Obstacle o, Contact contact) {
        // do we need to play a sound?
        o.playCollideSound();

        // reset rotation of hero if this obstacle is not a sensor
        if ((_currentRotation != 0) && !o._physBody.getFixtureList().get(0).isSensor())
            increaseRotation(-_currentRotation);

        // if there is code attached to the obstacle for modifying the hero's
        // behavior, run it
        if (o._heroCollision != null)
            o._heroCollision.go(this, contact);

        // If this is a wall, then mark us not in the air so we can do more
        // jumps. Note that sensors should not enable
        // jumps for the hero.
        if ((_inAir || _allowMultiJump) && !o._physBody.getFixtureList().get(0).isSensor()
                && !o._noJumpReenable)
            stopJump();
    }

    /**
     * Dispatch method for handling Hero collisions with SVG lines
     * 
     * @param s The svg line with which this hero collided
     */
    private void onCollideWithSVG(PhysicsSprite s) {
        // all we do is record that the hero is not in the air anymore, and is
        // not in a jump animation anymore
        stopJump();
    }

    /**
     * Dispatch method for handling Hero collisions with Goodies
     * 
     * @param g The goodie with which this hero collided
     */
    private void onCollideWithGoodie(Goodie g) {
        // hide the goodie
        g.remove(false);

        // count this goodie
        Level._currLevel._score.onGoodieCollected(g);

        // update strength if the goodie is a strength booster
        _strength += g._strengthBoost;

        // deal with invincibility
        if (g._invincibilityDuration > 0) {
            // update the time to end invincibility
            _invincibleRemaining += g._invincibilityDuration;
            // invincible animation
            if (_invincibleAnimation != null)
                _animator.setCurrentAnimation(_invincibleAnimation);
        }

        // deal with animation changes due to goodie count
        if (_goodieCountAnimation != null) {
            int goodies = Level._currLevel._score._goodiesCollected[0];
            for (int i = 0; i < _goodieCountAnimation._nextCell; ++i) {
                if (_goodieCountAnimation._durations[i] == goodies) {
                    _animator.setIndex(_goodieCountAnimation._frames[i]);
                    break;
                }
            }
        }
    }

    /*
     * PUBLIC INTERFACE
     */

    /**
     * Make a Hero with an underlying rectangular shape
     * 
     * @param x X coordinate of the hero
     * @param y Y coordinate of the hero
     * @param width width of the hero
     * @param height height of the hero
     * @param imgName File name of the default image to display
     * @return The hero that was created
     */
    public static Hero makeAsBox(float x, float y, float width, float height, String imgName) {
        Hero h = new Hero(width, height, imgName);
        h.setBoxPhysics(0, 0, 0, BodyType.DynamicBody, false, x, y);
        Level._currLevel.addSprite(h, 0);
        return h;
    }

    /**
     * Make a Hero with an underlying circular shape
     * 
     * @param x X coordinate of the hero
     * @param y Y coordinate of the hero
     * @param width width of the hero
     * @param height height of the hero
     * @param imgName File name of the default image to display
     * @return The hero that was created
     */
    public static Hero makeAsCircle(float x, float y, float width, float height, String imgName) {
        float radius = (width > height) ? width : height;
        Hero h = new Hero(width, height, imgName);
        h.setCirclePhysics(0, 0, 0, BodyType.DynamicBody, false, x, y, radius / 2);
        Level._currLevel.addSprite(h, 0);
        return h;
    }

    /**
     * Give the hero more strength than the default, so it can survive more
     * collisions with enemies
     * 
     * @param amount The new strength of the hero
     */
    public void setStrength(int amount) {
        _strength = amount;
    }

    /**
     * Indicate that upon a touch, this hero should begin moving with a specific
     * velocity
     * 
     * @param x Velocity in X dimension
     * @param y Velocity in Y dimension
     */
    public void setTouchAndGo(float x, float y) {
        _touchAndGo = new Vector2(x, y);
    }

    /**
     * Specify the X and Y force to apply to the hero whenever it is instructed
     * to jump
     * 
     * @param x Force in X direction
     * @param y Force in Y direction
     */
    public void setJumpImpulses(float x, float y) {
        _jumpImpulses = new Vector2(x, y);
    }

    /**
     * Indicate that this hero can jump while it is in the air
     */
    public void setMultiJumpOn() {
        _allowMultiJump = true;
    }

    /**
     * Indicate that touching this hero should make it jump
     */

    public void setTouchToJump() {
        _isTouchJump = true;
    }

    /**
     * Register an animation sequence, so that this hero can have a custom
     * animation while jumping
     * 
     * @param a The animation to display
     */
    public void setJumpAnimation(Animation a) {
        _jumpAnimation = a;
    }

    /**
     * Set the sound to play when a jump occurs
     * 
     * @param soundName The name of the sound file to use
     */
    public void setJumpSound(String soundName) {
        _jumpSound = Media.getSound(soundName);
    }

    /**
     * Register an animation sequence, so that this hero can have a custom
     * animation while throwing
     * 
     * @param a The animation to display
     */
    public void setThrowAnimation(Animation a) {
        _throwAnimation = a;
        // compute the length of the throw sequence, so that we can get our
        // timer right for restoring the default animation
        _throwAnimateTotalLength = 0;
        for (long l : a._durations)
            _throwAnimateTotalLength += l;
        _throwAnimateTotalLength /= 1000; // convert to seconds
    }

    /**
     * Register an animation sequence, so that this hero can have a custom
     * animation while crawling
     * 
     * @param a The animation to display
     */
    public void setCrawlAnimation(Animation a) {
        _crawlAnimation = a;
    }

    /**
     * Register an animation sequence, so that this hero can have a custom
     * animation while invincible
     * 
     * @param a The animation to display
     */
    public void setInvincibleAnimation(Animation a) {
        _invincibleAnimation = a;
    }

    /**
     * Indicate that this hero should change its animation cell depending on how
     * many (type-1) goodies have been collected
     * 
     * @param a An animation that encodes the information we want to display
     */
    public void setAnimateByGoodieCount(Animation a) {
        _goodieCountAnimation = a;
    }
}
