package controllers;

import managers.PDFManager;
import model.PDFPage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.index;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render("Test"));
    }

    public static Result loadFile() {
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart pdfFilePart = body.getFile("pdfFile");
        if (pdfFilePart != null) {
            System.out.println(pdfFilePart.getContentType());
            File file = pdfFilePart.getFile();
            if (file != null) {

            }
        } else {
            return ok("There was an error with the file you gave me.");
        }
    }
}