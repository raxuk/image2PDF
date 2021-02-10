import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws IOException {
        ArrayList<String> images = getFiles();
        try (PDDocument doc = new PDDocument()) {
            for (String input : images) {
                Files.find(Paths.get(input),
                        Integer.MAX_VALUE,
                        (path, basicFileAttributes) -> Files.isRegularFile(path))
                        .forEachOrdered(path -> addImageAsNewPage(doc, path.toString()));
            }
            doc.save("PDF.pdf");
        }
    }


    private static void addImageAsNewPage(PDDocument doc, String imagePath) {
        try {
            PDImageXObject image = null;
            try {
                image = PDImageXObject.createFromFile(imagePath, doc);
            } catch (IOException e) {
                if (e.toString().equals("javax.imageio.IIOException: Not a JPEG file: starts with 0x89 0x50")) {
                    String newImagePath = imagePath.split("\\.")[0] + ".PNG";
                    File im = new File(imagePath);
                    im.renameTo(new File(newImagePath));
                    image = PDImageXObject.createFromFile(newImagePath, doc);
                }
            }
            PDRectangle pageSize = PDRectangle.A4;

            int originalWidth = image.getWidth();
            int originalHeight = image.getHeight();
            float pageWidth = pageSize.getWidth();
            float pageHeight = pageSize.getHeight();
            float ratio = Math.min(pageWidth / originalWidth, pageHeight / originalHeight);
            float scaledWidth = originalWidth * ratio;
            float scaledHeight = originalHeight * ratio;
            float x = (pageWidth - scaledWidth) / 2;
            float y = (pageHeight - scaledHeight) / 2;

            PDPage page = new PDPage(pageSize);
            doc.addPage(page);
            try (PDPageContentStream contents = new PDPageContentStream(doc, page)) {
                contents.drawImage(image, x, y, scaledWidth, scaledHeight);
            }
            System.out.println("Added: " + imagePath);
        } catch (IOException e) {
            System.err.println("Failed to process: " + imagePath);
            e.printStackTrace(System.err);
        }
    }


    private static ArrayList<String> getFiles() {
        File curDir = new File(".");
        File[] filesList = curDir.listFiles();
        ArrayList<String> images = new ArrayList<String>();
        for (File f : filesList) {
            if (f.isFile()) {
                String nameFile = f.getName().toUpperCase();
                String extFile = nameFile.split("\\.")[1];
                if (extFile.equals("PNG") || extFile.equals("JPG") || extFile.equals("JPEG")) {
                    images.add(f.getName());
                }
            }
        }
        return images;
    }

}
