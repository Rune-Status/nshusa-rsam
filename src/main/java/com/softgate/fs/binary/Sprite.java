package com.softgate.fs.binary;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public final class Sprite {

	private int width;
	private int height;
	private int offsetX;
	private int offsetY;
	private int resizeHeight;
	private int resizeWidth;
	
	private int[] pixels;
	
	public Sprite(int width, int height) {		
		this.pixels = new int[width * height];		
		this.width = this.resizeWidth = width;
		this.height = this.resizeHeight = height;
	}

	public Sprite(int resizeWidth, int resizeHeight, int horizontalOffset, int verticalOffset, int width, int height,
			int[] pixels) {
		this.resizeWidth = resizeWidth;
		this.resizeHeight = resizeHeight;
		this.offsetX = horizontalOffset;
		this.offsetY = verticalOffset;
		this.width = width;
		this.height = height;
		this.pixels = pixels;		
	}
	
	public Sprite copy(Sprite sprite) {
		return new Sprite(sprite.getResizeWidth(), sprite.getResizeHeight(), sprite.getOffsetX(), sprite.getOffsetY(), sprite.getWidth(), sprite.getHeight(), sprite.getPixels());
	}
	
	public BufferedImage toBufferedImage() {
		
		BufferedImage image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);
		
		final int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
		
		System.arraycopy(this.pixels, 0, pixels, 0, this.pixels.length);
		
		return image;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getOffsetX() {
		return offsetX;
	}

	public void setOffsetX(int offsetX) {
		this.offsetX = offsetX;
	}

	public int getOffsetY() {
		return offsetY;
	}

	public void setOffsetY(int offsetY) {
		this.offsetY = offsetY;
	}

	public int getResizeHeight() {
		return resizeHeight;
	}

	public void setResizeHeight(int resizeHeight) {
		this.resizeHeight = resizeHeight;
	}

	public int getResizeWidth() {
		return resizeWidth;
	}

	public void setResizeWidth(int resizeWidth) {
		this.resizeWidth = resizeWidth;
	}

	public int[] getPixels() {
		return pixels;
	}

	public void setPixels(int[] pixels) {
		this.pixels = pixels;
	}
	
}