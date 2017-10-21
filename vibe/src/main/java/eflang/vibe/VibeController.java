package eflang.vibe;

import eflang.core.Parser;
import eflang.core.StringMusicSource;
import eflang.ear.EARCompiler;
import eflang.ear.EARException;
import eflang.ear.core.Scale;
import eflang.ear.core.Scales;
import eflang.ear.composer.GeometricComposer;
import eflang.lobe.LOBECompiler;
import eflang.lobe.LobeCompilationException;
import eflang.vibe.MainFrame.ControlButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.ArrayList;

/**
 * This class manages the behind-the-scenes action of the IDE.
 * @author Ryan Norris
 *
 */
public class VibeController implements ProgramTimer.TimerListener, ActionListener, KeyEventDispatcher {
    /**
     * The application window JFrame.
     */
    private MainFrame mFrame;

    /**
     * The EAR Compiler used to take the code in the middle (EAR) box
     * and compile it into raw EF code.<br/>
     * The compiler also should return a suitable array of line markings
     * to allow live code highlighting during playback.
     */
    private EARCompiler mEARCompiler;

    /**
     * The LOBE compiler used to compile the code in the Lobe box
     * into EAR code. <br/>
     * The compiler also should return a suitable array of line markings
     * to allow live code highlighting during playback.
     */
    private LOBECompiler mLOBECompiler;

    /**
     * The EF Parser to use to playback the compiled EF code.
     */
    private Parser mParser;

    /**
     * Path of the currently opened file.<br/>
     * Used for Save functionality.<br/>
     * Should only be modified when creating a new file, opening a file,
     * saving a file by a new name, or when the program starts up.
     */
    private String mOpenFilePath;

    /**
     * A worker that runs the EF parser on a background thread.<br/>
     * This worker runs the EF code until told to stop by a call to mWorker.cancel()
     */
    private ParserWorker mWorker;

    /**
     * A worker that runs the EF parser on a background thread.<br/>
     * This worker runs a single EF command and then stops.
     */
    private StepForwardWorker mStepWorker;

    /**
     * The timer that counts how long a program has been playing for.
     */
    private ProgramTimer mTimer;

    /**
     * The mode describes which type of code we're currently editing.<br/>
     * It can be HIGHLEVEL, EAR or EF.
     */
    private VibeMode mMode;

    /**
     * The PlayState describes what state the program is currently in.<br/>
     * It can be PLAYING, PAUSED or STOPPED.
     */
    private PlayState mPlayState;

    /**
     * This array contains the start positions (in terms of EF commands)
     * of each EAR command. <br/>
     * Used for code highlighting.
     */
    private ArrayList<Integer> mEARLineStartPositions;

    /**
     * This array contains the start positions (in terms of EAR commands)
     * of each High Level command. <br/>
     * Used for code highlighting.
     */
    private ArrayList<Integer> mHighLevelLineStartPositions;

    /**
     * The instrument to use for playing notes.
     * Codes are available at
     * http://www.cs.cofc.edu/~manaris/spring04/cs220-handouts/JFugue/JFugue-UserGuide.html#instruments
     */
    private byte mInstrument = 0; // Default to 'piano'

    /**
     * The mode describes which type of code we're currently editing.<br/>
     * It can be HIGHLEVEL, EAR or EF.
     */
    enum VibeMode {
        HIGHLEVEL, EAR, EF;
    }

    /**
     * The PlayState describes what state the program is currently in.<br/>
     * It can be PLAYING, PAUSED or STOPPED.
     */
    enum PlayState {
        PLAYING, PAUSED, STOPPED;
    }

    /**
     * Creates a new VibeController
     * @param frame The JFrame of the entire application window you want the
     * controller to manage.
     */
    public VibeController(MainFrame frame) {
        mFrame = frame;
        mEARCompiler = new EARCompiler(new GeometricComposer(Scales.CMajor));
        mLOBECompiler = new LOBECompiler();
        mOpenFilePath = null;
        mParser = new Parser(mInstrument);
        mWorker = null;
        mStepWorker = null;
        mPlayState = PlayState.STOPPED;
        mEARLineStartPositions = new ArrayList<>();
        mHighLevelLineStartPositions = new ArrayList<>();

        //Register us to handle keyboard events globally
        KeyboardFocusManager.getCurrentKeyboardFocusManager().
        addKeyEventDispatcher(this);

        //Setup mTimer
        mTimer = new ProgramTimer(1000, this);
    }

