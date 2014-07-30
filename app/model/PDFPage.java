package model;


public class PDFPage {
    private int mPercentColor;

    public PDFPage(int percentColor){
        mPercentColor = percentColor;
    }

    public int getPercentColor() {return mPercentColor;}

    public void setPercentColor(int percentColor) {mPercentColor = percentColor;}

}
