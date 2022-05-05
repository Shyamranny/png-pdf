package com.shyam.pngpdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

public class PngToPdf {

    public void createPdf(String pdfFileName, String imgFolderPath) throws Exception {

        try(FileOutputStream fileOutputStream = new FileOutputStream(pdfFileName)) {

            Document document = new Document(PageSize.A4, 10, 10, 10, 10);

            PdfWriter.getInstance(document, fileOutputStream);
            document.open();

            File file = new File(imgFolderPath);
            if (!file.exists()){
                throw new Exception("Image folder:" + imgFolderPath + " does not exist");
            }

            if(!file.isDirectory()){
                throw new Exception("Image folder:" + imgFolderPath + " is not a directory");
            }

            for (File imgFile : Objects.requireNonNull(file.listFiles())){
                if(ImageIO.read(imgFile) != null){
                    Image image = Image.getInstance(imgFile.toURI().toURL());
                    addImageToDoc(image, document);
                }
            }

            document.close();
        }
    }

    private void addImageToDoc(Image image, Document document) throws IOException {
        //See http://stackoverflow.com/questions/1373035/how-do-i-scale-one-rectangle-to-the-maximum-size-possible-within-another-rectang
        Rectangle A4 = PageSize.A4;

        float scalePortrait = Math.min(A4.getWidth() / image.getWidth(),
                A4.getHeight() / image.getHeight());

        float scaleLandscape = Math.min(A4.getHeight() / image.getWidth(),
                A4.getWidth() / image.getHeight());

        // We try to occupy as much space as possible
        // Sportrait = (w*scalePortrait) * (h*scalePortrait)
        // Slandscape = (w*scaleLandscape) * (h*scaleLandscape)

        // therefore the bigger area is where we have bigger scale
        boolean isLandscape = scaleLandscape > scalePortrait;

        float w;
        float h;
        if (isLandscape) {
            A4 = A4.rotate();
            w = image.getWidth() * scaleLandscape;
            h = image.getHeight() * scaleLandscape;
        } else {
            w = image.getWidth() * scalePortrait;
            h = image.getHeight() * scalePortrait;
        }

        image.scaleAbsolute(w, h);
        float posH = (A4.getHeight() - h) / 2;
        float posW = (A4.getWidth() - w) / 2;

        image.setAbsolutePosition(posW, posH);
        image.setBorder(Image.NO_BORDER);
        image.setBorderWidth(0);

        try {
            document.newPage();
            document.add(image);
        } catch (DocumentException de) {
            throw new IOException(de);
        }
    }

    public static void main(String[] args) throws Exception{
        PngToPdf pngToPdf = new PngToPdf();
        pngToPdf.createPdf("/Users/xyz/temp/test.pdf", "/Users/xyz/temp/pngs/");
    }
}
