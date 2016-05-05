import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

public class GUI {

	private JFrame mainFrame;
	private JPanel leftPanel;
	private JPanel cubePanel;
	private JList dimensionList;
	private JList attrList;
	private JTable cube;

	// demo
	DefaultListModel model;

	public static void main(String[] args) {
		GUI gui = new GUI();
	}

	public GUI() {
		prepareGUI();
	}

	public void prepareGUI() {

		// MAIN WINDOW
		mainFrame = new JFrame("Avalanche BI Tool");
		mainFrame.setSize(900, 600);
		mainFrame.setLayout(new BorderLayout());

		// MODEL
		model = new DefaultListModel();
		
		// LEFT PANEL *************************
		leftPanel = new JPanel(new GridLayout(3, 1));
		// JLIST DIMENSION
		dimensionList = new JList(model); // insert data into JLIST parameter
		JScrollPane dimPane = new JScrollPane(dimensionList);
		// ATTRIBUTES
		attrList = new JList(model);
		JScrollPane attrPane = new JScrollPane(attrList);
		// CONFIG LEFT PANEL
		leftPanel.add(dimPane);
		leftPanel.add(attrPane);
		
		// CUBE PANEL *************************
		cubePanel = new JPanel();
		JTable cube = new JTable();
		JScrollPane cubePane = new JScrollPane(cube);
		cubePanel.add(cubePane);
		
		
		
		
		
		// ADD TO MAINFRAME
		mainFrame.add(leftPanel, BorderLayout.WEST);
		mainFrame.add(cubePanel, BorderLayout.EAST);

		mainFrame.setVisible(true);
	}

}
