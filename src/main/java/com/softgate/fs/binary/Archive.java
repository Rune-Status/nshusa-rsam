package com.softgate.fs.binary;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.softgate.util.BufferUtils;
import com.softgate.util.CompressionUtil;
import com.softgate.util.HashUtils;

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
		
		ByteBuffer buffer = ByteBuffer.wrap(data);

		final int extractedSize = BufferUtils.getTriByte(buffer);
		final int size = BufferUtils.getTriByte(buffer);

		boolean extracted = false;

		if (extractedSize != size) {

			final byte[] compressed = new byte[size];
			final byte[] decompressed = new byte[extractedSize];

			buffer.get(compressed);

			CompressionUtil.debzip2(compressed, decompressed);

			buffer = ByteBuffer.wrap(decompressed);

			extracted = true;
		} else {
			extracted = false;
		}

		final int entryCount = buffer.getShort() & 0xffff;
		
		int[] hashes = new int[entryCount];
		int[] uncompressedSizes = new int[entryCount];
		int[] compressedSizes = new int[entryCount];		
		int[] offsets = new int[entryCount];
		
		int offset = buffer.position() + entryCount * 10;

		for (int file = 0; file < entryCount; file++) {

			hashes[file] = buffer.getInt();
			uncompressedSizes[file] = BufferUtils.getTriByte(buffer);
			compressedSizes[file] = BufferUtils.getTriByte(buffer);			
			offsets[file] = offset;
			
			offset += compressedSizes[file];
		}

		for (int entry = 0; entry < entryCount; entry++) {

			ByteBuffer entryBuf;

			if (!extracted) {
				byte[] compressed = new byte[compressedSizes[entry]];

				byte[] decompressed = new byte[uncompressedSizes[entry]];

				buffer.get(compressed);

				CompressionUtil.debzip2(compressed, decompressed);

				entryBuf = ByteBuffer.wrap(decompressed);

			} else {
				byte[] buf = new byte[uncompressedSizes[entry]];

				buffer.get(buf);

				entryBuf = ByteBuffer.wrap(buf);
			}		
			
			archive.getEntries().add(new ArchiveEntry(hashes[entry], uncompressedSizes[entry], compressedSizes[entry],
					entryBuf.array()));			

		}
		
		return archive;
	}
	
	public synchronized byte[] encode() throws IOException {		
		
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
			buf.put(file.getData(), 0, file.getCompresseedSize());
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
	
	public byte[] readFile(String name) {
		return readFile(HashUtils.nameToHash(name));
	}
	
	public byte[] readFile(int hash) {
		for (ArchiveEntry entry : entries) {
			if (entry.getHash() == hash) {
				return entry.getData();
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
	
	public void remove(String name) {
		remove(HashUtils.nameToHash(name));
	}
	
	public void remove(int hash) {		
		for (int i = 0; i < entries.size(); i++) {
			ArchiveEntry entry = entries.get(i);			
			
			if (entry.getHash() == hash) {
				entries.remove(i);
				break;
			}			
		}
	}
	
	public List<ArchiveEntry> getEntries() {
		return entries;
	}

}