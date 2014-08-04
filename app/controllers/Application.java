package controllers;

import managers.PDFManager;
import model.PDFSession;
import model.PDFSessionStatus;
import model.PageInformation;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.index;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render("Test"));
    }

    public static Result countPDF()  {
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart pdfFilePart = body.getFile("pdfFile");
        if (pdfFilePart != null) {
            System.out.println("The parsing has begun");
            File pdfFile = pdfFilePart.getFile();
            PDFSession pdfSession;
            try {
                pdfSession = PDFManager.get().parsePDF(pdfFile);
            } catch (IOException e) {
                e.printStackTrace();
                return ok("There was an error parsing the PDF");
            }
            return ok(Json.toJson(pdfSession));
        } else {
            return ok("There was an error with the file you gave me.");
        }
    }

    public static Result pageInformation(String pdfSessionId, String pageNumberString) {
        int pageNumber = Integer.parseInt(pageNumberString);
        List<PageInformation> pageInformation = PDFManager.get().pageInformation(pdfSessionId, pageNumber);
        return ok(Json.toJson(pageInformation));
    }

    public static Result status (String pdfSessionID) {
        PDFSessionStatus status = PDFManager.get().getStatus(pdfSessionID);
        return ok(Json.toJson(status));
    }
}