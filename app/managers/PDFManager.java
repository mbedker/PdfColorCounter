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


        ExecutorService pageExecutor = Executors.newFixedThreadPool(
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
            pageExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        System.out.println("Thread" + finalI + "initiated");
                        BufferedImage image = (BufferedImage) document.getPageImage(finalI - 1, GraphicsRenderingHints.SCREEN,
                                Page.BOUNDARY_CROPBOX, 0f, 1f);

                        int percentColor = calculatePercentColor(image);

                        BufferedImage thumbailImage = createThumbnail(image);

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ImageIO.write(image, "png", baos);

                        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
                        ImageIO.write(thumbailImage, "png", baos2);

                        ParsedPDFPage page = new ParsedPDFPage();
                        page.sessionId = session.sessionId;
                        page.date = System.currentTimeMillis();
                        page.pageNumber = finalI;
                        page.percentColor = percentColor;
                        page.imageBlob = baos.toByteArray();
                        page.thumbnailBlob = baos2.toByteArray();

                        Ebean.save(page);
                    } catch (IOException e) {
                        throw new RuntimeException("Error parsing page" + finalI + "(" + e + ")");
                    } finally {
                        System.out.println("Thread" + finalI + "Completed");
                    }
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

        session.isComplete = true;
        session.endDate = System.currentTimeMillis();
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

    private static final int LARGEST_DIMENSION = 150;

    public static BufferedImage createThumbnail(BufferedImage image) {
        int imageHeight = image.getHeight();
        int imageWidth = image.getWidth();

        int finalHeight;
        int finalWidth;
        if (imageHeight > imageWidth) {
            finalHeight = LARGEST_DIMENSION;
            //double factor = (double) LARGEST_DIMENSION / imageHeight;
            // finalWidth = (int) factor * imageWidth;
            //TODO stop this from returning as a zero and breaking the app
            finalWidth = 116;
        } else {
            finalWidth = LARGEST_DIMENSION;
            //double factor = (double) LARGEST_DIMENSION / imageWidth;
            //finalHeight = (int)factor * imageHeight;
            //TODO stop this from returning as zero ad breaking the app
            finalHeight = 116;
        }

        Image thumbnailImage = image.getScaledInstance(finalWidth, finalHeight, Image.SCALE_DEFAULT);
        BufferedImage bufferedThumbnail = new BufferedImage(finalWidth, finalHeight, image.getType());
        bufferedThumbnail.createGraphics().drawImage(thumbnailImage, 0, 0, null);

        return bufferedThumbnail;
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

        for (ParsedPDFPage page : parsedPDFPages) {
            PageInformation pageInfo = new PageInformation();
            pageInfo.pageNumber = page.pageNumber;
            pageInfo.percentColor = page.percentColor;

            completedPages.add(pageInfo);
        }
        PDFSessionStatus status = new PDFSessionStatus(pdfSessionID, completedPages);
        return status;
    }

    public String getPageInformation(String pdfSessionId, int pageNumber) {
        ParsedPDFPage page = Ebean.createQuery(ParsedPDFPage.class)
                .where()
                .eq("sessionId", pdfSessionId)
                .eq("pageNumber", pageNumber)
                .findUnique();
        return (page == null) ? null : "Page " + page.pageNumber + " is " + page.percentColor.toString() + " percent color";
    }

    public byte[] getPageImage(String pdfSessionId, int pageNumber) {
        ParsedPDFPage page = Ebean.createQuery(ParsedPDFPage.class)
                .where()
                .eq("sessionId", pdfSessionId)
                .eq("pageNumber", pageNumber)
                .findUnique();

        return (page == null) ? null : page.imageBlob;
    }

    public byte[] getPageThumbnail(String pdfSessionId, int pageNumber) {
        ParsedPDFPage page = Ebean.createQuery(ParsedPDFPage.class)
                .where()
                .eq("sessionId", pdfSessionId)
                .eq("pageNumber", pageNumber)
                .findUnique();

        return (page == null) ? null : page.thumbnailBlob;
    }
}