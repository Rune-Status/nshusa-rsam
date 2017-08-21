package main.java.io.nshusa.rsam.util;

import java.nio.ByteBuffer;

public final class BufferUtils {

	private BufferUtils() {

	}

	public static void putMedium(ByteBuffer buffer, int value) {
		buffer.put((byte) (value >> 16)).put((byte) (value >> 8)).put((byte) value);
	}

	public static int getUMedium(ByteBuffer buffer) {
		return (buffer.getShort() & 0xFFFF) << 8 | buffer.get() & 0xFF;
	}

	public static int getUShort(ByteBuffer buffer) {
		return buffer.getShort() & 0xffff;
	}

	public static int getTriByte(ByteBuffer buffer) {
		return ((buffer.get() & 0xff) << 16) | ((buffer.get() & 0xff) << 8) | (buffer.get() & 0xff);
	}

	public static int getSmart(ByteBuffer buffer) {
		int peek = buffer.get(buffer.position()) & 0xFF;
		if (peek < 128) {
			return buffer.get() & 0xFF;
		}
		return (buffer.getShort() & 0xFFFF) - 32768;
	}

}
