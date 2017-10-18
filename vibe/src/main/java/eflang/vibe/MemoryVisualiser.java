package eflang.vibe;

import javax.swing.JLabel;

import eflang.core.Parser;

/**
 * This class takes an EF Parser, and displays the memory
 * state as a JLabel.
 * @author Ryan Norris
 *
 */
public class MemoryVisualiser extends JLabel {

    /**
     * The EF Parser to display the memory of.
     */
    private Parser mParser;

    /**
     * The number of cells to display in each row
     */
    private int mCellsPerRow = 10;

    /**
     * The number of rows of cells to display
     */
    private int mNumberOfRows = 5;

    /**
     * Whether to scroll the display vertically as
     * the pointer moves around.<br/>
     * Default = true
     */
    private boolean mScrolling = false;

    public MemoryVisualiser(Parser parser) {
        super();
        mParser = parser;
        setAlignmentX(CENTER_ALIGNMENT);
        setAlignmentY(CENTER_ALIGNMENT);
        update();
    }

    /**
     * Tells the visualiser the state of the parser may have changed
     * and it should update its display
     */
    public void update() {
        int rowNumber;
        if (mScrolling) {
            rowNumber = (int) Math.floor(
                    (((float) mParser.getPointer() )/ mCellsPerRow));
        }
        else {
            rowNumber = 0;
        }
        int firstCell = (rowNumber - (mNumberOfRows/2)) * mCellsPerRow;
        String text = "<html><font face=\"courier new\">";
        String cellText;
        String rowLabel;
        int cellValue;
        int memoryIndex;

        for (int row=0; row<mNumberOfRows; row++) {
            rowLabel = String.format("%1$10s",firstCell+(row*mCellsPerRow)+": ");
            rowLabel = rowLabel.replaceAll(" ", "&nbsp;");
            text += rowLabel;
            for (int cell=0; cell<mCellsPerRow; cell++) {
                memoryIndex = firstCell+(row*mCellsPerRow)+cell;
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

    /**
     * @return whether or not this visualiser is set to scroll
     */
    public boolean getScrolling() {
        return mScrolling;
    }

    /**
     * @param scrolling to scroll or not (true/false)
     */
    public void setScrolling(boolean scrolling) {
        mScrolling = scrolling;
    }

    /**
     * @return the number of cells displayed per row
     */
    public int getCellsPerRow() {
        return mCellsPerRow;
    }

    /**
     * @param mCellsPerRow the number of cells to display per row
     */
    public void setCellsPerRow(int mCellsPerRow) {
        this.mCellsPerRow = mCellsPerRow;
    }

    /**
     * @return the number of rows displayed
     */
    public int getNumberOfRows() {
        return mNumberOfRows;
    }

    /**
     * @param mNumberOfRows the number of rows to display
     */
    public void setNumberOfRows(int mNumberOfRows) {
        this.mNumberOfRows = mNumberOfRows;
    }
}
