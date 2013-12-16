package edu.lehigh.cse.lol;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;

/**
 * Destinations are entities that the hero should try to reach. When a hero reaches a destination, the hero disappears,
 * and the score updates.
 */
public class Destination extends PhysicsSprite
{
    /*
     * BASIC CONFIGURATION
     */

    /**
     * number of heroes who can fit at /this/ destination
     */
    int   _capacity;

    /**
     * number of heroes already in /this/ destination
     */
    int   _holding;

    /**
     * number of type each type of goodies that must be collected before this destination
     * accepts any heroes
     */
    int[] _activationScore = new int[4];

    /**
     * Sound to play when a hero arrives at this destination
     */
    Sound _arrivalSound;

    /**
     * Create a destination. This is an internal method, the game designer should not use this.
     * 
     * @param width
     *            Width of this destination
     * @param height
     *            Height of this destination
     * @param imgName
     *            Name of the image to display
     */
    private Destination(float width, float height, String imgName)
    {
        super(imgName, SpriteId.DESTINATION, width, height);
        _capacity = 1;
        _holding = 0;
    }

    /**
     * Destinations are the last collision detection entity, so their collision detection code does nothing.
     * 
     * @param other
     *            Other object involved in this collision
     * @param contact
     *            A description of the collision between this destination and the other entity
     */
    void onCollide(PhysicsSprite other, Contact contact)
    {
    }

    /*
     * PUBLIC INTERFACE
     */

    /**
     * Make a destination that has an underlying rectangular shape.
     * 
     * @param x
     *            The X coordinate of the bottom left corner
     * @param y
     *            The Y coordinate of the bottom right corner
     * @param width
     *            The width of the destination
     * @param height
     *            The height of the destination
     * @param imgName
     *            The name of the image to display
     * @return The destination, so that it can be modified further
     */
    public static Destination makeAsBox(float x, float y, float width, float height, String imgName)
    {
        Destination d = new Destination(width, height, imgName);
        d.setBoxPhysics(0, 0, 0, BodyType.StaticBody, false, x, y);
        d._physBody.getFixtureList().get(0).setSensor(true);
        Level._currLevel._sprites.add(d);
        return d;
    }

    /**
     * Make a destination that has an underlying circular shape.
     * 
     * @param x
     *            The X coordinate of the bottom left corner
     * @param y
     *            The Y coordinate of the bottom right corner
     * @param width
     *            The width of the destination
     * @param height
     *            The height of the destination
     * @param imgName
     *            The name of the image to display
     * @return The destination, so that it can be modified further
     */
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
     * destination accepts any heroes (the default is 0,0,0,0)
     * 
     * @param score1
     *            The number of type-1 goodies that must be collected.
     * @param score2
     *            The number of type-2 goodies that must be collected.
     * @param score3
     *            The number of type-3 goodies that must be collected.
     * @param score4
     *            The number of type-4 goodies that must be collected.
     */
    public void setActivationScore(int score1, int score2, int score3, int score4)
    {
        _activationScore[0] = score1;
        _activationScore[1] = score2;
        _activationScore[2] = score3;
        _activationScore[3] = score4;
    }

    /**
     * Change the number of heroes that can be accepted by this destination
     * (the default is 1)
     * 
     * @param heroes
     *            The number of _heroes that can be accepted
     */
    public void setHeroCount(int heroes)
    {
        _capacity = heroes;
    }

    /**
     * Specify the sound to play when a hero arrives at this destination
     * 
     * @param soundName
     *            The name of the sound file that should play
     */
    public void setArrivalSound(String soundName)
    {
        _arrivalSound = Media.getSound(soundName);
    }
}