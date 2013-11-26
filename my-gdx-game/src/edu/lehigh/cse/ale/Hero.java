package edu.lehigh.cse.ale;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class Hero extends PhysicsSprite
{

    Hero(TextureRegion tr, float width, float height)
    {
        super(tr, SpriteId.HERO, width, height);
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
        // if (other._psType == SpriteId.ENEMY)
        // onCollideWithEnemy((Enemy) other);
        // else
        if (other._psType == SpriteId.DESTINATION)
            onCollideWithDestination((Destination) other);
        else if (other._psType == SpriteId.OBSTACLE)
            onCollideWithObstacle((Obstacle) other);
        // else if (other._psType == SpriteId.SVG)
        // onCollideWithSVG(other);
        // else if (other._psType == SpriteId.GOODIE)
        // onCollideWithGoodie((Goodie) other);

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
            scheduleRemove(true);
            d._holding++;
            if (d._arrivalSound != null)
                d._arrivalSound.play();
            Score.onDestinationArrive();
        }
    }

    private void onCollideWithObstacle(Obstacle o)
    {
    }

}
