package model;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class PDFSession extends Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String sessionId;

    private long startDate;

    private Long endDate;

    private int numberOfPages;

    private boolean isComplete;

    public PDFSession() {
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    public String getSessionId(){
        return sessionId;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getStartDate(){
        return startDate;
    }
    public void setNumberOfPages(int numberOfPages){
        this.numberOfPages = numberOfPages;
    }
    public int getNumberOfPages(){
        return numberOfPages;
    }

    public void setEndDate(Long endDate) {
        this.endDate = endDate;
    }

    public Long getEndDate() {
        return endDate;
    }

    public void setComplete(boolean isComplete) {
        this.isComplete = isComplete;
    }
    public boolean getIsComplete(){
        return isComplete;
    }
}
