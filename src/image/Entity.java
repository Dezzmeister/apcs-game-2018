package image;

import java.awt.image.BufferedImage;

public interface Entity{
    
        public BufferedImage getImage();
            //defined in adopted children (classes that implement this, since every img will have a different location [name] ; they don’t share any variables/constructors)
        
}


