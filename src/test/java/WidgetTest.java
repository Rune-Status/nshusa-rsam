import io.nshusa.rsam.RSFileStore;
import io.nshusa.rsam.IndexedFileSystem;
import io.nshusa.rsam.binary.RSArchive;
import io.nshusa.rsam.binary.RSFont;
import io.nshusa.rsam.binary.RSWidget;
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
            RSFileStore archiveStore = fs.getStore(RSFileStore.ARCHIVE_FILE_STORE);

            RSArchive widgetArchive = RSArchive.decode(archiveStore.readFile(RSArchive.INTERFACE_ARCHIVE));
            RSArchive graphicArchive = RSArchive.decode(archiveStore.readFile(RSArchive.MEDIA_ARCHIVE));
            RSArchive fontArchive = RSArchive.decode(archiveStore.readFile(RSArchive.TITLE_ARCHIVE));

            RSFont smallFont = RSFont.decode(fontArchive, "p11_full", false);
            RSFont frameFont = RSFont.decode(fontArchive, "p12_full", false);
            RSFont boldFont = RSFont.decode(fontArchive, "b12_full", false);
            RSFont font2 =  RSFont.decode(fontArchive, "q8_full", true);

            RSFont[] fonts = {smallFont, frameFont, boldFont, font2};
            RSWidget.decode(widgetArchive, graphicArchive, fonts);

            exportRectangle();

            System.out.println(String.format("There are %s widgets.", RSWidget.count()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void exportWidget(int id) throws IOException {
        RSWidget widget = RSWidget.lookup(id);

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
            for (int i = 0; i < RSWidget.count(); i++) {
                RSWidget widget = RSWidget.lookup(i);

                if (widget == null) {
                    return;
                }

                if (widget.group == RSWidget.TYPE_RECTANGLE) {
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