    /**
     * Callback function called each time the program timer ticks.
     */
    public void onTick() {
        int seconds = mTimer.getTicks()%60;
        int minutes = mTimer.getTicks()/60;
        mFrame.setTimerLabel(String.format("%02d:%02d",minutes,seconds));
    }

    /**
     * Controls what happens when actions are called by the window.
     * (Button presses etc)
     */
    @Override
    public void actionPerformed(ActionEvent action) {
        switch (action.getActionCommand()) {
            case "compile":
                compile();
                break;
            case "exit":
                exitProgram();
                break;
            case "open":
                openFile();
                break;
            case "save":
                save();
                break;
            case "saveas":
                saveAs();
                break;
            case "new":
                newFile();
                break;
            case "play":
                play();
                break;
            case "pause":
                pause();
                break;
            case "step":
                step();
                break;
            case "stop":
                stop();
                break;
        }
    }

    /**
     * Compiles the currently edited code all the way down to EF code.
     * Fills all code boxes with code at that level if generated.
     */
    private void compile() {
        mFrame.setHighLevelCode(mFrame.getHighLevelCode().
                replaceAll("\\\r\\\n", "\\\n"));
        mFrame.setEARCode(mFrame.getEARCode().
                replaceAll("\\\r\\\n", "\\\n"));
        if (mMode==VibeMode.HIGHLEVEL) {
            compileLOBECode();
            compileEARCode();
        }
        else if (mMode==VibeMode.EAR){
            compileEARCode();
        }
    }

    /**
     * Uses mLOBECompiler to compile the code in LOBE box to EAR code.
     * Puts the EAR code into the EAR box.
     */
    private void compileLOBECode() {
        String LOBECode = mFrame.getHighLevelCode();
        if (mPlayState==PlayState.STOPPED) {
            try {
                mFrame.setEARCode(mLOBECompiler.compile(LOBECode));
                mHighLevelLineStartPositions = mLOBECompiler.getCommandStartPositions();
            } catch (LobeCompilationException e) {
                mFrame.setEARCode(e.getMessage());
            }
        }
    }

    /**
     * Uses mEARCompiler to compile the code in EAR box to EF code.
     * Puts the EF code into the EF box.
     */
    private void compileEARCode() {
        String EARCode = mFrame.getEARCode();
        if (mPlayState==PlayState.STOPPED) {
            try {
                mFrame.setEFCode(mEARCompiler.compile(EARCode));
                mEARLineStartPositions = mEARCompiler.getCommandStartPositions();
            } catch (EARException e) {
                mFrame.setEFCode(e.getMessage());
            }
        }
    }

    /**
     * Safely (I hope!) exits the program.
     */
    private void exitProgram() {
        pause();
        mFrame.dispose();
        System.exit(0);
    }

