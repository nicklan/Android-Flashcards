package com.androidflashcards.filters;

public class LowerGermanSuperliminal extends CSVColFilter {

	private static int[] outOrder = {1,0};

	public LowerGermanSuperliminal() 
		throws Exception {
		super(9,new RowTester() {
				public boolean keepRow(String[] row) {
					return (Integer.parseInt(row[2]) < 5);
				}
			},
			outOrder);
	}

	public String filterLine(String in,int lineNum) {
		if (lineNum == 1) 
			return "\"Lower German Words\",\"Words of importance 2 3 and 4 from Melinda Green's list of German vocabulary.  See: www.superliminal.com\"\n";
		else
			return 
				super.filterLine(in,lineNum);
	}
}

