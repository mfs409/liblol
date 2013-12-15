package edu.lehigh.cse.lol;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;

/**
 * Goodies are physical entities in a game whose main purpose is for the hero to collect them. Collecting a goodie has
 * three possible consequences: it can lead to the score changing, it can lead to the hero's strength changing, and it
 * can lead to the hero becoming invincible for some time.
 */
public class Goodie extends PhysicsSprite
{
    /**
     * The "score" of this goodie... it is the amount that will be added to the score when this goodie is collected.
     * 
     * This is different than a hero's strength because this actually bumps the score, which in turn lets us have
     * "super goodies" that turn on trigger obstacles.
     */
    int[] _score                 = new int[4];

    /**
     * How much strength does the hero get by collecting this goodie
     */
    int   _strengthBoost         = 0;

    /**
     * How long will the hero be invincible if it collects this goodie
     */
    float _invincibilityDuration = 0;

    /*
     * INTERNAL INTERFACE
     */

    /**
     * Build a Goodie
     * 
     * This should never be invoked directly. Instead, LOL game designers should use the makeAsXYZ methods
     * 
     * @param x
     *            X position of top left corner
     * @param y
     *            Y position of top left corner
     * @param width
     *            width of this Obstacle
     * @param height
     *            height of this Obstacle
     * @param tr
     *            image to use for this Obstacle
     */
    private Goodie(float width, float height, String imgName)
    {
        super(imgName, SpriteId.GOODIE, width, height);
        _score[0] = 1;
        _score[1] = 0;
        _score[2] = 0;
        _score[3] = 0;
    }

    /**
     * Internal method: Goodie collision is always handled by the other entity involved in the collision, so we leave
     * this method blank
     * 
     * @param other
     *            Other object involved in this collision
     * @param contact
     *            A description of the contact that caused this collision
     */
    void onCollide(PhysicsSprite other, Contact contact)
    {
    }

    /*
     * PUBLIC INTERFACE
     */

    /**
     * Draw a goodie with an underlying box shape, and a default score of [1,0,0,0]
     * 
     * @param x
     *            X coordinate of top left corner
     * @param y
     *            Y coordinate of top left corner
     * @param width
     *            Width of the image
     * @param height
     *            Height of the image
     * @param imgName
     *            Name of image file to use
     * 
     * @return The goodie, so that it can be further modified
     */
    public static Goodie makeAsBox(float x, float y, float width, float height, String imgName)
    {
        Goodie g = new Goodie(width, height, imgName);
        g.setBoxPhysics(0, 0, 0, BodyType.StaticBody, false, x, y);
        g._physBody.getFixtureList().get(0).setSensor(true);
        Level._currLevel._sprites.add(g);
        return g;
    }

    /**
     * Draw a goodie with an underlying circle shape, and a default score of [1,0,0,0]
     * 
     * @param x
     *            X coordinate of top left corner
     * @param y
     *            Y coordinate of top left corner
     * @param width
     *            Width of the image
     * @param height
     *            Height of the image
     * @param imgName
     *            Name of image file to use
     * 
     * @return The goodie, so that it can be further modified
     */
    public static Goodie makeAsCircle(float x, float y, float width, float height, String imgName)
    {
        float radius = (width > height) ? width : height;
        Goodie g = new Goodie(width, height, imgName);
        g.setCirclePhysics(0, 0, 0, BodyType.StaticBody, false, x, y, radius / 2);
        g._physBody.getFixtureList().get(0).setSensor(true);
        Level._currLevel._sprites.add(g);
        return g;
    }

    /**
     * Set the score of this goodie. This indicates how many points the goodie is worth... each value can be positive or
     * negative
     * 
     * @param v1
     *            The number of points that are added to the first score when the goodie is collected
     * @param v2
     *            The number of points that are added to the second score when the goodie is collected
     * @param v3
     *            The number of points that are added to the third score when the goodie is collected
     * @param v4
     *            The number of points that are added to the fourth score when the goodie is collected
     */
    public void setScore(int v1, int v2, int v3, int v4)
    {
        _score[0] = v1;
        _score[1] = v2;
        _score[2] = v3;
        _score[3] = v4;
    }

    /**
     * Indicate how much strength the hero gains by collecting this goodie
     * 
     * @param boost
     *            Amount of strength to add (can be negative)
     */
    public void setStrengthBoost(int boost)
    {
        _strengthBoost = boost;
    }

    /**
     * Indicate how long the hero will be invincible after collecting this goodie
     * 
     * @param duration
     *            Amount of time the hero will be invincible. Note that for a hero who is currently invincible, this
     *            value will be /added/ to the hero's remaining invincibility time
     */
    public void setInvincibilityDuration(float duration)
    {
        assert (duration >= 0);
        _invincibilityDuration = duration;
    }
}
