import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JWindow;

/**
 * The game is called from here.
 **/
public class BeginGame {
	public static void main(String[] args) {
		final JFrame frame = new JFrame("Menu");
		frame.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		JButton newGameButton = new JButton("New game");
		newGameButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
				frame.dispose();
				new Game(false);
			}
		});
		JButton tutorialButton = new JButton("Tutorial");
		tutorialButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
				frame.dispose();
				new Game(true);
			}
		});
		JButton loadGameButton = new JButton("Load saved game");
		loadGameButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				int chooserState = chooser.showOpenDialog(frame);
				if (chooserState == JFileChooser.APPROVE_OPTION) {
					File target = chooser.getSelectedFile();
					// Create a 'save' object
					SavedGame saved = null;
					try {
						FileInputStream fileIn = new FileInputStream(target);
						ObjectInputStream in = new ObjectInputStream(fileIn);
						saved = (SavedGame) (in.readObject());
						in.close();
						fileIn.close();
						
						frame.setVisible(false);
						frame.dispose();
						
						new Game(saved);
						
					} catch (IOException i) {
						//saved remains null
						//i.printStackTrace();
					} catch (ClassNotFoundException e1) {
						//saved remains null
					}
					if (saved == null) {
						final JWindow failWindow = new JWindow();
						failWindow.setLayout(new GridBagLayout());
						gbc.gridx = 0;
						gbc.gridy = 0;
						JLabel label = new JLabel("Failed to load file.");
						failWindow.add(label, gbc);
						JButton ok = new JButton("Ok");
						ok.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								failWindow.setVisible(false);
								frame.setVisible(true);
								failWindow.dispose();
							}
						});
						gbc.gridy++;
						failWindow.add(ok, gbc);
						failWindow.pack();
						failWindow.setLocation(frame.getX() + (frame.getWidth()-failWindow.getWidth())/3,
								frame.getY() + (frame.getHeight()-failWindow.getHeight())/3);
						failWindow.setVisible(true);
						frame.setVisible(false);
					}
				}
			}
		});
		frame.add(newGameButton, gbc);
		gbc.gridy++;
		frame.add(tutorialButton, gbc);
		gbc.gridy++;
		frame.add(loadGameButton, gbc);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}