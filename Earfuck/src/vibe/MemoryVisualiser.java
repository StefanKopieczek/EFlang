package vibe;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

import earfuck.EarfuckMemory;
import earfuck.Parser;

public class MemoryVisualiser extends JLabel {
	Parser mParser;
	private final int CELLS_PER_ROW = 10;
	
	public MemoryVisualiser(Parser parser) {
		super();
		mParser = parser;
		setAlignmentX(CENTER_ALIGNMENT);
		setAlignmentY(CENTER_ALIGNMENT);
		update();
	}
	
	public void update() {
		int rowNumber = (int) Math.floor(
				(((float) mParser.getPointer() )/ CELLS_PER_ROW));
		int firstCell = (rowNumber - 1) * CELLS_PER_ROW;
		System.out.println(mParser.getPointer() / CELLS_PER_ROW);
		String text = "<html><font face=\"courier new\">";
		String cellText;
		String rowLabel;
		int cellValue;
		int memoryIndex;
		
		for (int row=0; row<3; row++) {
			rowLabel = String.format("%1$10s",firstCell+(row*CELLS_PER_ROW)+": ");
			rowLabel = rowLabel.replaceAll(" ", "&nbsp;");
			text += rowLabel;
			for (int cell=0; cell<CELLS_PER_ROW; cell++) {
				memoryIndex = firstCell+(row*CELLS_PER_ROW)+cell;
				cellValue = mParser.getMemoryValueAt(memoryIndex);
				cellText = String.valueOf(cellValue);
				//Pad with spaces
				cellText = String.format("%1$-5s",cellText);
				cellText = cellText.replaceAll(" ","&nbsp;");
				//Colour the cell we're pointing at
				if (memoryIndex==mParser.getPointer()) {
					cellText = "<font color=red>"+cellText+"</font>";
				}
				text += cellText;
			}
			text += "<br/>";
		}
		text += "</font>";
		setText(text);
	}
}
