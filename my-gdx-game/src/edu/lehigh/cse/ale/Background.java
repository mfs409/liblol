package edu.lehigh.cse.ale;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Background
{
    static class ParallaxLayer
    {
        float _xSpeed;
        float _ySpeed;
        TextureRegion _tr;
        float _xOffset;
        float _yOffset;
        boolean _xRepeat;
        boolean _yRepeat;
        
        ParallaxLayer(float xSpeed, float ySpeed, TextureRegion tr, float xOffset, float yOffset)
        {
            _xSpeed = xSpeed;
            _ySpeed = ySpeed;
            _tr = tr;
            _xOffset = xOffset;
            _yOffset = yOffset;
        }        
    }

    static float _bgRed;
    static float _bgGreen;
    static float _bgBlue;
    static ArrayList<ParallaxLayer> _layers = new ArrayList<ParallaxLayer>();
    
    static public void setColor(int red, int green, int blue)
    {
        _bgRed = ((float)red)/255;
        _bgGreen = ((float)green)/255;
        _bgBlue = ((float)blue)/255;
    }

    static void reset()
    {
        _bgRed = 0;
        _bgGreen = 0;
        _bgBlue = 0;
        _layers.clear();
    }
    
    static public void addHorizontalLayer(float xSpeed, float ySpeed, String imgName, float yOffset)
    {
        ParallaxLayer pl = new ParallaxLayer(xSpeed, ySpeed, Media.getImage(imgName), 0, yOffset*Physics.PIXEL_METER_RATIO);
        pl._xRepeat = xSpeed != 0;
        _layers.add(pl);
    }
    
    static void doParallaxLayers(SpriteBatch renderer)
    {
        // center camera on _gameCam's camera
        float x = Level._currLevel._gameCam.position.x;
        float y = Level._currLevel._gameCam.position.y;
        Level._currLevel._bgCam.position.set(x, y, 0);
        Level._currLevel._bgCam.update();
        
        for (ParallaxLayer pl : _layers) {
            renderer.setProjectionMatrix(Level._currLevel._bgCam.calculateParallaxMatrix(pl._xSpeed*Physics.PIXEL_METER_RATIO, pl._ySpeed*Physics.PIXEL_METER_RATIO));
            renderer.begin();
            if (pl._xRepeat) {
                int i = -(int)pl._tr.getRegionWidth()/2;
                while (i/Physics.PIXEL_METER_RATIO < x + Level._currLevel._camBoundX) {
                    // TODO: verify that GDX culls... otherwise, we should manually cull...
                    renderer.draw(pl._tr,  i,  0+pl._yOffset);
                    // Gdx.app.log("parallax", x+" "+_width + " " + pl._tr.getRegionWidth() + " " + i);
                    i+=pl._tr.getRegionWidth();
                }
            }
            else if (pl._yRepeat) {
                // TODO: vertical repeat... note that we don't support vertical
                // and horizontal repeat... should we? 'twould allow for easy
                // tiled backgrounds...
            }
            else {
                // probably the background layer...
                renderer.draw(pl._tr, -pl._tr.getRegionWidth()/2,  0);
            }
            renderer.end();            
        }        
    }

}
