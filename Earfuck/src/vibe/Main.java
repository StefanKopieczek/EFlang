package vibe;

import javax.swing.SwingUtilities;

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
