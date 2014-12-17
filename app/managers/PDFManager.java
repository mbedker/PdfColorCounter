package managers;

import com.avaje.ebean.Ebean;
import model.*;
import org.icepdf.core.exceptions.PDFException;
import org.icepdf.core.exceptions.PDFSecurityException;
import org.icepdf.core.pobjects.*;
import org.icepdf.core.util.GraphicsRenderingHints;
import util.ColorUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PDFManager {

    private static final ThreadFactory MIN_PRIORITY_THREAD_FACTORY = new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setPriority(Thread.MIN_PRIORITY);
            return t;
        }
    };

    private static final int MAX_GREYSCALE_DIFFERENCE = 5;

    private static PDFManager mInstance = null;

    private final ExecutorService mDocumentExecutor = Executors.newCachedThreadPool();

    public static PDFManager get() {
        if (mInstance == null)
            mInstance = new PDFManager();
        return mInstance;
    }

    private PDFManager() {
    }

    public PDFSession parsePDF(final File pdfFile) throws IOException {
        if (pdfFile == null)
            throw new IllegalArgumentException("File cannot be null.");
        if (!pdfFile.exists())
            throw new IllegalArgumentException("File must exist.");

        final Document document = new Document();
        try {
            String path = pdfFile.getAbsolutePath();
            System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
            System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
            System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
            System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
            System.out.println(path);
            document.setFile(path);
        } catch (PDFException | PDFSecurityException | IOException e) {
            e.printStackTrace();
        }

        final PDFSession session = new PDFSession();
        session.setStartDate(System.currentTimeMillis());
        session.setNumberOfPages(document.getNumberOfPages());

        Ebean.save(session);

        mDocumentExecutor.execute(new Runnable() {
            @Override
            public void run() {
                startParsingPdf(session, document);
            }
        });

        return session;
    }


    private void startParsingPdf(final PDFSession session, final Document document) {
        int numOfPages = session.getNumberOfPages();

        ExecutorService pageExecutor = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors() + 1, MIN_PRIORITY_THREAD_FACTORY);

        for (int i = 0; i < numOfPages; i++) {
            final int finalI = i;
            final int pageNum = i + 1;
            pageExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    String sessionInfoString = "Session: " + session.getSessionId() + " Page: " + pageNum;
                    System.out.println("Start - " + sessionInfoString);

                    try {
                        // the pageNum the docment takes appears to be actually an index.
                        BufferedImage image = (BufferedImage) document.getPageImage(finalI, GraphicsRenderingHints.SCREEN,
                                Page.BOUNDARY_CROPBOX, 0f, 1f);

                        int percentColor = calculatePercentColor(image);

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ImageIO.write(image, "png", baos);

                        ParsedPDFPage page = new ParsedPDFPage(session.getSessionId(), pageNum, percentColor,
                                baos.toByteArray());

                        Ebean.save(page);
                    } catch (IOException e) {
                        throw new RuntimeException("Error - " + sessionInfoString, e);
                    }

                    System.out.println("End - " + sessionInfoString);
                }
            });
        }

        {
            pageExecutor.shutdown();
            try {
                pageExecutor.awaitTermination(5, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (!pageExecutor.isShutdown()) {
                pageExecutor.shutdownNow();
            }
        }

        session.setComplete(true);
        session.setEndDate(System.currentTimeMillis());
        Ebean.save(session);
    }


    private int calculatePercentColor(BufferedImage image) {
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

    public PDFSessionStatus getStatus(String pdfSessionID) {
        PDFSession session = Ebean.find(PDFSession.class, pdfSessionID);

        if (session == null)
            return null;

        List<ParsedPDFPage> parsedPDFPages = Ebean.createQuery(ParsedPDFPage.class)
                .orderBy("pageNumber ASC")
                .where()
                .eq("sessionId", pdfSessionID)
                .findList();

        ArrayList<PageInformation> completedPages = new ArrayList<PageInformation>(parsedPDFPages.size());
        PDFSessionStatus status = new PDFSessionStatus();
        status.setSessionId(pdfSessionID);

        for (ParsedPDFPage page : parsedPDFPages) {
            PageInformation pageInfo = new PageInformation(page.getPageNumber(), page.getPercentColor());

            completedPages.add(pageInfo);
        status.setCompletedPages(completedPages);
        }
        status.setIsComplete(session.getIsComplete());

        return status;
    }

    public String getPageInformation(String pdfSessionId, int pageNumber) {
        ParsedPDFPage page = Ebean.createQuery(ParsedPDFPage.class)
                .where()
                .eq("sessionId", pdfSessionId)
                .eq("pageNumber", pageNumber)
                .findUnique();
        return (page == null) ? null : "Page " + page.getPageNumber() + " is " + (page.getPercentColor()) + " percent color";
    }

    public byte[] getPageImage(String pdfSessionId, int pageNumber) {
        ParsedPDFPage page = Ebean.createQuery(ParsedPDFPage.class)
                .where()
                .eq("sessionId", pdfSessionId)
                .eq("pageNumber", pageNumber)
                .findUnique();

        return (page == null) ? null : page.getImageBlob();
    }

    public byte[] getPageThumbnail(String pdfSessionId, int pageNumber) {
        ParsedPDFPage page = Ebean.createQuery(ParsedPDFPage.class)
                .where()
                .eq("sessionId", pdfSessionId)
                .eq("pageNumber", pageNumber)
                .findUnique();

        return (page == null) ? null : page.getImageBlob();
    }
}