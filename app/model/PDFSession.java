package model;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class PDFSession extends Model {

    @Id
    public String id;

    public long startDate;

    public Long endDate;

    public int numberOfPages;

    public boolean isComplete;

}
