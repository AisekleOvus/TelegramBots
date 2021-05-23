package gmd;

import java.io.*;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

public class SvgJpg {
    public static void SvgJpg(String fileName) {
    	try {
            String svg_URI_input = Paths.get(fileName).toUri().toURL().toString();
            TranscoderInput input_svg_image = new TranscoderInput(svg_URI_input);   
            
            // getting sizes
            
            OutputStream png_ostream = new FileOutputStream(fileName + ".jpg");
            TranscoderOutput output_jpg_image = new TranscoderOutput(png_ostream); 
            JPEGTranscoder my_converter = new JPEGTranscoder();
            my_converter.addTranscodingHint(JPEGTranscoder.KEY_QUALITY,
                    new Float(.8));
            my_converter.transcode(input_svg_image, output_jpg_image);
            png_ostream.flush();
            png_ostream.close(); 
            Files.deleteIfExists(Paths.get(fileName));
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    }
}
