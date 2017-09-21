package io.nshusa.rsam;

import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class IndexedFileSystem implements Closeable {
	
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
				indexedFileSystem.fileStores[i] = new FileStore(i, dataRaf.getChannel(), new RandomAccessFile(indexPath.toFile(), "rw").getChannel());
			}			
		}
		
		indexedFileSystem.loaded = true;
		return indexedFileSystem;
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
