package model;

public class PageInformation {
    private final int pageNumber;
    private final int percentColor;

    public PageInformation(int pageNumber, int percentColor){
        this.pageNumber = pageNumber;
        this.percentColor = percentColor;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getPercentColor() {
        return percentColor;
    }
}
