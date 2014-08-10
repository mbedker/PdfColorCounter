package model;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class PDFSession extends Model {

    @Id
    public String sessionId;

    public long startDate;

    public Long endDate;

    public int numberOfPages;

    public boolean isComplete;

}
