# RuneScape Asset Manager (RSAM)
Is a library for reading and manipulating contents from the old RuneScape file system. It's designed to be lightweight and easy to use.

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
```

### Accessing a file archive
```java
FileStore archiveStore = fs.getStore(FileStore.ARCHIVE_FILE_STORE);
Archive archive = Archive.decode(archiveStore.readFile(Archive.CONFIG_ARCHIVE));
```

### Reading from a file archive
```java
FileStore archiveStore = fs.getStore(FileStore.ARCHIVE_FILE_STORE);
Archive archive = Archive.decode(archiveStore.readFile(Archive.CONFIG_ARCHIVE));

if (archive.contains("obj.dat")) {
    byte[] data = archive.readFile("obj.dat");
}
```

