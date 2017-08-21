package main.java.io.nshusa.rsam.codec;

import main.java.io.nshusa.rsam.binary.sprite.Sprite;
import main.java.io.nshusa.rsam.util.ByteBufferUtils;

import java.nio.ByteBuffer;

public final class SpriteDecoder {

    private SpriteDecoder() {

    }

    public static Sprite decode(ByteBuffer dataBuf, ByteBuffer indexBuf, int spriteId) {
        Sprite sprite = new Sprite();

        // position of the current image archive within the archive
        indexBuf.position(dataBuf.getShort() & 0xFFFF);

        // the image with the largest width
        sprite.setLargestWidth(indexBuf.getShort() & 0xFFFF);

        // the image width the largest height
        sprite.setLargestHeight(indexBuf.getShort() & 0xFFFF);

        // the number of colors that are used in this image archive
        int colors = indexBuf.get() & 0xFF;

        // the palette of colors that can only be used.

        int palette[] = new int[colors];

        for (int i = 0; i < colors - 1; i++) {

            palette[i + 1] = ByteBufferUtils.readU24Int(indexBuf);

            // + 1 because index = 0 is for transparency, = 1 is a flag for opacity. (BufferedImage#OPAQUE)
            if (palette[i + 1] == 0) {
                palette[i + 1] = 1;
            }
        }

        for (int spriteIndex = 0; spriteIndex < spriteId; spriteIndex++) {

            // skip the next the offsetX and offsetY
            indexBuf.position(indexBuf.position() + 2);

            // the width of the sprite at this position in the image archive
            int width = indexBuf.getShort() & 0xFFFF;

            // the height of the sprite at this position in the image archive
            int height = indexBuf.getShort() & 0xFFFF;

            // skip this image's array of pixels
            dataBuf.position(dataBuf.position() + (width * height));

            // skip this image's type
            indexBuf.position(indexBuf.position() + 1);
        }

        // the offsets are used to reposition the sprite on an interface.
        sprite.setOffsetX(indexBuf.get() & 0xFF);

        sprite.setOffsetY(indexBuf.get() & 0xFF);

        // actual width of this sprite
        sprite.setWidth(indexBuf.getShort() & 0xFFFF);

        // actual height of this sprite
        sprite.setHeight(indexBuf.getShort() & 0xFFFF);

        // there are 2 ways the pixels can be written (0 or 1, 0 means the position is read horizontally, 1 means vertically)
        final int type = indexBuf.get() & 0xFF;

        sprite.setPixels(new int[sprite.getWidth() * sprite.getHeight()]);

        if (type == 0) { // read horizontally
            for (int i = 0; i < sprite.getPixels().length; i++) {
                sprite.getPixels()[i] = palette[dataBuf.get() & 0xFF];
            }
        } else if (type == 1) { // read vertically
            for (int x = 0; x < sprite.getWidth(); x++) {
                for (int y = 0; y < sprite.getHeight(); y++) {
                    sprite.getPixels()[x + y * sprite.getWidth()] = palette[dataBuf.get() & 0xFF];
                }
            }
        }
        return sprite;
    }

}