package vibe;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.Window;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * The main application frame.
 * Contains all other components.
 * @author Ryan Norris
 *
 */
public class MainFrame extends JFrame {
	/**
	 * Container for the whole UI.
	 */
	private Container mContainer;
	
	/**
	 * The outside JSplitPane for the code edit boxes.
	 * Contains the HighLevelTextPane and the
	 * other SplitPane.
	 */
	private JSplitPane mEditPane;
	
	/**
	 * The inner JSplitPane for the code edit boxes.
	 * Contains the EAR and EF TextPanes.
	 */
	private JSplitPane mEARAndEFPane;
	
	/**
	 * Text box for entering high level code.
	 */
	private CodePane mHighLevelTextPane;
	
	/**
	 * Text box for entering EAR code.
	 */
	private CodePane mEARTextPane;
	
	/**
	 * Text box for entering EF code.
	 */
	private CodePane mEFTextPane;
	
	/**
	 * The menu bar at the top of the application. (File etc...)
	 */
	private JMenuBar mMenuBar;
	
	/**
	 * Toolbar running along the top of the application just below the menu bar. <br/>
	 * Contains useful buttons (compile/play/pause/stop/step) and tempo slider.
	 */
	private JPanel mToolBar;
	
	private JPanel mTopPane;
	
	private JLabel mTempoLabel;
	
	/**
	 * Slider to control the tempo of the code playback.
	 */
	private JSlider mTempoSlider;
	
	/**
	 * Label that shows the time the current program has been running for.
	 */
	private JLabel mTimerLabel;
	
	/**
	 * Label that shows the current file name and a * to show if it needs saving.
	 */
	private JLabel mFileNameLabel;
	
	/**
	 * Minimum selectable tempo for slider.
	 */
	private final static int MIN_TEMPO = 30;
	
	/**
	 * Initial value for tempo slider.
	 */
	private final static int INITIAL_TEMPO = 130;
	
	/**
	 * Maximum selectable tempo for slider.
	 */
	private final static int MAX_TEMPO = 1000;
	
	/**
	 * Visualiser for Parser memory.
	 */
	private MemoryVisualiser mMemoryVisualiser;
	
	/**
	 * The input/output console
	 */
	private Console mConsole;
	
	private HashMap<ControlButton, JButton> mButtons = new HashMap<ControlButton, JButton>();
	
	/**
	 * Controller to handle all events on this window.
	 */
	private VibeController mController;	
	
	public MainFrame() {
		initComponents();
		this.setTitle("VIBE Earfuck IDE");
	}
	
	/**
	 * UI setup to be done AFTER made visible.
	 */
	private void setupUI() {
		mController.setMode(VibeController.VibeMode.EAR);
		mEditPane.setDividerLocation(0.333);
		mEARAndEFPane.setDividerLocation(0.5);
		
		Dimension size = mMemoryVisualiser.getSize();
		//size.width = this.getWidth() - mConsole.getWidth();
		//mMemoryVisualiser.setSize(size);
		mMemoryVisualiser.setHorizontalAlignment(SwingConstants.CENTER);
	}
	
	/**
	 * Should be called when the user wants the window to appear.
	 */
	public void create() {
		setVisible(true);
		setupUI();
	}
	
	/**
	 * Initialises all components. </br>
	 * Called from constructor, so should not contain any initialisation
	 * that requires the components to be visible.
	 */
	private void initComponents() {
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		
		mController = new VibeController(this);
		
		mContainer = getContentPane();
		
		//Create text panes
		JPanel noWrapPanel;
		JScrollPane scrollPane1, scrollPane2, scrollPane3;
		mHighLevelTextPane = new CodePane();
		mHighLevelTextPane.setDivider('\n');
		noWrapPanel = new JPanel(new BorderLayout());
		noWrapPanel.add(mHighLevelTextPane);
		scrollPane1 = new JScrollPane(noWrapPanel);
		
		mEARTextPane = new CodePane();
		mEARTextPane.setDivider('\n');
		noWrapPanel = new JPanel(new BorderLayout());
		noWrapPanel.add(mEARTextPane);
		scrollPane2 = new JScrollPane(noWrapPanel);
		
		mEFTextPane = new CodePane();
		scrollPane3 = new JScrollPane(mEFTextPane);
		scrollPane3.setHorizontalScrollBarPolicy(
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		//Create split panes
		mEARAndEFPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
					scrollPane2,scrollPane3);
		mEditPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
					scrollPane1,mEARAndEFPane);
		mEARAndEFPane.setContinuousLayout(true);
		mEditPane.setContinuousLayout(true);
		
		//Add to container
		mContainer.add(mEditPane,BorderLayout.CENTER);
		
		//Set window size
		Rectangle rect = this.getBounds();
		rect.height = 480;
		rect.width = 640;
		this.setBounds(rect);
		
