package model;


import java.util.List;

public class PDFSessionStatus {

    private final String sessionId;

    private final List<PageInformation> completedPages;

    public PDFSessionStatus(String sessionId, List<PageInformation> completePages){
        this.sessionId = sessionId;
        this.completedPages = completePages;
    }

    public String getSessionId() {
        return sessionId;
    }

    public List<PageInformation> getCompletedPages() {
        return completedPages;
    }
}
