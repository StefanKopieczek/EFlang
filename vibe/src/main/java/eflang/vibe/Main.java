package eflang.vibe;

import java.io.File;

import javax.swing.SwingUtilities;

/**
 * VIBE - Visual Interface for Building Earfuck
 * @author Ryan Norris
 *
 */

public class Main {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {			
			public void run() {
				MainFrame frame = new MainFrame();
				frame.create();
			}
		});
	}
}
