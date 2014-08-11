package managers;


import com.avaje.ebean.Ebean;
import model.*;
import org.icepdf.core.exceptions.PDFException;
import org.icepdf.core.exceptions.PDFSecurityException;
import org.icepdf.core.pobjects.*;
import org.icepdf.core.util.GraphicsRenderingHints;
import play.mvc.Http;
import util.ColorUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PDFManager {

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
        final Document document = new Document();
        try {
            document.setFile(pdfFile.getAbsolutePath());
        } catch (PDFException | PDFSecurityException | IOException e) {
            e.printStackTrace();
        }


        final PDFSession session = new PDFSession();
        session.startDate = System.currentTimeMillis();
        session.numberOfPages = document.getNumberOfPages();
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
        int pageNum = session.numberOfPages;
        final CountDownLatch latch = new CountDownLatch(pageNum);

        ExecutorService mPageExecutor = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors() + 1, new ThreadFactory() {
                    private AtomicInteger counter = new AtomicInteger(0);

                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r, "PDFParseThread-" + counter.incrementAndGet());
                        t.setPriority(Thread.MIN_PRIORITY);
                        return t;
                    }
                });

        for (int i = 0; i < pageNum; i++) {
            final int finalI = i + 1;
            mPageExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        System.out.println("Thread" + finalI + "initiated");
                        BufferedImage image = (BufferedImage) document.getPageImage(finalI - 1, GraphicsRenderingHints.SCREEN,
                                Page.BOUNDARY_CROPBOX, 0f, 1f);

                        int percentColor = colorPercentCounter(image);

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ImageIO.write(image, "png", baos);

                        ParsedPDFPage page = new ParsedPDFPage();
                        page.sessionId = session.sessionId;
                        page.date = System.currentTimeMillis();
                        page.pageNumber = finalI;
                        page.percentColor = percentColor;
                        page.imageBlob = baos.toByteArray();

                        Ebean.save(page);
                    } catch (Exception ignored) {
                        //Database entry will be missing and signify an error
                    } finally {
                        System.out.println("Thread" + finalI + "Completed");
                        latch.countDown();
                    }

                }
            });
        }
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
        }
        session.isComplete = true;
        session.endDate = System.currentTimeMillis();
        Ebean.save(session);
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

    public String getPageInformation(String pdfSessionId, int pageNumber) {
        ParsedPDFPage page = Ebean.createQuery(ParsedPDFPage.class)
                .where()
                .eq("sessionId", pdfSessionId)
                .eq("pageNumber", pageNumber)
                .findUnique();
        return (page == null) ? null: "Page " + page.pageNumber + " is " + page.percentColor.toString() + " percent color";
    }

    public byte[] getPageImage(String pdfSessionId, int pageNumber) {
        ParsedPDFPage page = Ebean.createQuery(ParsedPDFPage.class)
                .where()
                .eq("sessionId", pdfSessionId)
                .eq("pageNumber", pageNumber)
                .findUnique();

        return (page == null) ? null : page.imageBlob;
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

        PDFSessionStatus  status= new PDFSessionStatus();
        status.sessionId = pdfSessionID;
        status.competedPages = new ArrayList<Integer>(parsedPDFPages.size());

        for (ParsedPDFPage page : parsedPDFPages) {
            status.competedPages.add(page.pageNumber);
        }
        return status;
    }

    public List<ThumbnailPageInformation> thumbnailPageInformation (String pdfSessionId) {
        List<ParsedPDFPage> parsedPages= Ebean.createQuery(ParsedPDFPage.class)
                .orderBy("pageNumber ASC")
                .where()
                .eq("sessionId", pdfSessionId)
                .findList();
        List<ThumbnailPageInformation> thumbnailList = new ArrayList<>(parsedPages.size());
        for (ParsedPDFPage page : parsedPages){

        }
        return null;
    }
}

