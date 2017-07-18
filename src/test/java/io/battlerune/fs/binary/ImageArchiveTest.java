package io.battlerune.fs.binary;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.List;

import javax.imageio.ImageIO;

import io.battlerune.fs.FileStore;
import io.battlerune.fs.IndexedFileSystem;
import io.battlerune.fs.binary.Archive;
import io.battlerune.fs.binary.Archive.ArchiveEntry;
import io.battlerune.fs.binary.sprite.ImageArchive;
import io.battlerune.fs.binary.sprite.Sprite;
import io.battlerune.util.HashUtils;

public class ImageArchiveTest {

	public static void main(String[] args) throws IOException {
		IndexedFileSystem fs = IndexedFileSystem.init(Paths.get("./cache/"));
		
		// store containing archives
		FileStore store = fs.getStore(0);
		
		// 2d graphics archive
		Archive archive = Archive.decode(store.readFile(4));
		
		for (int file = 0; file < archive.getEntries().size(); file++) {
			
			ArchiveEntry entry = archive.getEntries().get(file);
			
			ArchiveEntry idxEntry = archive.getEntry("index.dat");
			
			if (entry.getHash() == HashUtils.nameToHash("index.dat")) {
				continue;
			}
			
			byte[] data = archive.readFile(entry.getHash());
			
			byte[] idx = archive.readFile(idxEntry.getHash());
			
			List<Sprite> sprites = ImageArchive.decode(ByteBuffer.wrap(data), ByteBuffer.wrap(idx), true);
			
			System.out.println(sprites.size());
			
			for (int spriteId = 0; spriteId < sprites.size(); spriteId++) {			
				Sprite sprite = sprites.get(spriteId);
				
				ImageIO.write(sprite.toBufferedImage(), "png", new File("./dump/" + file + "_" + spriteId + ".png"));
				
				System.out.println("file= " + file + " sprite= " + spriteId);
			}
		}
		

		
	}

}
