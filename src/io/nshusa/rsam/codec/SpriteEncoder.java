package io.nshusa.rsam.codec;

import io.nshusa.rsam.binary.Archive;
import io.nshusa.rsam.util.ColorQuantizer;
import io.nshusa.rsam.util.ByteBufferUtils;
import io.nshusa.rsam.util.CompressionUtil;
import io.nshusa.rsam.util.HashUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

public final class SpriteEncoder {

    private SpriteEncoder() {

    }

    public static void encode(File dir, int encodingType) {

        Archive archive = Archive.create();

        ByteArrayOutputStream ibos = new ByteArrayOutputStream();

        File outputDir = new File("./output/");

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // marks the position of the first sprite within each image archive.
        int idxOffset = 0;

        try(DataOutputStream idxOut = new DataOutputStream(ibos)) {

            // iterator over the image archives which are essentially directories that store images
            for (File imageArchiveDir : dir.listFiles()) {

                // its not an image archive so skip it
                if (!imageArchiveDir.isDirectory()) {
                    continue;
                }

                ByteArrayOutputStream dbos = new ByteArrayOutputStream();

                try(DataOutputStream datOut = new DataOutputStream(dbos)) {

                    // first we have to calculate the largest width and largest height of the image found in this image archive.
                    int largestWidth = 0;

                    int largestHeight = 0;

                    // cache all of the images so we don't have to perform redundant I/O operations again
                    final List<BufferedImage> images = new ArrayList<>();

                    // list that acts as a set, using a list for the #get and #indexOf functions
                    final List<Integer> colorSet = new ArrayList<>();

                    colorSet.add(0);

                    // iterator over the actual images
                    for (int imageIndex = 0; imageIndex < imageArchiveDir.listFiles().length; imageIndex++) {

                        // order is really important, so make sure we grab the right image (File#listFiles isn't sorted)
                        final File imageFile = new File(imageArchiveDir,imageIndex + ".png");

                        // an image can't be a directory so skip it
                        if (imageFile.isDirectory()) {
                            continue;
                        }

                        try {
                            BufferedImage bimage = ColorQuantizer.quantize(ImageIO.read(imageFile));

                            if (largestWidth < bimage.getWidth()) {
                                largestWidth = bimage.getWidth();
                            }

                            if (largestHeight < bimage.getHeight()) {
                                largestHeight = bimage.getHeight();
                            }

                            for (int x = 0; x < bimage.getWidth(); x++) {
                                for (int y = 0; y < bimage.getHeight(); y++) {
                                    final int argb = bimage.getRGB(x,y);

                                    final int rgb = argb & 0xFFFFFF;

                                    // make sure there's no duplicate rgb values
                                    if (colorSet.contains(rgb)) {
                                        continue;
                                    }

                                    colorSet.add(rgb);

                                }
                            }

                            images.add(bimage);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }

                    }

                    // the largest width found in this image archive
                    idxOut.writeShort(largestWidth);

                    // the largest height found in this image archive
                    idxOut.writeShort(largestHeight);

                    // the palette size
                    idxOut.writeByte(colorSet.size());

                    // make sure to skip the first index
                    for (int i = 1; i < colorSet.size(); i++) {
                        ByteBufferUtils.writeU24Int(colorSet.get(i), idxOut);
                    }

                    for (BufferedImage bimage : images) {
                        // offsetX
                        idxOut.writeByte(0);

                        // offsetY
                        idxOut.writeByte(0);

                        // image width
                        idxOut.writeShort(bimage.getWidth());

                        // image height
                        idxOut.writeShort(bimage.getHeight());

                        // encoding type (0 horizontal | 1 vertical)
                        idxOut.writeByte(encodingType);

                    }

                    datOut.writeShort(idxOffset);

                    idxOffset = idxOut.size();

                    for (BufferedImage bimage : images) {

                        if (encodingType == 0) { // horizontal encoding
                            for (int y = 0; y < bimage.getHeight(); y++) {
                                for (int x = 0; x < bimage.getWidth(); x++) {
                                    final int argb = bimage.getRGB(x, y);

                                    final int rgb = argb & 0xFFFFFF;

                                    final int paletteIndex = colorSet.indexOf(rgb);

                                    assert(paletteIndex != -1);

                                    datOut.writeByte(paletteIndex);
                                }
                            }
                        } else { // vertical encoding
                            for (int x = 0; x < bimage.getWidth(); x++) {
                                for (int y = 0; y < bimage.getHeight(); y++) {
                                    final int argb = bimage.getRGB(x, y);

                                    final int rgb = argb & 0xFFFFFF;

                                    final int paletteIndex = colorSet.indexOf(rgb);

                                    assert(paletteIndex != -1);

                                    datOut.writeByte(paletteIndex);
                                }
                            }
                        }
                    }

                }

                final byte[] uncompresedData = dbos.toByteArray();

                try {
                    int hash = Integer.parseInt(imageArchiveDir.getName());
                } catch (Exception ex) {

                }

                final String fileName = imageArchiveDir.getName();

                int hash = 0;

                if (fileName.matches("[0-9]+")) {
                    hash = Integer.parseInt(imageArchiveDir.getName());
                }

                if (hash == 0) {
                    hash = HashUtils.nameToHash(imageArchiveDir.getName() + ".dat");
                }

                final byte[] compressedData = CompressionUtil.bzip2(uncompresedData);

                archive.getEntries().add(new Archive.ArchiveEntry(hash, uncompresedData.length, compressedData.length, compressedData));

            }

            final byte[] uncompressed = ibos.toByteArray();

            final byte[] compressed = CompressionUtil.bzip2(uncompressed);

            archive.getEntries().add(new Archive.ArchiveEntry(HashUtils.nameToHash("index.dat"), uncompressed.length, compressed.length, compressed));

            final byte[] encoded = archive.encode();

            try(FileOutputStream fos = new FileOutputStream(new File(outputDir, "sprites.dat"))) {
                fos.write(encoded);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
