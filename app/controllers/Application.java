package controllers;
// play debug : ./activator -jvm-debug <port> ~run
import managers.PDFManager;
import model.*;
import play.api.Play;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.index;

import java.io.File;
import java.io.IOException;

public class Application extends Controller {

    //play framework controller
    public static Result index(){
        return ok(index.render("Test"));
    }


    public static Result countPDF() {
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart pdfFilePart = body.getFile("pdfFile");

        if (pdfFilePart != null) {
            System.out.println("The parsing has begun");
            File pdfFile = pdfFilePart.getFile();
            PDFSession pdfSession;
            try {
                pdfSession = PDFManager.get().parsePDF(pdfFile);
            } catch (IOException e) {
                return ok("There was an error parsing the PDF");
            }
            System.out.println(Json.toJson(pdfSession));
            return ok(Json.toJson(pdfSession));
        } else {
            return ok("There was an error with the file you gave me.");
        }
    }

    public static Result countPDFSampleOne() {
        File pdfFile = Play.getFile("public/pdfs/ColonelSanders-Cookbook.pdf", Play.current());
        PDFSession pdfSession;
        try {
            pdfSession = PDFManager.get().parsePDF(pdfFile);
        } catch (IOException e) {
            return ok("There was an error parsing the PDF");
        }
        System.out.println(Json.toJson(pdfSession));
        return ok(Json.toJson(pdfSession));
    }

    public static Result countPDFSampleTwo() {
        File pdfFile = Play.getFile("public/pdfs/free_stonesoup_ecookbook.pdf", Play.current());
        PDFSession pdfSession;
        try {
            pdfSession = PDFManager.get().parsePDF(pdfFile);
        } catch (IOException e) {
            return ok("There was an error parsing the PDF");
        }
        System.out.println(Json.toJson(pdfSession));
        return ok(Json.toJson(pdfSession));
    }



    public static Result status(String pdfSessionID) {
        PDFSessionStatus status = PDFManager.get().getStatus(pdfSessionID);
        return ok(Json.toJson(status));
    }

    public static Result pageInformation(String pdfSessionId, String pageNumberString) {
        int pageNumber;
        try {
            pageNumber = Integer.parseInt(pageNumberString);
        } catch (NumberFormatException e) {
            return badRequest("Page number not an integer.");
        }
        String pageInformation = PDFManager.get().getPageInformation(pdfSessionId, pageNumber);
        if (pageInformation == null) {
            return badRequest("That page does not exist or is not done parsing");
        }
        return ok(Json.toJson(pageInformation));
    }

    public static Result pageImage(String pdfSessionID, String pageNumberString) {
        int pageNumber;
        try {
            pageNumber = Integer.parseInt(pageNumberString);
        } catch (NumberFormatException e) {
            return badRequest("Page number not an integer.");
        }

        byte[] pageImage = PDFManager.get().getPageImage(pdfSessionID, pageNumber);
        if (pageImage == null) {
            return badRequest("That page does not exist or is not done parsing");
        }
        return ok(pageImage).as("image/png");
    }

    public static Result pageThumbnail(String pdfSessionID, String pageNumberString) {
        int pageNumber;
        try {
            pageNumber = Integer.parseInt(pageNumberString);
        } catch (NumberFormatException e) {
            return badRequest("Page number not an integer.");
        }

        byte[] pageImage = PDFManager.get().getPageThumbnail(pdfSessionID, pageNumber);
            if (pageImage == null) {
                return badRequest("That page does not exist or is not done parsing");
            }
            return ok(pageImage).as("image/png");
    }
}