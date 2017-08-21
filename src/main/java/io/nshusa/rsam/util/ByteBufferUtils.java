package main.java.io.nshusa.rsam.util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ByteBufferUtils {

    private ByteBufferUtils() {

    }

    public static int readU24Int(ByteBuffer buffer) {
        return (buffer.get() & 0x0ff) << 16 |(buffer.get() & 0x0ff) << 8 | (buffer.get() & 0x0ff);
    }

    public static void writeU24Int(int rgb, DataOutputStream dos) throws IOException {
        dos.writeByte(rgb >> 16);
        dos.writeByte(rgb >> 8);
        dos.writeByte(rgb);
    }

}
