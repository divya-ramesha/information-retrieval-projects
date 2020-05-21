import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;
import org.apache.tika.language.LanguageIdentifier;

public class MyParser {

    public static void main(final String[] args) throws IOException,
            SAXException, TikaException {


        String dirPath = "C:\\Users\\Divya Ramesha\\Downloads\\FOXNEWS\\FOXNEWS\\foxnews";
        File dir = new File(dirPath);
        int count = 0;


        File opFile = new File("big.txt");
        opFile.createNewFile();
        FileWriter writer = new FileWriter(opFile);

        for(File file: dir.listFiles()){

            BodyContentHandler handler = new BodyContentHandler(-1);
            Metadata metadata = new Metadata();
            FileInputStream inputstream = new FileInputStream(new File(dirPath+"\\\\"+file.getName()));
            ParseContext pcontext = new ParseContext();

            // Html parser
            HtmlParser htmlparser = new HtmlParser();
            htmlparser.parse(inputstream, handler, metadata, pcontext);
            LanguageIdentifier object = new LanguageIdentifier(handler.toString());

            if(object.getLanguage().equals("en")){
                writer.write(handler.toString().replaceAll("\\s+", " "));
                writer.write("\n");
            }

            System.out.println(file.getName() + " : " + ++count);
        }

        writer.flush();
        writer.close();

		/*
		System.out.println("Metadata of the document:");
		String[] metadataNames = metadata.names();
		for (String name : metadataNames) {
			System.out.println(name + ":   " + metadata.get(name));
		}
		*/

    }
}