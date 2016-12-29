package com.codeportal.pdfduplicatefinder;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.fontbox.ttf.MaximumProfileTable;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

public class PdfCleaner {
	String inputPath;
	String outputPath;
	PDDocument sourceDocument;
	List<Page> pageCollection = new ArrayList<>();

	private static final int DUPLICATE_PERCENTAGE = 90;

	public PdfCleaner(String inputPath) {
		this.inputPath = inputPath;
	}

	public void startProcessing(OptimizationLevel level) throws IOException {
		sourceDocument = PDDocument.load(inputPath);
		loadPageList();

		switch (level) {
		case SURE_DUPLICATES:
			processSureDuplicates();
			break;
		case NEARLY_DUPLICATES:
			processSureDuplicates();
			processPossibleDuplicatesByImage();
			break;
		}
	}

	/**
	 * Extract all Pages from the Source Document
	 * 
	 * @throws IOException
	 */
	private void loadPageList() throws IOException {
		pageCollection.clear();

		@SuppressWarnings("unchecked")
		List<PDPage> catalog = sourceDocument.getDocumentCatalog().getAllPages();

		for (PDPage pageRaw : catalog) {

			Page page = new Page(pageRaw);
			page.setIndex(catalog.indexOf(pageRaw));

			pageCollection.add(page);
		}
	}

	/**
	 * Remove sure Duplicates from the Page List.<br>
	 * This is done by generating a Hash of the Page and only storing the first
	 * Page for a Hash.
	 * 
	 * @throws IOException
	 */
	private void processSureDuplicates() throws IOException {
		Map<Integer, Page> pageMap = new HashMap<Integer, Page>();
		List<Page> resultingPageCollection = new ArrayList<>();

		for (Page page : pageCollection) {
			PDPage pageRaw = page.getContent();

			BufferedImage pageImage = pageRaw.convertToImage();
			int[] pageData = ((DataBufferInt) pageImage.getData().getDataBuffer()).getData();
			int pageHash = Arrays.hashCode(pageData);

			if (pageMap.containsKey(pageHash))
				continue;

			pageMap.put(pageHash, page);
		}

		for (Entry<Integer, Page> entry : pageMap.entrySet()) {
			resultingPageCollection.add(entry.getValue());
		}

		pageCollection = resultingPageCollection;
	}

	/**
	 * Remove possible Duplicates from the Page List.<br>
	 * This is done by getting the raw Page Data and comparing it with all other
	 * Pages.<br>
	 * Please Note: This could take a LONG time if you have some hundred Pages..
	 * 
	 * @throws IOException
	 */
	private void processPossibleDuplicatesByData() throws IOException {
		List<Page> resultingPageCollection = new ArrayList<>();

		for (Page sourcePage : pageCollection) {
			boolean isDuplicate = isPageDuplicatedByData(sourcePage, resultingPageCollection, DUPLICATE_PERCENTAGE);

			if (isDuplicate == false) {
				resultingPageCollection.add(sourcePage);
			}
		}

		pageCollection = resultingPageCollection;
	}

	private void processPossibleDuplicatesByImage() throws IOException {
		List<Page> resultingPageCollection = new ArrayList<>();

		for (Page sourcePage : pageCollection) {
			boolean isDuplicate = isPageDuplicatedByImage(sourcePage, resultingPageCollection, DUPLICATE_PERCENTAGE);

			if (isDuplicate == false) {
				resultingPageCollection.add(sourcePage);
			}
		}

		pageCollection = resultingPageCollection;
	}

	/**
	 * Check a Page if it is contained in a List of Pages
	 * 
	 * @param sourcePage
	 *            The Page to check
	 * @param pageList
	 *            The List to check in
	 * @param minDuplicatePercentage
	 *            The minimum Percentage that must be duplicated
	 * @return True if Duplication Percentage is at least as high as specified,
	 *         else False
	 * @throws IOException
	 */
	private boolean isPageDuplicatedByData(Page sourcePage, List<Page> pageList, int minDuplicatePercentage)
			throws IOException {
		int[] sourceData = ((DataBufferInt) sourcePage.getContent().convertToImage().getData().getDataBuffer())
				.getData();

		for (Page comparePage : pageList) {

			// if (comparePage.getIndex() > sourcePage.getIndex())
			// continue;

			int[] compareData = ((DataBufferInt) comparePage.getContent().convertToImage().getData().getDataBuffer())
					.getData();

			int duplicatePercentage = ArrayComparator.compareIntArray(sourceData, compareData);
			if (duplicatePercentage >= minDuplicatePercentage) {
				System.out.println("Deleting Duplicate Page (#" + sourcePage.getIndex() + " same as #"
						+ comparePage.getIndex() + ") with %: " + duplicatePercentage);
				return true;
			}
		}

		return false;
	}

	private boolean isPageDuplicatedByImage(Page sourcePage, List<Page> pageList, int minDuplicatePercentage)
			throws IOException {
		BufferedImage sourceData = sourcePage.getContent().convertToImage();
		
		System.out.println("checking "+sourcePage.getIndex());

		for (Page comparePage : pageList) {
			
			if (comparePage.getIndex() > sourcePage.getIndex())
				continue;

			BufferedImage compareData = comparePage.getContent().convertToImage();
			long maxPixels = 0;
			long duplicatePixels = 0;

			if (sourceData.getWidth() == compareData.getWidth() && sourceData.getHeight() == compareData.getHeight()) {
				maxPixels = sourceData.getWidth() * sourceData.getHeight();
				duplicatePixels = 0L;
				for (int x = 0; x < sourceData.getWidth(); x++) {
					for (int y = 0; y < sourceData.getHeight(); y++) {
						if (sourceData.getRGB(x, y) == compareData.getRGB(x, y))
							duplicatePixels++;
					}
				}
			} else {
				System.out.println("Images have different Size");
			}

			int duplicatePercentage = (int) ((duplicatePixels / maxPixels) * 100);
			if (duplicatePercentage >= minDuplicatePercentage) {
				System.out.println("Deleting Duplicate Page (#" + sourcePage.getIndex() + " same as #"
						+ comparePage.getIndex() + ") with %: " + duplicatePercentage);
				return true;
			}
		}

		return false;
	}

	public void saveFile(String outputPath) throws IOException {
		PDDocument targetDocument = new PDDocument();
		PDPage[] orderedPages = new PDPage[sourceDocument.getNumberOfPages()];

		for (Page page : pageCollection) {
			orderedPages[page.getIndex()] = page.getContent();
		}

		for (PDPage page : orderedPages) {
			if (page == null)
				continue;

			targetDocument.addPage(page);
		}

		try {
			targetDocument.save(outputPath);
		} catch (COSVisitorException e) {
		}

		targetDocument.close();
		sourceDocument.close();
	}
}
