package edu.lehigh.cse.lol;

// TODO: clean up comments

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;

public class Destination extends PhysicsSprite
{

    /*
     * BASIC FUNCTIONALITY
     */

    /**
     * number of _heroes who can fit at /this/ destination
     */
    int _capacity;

    /**
     * number of _heroes already in /this/ destination
     */
    int _holding;

    /**
     * number of type 1 goodies that must be collected before this destination
     * accepts any heroes
     */
    int _activationScore1;

    /**
     * number of type 2 goodies that must be collected before this destination
     * accepts any heroes
     */
    int _activationScore2;

    /**
     * number of type 3 goodies that must be collected before this destination
     * accepts any heroes
     */
    int _activationScore3;

    /**
     * number of type 4 goodies that must be collected before this destination
     * accepts any heroes
     */
    int _activationScore4;

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
    private Destination(float width, float height, String imgName)
    {
        super(imgName, SpriteId.DESTINATION, width, height);
        _capacity = 1;
        _holding = 0;
        _activationScore1 = 0;
        _activationScore2 = 0;
        _activationScore3 = 0;
        _activationScore4 = 0;
    }

    public static Destination makeAsBox(float x, float y, float width, float height, String imgName)
    {
        Destination d = new Destination(width, height, imgName);
        d.setBoxPhysics(0, 0, 0, BodyType.StaticBody, false, x, y);
        d._physBody.getFixtureList().get(0).setSensor(true);
        Level._currLevel._sprites.add(d);
        return d;
    }

    public static Destination makeAsCircle(float x, float y, float width, float height, String imgName)
    {
        float radius = (width > height) ? width : height;
        Destination d = new Destination(radius, radius, imgName);
        d.setCirclePhysics(0, 0, 0, BodyType.StaticBody, false, x, y, radius / 2);
        d._physBody.getFixtureList().get(0).setSensor(true);
        Level._currLevel._sprites.add(d);
        return d;
    }

    /**
     * Change the number of goodies that must be collected before the
     * destination accepts any heroes (the default is 0)
     * 
     * @param score
     *            The number of goodies that must be collected.
     */
    public void setActivationScore(int score1, int score2, int score3, int score4)
    {
        _activationScore1 = score1;
        _activationScore2 = score2;
        _activationScore3 = score3;
        _activationScore4 = score4;
    }

    /**
     * Change the number of _heroes that can be accepted by this destination
     * (the default is 1)
     * 
     * @param _heroes
     *            The number of _heroes that can be accepted
     */
    public void setHeroCount(int heroes)
    {
        _capacity = heroes;
    }

    /*
     * COLLISION SUPPORT
     */

    /**
     * Destinations are the last collision detection entity, so their collision
     * detection code does nothing.
     * 
     * @param other
     *            Other object involved in this collision
     */
    void onCollide(PhysicsSprite other, Contact contact)
    {
    }

    /*
     * AUDIO SUPPORT
     */

    /**
     * Sound to play when a hero arrives at this destination
     */
    Sound _arrivalSound;

    /**
     * Specify the sound to play when a hero arrives at this destination
     * 
     * @param sound
     *            The sound file name that should play
     */
    public void setArrivalSound(String soundName)
    {
        _arrivalSound = Media.getSound(soundName);
    }
}
