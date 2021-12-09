
import java.awt.Image;
import java.awt.Rectangle;
import java.util.Random;

import javax.swing.ImageIcon;

/* base class for all the elements in the app */
class JunctionElement {

	protected int _x;
	protected int _y;
	protected int _width;
	protected int _height;
	protected boolean _visible;
	protected Image _image;
	protected static Random _random = new Random(); // for random image selection

	public JunctionElement(int x, int y) {
		this._x = x;
		this._y = y;
		this._visible = true;
	}

    
	protected void getImageDimensions() {

		this._width = this._image.getWidth(null);
		this._height = this._image.getHeight(null);
	}
	
	protected void loadImage(String imageName) {
		ImageIcon ii = new ImageIcon(imageName);
		this._image = ii.getImage();
	}
	
    public Image getImage() {
        return this._image;
    }
    
    public int getX() {
        return this._x;
    }

    public int getY() {
        return this._y;
    }

    public boolean isVisible() {
        return this._visible;
    }

    public void setVisible(Boolean visible) {
    	this._visible = visible;
    }

    public Rectangle getBounds() {
        return new Rectangle(this._x, this._y, this._width, this._height);
    }
}
