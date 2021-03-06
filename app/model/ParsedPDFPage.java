package model;


import javax.persistence.*;

import play.db.ebean.Model;

@Entity
public class ParsedPDFPage extends Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public String id;

    private final String sessionId;

    private final int pageNumber;

    private final Integer percentColor;

    @Lob
    private final byte[] imageBlob;

    public ParsedPDFPage(String sessionId, int pageNumber, Integer percentColor, byte[] imageBlob){
        this.sessionId = sessionId;
        this.pageNumber = pageNumber;
        this.percentColor = percentColor;
        this.imageBlob = imageBlob;
    }

    public String getSessionId() {return sessionId;}
    public int getPageNumber() {return pageNumber;}
    public int getPercentColor() {return percentColor;}
    public byte[] getImageBlob() {return imageBlob;}

}