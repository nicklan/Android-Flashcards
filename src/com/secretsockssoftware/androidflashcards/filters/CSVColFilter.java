package com.androidflashcards.filters;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVWriter;
import java.io.StringWriter;

public class CSVColFilter implements LessonFilter {
	private long keepMask;
	private RowTester rowTest;
	private CSVParser parser;
	private String[] outRow;
	private int[] outOrder;

	public CSVColFilter(long km, RowTester t, int[] oo) 
		throws Exception {
		keepMask = km;
		rowTest = t;
		parser = new CSVParser();
		outOrder = oo;

		int keepCount = 0;
		long tmp = km;
		while(tmp != 0) {
			if ((tmp & 0x01) != 0)
				keepCount++;
			tmp >>= 1;
		}
		outRow = new String[keepCount];
		if (outOrder != null &&
				outOrder.length < keepCount) {
			throw new Exception("Output order array shorter than requested number of columns in mask");
		}
	}

	public String filterLine(String in,int lineNum) {
		try {
			String[] toks = parser.parseLine(in);
			if (rowTest != null &&
					!rowTest.keepRow(toks))
				return null;
			for(int i = 0,j=0;i < outRow.length;j++)
				if ((keepMask & (1 << j)) != 0)
					if (outOrder != null)
						outRow[outOrder[i++]] = toks[j];
					else
						outRow[i++] = toks[j];
			StringWriter sw = new StringWriter();
			CSVWriter cw = new CSVWriter(sw);
			cw.writeNext(outRow);
			return sw.toString();
		} catch (Exception e) {
			return null;
		}
	}
}