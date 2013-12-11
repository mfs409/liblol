package edu.lehigh.cse.ale;

// STATUS: Done?

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class Goodie extends PhysicsSprite
{
    /*
     * BASIC FUNCTIONALITY
     */

    /**
     * Internal constructor to build a Goodie
     * 
     * This should never be invoked directly. Instead, use the 'addXXX' methods
     * of the Object class.
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
    protected Goodie(float width, float height, String imgName)
    {
        super(imgName, SpriteId.GOODIE, width, height);
        _score1 = 1;
        _score2 = 0;
        _score3 = 0;
        _score4 = 0;
    }

    /**
     * Draw a goodie with an underlying box shape
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
     * @return The obstacle, so that it can be further modified
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
     * Draw a goodie with an underlying circle shape
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
     * @return The obstacle, so that it can be further modified
     */
    public static Goodie makeAsCircle(float x, float y, float width, float height, String imgName)
    {
        float radius = (width > height) ? width : height;
        Goodie g = new Goodie(width, height, imgName);
        g.setCirclePhysics(0, 0, 0, BodyType.StaticBody, false, x, y, radius/2);
        g._physBody.getFixtureList().get(0).setSensor(true);
        Level._currLevel._sprites.add(g);
        return g;
    }

 
    /**
     * The "score" of this goodie... it is the amount that will be added to the score when this goodie is collected.
     * 
     * This is different than strength because this actually bumps the score, which in turn lets us have
     * "super goodies" that turn on trigger obstacles.
     * 
     * Note: the right way to do this is to have an array of scores, but we're going to be sloppy for now and just have
     * four separate scores
     */
    int _score1;
    
    /**
     * The second score
     */
    int _score2;
    
    /**
     * The third score
     */
    int _score3;
    
    /**
     * The fourth score
     */
    int _score4;
    

    /**
     * Set the score of this goodie. This indicates how many points the goodie is worth... can be positive or negative
     * 
     * @param value
     *            The number of points that are added to the score when the goodie is collected
     */
    public void setScore(int v1, int v2, int v3, int v4)
    {
        // save this value
        _score1 = v1;
        _score2 = v2;
        _score3 = v3;
        _score4 = v4;
    }

    /*
     * COLLISION SUPPORT
     */

    /**
     * Internal method: Goodie collision is meaningless, so we leave this method blank
     * 
     * @param other
     *            Other object involved in this collision
     */
    void onCollide(PhysicsSprite other)
    {
    }

    /*
     * STRENGTH AND INVINCIBILITY SUPPORT
     */

    /**
     * How much _strength does the hero get by collecting this goodie
     */
    int   _strengthBoost         = 0;

    /**
     * How long will the hero be invincible if it collects this goodie
     */
    float _invincibilityDuration = 0;

    /**
     * Indicate how much _strength the hero gains by collecting this goodie
     * 
     * @param boost
     *            Amount of _strength boost
     */
    public void setStrengthBoost(int boost)
    {
        _strengthBoost = boost;
    }

    /**
     * Indicate how long the hero will be invincible after collecting this goodie
     * 
     * @param duration
     *            duration for invincibility
     */
    public void setInvincibilityDuration(float duration)
    {
        _invincibilityDuration = duration;
    }
}
