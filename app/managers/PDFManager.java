package managers;


import javafx.stage.Screen;
import model.PDFPage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.icepdf.core.exceptions.PDFException;
import org.icepdf.core.pobjects.*;
import org.icepdf.core.*;
import org.icepdf.core.util.GraphicsRenderingHints;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PDFManager {

    private static final int MAX_GREYSCALE_DIFFERENCE = 5;

    private static PDFManager mInstance;

    public static PDFManager get() {
        if (mInstance == null)
            mInstance = new PDFManager();
        return mInstance;
    }

    private PDFManager() {}


    public List<PDFPage> parsePDF (File file)throws IOException {
        List<PDFPage> parsedPDFpages = null;
        Document document = new Document();
        List<BufferedImage> pages = null;
        for (int i = 0; i < document.getNumberOfPages(); i++) {
            BufferedImage image = (BufferedImage) document.getPageImage(i, GraphicsRenderingHints.SCREEN, Page.BOUNDARY_CROPBOX, 0f, 0f);
            pages.add(image);
        }
        for (int i = 0; i < pages.size(); i++) {
            PDFPage pageHolder = new PDFPage(colorPercentCounter(pages.get(i)), i+1);
            parsedPDFpages.add(i, pageHolder);
        }
        return parsedPDFpages;
    }

    private int colorPercentCounter(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int count = 0;

        for (int x = 0; x < width; x ++) {
            for (int y = 0; y < height; y ++) {
                int color = image.getRGB(x, y);
                if (!isGreyscale(color)) {
                    count++;
                }
            }
        }
        return 100 * count / (width * height);
    }


    private boolean isGreyscale(int color) {
        // A perfect Greyscale color would be one where R, G, and B were equal.
        int red = red(color);
        int green = green(color);
        int blue = blue(color);

        int max = Math.max(Math.max(red, green), blue);
        int min = Math.min(Math.min(red, green), blue);

        return (max - min) <= MAX_GREYSCALE_DIFFERENCE;
    }

    /**
     * Return the red component of a color int. This is the same as saying
     * (color >> 16) & 0xFF
     */
    public static int red(int color) {
        return (color >> 16) & 0xFF;
    }

    /**
     * Return the green component of a color int. This is the same as saying
     * (color >> 8) & 0xFF
     */
    public static int green(int color) {
        return (color >> 8) & 0xFF;
    }

    /**
     * Return the blue component of a color int. This is the same as saying
     * color & 0xFF
     */
    public static int blue(int color) {
        return color & 0xFF;
    }
}