		//Create menu bar
		mMenuBar = new JMenuBar();
		JMenu menu = new JMenu("File");
		
		JMenuItem menuItem;
		
		menuItem = new JMenuItem("New");
		menuItem.addActionListener(mController);
		menuItem.setActionCommand("new");
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Open");
		menuItem.addActionListener(mController);
		menuItem.setActionCommand("open");
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Save");
		menuItem.addActionListener(mController);
		menuItem.setActionCommand("save");
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Save as");
		menuItem.addActionListener(mController);
		menuItem.setActionCommand("saveas");
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Exit");
		menuItem.addActionListener(mController);
		menuItem.setActionCommand("exit");
		menu.add(menuItem);
		
		mMenuBar.add(menu);
		
		setJMenuBar(mMenuBar);
		
		//Create tool bar
		mTopPane = new JPanel(new BorderLayout());
		
		mTimerLabel = new JLabel("00:00");
		mTopPane.add(mTimerLabel,BorderLayout.EAST);
		
		JPanel fileNamePanel = new JPanel();
		mFileNameLabel = new JLabel("FILENAME");				
		fileNamePanel.add(mFileNameLabel);
		mTopPane.add(fileNamePanel,BorderLayout.WEST);

		mToolBar = new JPanel();
		mToolBar.setLayout(new BoxLayout(mToolBar,BoxLayout.X_AXIS));
				
		mTopPane.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
		mTopPane.add(mToolBar,BorderLayout.CENTER);
				
		mContainer.add(mTopPane,BorderLayout.NORTH);
		
		JPanel buttonsBar = new JPanel();
        addButtonToBar(buttonsBar, "Compile", "compile", ControlButton.COMPILE);
		
        addButtonToBar(buttonsBar, "|>", "play", ControlButton.PLAY);
		
        addButtonToBar(buttonsBar, "||", "pause", ControlButton.PAUSE);
        setButtonEnabled(ControlButton.PAUSE, false);
		
        addButtonToBar(buttonsBar, "O", "stop", ControlButton.STOP);
        setButtonEnabled(ControlButton.STOP, false);
		
        addButtonToBar(buttonsBar, "->", "step", ControlButton.STEP);
		
		mToolBar.add(buttonsBar);
		
		//Tempo slider frame
		mTempoSlider = new JSlider(MIN_TEMPO,MAX_TEMPO,INITIAL_TEMPO);
		mTempoSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				//Set the label text
				//All in one line, because I'm awesome and love unreadable code <_<
				mTempoLabel.setText("Tempo: "+String.valueOf(mTempoSlider.getValue()));				
			}
		});
		mTempoLabel = new JLabel();
		mTempoLabel.setAlignmentY(CENTER_ALIGNMENT);
		mTempoLabel.setText("Tempo: "+String.valueOf(mTempoSlider.getValue()));
		mTempoSlider.setMinimumSize(mTempoSlider.getPreferredSize());
		mToolBar.add(mTempoLabel);
		mToolBar.add(mTempoSlider);
