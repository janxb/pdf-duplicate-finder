package com.codeportal.pdfduplicatefinder;

import org.apache.pdfbox.pdmodel.PDPage;

public class Page {
	private int index;
	private PDPage content;
	
	public Page(PDPage content){
		this.setContent(content);
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}
	
	public void setContent(PDPage content) {
		this.content = content;
	}
	
	public PDPage getContent() {
		return content;
	}
}
