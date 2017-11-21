# RuneScape Asset Manager (RSAM)
A lightweight library for accessing and modifying RuneScape's 2006 file system. It's designed to be lightweight and easy to use.

### Required Libraries
[Apache Commons Compress 1.14](https://mvnrepository.com/artifact/org.apache.commons/commons-compress/1.14)

### Loading cache
```java
 try(IndexedFileSystem fs = IndexedFileSystem.init(Paths.get("./Cache/"))) {

} catch (IOException e) {
    e.printStackTrace();
}
```

### Accessing a file store
```java
FileStore store = fs.getStore(FileStore.MODEL_FILE_STORE);
```

### Reading from a file store
```java
store.readFile(100);

// or

fs.readFile(FileStore.MODEL_FILE_STORE, 100);
```

### Accessing a file archive
```java
FileStore archiveStore = fs.getStore(FileStore.ARCHIVE_FILE_STORE);
Archive configArchive = Archive.decode(archiveStore.readFile(Archive.CONFIG_ARCHIVE));

// or
Archive configArchive = Archive.decode(fs.readFile(FileStore.ARCHIVE_FILE_STORE, Archive.CONFIG_ARCHIVE));
```

### Reading from a file archive
```java
FileStore archiveStore = fs.getStore(FileStore.ARCHIVE_FILE_STORE);
Archive archive = Archive.decode(archiveStore.readFile(Archive.CONFIG_ARCHIVE));

if (archive.contains("obj.dat")) {
    ByteBuffer dataBuf = archive.readFile("obj.dat");

    try(FileChannel channel = new FileOutputStream(new File("./obj.dat")).getChannel()) {
        channel.write(dataBuf);
    }
}
```

### Reading from an ImageArchive
```java
        try(IndexedFileSystem fs = IndexedFileSystem.init(Paths.get("./Cache/"))) {

            FileStore store = fs.getStore(FileStore.ARCHIVE_FILE_STORE);

            Archive mediaArchive = Archive.decode(store.readFile(Archive.MEDIA_ARCHIVE));

            ImageArchive imageArchive = ImageArchive.decode(mediaArchive, "mod_icons.dat");

            for (int i = 0; i < imageArchive.getSprites().size(); i++) {
                Sprite sprite = imageArchive.getSprites().get(i);

                ImageIO.write(sprite.toBufferedImage(), "png", new File(output, i + ".png"));
            }

            System.out.println(String.format("There are %d sprites in archive=%d", imageArchive.getSprites().size(), imageArchive.getHash()));

        }
```

### Reading Widgets and fonts
```java
        try(IndexedFileSystem fs = IndexedFileSystem.init(Paths.get("./cache/"))) {
            FileStore archiveStore = fs.getStore(FileStore.ARCHIVE_FILE_STORE);

            Archive widgetArchive = Archive.decode(archiveStore.readFile(Archive.INTERFACE_ARCHIVE));
            Archive graphicArchive = Archive.decode(archiveStore.readFile(Archive.MEDIA_ARCHIVE));
            Archive fontArchive = Archive.decode(archiveStore.readFile(Archive.TITLE_ARCHIVE));

            Font smallFont = new Font(fontArchive, "p11_full", false);
            Font frameFont = new Font(fontArchive, "p12_full", false);
            Font boldFont = new Font(fontArchive, "b12_full", false);
            Font font2 = new Font(fontArchive, "q8_full", true);

            Font[] fonts = {smallFont, frameFont, boldFont, font2};
            Widget.load(widgetArchive, graphicArchive, fonts);

            System.out.println(String.format("There are %s widgets.", Widget.widgets.length));

        } catch (IOException e) {
            e.printStackTrace();
        }
```
