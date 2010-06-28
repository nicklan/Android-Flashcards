package com.secretsockssoftware.androidflashcards.filters;

public class EssentialGermanSuperliminal extends CSVColFilter {

	private static int[] outOrder = {1,0};

	public EssentialGermanSuperliminal() 
		throws Exception {
		super(9,new RowTester() {
				public boolean keepRow(String[] row) {
					if (Integer.parseInt(row[2]) >= 8)
						return true;
					return false;
				}
			},
			outOrder);
	}

	public String filterLine(String in,int lineNum) {
		if (lineNum == 1) 
			return "\"Essential German Words\",\"Words of importance 8 9 and 10 from Melinda Green's list of German vocabulary.  See: www.superliminal.com\"\n";
		else
			return 
				super.filterLine(in,lineNum);
	}
}