//		mToolBar.add(frame);
		
		//SOUTH SECTION
		JPanel frame = new JPanel();
		frame.setLayout(new BoxLayout(frame,BoxLayout.LINE_AXIS));
		
		//Create the Memory Visualiser
		mMemoryVisualiser = new MemoryVisualiser(mController.getParser());
		frame.add(mMemoryVisualiser);
		
		//Create the console
		mConsole = new Console();
		mController.getParser().setIoManager(mConsole);
		noWrapPanel = new JPanel(new BorderLayout());
		noWrapPanel.add(mConsole);
		scrollPane1 = new JScrollPane(noWrapPanel);
		Dimension size = new Dimension(150,100);
		scrollPane1.setPreferredSize(size);
		
		frame.add(scrollPane1);
		
		mContainer.add(frame,BorderLayout.SOUTH);
	}
	
    /**
     * Creates a new button with the given parameters and adds it to the given
     * JPanel.
     * @param buttonBar - the JPanel to add this button to.
     * @param text - The text to display on the button.
     * @param action - the action string to send when clicked.
     * @param type - the ControlButton referring to this button.
     */
    private void addButtonToBar(JPanel buttonBar, String text, String action, ControlButton type) {
		JButton button = new JButton();
		button.setText(text);
		button.setActionCommand(action);
		button.addActionListener(mController);
		buttonBar.add(button);
		mButtons.put(type, button);
    }

	/**
	 * Returns the contents of the EAR code pane.
	 * @return
	 */
	public String getEARCode() {
		return mEARTextPane.getText();
	}
	
	/**
	 * Sets the contents of the EAR code pane.
	 * @param text
	 */
	public void setEARCode(String text) {
		mEARTextPane.setText(text);
	}
	
	/**
	 * Returns the contents of the high level code pane.
	 * @return
	 */
	public String getHighLevelCode() {
		return mHighLevelTextPane.getText();
	}
	
	/**
	 * Sets the contents of the high level code pane.
	 * @param text
	 */
	public void setHighLevelCode(String text) {
		mHighLevelTextPane.setText(text);
	}
	
	/**
	 * Returns the contents of the EF code pane.
	 * @return
	 */
	public String getEFCode() {
		return mEFTextPane.getText();
	}
	
	/**
	 * Sets the contents of the EF code pane.
	 * @param text
	 */
	public void setEFCode(String text) {
		mEFTextPane.setText(text);
	}
	
	/**
	 * Returns the EF text pane itself. <br/>
	 * Used by the ParserWorker & StepForwardWorker for code highlighting.
	 * @return
	 */
	public CodePane getEFTextPane() {
		return mEFTextPane;
	}
	
	/**
	 * Returns the EAR text pane itself. <br/>
	 * Used by the ParserWorker & StepForwardWorker for code highlighting.
	 * @return
	 */
	public CodePane getEARTextPane() {
		return mEARTextPane;
	}
	
	/**
	 * Returns the High Level text pane itself. <br/>
	 * Used by the ParserWorker & StepForwardWorker for code highlighting.
	 * @return
	 */
	public CodePane getHighLevelTextPane() {
		return mHighLevelTextPane;
	}
	
	/**
	 * Sets whether the code is editable. (Only the code of the current mode
	 * is ever editable)
	 * @param editable
	 */
	public void setCodeEditable(boolean editable) {
		VibeController.VibeMode mode = mController.getMode();
		if (mode==VibeController.VibeMode.EF) {
			mEFTextPane.setEditable(editable);
		}
		else if (mode==VibeController.VibeMode.EAR) {
			mEARTextPane.setEditable(editable);
		}
		else if (mode==VibeController.VibeMode.HIGHLEVEL) {
			mHighLevelTextPane.setEditable(editable);
		}
	}
	
	/**
	 * Gets the user selected tempo from the tempo slider.
	 * @return
	 */
	public int getTempo() {
		return mTempoSlider.getValue();
	}
	
	/**
	 * Sets buttons enabled or disabled. <br/>
	 * @param buttonType The ControlButton to enable/disable
	 * @param enabled true/false
	 */
	public void setButtonEnabled(ControlButton buttonType, boolean enabled) {		
		mButtons.get(buttonType).setEnabled(enabled);
	}
	
	/**
	 * Sets up the edit pane for the given mode. <br/>
	 * Sets the correct text pane to editable and hides irrelevant panes.
	 * @param mode the VibeMode we're setting up for
	 */
	public void setupEditPane(VibeController.VibeMode mode) {
		JScrollPane leftPane = (JScrollPane) mEditPane.getComponent(0);
		JScrollPane midPane = (JScrollPane) mEARAndEFPane.getComponent(0);
		
		mEFTextPane.setEditable(false);
		mEARTextPane.setEditable(false);
		mHighLevelTextPane.setEditable(false);
		
		
		if (mode==VibeController.VibeMode.EAR) {
			leftPane.setVisible(false);
			midPane.setVisible(true);
			mEARAndEFPane.setDividerSize(5);
			mEditPane.setDividerSize(0);
			mEARTextPane.setEditable(true);
		}
		if (mode==VibeController.VibeMode.EF) {
			leftPane.setVisible(false);
			midPane.setVisible(false);
			mEARAndEFPane.setDividerSize(0);
			mEditPane.setDividerSize(0);
			mEFTextPane.setEditable(true);
		}
		if (mode==VibeController.VibeMode.HIGHLEVEL) {
			leftPane.setVisible(true);
			midPane.setVisible(true);
			mEARAndEFPane.setDividerSize(5);
			mEditPane.setDividerSize(5);
			mHighLevelTextPane.setEditable(true);
		}
		
		int totalWidth = mEditPane.getWidth();
		mEARAndEFPane.setDividerLocation(totalWidth/3);
		mEditPane.setDividerLocation(totalWidth/3);
		
	}
	
	public void updateMemoryVisualiser() {
		mMemoryVisualiser.update();
	}
	
	public void setFileNameLabel(String text) {
		int maxLength = 12;
		if (text.length() > maxLength)
		{
			text = text.substring(0, maxLength) + "...";
		}
		mFileNameLabel.setText(text);
		mToolBar.revalidate();
	}
	
	public void setTimerLabel(String text) {
		mTimerLabel.setText(text);
        mTimerLabel.invalidate();
	}
	
	public void clearConsole() {
		mConsole.clearDisplay();
	}
	
	public enum ControlButton
	{
		PLAY,
		PAUSE,
		STOP,
		COMPILE,
		STEP;
	}
}
