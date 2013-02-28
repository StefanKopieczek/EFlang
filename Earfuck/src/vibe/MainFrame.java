package vibe;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Rectangle;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ScrollPaneConstants;

public class MainFrame extends JFrame {

	private Container mContainer;
	private JSplitPane mEditPane;
	private JSplitPane mEARAndEFPane;
	private CodePane mHighLevelTextPane;
	private CodePane mEARTextPane;
	private CodePane mEFTextPane;
	private JMenuBar mMenuBar;
	private JPanel mToolBar;
	
	private VibeController mController;
	
	public MainFrame() {
		initComponents();
		this.setTitle("VIBE Earfuck IDE");
	}
	
	private void setupUI() {
		mController.setMode(VibeController.VibeMode.EAR);
		mEditPane.setDividerLocation(0.333);
		mEARAndEFPane.setDividerLocation(0.5);
	}
	
	public void create() {
		setVisible(true);
		setupUI();
	}
	
	private void initComponents() {
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		
		mController = new VibeController(this);
		
		mContainer = getContentPane();
		
		//Create text panes
		JPanel noWrapPanel;
		JScrollPane scrollPane1, scrollPane2, scrollPane3;
		mHighLevelTextPane = new CodePane();
		noWrapPanel = new JPanel(new BorderLayout());
		noWrapPanel.add(mHighLevelTextPane);
		scrollPane1 = new JScrollPane(noWrapPanel);
		
		mEARTextPane = new CodePane();
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
		mToolBar = new JPanel();
		mContainer.add(mToolBar,BorderLayout.NORTH);
		JButton button = new JButton();
		button.setText("Compile");
		button.setActionCommand("compile");
		button.addActionListener(mController);
		mToolBar.add(button);
		
		button = new JButton();
		button.setText("|>");
		button.setActionCommand("play");
		button.addActionListener(mController);
		mToolBar.add(button);
		
		button = new JButton();
		button.setText("||");
		button.setActionCommand("pause");
		button.addActionListener(mController);
		button.setEnabled(false);
		mToolBar.add(button);
		
		button = new JButton();
		button.setText("O");
		button.setActionCommand("stop");
		button.addActionListener(mController);
		button.setEnabled(false);
		mToolBar.add(button);
		
		button = new JButton();
		button.setText("->");
		button.setActionCommand("step");
		button.addActionListener(mController);
		mToolBar.add(button);
		
	}
	
	public String getEARCode() {
		return mEARTextPane.getText();
	}
	
	public void setEARCode(String text) {
		mEARTextPane.setText(text);
	}
	
	public String getHighLevelCode() {
		return mHighLevelTextPane.getText();
	}
	
	public void setHighLevelCode(String text) {
		mHighLevelTextPane.setText(text);
	}
	
	public String getEFCode() {
		return mEFTextPane.getText();
	}
	
	public void setEFCode(String text) {
		mEFTextPane.setText(text);
	}
	
	public CodePane getEFTextPane() {
		return mEFTextPane;
	}
	
	public void setButtonEnabled(int index, boolean enabled) {
		JButton button = (JButton) mToolBar.getComponent(index);
		button.setEnabled(enabled);
	}
	
	public void setupEditPane(VibeController.VibeMode mode) {
		JScrollPane leftPane = (JScrollPane) mEditPane.getComponent(0);
		JScrollPane midPane = (JScrollPane) mEARAndEFPane.getComponent(0);
		
		if (mode==VibeController.VibeMode.EAR) {
			leftPane.setVisible(false);
			midPane.setVisible(true);
			mEARAndEFPane.setDividerSize(5);
			mEditPane.setDividerSize(0);
		}
		if (mode==VibeController.VibeMode.EF) {
			leftPane.setVisible(false);
			midPane.setVisible(false);
			mEARAndEFPane.setDividerSize(0);
			mEditPane.setDividerSize(0);
		}
		if (mode==VibeController.VibeMode.HIGHLEVEL) {
			leftPane.setVisible(true);
			midPane.setVisible(true);
			mEARAndEFPane.setDividerSize(5);
			mEditPane.setDividerSize(5);
		}
		
	}

}
