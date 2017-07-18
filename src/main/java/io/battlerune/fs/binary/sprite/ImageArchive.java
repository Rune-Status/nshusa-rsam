package io.battlerune.fs.binary.sprite;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import io.battlerune.util.BufferUtils;

public final class ImageArchive {	
	
	private ImageArchive() {
		
	}
	
	public static List<Sprite> decode(ByteBuffer dataBuf, ByteBuffer indexBuf, boolean unpack) {
		
		List<Sprite> sprites = new ArrayList<>();
		
		int dataPos = dataBuf.getShort() & 0xffff;
				
		indexBuf.position(dataPos);
		
		int resizeWidth = indexBuf.getShort() & 0xffff;
		int resizeHeight = indexBuf.getShort() & 0xffff;
		
		int colors = indexBuf.get() & 0xff;

		int[] raster = new int[colors];
		
		for (int index = 0; index < colors - 1; index++) {
			int colour = BufferUtils.getTriByte(indexBuf);
			raster[index + 1] = colour == 0 ? 1 : colour;
		}
		
		if (unpack) {
			int indexOffSet = indexBuf.position();
			
			int dataOffset;
			byte drawOffsetX;
			byte drawOffsetY;
			int width;
			int height;
			int[] pixels;
			
			for(dataOffset = dataBuf.position(); dataBuf.position() < dataBuf.array().length; 
					sprites.add(new Sprite(resizeWidth, resizeHeight, drawOffsetX, drawOffsetY, width, height, pixels))) {
				drawOffsetX = indexBuf.get();
				drawOffsetY = indexBuf.get();
				width = indexBuf.getShort() & 0xffff;
				height = indexBuf.getShort() & 0xffff;
				
				int format = indexBuf.get();
				
				pixels = new int[width * height];
				
				if (format == 0) {
					for (int index = 0; index < pixels.length; index++) {
						pixels[index] = raster[dataBuf.get() & 0xff];
					}
				} else if (format == 1) {
					for (int x = 0; x < width; x++) {
						for (int y = 0; y < height; y++) {
							pixels[x + y * width] = raster[dataBuf.get() & 0xff];
						}
					}
				}
			}
			indexBuf.position(indexOffSet);
			dataBuf.position(dataOffset);
		}
		
		return sprites;		
	}

}
