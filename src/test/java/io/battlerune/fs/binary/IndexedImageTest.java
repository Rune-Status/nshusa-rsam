package io.battlerune.fs.binary;

import java.io.IOException;
import java.nio.file.Paths;

import com.nshusa.rsam.FileStore;
import com.nshusa.rsam.IndexedFileSystem;
import com.nshusa.rsam.binary.Archive;
import com.nshusa.rsam.binary.sprite.IndexedImage;

public class IndexedImageTest {

	public static void main(String[] args) throws IOException {
		IndexedFileSystem fs = IndexedFileSystem.init(Paths.get("./Cache/"));
		
		FileStore store = fs.getStore(0);
		
		Archive archive = Archive.decode(store.readFile(6));
		
		@SuppressWarnings("unused")
		IndexedImage image = IndexedImage.decode(archive, "0", 0);
		
		System.out.println("success!");
	}

}
