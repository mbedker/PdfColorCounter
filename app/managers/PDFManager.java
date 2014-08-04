package managers;


import com.avaje.ebean.Ebean;
import model.PDFSession;
import model.PDFSessionStatus;
import model.PageInformation;
import model.ParsedPDFPage;
import org.icepdf.core.exceptions.PDFException;
import org.icepdf.core.exceptions.PDFSecurityException;
import org.icepdf.core.pobjects.*;
import org.icepdf.core.util.GraphicsRenderingHints;
import util.ColorUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
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

    private PDFManager() {
    }


    public PDFSession parsePDF(final File pdfFile) throws IOException {
        final Document document = new Document();
        try {
            document.setFile(pdfFile.getAbsolutePath());
        } catch (PDFException e) {
            e.printStackTrace();
        } catch (PDFSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        final PDFSession session = new PDFSession();
        session.startDate = System.currentTimeMillis();
        session.numberOfPages = document.getNumberOfPages();
        startParsingPdf(session, document);
        Ebean.save(session);
        return session;
    }
    //scott has threading block here

    private void startParsingPdf(final PDFSession session, final Document document) throws IOException {
        int pageNum = session.numberOfPages;
        // scott's countdown latch

        for (int i = 0; i < pageNum; i++) {
            final int finalI = i + 1;
            BufferedImage image = (BufferedImage) document.getPageImage(finalI - 1, GraphicsRenderingHints.SCREEN,
                    Page.BOUNDARY_CROPBOX, 0f, 1f);
            int percentColor = colorPercentCounter(image);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);

            ParsedPDFPage parsedPDFPage = new ParsedPDFPage();
            parsedPDFPage.sessionId = session.id;
            parsedPDFPage.date = System.currentTimeMillis();
            parsedPDFPage.pageNumber = finalI;
            parsedPDFPage.percentColor = percentColor;
            parsedPDFPage.imageBlob = baos.toByteArray() ;

            System.out.println(parsedPDFPage.pageNumber + " : " + parsedPDFPage.percentColor);
            Ebean.save(parsedPDFPage);
        }
    }

    private int colorPercentCounter(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int count = 0;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int color = image.getRGB(x, y);
                if (!ColorUtil.isGreyscale(color, MAX_GREYSCALE_DIFFERENCE)) {
                    count++;
                }
            }
        }
        return 100 * count / (width * height);
    }

    public List<PageInformation> pageInformation (String pdfSessionId, int pageNumber) {
        List<ParsedPDFPage> parsedPages = Ebean.createQuery(ParsedPDFPage.class)
                .orderBy("pageNumber ASC")
                .where()
                .eq("sessionId", pdfSessionId)
                .findList();
        List<PageInformation> pageInformationList = new ArrayList<>(parsedPages.size());
        for (ParsedPDFPage page: parsedPages){
            PageInformation pageInformation = new PageInformation();
            pageInformation.pageNumber = page.pageNumber;
            pageInformation.percentColor = page.percentColor;
            pageInformation.imageblob = page.imageBlob;
            pageInformationList.add(pageInformation);
        }
        return pageInformationList;
    }

    public PDFSessionStatus getStatus(String pdfSessionID) {
        PDFSession session = Ebean.find(PDFSession.class, pdfSessionID);

        if (session == null)
            return null;

        List<ParsedPDFPage> parsedPDFPages = Ebean.createQuery(ParsedPDFPage.class)
                .orderBy("pageNumber ASC")
                .where()
                .eq("sessionID", pdfSessionID)
                .findList();

        PDFSessionStatus  status= new PDFSessionStatus();
        status.sessionId = pdfSessionID;
        status.competedPages = new ArrayList<Integer>(parsedPDFPages.size());

        for (ParsedPDFPage page : parsedPDFPages) {
            status.competedPages.add(page.pageNumber);
        }
        return status;
    }
}

