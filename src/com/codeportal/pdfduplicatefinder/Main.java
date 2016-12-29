package com.codeportal.pdfduplicatefinder;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;

import org.apache.pdfbox.exceptions.COSVisitorException;

public class Main {

	public static void main(String[] args) throws IOException, URISyntaxException, COSVisitorException {

		PrintStream nullStream = new PrintStream(new OutputStream() {
			public void write(int b) {
				// DO NOTHING
			}
		});
		
		//System.setOut(nullStream);
		System.setErr(nullStream);

		String sourcePath = "/home/jan/gdrive/Eclipse_Workspace/PdfDuplicateFinder/bin/com/codeportal/pdfduplicatefinder/four.pdf";
		String targetPathSure = "/home/jan/gdrive/Eclipse_Workspace/PdfDuplicateFinder/bin/com/codeportal/pdfduplicatefinder/four_filtered_sure.pdf";
		String targetPathNearly = "/home/jan/gdrive/Eclipse_Workspace/PdfDuplicateFinder/bin/com/codeportal/pdfduplicatefinder/four_filtered_nearly.pdf";

		//PdfCleaner w1 = new PdfCleaner(sourcePath);
		//w1.startProcessing(OptimizationLevel.SURE_DUPLICATES);
		//w1.saveFile(targetPathSure);

		PdfCleaner w2 = new PdfCleaner(sourcePath);
		w2.startProcessing(OptimizationLevel.NEARLY_DUPLICATES);
		w2.saveFile(targetPathNearly);

	}
}
