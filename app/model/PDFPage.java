package model;


public class PDFPage {
    private int mPercentColor;
    private int mPageNumber;

    public PDFPage(int percentColor, int pageNumber){
        mPercentColor = percentColor;
        mPageNumber = pageNumber;
    }


    public int getPercentColor() {return mPercentColor;}

    public void setPercentColor(int percentColor) {mPercentColor = percentColor;}

    public int getPageNumber() {return mPageNumber;}

    public void setPageNumber (int pageNumber) {mPageNumber = pageNumber;}

}
