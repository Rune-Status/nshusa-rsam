import io.nshusa.rsam.FileStore;
import io.nshusa.rsam.IndexedFileSystem;
import io.nshusa.rsam.binary.Archive;
import io.nshusa.rsam.binary.Font;
import io.nshusa.rsam.binary.Widget;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class WidgetTest {

    @Test
    public void test() {
        try(IndexedFileSystem fs = IndexedFileSystem.init(Paths.get("cache"))) {
            fs.load();
            FileStore archiveStore = fs.getStore(FileStore.ARCHIVE_FILE_STORE);

            Archive widgetArchive = Archive.decode(archiveStore.readFile(Archive.INTERFACE_ARCHIVE));
            Archive graphicArchive = Archive.decode(archiveStore.readFile(Archive.MEDIA_ARCHIVE));
            Archive fontArchive = Archive.decode(archiveStore.readFile(Archive.TITLE_ARCHIVE));

            Font smallFont = Font.decode(fontArchive, "p11_full", false);
            Font frameFont = Font.decode(fontArchive, "p12_full", false);
            Font boldFont = Font.decode(fontArchive, "b12_full", false);
            Font font2 =  Font.decode(fontArchive, "q8_full", true);

            Font[] fonts = {smallFont, frameFont, boldFont, font2};
            Widget.decode(widgetArchive, graphicArchive, fonts);

            exportRectangle();

            System.out.println(String.format("There are %s widgets.", Widget.count()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void exportWidget(int id) throws IOException {
        Widget widget = Widget.lookup(id);

        if (widget == null) {
            return;
        }

        BufferedImage bimage = widget.toBufferedImage();

        if (bimage == null) {
            return;
        }

        System.out.println(widget.defaultText);

        ImageIO.write(bimage, "png", new File(id + ".png"));
    }

    private static void exportRectangle() {
        try {
            for (int i = 0; i < Widget.count(); i++) {
                Widget widget = Widget.lookup(i);

                if (widget == null) {
                    return;
                }

                if (widget.group == Widget.TYPE_RECTANGLE) {
                    BufferedImage bimage = widget.toBufferedImage();

                    if (bimage == null) {
                        return;
                    }

                    System.out.println(widget.defaultText);

                    ImageIO.write(bimage, "png", new File(widget.id + ".png"));
                    break;
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }



}
