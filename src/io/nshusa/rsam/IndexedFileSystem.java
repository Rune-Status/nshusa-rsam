package io.nshusa.rsam;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class IndexedFileSystem implements Closeable {

    private Path root;

    private final FileStore[] fileStores = new FileStore[255];

    private boolean loaded;

    private IndexedFileSystem(Path root) {
        this.root = root;
    }

    public static IndexedFileSystem init(Path root) {
        return new IndexedFileSystem(root);
    }

    public void initialize(int count) throws IOException {
        if (count <= 0) {
            return;
        }

        final Path dataPath = root.resolve("main_file_cache.dat");

        if (!Files.exists(dataPath)) {
            Files.createFile(dataPath);
        }

        for (int i = 0; i < count; i++) {
            Path indexPath = root.resolve("main_file_cache.idx" + i);

            if (!Files.exists(indexPath)) {
                Files.createFile(indexPath);
            }
        }
    }

    public boolean load() {
        try {
            if (!Files.exists(root)) {
                Files.createDirectory(root);
            }

            final Path dataPath = root.resolve("main_file_cache.dat");

            if (!Files.exists(dataPath)) {
                return false;
            }

            RandomAccessFile dataRaf = new RandomAccessFile(dataPath.toFile(), "rw");
            for (int i = 0; i < 255; i++) {
                Path indexPath = root.resolve("main_file_cache.idx" + i);
                if (Files.exists(indexPath)) {
                    fileStores[i] = new FileStore(i, dataRaf.getChannel(), new RandomAccessFile(indexPath.toFile(), "rw").getChannel());
                }
            }
            loaded = true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean createStore(int storeId) throws IOException {
        if (storeId < 0 || storeId >= fileStores.length) {
            return false;
        }

        if (fileStores[storeId] != null) {
            return false;
        }

        final Path dataPath = root.resolve("main_file_cache.dat");

        if (!Files.exists(dataPath)) {
            Files.createFile(dataPath);
        }

        final Path path = root.resolve("main_file_cache.idx" + storeId);

        if (!Files.exists(path)) {
            Files.createFile(path);
        }

        final RandomAccessFile dataRaf = new RandomAccessFile(dataPath.toFile(), "rw");

        fileStores[storeId] = new FileStore(storeId + 1, dataRaf.getChannel(), new RandomAccessFile(path.toFile(), "rw").getChannel());
        return true;
    }

    public boolean defragment() {
        try {
            File[] files = root.toFile().listFiles();

            if (files == null) {
                return false;
            }

            Map<Integer, List<ByteBuffer>> map = new LinkedHashMap<>();

            for (int store = 0; store < getStoreCount(); store++) {

                FileStore fileStore = getStore(store);
                if (fileStore == null) {
                    continue;
                }

                map.put(store, new ArrayList<>());

                for (int file = 0; file < fileStore.getFileCount(); file++) {
                    ByteBuffer buffer = fileStore.readFile(file);

                    List<ByteBuffer> data = map.get(store);
                    data.add(buffer);
                }

            }

            close();

            for (File f : files) {
                if (f.isDirectory() || !f.getName().contains("main_file_cache")) {
                    continue;
                }

                f.delete();
            }

            initialize(map.size());

            load();

            for (Map.Entry<Integer, List<ByteBuffer>> entry : map.entrySet()) {

                int fileStoreId = entry.getKey();

                FileStore fileStore = getStore(fileStoreId);

                for (int file = 0; file < entry.getValue().size(); file++) {
                    ByteBuffer data = entry.getValue().get(file);
                    fileStore.writeFile(file, data == null ? new byte[0] : data.array());
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
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

    void setRoot(Path root) {
        try {
            close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Arrays.fill(fileStores, null);

        this.root = root;
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

            synchronized (fileStore) {
                fileStore.close();
            }

        }
    }

}
