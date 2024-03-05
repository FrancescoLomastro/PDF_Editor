package org.example;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class PDFModel {
    private PDDocument document;
    private float documentWidth;
    private float documentHeight;
    private String documentFilePath;
    private PDFRenderer renderer;
    private static PDFModel instance = null;

    private PDFModel() {}

    public static PDFModel getInstance() {
        if (instance == null) {
            instance = new PDFModel();
        }
        return instance;
    }
    public void setDocument(String filePath) throws IOException {
        documentFilePath = filePath;
        document = PDDocument.load(new File(documentFilePath));
        renderer = new PDFRenderer(document);

        PDPage firstPage = document.getPage(0);
        documentWidth = firstPage.getMediaBox().getWidth();
        documentHeight = firstPage.getMediaBox().getHeight();
    }

    public int getNumberOfPages() {
        return document.getNumberOfPages();
    }

    public byte[] getPageAsImage(int pageNumber, int dpi) throws IOException {
        BufferedImage image = renderer.renderImage(pageNumber, 1.0f);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }

    public void closeDocument() throws IOException {
        document.close();
        clearDocument();
    }

    public float getAspectRatio() {
        return documentHeight / documentWidth ;
    }

    public void addBlankPage(int index) {
        PDRectangle pageSize = new PDRectangle(documentWidth, documentHeight);
        PDPage blankPage = new PDPage(pageSize);
        if (index < 0) {
            document.getPages().add(blankPage);
        }
        else
            document.getPages().insertBefore(blankPage, document.getPage(index));
    }

    public void saveDocument() throws IOException {
        document.save(documentFilePath);
    }
    private void clearDocument() {
        document = null;
        documentWidth = 0;
        documentHeight = 0;
        documentFilePath = null;
        renderer = null;
    }

    public void removePage(int index) {
        document.removePage(index);
    }
}
