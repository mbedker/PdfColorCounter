package model;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

import play.db.ebean.Model;

@Entity
public class ParsedPDFPage extends Model {
    @Id

    private final String sessionId;

    private final int pageNumber;

    private final Integer percentColor;

    @Lob
    private final byte[] imageBlob;

    @Lob
    private final byte[] thumbnailBlob;

    public ParsedPDFPage( String sessionId,
                         int pageNumber, Integer percentColor, byte[] imageBlob, byte[] thumbnailBlob){
        this.sessionId = sessionId;
        this.pageNumber = pageNumber;
        this.percentColor = percentColor;
        this.imageBlob = imageBlob;
        this.thumbnailBlob = thumbnailBlob;
    }

    public String getSessionId() {return sessionId;}
    public int getPageNumber() {return pageNumber;}
    public int getPercentColor() {return percentColor;}
    public byte[] getImageBlob() {return imageBlob;}
    public byte[] getThumbnailBlob() {return thumbnailBlob;}

}