    /**
     * Opens an EF, EAR or Higher Level language file. <br/>
     * Switch mode and put the code into the correct box
     * depending on file extension.
     */
    private void openFile() {
        stop();
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new EFCodeFilter());
        int returnVal = fc.showOpenDialog(mFrame);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            mOpenFilePath = file.getPath();
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));


                StringBuilder builder = new StringBuilder();

                String currentLine;
                while ((currentLine = br.readLine()) != null) {
                    builder.append(currentLine);
                    builder.append('\n');
                }

                br.close();

                //Decide what to do based on file extension
                String name = file.getName();
                String extension = "";
                int i = name.lastIndexOf('.');
                if ((i>0) && (i<name.length()-1)) {
                    extension = name.substring(i+1).toLowerCase();
                }

                switch (extension) {
                    case "ef":
                        mFrame.setEFCode(builder.toString());
                        setMode(VibeMode.EF);
                        break;
                    case "ear":
                        mFrame.setEARCode(builder.toString());
                        setMode(VibeMode.EAR);
                        break;
                    case "lobe":
                        mFrame.setHighLevelCode(builder.toString());
                        setMode(VibeMode.HIGHLEVEL);
                        break;
                }
                setCurrentFileName();

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * Saves the given text to the given file.
     * @param file
     * @param text
     */
    private void saveFile(File file, String text) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.append(text);
            bw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Saves the current code to the already open file if possible.
     * Otherwise loads the saveAs dialog.<br/>
     */
    private void save() {
        if (mOpenFilePath==null) {
            saveAs();
            return;
        }
        File file = new File(mOpenFilePath);
        saveFile(file,getCurrentCode());
    }

    /**
     * Prompts user to choose where to save code.
     * And then saves it.
     */
    private void saveAs() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new EFCodeFilter());
        int returnVal = fc.showSaveDialog(mFrame);

        if (returnVal==JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            saveFile(file, getCurrentCode());

            mOpenFilePath = file.getPath();
            setCurrentFileName();
        }
    }

    /**
     * Creates a "new" workspace by clearing all textboxes and forgetting
     * current filename. <br/>
     * Prompts user to select which kind of file they want to create
     * and switch modes accordingly.
     */
    private void newFile() {
        stop();
        mOpenFilePath = null;
        mFrame.setEARCode("");
        mFrame.setEFCode("");
        mFrame.setHighLevelCode("");

        String[] options = new String[]{"LOBE","EAR","RAW EF"};
        int choice = JOptionPane.showOptionDialog(mFrame,
                                "Which type of file would you like to create?",
                                "Please choose a file type",
                                JOptionPane.DEFAULT_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                options,
                                options[0]);

        switch (choice) {
        case 0: setMode(VibeMode.HIGHLEVEL);
                break;
        case 1: setMode(VibeMode.EAR);
                break;
        case 2: setMode(VibeMode.EF);
                break;
        }
        setCurrentFileName();
    }

    /**
     * Uses mCurrentFilePath to get the name of the file and display it
     * in the window title and the filename label.
     */
    private void setCurrentFileName() {
        String fileName = "";
        if (mOpenFilePath!=null) {
            fileName = mOpenFilePath.replaceAll("^.*\\\\","");
        }
        mFrame.setFileNameLabel(fileName);
        mFrame.setTitle("VIBE Earfuck IDE - "+fileName);
    }

    /**
     * Gets the code from the box currently being edited.
     * @return
     */
    public String getCurrentCode() {
        switch (mMode) {
        case EF: return mFrame.getEFCode();
        case EAR: return mFrame.getEARCode();
        case HIGHLEVEL: return mFrame.getHighLevelCode();
        default: return null;
        }
    }

    /**
     * Begin playing the current program (from current location)
     * until told to stop.
     */
    public void play() {
        if (mPlayState==PlayState.PLAYING) {
            return;
        }
        String EFCode = mFrame.getEFCode();

        if (mPlayState==PlayState.STOPPED) {
            mParser.refreshState();
        }
        mFrame.clearConsole();
        mPlayState = PlayState.PLAYING;
        mParser.giveMusic(new StringMusicSource(EFCode));
        mParser.setInstrument(mInstrument);
        mWorker = new ParserWorker(this);
        mWorker.execute();

        setPlayState(PlayState.PLAYING);
        mTimer.start();
    }

    /**
     * Pauses playback.
     */
    public void pause() {
        if (mPlayState==PlayState.PLAYING) {
            if (mWorker!=null) {
                mWorker.cancel(true);
            }
            setPlayState(PlayState.PAUSED);
            mTimer.stop();
        }
    }

    /**
     * Stop playback. Call to setPlayState resets program state.
     */
    public void stop() {
        if (mPlayState==PlayState.STOPPED) {
            return;
        }
        if (mWorker!=null) {
            mWorker.cancel(true);
        }
        setPlayState(PlayState.STOPPED);
        mParser.refreshState();
        mFrame.getEFTextPane().setCurrentCommandIndex(-1);
        mFrame.getEFTextPane().invalidate();

        mFrame.getEARTextPane().setCurrentCommandIndex(-1);
        mFrame.getEARTextPane().invalidate();
        mFrame.updateMemoryVisualiser();

        mTimer.reset();
    }

    /**
     * Steps the EF program forward one command.
     */
    public void step() {
        if (mPlayState==PlayState.PLAYING) {
            return;
        }
        if (mPlayState==PlayState.STOPPED) {
            mParser.giveMusic(new StringMusicSource(mFrame.getEFCode()));
        }
        setPlayState(PlayState.PLAYING);
        mStepWorker = new StepForwardWorker(this);
        mStepWorker.execute();
    }

    /**
     * Sets the current play state. <br/>
     * Enables & disables Play/Pause/Stop/Step buttons accordingly. <br/>
     * Also prevents the user from editing the code whilst it's running.
     * This is essential for code highlighting to work consistently.
     * @param state PlayState to change to.
     */
    public void setPlayState(PlayState state) {
        mPlayState = state;
        if (state==PlayState.PLAYING) {
            mFrame.setButtonEnabled(ControlButton.PLAY, false);
            mFrame.setButtonEnabled(ControlButton.PAUSE, true);
            mFrame.setButtonEnabled(ControlButton.STOP, true);
            mFrame.setButtonEnabled(ControlButton.STEP, false);
            mFrame.setCodeEditable(false);
            return;
        }
        if (state==PlayState.PAUSED) {
            mFrame.setButtonEnabled(ControlButton.PLAY, true);
            mFrame.setButtonEnabled(ControlButton.PAUSE, false);
            mFrame.setButtonEnabled(ControlButton.STOP, true);
            mFrame.setButtonEnabled(ControlButton.STEP, true);
            mFrame.setCodeEditable(false);
            return;
        }
        if (state==PlayState.STOPPED) {
            mFrame.setButtonEnabled(ControlButton.PLAY, true);
            mFrame.setButtonEnabled(ControlButton.PAUSE, false);
            mFrame.setButtonEnabled(ControlButton.STOP, false);
            mFrame.setButtonEnabled(ControlButton.STEP, true);
            mFrame.setCodeEditable(true);
            return;
        }
    }

    /**
     * Sets the mode we're working in. <br/>
     * Call to mFrame.setupEditPane hides irrelevant text boxes.
     * @param mode
     */
    public void setMode(VibeMode mode) {
        mMode = mode;
        mFrame.setupEditPane(mode);
    }

    /**
     * Returns the current working mode.
     * @return
     */
    public VibeMode getMode() {
        return mMode;
    }

    /**
     * Returns the application JFrame.
     * @return
     */
    public MainFrame getFrame() {
        return mFrame;
    }

    /**
     * Returns the EF code parser.
     * @return
     */
    public Parser getParser() {
        return mParser;
    }

    /**
     * Sets the instrument to be used for playback.
     *
     * @param instrumentCode The instrument to use - should be in the range 0 =< n < 128.
     */
    public void setInstrument(byte instrumentCode)
    {
        mInstrument = instrumentCode;

        mParser.setInstrument(instrumentCode);
    }

    /**
     * Sets the scale to write the music in.
     */
    public void setScale(Scale scale) {
        mEARCompiler = new EARCompiler(new GeometricComposer(scale));
    }

    /**
     * Returns the EAR line start positions. <br/>
     * Used by workers to do code highlighting.
     * @return
     */
    public ArrayList<Integer> getEARCommandStartPositions() {
        return mEARLineStartPositions;
    }

    /**
     * Returns the High Level line start positions. <br/>
     * Used by workers to do code highlighting.
     * @return
     */
    public ArrayList<Integer> getHighLevelCommandStartPositions() {
        return mHighLevelLineStartPositions;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getID() == KeyEvent.KEY_PRESSED &&
                e.getModifiers() == java.awt.event.InputEvent.CTRL_MASK) {
            if (e.getKeyCode() == KeyEvent.VK_S) {
                save();
                return true;
            }
            if (e.getKeyCode() == KeyEvent.VK_O) {
                openFile();
                return true;
            }
            if (e.getKeyCode() == KeyEvent.VK_N) {
                newFile();
                return true;
            }
        }
        return false;
    }
}
