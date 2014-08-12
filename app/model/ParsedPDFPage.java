package model;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

import play.db.ebean.Model;

@Entity
public class ParsedPDFPage extends Model {
    @Id
    public String id;

    public long date;

    public String sessionId;

    public int pageNumber;

    public Integer percentColor;

    public boolean toBeReviewed;

    public boolean exceedsThreshold;

    @Lob
    public byte[] imageBlob;

    @Lob
    public byte[] thumbnailBlob;
}