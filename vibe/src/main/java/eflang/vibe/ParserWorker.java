package eflang.vibe;

import eflang.vibe.VibeController.VibeMode;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This worker runs the EF Parser on a background thread until told to stop. <br/>
 * It also passes forward information for code highlighting to the text panes.
 * @author Ryan Norris
 *
 */
public class ParserWorker extends SwingWorker<Void,Long> {
    private VibeController mController;

    public ParserWorker(VibeController controller) {
        mController = controller;
    }

    @Override
    public Void doInBackground() {
        while (!this.isCancelled()) {
            //If we've reached the end of the piece, stop playback.
            if (!mController.getParser().getPiece().hasNext()) {
                mController.stop();
                break;
            }
            publish(mController.getParser().getPlace());
            //Change tempo if the slider has been moved
            int newTempo = mController.getFrame().getTempo();
            if (newTempo != mController.getParser().getTempo()) {
                mController.getParser().setTempo(newTempo);
            }
            mController.getParser().stepForward();
        }
        return null;
    }

    @Override
    protected void process(List<Long> indices) {
        //Get EF command index
        long currentEFCommand = indices.get(indices.size()-1);
        //Get EAR command index
        int currentEARLine = 0;
        ArrayList<Integer> earLineStartPositions =
                mController.getEARCommandStartPositions();
        for (int i=0; i<earLineStartPositions.size(); i++) {
            if (earLineStartPositions.get(i) <= currentEFCommand) {
                currentEARLine = i;
            }
        }

        //Get High Level command index
        int currentHighLevelLine = 0;
        ArrayList<Integer> highLevelLineStartPositions =
                mController.getHighLevelCommandStartPositions();
        for (int i=0; i<highLevelLineStartPositions.size(); i++) {
            if (highLevelLineStartPositions.get(i) <= currentEARLine) {
                currentHighLevelLine = i;
            }
        }

        //Highlight the correct command in each code pane
        mController.getFrame().getEFTextPane().
                setCurrentCommandIndex(currentEFCommand);
        mController.getFrame().getEFTextPane().invalidate();

        if ((mController.getMode() == VibeMode.EAR) ||
            (mController.getMode() == VibeMode.HIGHLEVEL)) {
            mController.getFrame().getEARTextPane().
                    setCurrentCommandIndex(currentEARLine);
            mController.getFrame().getEARTextPane().invalidate();
        }

        if (mController.getMode() == VibeMode.HIGHLEVEL) {
            mController.getFrame().getHighLevelTextPane().
                setCurrentCommandIndex(currentHighLevelLine);
            mController.getFrame().getHighLevelTextPane().invalidate();
        }

        mController.getFrame().updateMemoryVisualiser();
    }
}
