package io.nshusa.rsam.binary;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import io.nshusa.rsam.util.CompressionUtil;
import io.nshusa.rsam.util.HashUtils;
import io.nshusa.rsam.util.BufferUtils;

public final class Archive {
	
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
	
	public static Archive create() {
		Archive archive = new Archive();
		
		return archive;
	}
	
	public static Archive decode(byte[] data) throws IOException {
		Archive archive = new Archive();
		
		ByteBuffer archiveBuf = ByteBuffer.wrap(data);
		
		int uncompressedSize = BufferUtils.getTriByte(archiveBuf);
		int compressedSize = BufferUtils.getTriByte(archiveBuf);
		
		if (uncompressedSize != compressedSize) {
			byte[] decompressed = new byte[uncompressedSize];			
			CompressionUtil.debzip2(data, decompressed);
			data = decompressed;
			archiveBuf = ByteBuffer.wrap(decompressed);
		}
		
		int entries = archiveBuf.getShort() & 0xffff;
		
		ByteBuffer entryBuf = ByteBuffer.wrap(data);
		
		entryBuf.position(archiveBuf.position() + entries * 10);
		
		int[] hashes = new int[entries];
		int[] uncompressedSizes = new int[entries];
		int[] compressedSizes = new int[entries];
		
		for (int i = 0; i < entries; i++) {
			
			hashes[i] = archiveBuf.getInt();
			uncompressedSizes[i] = BufferUtils.getTriByte(archiveBuf);
			compressedSizes[i] = BufferUtils.getTriByte(archiveBuf);
			
			byte[] entryData = new byte[compressedSizes[i]];
			entryBuf.get(entryData, 0, compressedSizes[i]);
			
			archive.getEntries().add(new ArchiveEntry(hashes[i], uncompressedSizes[i], compressedSizes[i], entryData));
		}
		
		archive.extracted = uncompressedSize != compressedSize;		
		return archive;
	}
	
	public synchronized byte[] encode() throws IOException {		
		
		// 2 bytes for bzip2 signature
		// amount of entries
		// entry size is 10
		int size = 2 + entries.size() * 10;
		
		for (ArchiveEntry file : entries) {			
			size += file.getCompresseedSize();
		}
		
		ByteBuffer buf;
		if (!extracted) {
			buf = ByteBuffer.allocate(size + 6);
			BufferUtils.putMedium(buf, size);
			BufferUtils.putMedium(buf, size);
		} else {
			buf = ByteBuffer.allocate(size);
		}
		
		buf.putShort((short)entries.size());
		
		for (ArchiveEntry entry : entries) {			
			buf.putInt(entry.getHash());
			BufferUtils.putMedium(buf, entry.getUncompressedSize());
			BufferUtils.putMedium(buf, entry.getCompresseedSize());
		}
		
		for (ArchiveEntry file : entries) {			
			buf.put(file.getData());			
		}
		
		byte[] data;
		if (!extracted) {
			data = buf.array();
		} else {
			byte[] unzipped = buf.array();
			byte[] zipped = CompressionUtil.bzip2(unzipped);
			if (unzipped.length == zipped.length) {
				throw new RuntimeException("error zipped size matches original");
			}
			buf = ByteBuffer.allocate(zipped.length + 6);
			BufferUtils.putMedium(buf, unzipped.length);
			BufferUtils.putMedium(buf, zipped.length);
			buf.put(zipped, 0, zipped.length);
			data = buf.array();
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
	
	public ArchiveEntry getEntry(String name) {
		return getEntry(HashUtils.nameToHash(name));
	}
	
	public ArchiveEntry getEntry(int hash) {
		for (ArchiveEntry entry : entries) {
			if (entry.getHash() == hash) {
				return entry;
			}
		}
		return null;
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