package io.nshusa.rsam.binary.sprite;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public final class Sprite {

	private int width;
	private int height;
	private int offsetX;
	private int offsetY;
	private int largestHeight;
	private int largestWidth;
	private int[] pixels;
	private int format;

	public Sprite() {

	}

	public Sprite(int width, int height) {
		this.pixels = new int[width * height];
		this.width = this.largestWidth = width;
		this.height = this.largestHeight = height;
	}

	public Sprite(int resizeWidth, int resizeHeight, int horizontalOffset, int verticalOffset, int width, int height, int format,
			int[] pixels) {
		this.largestWidth = resizeWidth;
		this.largestHeight = resizeHeight;
		this.offsetX = horizontalOffset;
		this.offsetY = verticalOffset;
		this.width = width;
		this.height = height;
		this.format = format;
		this.pixels = pixels;
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

	public int getLargestHeight() {
		return largestHeight;
	}

	public void setLargestHeight(int resizeHeight) {
		this.largestHeight = resizeHeight;
	}

	public int getLargestWidth() {
		return largestWidth;
	}

	public void setLargestWidth(int resizeWidth) {
		this.largestWidth = resizeWidth;
	}

	public int[] getPixels() {
		return pixels;
	}

	public void setPixels(int[] pixels) {
		this.pixels = pixels;
	}

	public int getFormat() {
		return format;
	}

	public void setFormat(int format) {
		this.format = format;
	}

}