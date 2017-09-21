package io.nshusa.rsam;

import io.nshusa.rsam.binary.Archive;

import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public final class IndexedFileSystem implements Closeable {

	private static final String[] crcFileNames = {"", "model_crc", "anim_crc", "midi_crc", "map_crc"};
	private static final String[] versionFileNames = {"", "model_version", "anim_version", "midi_version", "map_version"};
	
	private final Path root;
	
	private final FileStore[] fileStores = new FileStore[255];
	
	private boolean loaded;
	
	private IndexedFileSystem(Path root) {
		this.root = root;
	}
	
	public static IndexedFileSystem init(Path root) throws IOException {
		IndexedFileSystem indexedFileSystem = new IndexedFileSystem(root);

		Path dataPath = root.resolve("main_file_cache.dat");
		
		if (!Files.exists(dataPath)) {
			throw new IOException("could not locate data file");
		}
		
		RandomAccessFile dataRaf = new RandomAccessFile(dataPath.toFile(), "rw");
		
		for (int i = 0; i < 255; i++) {			
			Path indexPath = root.resolve("main_file_cache.idx" + i);			
			if (Files.exists(indexPath)) {				
				indexedFileSystem.fileStores[i] = new FileStore(i + 1, dataRaf.getChannel(), new RandomAccessFile(indexPath.toFile(), "rw").getChannel());
			}			
		}
		
		indexedFileSystem.loaded = true;
		return indexedFileSystem;
	}

	public int calculateChecksum(int storeId, int fileId) throws IOException {

		// you can't calculate the checksum for archives like this they don't have an associated version and crc file in the version list archive
		if (storeId == 0) {
			return -1;
		}

		FileStore store = getStore(storeId);

		// this archive stores all of the files where each store (besides index 0) that keep track of versions (_version) and checksums (_crc) of its files
		Archive updateArchive = Archive.decode(readFile(FileStore.ARCHIVE_FILE_STORE, Archive.VERSION_LIST_ARCHIVE));

		// this file stores all of the crcs for each file for the specified store
		ByteBuffer crcBuf = updateArchive.readFile(crcFileNames[storeId]);

		// crcs are stored as integers so you can divide by the length of data to get how many entries there are
		final int crcCount = crcBuf.capacity() / Integer.BYTES;

		if (fileId > crcCount) {
			return -1;
		}

		// this file stores all of the versions for each file for the specified store. when a file is updated, the version increments by 1 and is stored as a short
		ByteBuffer versionBuf = updateArchive.readFile(versionFileNames[storeId]);

		// since the version is stored as a short you can get the amount of entries by dividing the files length of data by the amount of bytes the type takes up
		final int versionCount = versionBuf.capacity() / Short.BYTES;

		// when you add files to the end of the file store the corresponding _version and _crc file has to be updated since it wont have the previous versions of that file
		if (fileId > versionCount) {
			return -1;
		}

		// read the version at the specified file, Short.BYTES because a version is an unsigned short which takes up 2 bytes
		versionBuf.position(fileId * Short.BYTES);

		final int version = versionBuf.getShort() & 0xFFFF;

		// read the checksum at the specified file, Integer.BYTES because a checksums are stored as Integers which take up 4 bytes
		crcBuf.position(fileId * Integer.BYTES);

		// when a file is added into the cache, the checksum is calculated and stored this is the value that was previously calculated last time a file was added to this spot
		final int lastRecordedChecksum = crcBuf.getInt();

		// read the file
		ByteBuffer fileBuf = store.readFile(fileId);

		if (fileBuf == null) {
			return -1;
		}

		// checksums are calculated by filepayload first then the version following after
		ByteBuffer buf = ByteBuffer.allocate(fileBuf.capacity() + Short.BYTES);
		buf.put(fileBuf);
		buf.putShort((short) version);

		byte[] combined = new byte[buf.capacity()];

		Checksum checksum = new CRC32();

		checksum.update(combined, 0, combined.length);

		int calculatedCrc = (int) checksum.getValue();

		if (calculatedCrc != lastRecordedChecksum) {
			// the files are different so update
		} else {
			// the file has not been modified
		}

		return calculatedCrc;
	}
	
	public void createStore(int storeId) throws IOException {		
		if (storeId < 0 || storeId >= fileStores.length) {
			return;
		}
		
		if (fileStores[storeId] != null) {
			return;
		}
		
		Path dataPath = root.resolve("main_file_cache.dat");
		
		if (!Files.exists(dataPath)) {
			Files.createFile(dataPath);
		}
		
		Path path = root.resolve("main_file_cache.idx" + storeId);
		
		if (!Files.exists(path)) {
			Files.createFile(path);
		}
		
		RandomAccessFile dataRaf = new RandomAccessFile(dataPath.toFile(), "rw");
		
		fileStores[storeId] = new FileStore(storeId + 1, dataRaf.getChannel(), new RandomAccessFile(path.toFile(), "rw").getChannel());
	}
	
	public FileStore getStore(int storeId) {
		if (storeId < 0 || storeId >= fileStores.length) {
			throw new IllegalArgumentException(String.format("storeId=%d out of range=[0, 254]", storeId));
		}
		
		return fileStores[storeId];
	}

	public ByteBuffer readFile(int storeId, int fileId) {
		FileStore store = getStore(storeId);

		return store.readFile(fileId);
	}

	public Path getRoot() {
		return root;
	}

	public int getStoreCount() {		
		int count = 0;		
		for (int i = 0; i < 255; i++) {			
			Path indexPath = root.resolve("main_file_cache.idx" + i);			
			if (Files.exists(indexPath)) {				
				count++;
			}			
		}
		
		return count;
	}	

	public boolean isLoaded() {
		return loaded;
	}

	@Override
	public void close() throws IOException {
		for (FileStore fileStore : fileStores) {
			if (fileStore == null) {
				continue;
			}
			
			synchronized(fileStore) {
				fileStore.close();
			}
			
		}
	}

}
