package test.java;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.List;

import javax.imageio.ImageIO;

import main.java.io.nshusa.rsam.FileStore;
import main.java.io.nshusa.rsam.IndexedFileSystem;
import main.java.io.nshusa.rsam.binary.Archive;
import main.java.io.nshusa.rsam.binary.Archive.ArchiveEntry;
import main.java.io.nshusa.rsam.codec.ImageArchiveDecoder;
import main.java.io.nshusa.rsam.binary.sprite.Sprite;
import main.java.io.nshusa.rsam.util.HashUtils;

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
			
			List<Sprite> sprites = ImageArchiveDecoder.decode(ByteBuffer.wrap(data), ByteBuffer.wrap(idx));
			
			System.out.println(sprites.size());
			
			for (int spriteId = 0; spriteId < sprites.size(); spriteId++) {			
				Sprite sprite = sprites.get(spriteId);
				
				ImageIO.write(sprite.toBufferedImage(), "png", new File("./dump/" + file + "_" + spriteId + ".png"));
				
				System.out.println("file= " + file + " sprite= " + spriteId);
			}
		}
		

		
	}

}
