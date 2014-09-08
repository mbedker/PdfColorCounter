package model;


import java.util.List;

public class PDFSessionStatus {

    private String sessionId;

    private List<PageInformation> completedPages;

    private boolean isComplete;

    public void setSessionId(String sessionId){
        this.sessionId = sessionId;
    }
    public String getSessionId() {
        return sessionId;
    }

    public void setCompletedPages(List<PageInformation> completedPages){
        this.completedPages = completedPages;
    }
    public List<PageInformation> getCompletedPages() {
        return completedPages;
    }

    public void setIsComplete(boolean isComplete) {
        this.isComplete = isComplete;
    }
    public boolean getIsComplete() {return isComplete;}
}
