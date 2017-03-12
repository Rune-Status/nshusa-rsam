package com.softgate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.softgate.fs.binary.Archive;

public class ArchiveEncodeTest {

	public static void main(String[] args) throws IOException {

		byte[] data = Files.readAllBytes(Paths.get("./config.jag"));
		
		Archive archive = Archive.decode(data);
		
		System.out.println(archive.getEntries().size());
		
		archive.remove(8791887);
		
		System.out.println(archive.getEntries().size());
		
		byte[] encoded = archive.encode();
		
		try(FileOutputStream fos = new FileOutputStream(new File("./config.jag"))) {
			fos.write(encoded);
		}
		
		System.out.println("success! " + encoded == null);
		
	}

}
