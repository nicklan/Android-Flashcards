package com.androidflashcards.filters;

public class ExpandedGermanSuperliminal extends CSVColFilter {

	private static int[] outOrder = {1,0};

	public ExpandedGermanSuperliminal() 
		throws Exception {
		super(9,new RowTester() {
				public boolean keepRow(String[] row) {
					int i = Integer.parseInt(row[2]);
					return (i > 5 && i < 8);
				}
			},
			outOrder);
	}

	public String filterLine(String in,int lineNum) {
		if (lineNum == 1) 
			return "\"Expanded German Words\",\"Words of importance 6 and 7 from Melinda Green's list of German vocabulary.  See: www.superliminal.com\"\n";
		else
			return 
				super.filterLine(in,lineNum);
	}
}

