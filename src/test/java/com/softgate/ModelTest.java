package com.softgate;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.Arrays;

import com.softgate.fs.FileStore;
import com.softgate.fs.IndexedFileSystem;
import com.softgate.fs.binary.Model;
import com.softgate.util.CompressionUtil;

public class ModelTest {

	public static void main(String[] args) throws IOException {
		IndexedFileSystem fs = IndexedFileSystem.init(Paths.get("./cache/"));
		
		// store containing archives
		FileStore store = fs.getStore(1);
		
		byte[] data = store.readFile(0);
		
		if (data == null) {
			return;
		}
		
		byte[] decompressed = CompressionUtil.degzip(ByteBuffer.wrap(data));
		
		Model model = new Model(decompressed);
		
		System.out.println(Arrays.toString(model.verticesX)+"\n"+Arrays.toString(model.verticesY));

	}

}
