package io.nshusa.rsam.binary;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.nshusa.rsam.util.CompressionUtil;
import io.nshusa.rsam.util.HashUtils;
import io.nshusa.rsam.util.ByteBufferUtils;

public final class Archive {

	public static final int TITLE_ARCHIVE = 1;
	public static final int CONFIG_ARCHIVE = 2;
	public static final int INTERFACE_ARCHIVE = 3;
	public static final int MEDIA_ARCHIVE = 4;
	public static final int VERSION_LIST_ARCHIVE = 5;
	public static final int TEXTURE_ARCHIVE = 6;
	public static final int WORDENC_ARCHIVE = 7;
	public static final int SOUND_ARCHIVE = 8;

	public static final class ArchiveEntry {

		private final int hash;
		private final int uncompressedSize;
		private final int compressedSize;
		private final byte[] bzipped;

		public ArchiveEntry(int hash, int uncompressedSize, int compressedSize, byte[] bzipped) {
			this.hash = hash;
			this.uncompressedSize = uncompressedSize;
			this.compressedSize = compressedSize;
			this.bzipped = bzipped;
		}

		public int getHash() {
			return hash;
		}

		public int getUncompressedSize() {
			return uncompressedSize;
		}

		public int getCompresseedSize() {
			return compressedSize;
		}

		public byte[] getData() {
			return bzipped;
		}

	}

	private boolean extracted;	
	
	private final List<ArchiveEntry> entries = new ArrayList<>();
	
	private Archive() {		

	}

	public Archive(ArchiveEntry[] entries) {
		this.entries.addAll(Arrays.asList(entries));
	}
	
	public static Archive create() {
		Archive archive = new Archive();
		return archive;
	}

	public static Archive decode(ByteBuffer archiveBuf) throws IOException {
		Archive archive = new Archive();

		int uncompressedSize = ByteBufferUtils.readU24Int(archiveBuf);
		int compressedSize = ByteBufferUtils.readU24Int(archiveBuf);

		archive.extracted = false;

		if (uncompressedSize != compressedSize) {
			byte[] compressed = new byte[compressedSize];
			byte[] decompressed = new byte[uncompressedSize];
			archiveBuf.get(compressed);
			CompressionUtil.debzip2(compressed, decompressed);
			archiveBuf = ByteBuffer.wrap(decompressed);
			archive.extracted = true;
		}

		int entries = archiveBuf.getShort() & 0xffff;

		ByteBuffer entryBuf = ByteBuffer.wrap(archiveBuf.array());

		entryBuf.position(archiveBuf.position() + entries * 10);

		int[] hashes = new int[entries];
		int[] uncompressedSizes = new int[entries];
		int[] compressedSizes = new int[entries];

		for (int i = 0; i < entries; i++) {

			hashes[i] = archiveBuf.getInt();
			uncompressedSizes[i] = ByteBufferUtils.readU24Int(archiveBuf);
			compressedSizes[i] = ByteBufferUtils.readU24Int(archiveBuf);

			byte[] entryData = new byte[compressedSizes[i]];
			entryBuf.get(entryData, 0, compressedSizes[i]);

			archive.getEntries().add(new ArchiveEntry(hashes[i], uncompressedSizes[i], compressedSizes[i], entryData));
		}

		return archive;
	}
	
	public synchronized byte[] encode() throws IOException {
		int size = 2 + entries.size() * 10;
		
		for (ArchiveEntry file : entries) {			
			size += file.getCompresseedSize();
		}
		
		ByteBuffer buffer;
		if (!extracted) {
			buffer = ByteBuffer.allocate(size + 6);
			ByteBufferUtils.write24Int(buffer, size);
			ByteBufferUtils.write24Int(buffer, size);
		} else {
			buffer = ByteBuffer.allocate(size);
		}
		
		buffer.putShort((short)entries.size());
		
		for (ArchiveEntry entry : entries) {			
			buffer.putInt(entry.getHash());
			ByteBufferUtils.write24Int(buffer, entry.getUncompressedSize());
			ByteBufferUtils.write24Int(buffer, entry.getCompresseedSize());
		}
		
		for (ArchiveEntry file : entries) {			
			buffer.put(file.getData());
		}
		
		byte[] data;
		if (!extracted) {
			data = buffer.array();
		} else {
			byte[] unzipped = buffer.array();
			byte[] zipped = CompressionUtil.bzip2(unzipped);
			if (unzipped.length == zipped.length) {
				throw new RuntimeException("error zipped size matches original");
			}
			buffer = ByteBuffer.allocate(zipped.length + 6);
			ByteBufferUtils.write24Int(buffer, unzipped.length);
			ByteBufferUtils.write24Int(buffer, zipped.length);
			buffer.put(zipped, 0, zipped.length);
			data = buffer.array();
		}
		
		return data;

	}

	public byte[] readFile(String name) throws IOException {
		return readFile(HashUtils.nameToHash(name));
	}

	public byte[] readFile(int hash) throws IOException {
		for (ArchiveEntry entry : entries) {
			if (entry.getHash() == hash) {

				byte[] decompressed = new byte[entry.getUncompressedSize()];

				if (extracted) {
					System.arraycopy(entry.getData(), 0, decompressed, 0, decompressed.length);
				} else {
					CompressionUtil.debzip2(entry.getData(), decompressed);
				}
				return decompressed;
			}
		}

		return null;
	}
	
	public ArchiveEntry getEntry(String name) throws FileNotFoundException {
		return getEntry(HashUtils.nameToHash(name));
	}
	
	public ArchiveEntry getEntry(int hash) throws FileNotFoundException {
		for (ArchiveEntry entry : entries) {
			if (entry.getHash() == hash) {
				return entry;
			}
		}
		throw new FileNotFoundException(String.format("Could not find entry: %d.", hash));
	}
	
	public int indexOf(String name) {
		return indexOf(HashUtils.nameToHash(name));
	}
	
	public int indexOf(int hash) {
		for (int i = 0; i < entries.size(); i++) {
			ArchiveEntry entry = entries.get(i);			
			
			if (entry.getHash() == hash) {
				return i;
			}			
		}
		
		return -1;
	}
	
	public boolean contains(String name) {
		return contains(HashUtils.nameToHash(name));
	}
	
	public boolean contains(int hash) {
		for (ArchiveEntry entry : entries) {
			if (entry.getHash() == hash) {
				return true;
			}
		}
		return false;
	}
	
	public boolean remove(String name) {
		return remove(HashUtils.nameToHash(name));
	}
	
	public boolean remove(int hash) {		
		for (int i = 0; i < entries.size(); i++) {
			ArchiveEntry entry = entries.get(i);			
			
			if (entry.getHash() == hash) {
				entries.remove(i);
				return true;
			}			
		}		
		return false;
	}
	
	public List<ArchiveEntry> getEntries() {
		return entries;
	}

}