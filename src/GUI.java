import java.awt.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.*;

public class GUI {

	// SQL Engine
	private static SQLEngine sqlEngine;

	// GUI Variables
	private JFrame mainFrame;
	private JPanel leftPanel;
	private JPanel cubePanel;
	private JList dimensionList;
	private JList attrList;
	private JTable cube;

	// Data Variables
	private ArrayList<String> attributes;

	public static void main(String[] args) throws SQLException {
		sqlEngine = new SQLEngine();
		// Do GUI stuff
		GUI gui = new GUI();
		// Instantiate SQLEngine

	}

	public GUI() throws SQLException {
		buildGUI();
	}

	public void buildGUI() throws SQLException {

		// MAIN WINDOW *************************
		mainFrame = new JFrame("Avalanche BI Tool");
		mainFrame.setSize(1280, 960);
		mainFrame.setLayout(new BorderLayout());
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// LEFT PANEL *************************
		leftPanel = new JPanel(new GridLayout(3, 1));

		// JLIST DIMENSION
		dimensionList = new JList(sqlEngine.populateDimensions().toArray());
		JScrollPane dimPane = new JScrollPane(dimensionList);
		// Trigger populateAttr when dimension is clicked
		dimensionList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					try {
						attributes = sqlEngine.populateAttr(dimensionList.getSelectedValue().toString());
						DefaultListModel<String> model = new DefaultListModel<String>();
						for (String s : attributes) {
							model.addElement(s);
						}
						attrList.setModel(model);
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		// ATTRIBUTES
		if (attrList == null) {
			attrList = new JList(new DefaultListModel());
		}
		JScrollPane attrPane = new JScrollPane(attrList);

		// CONFIG LEFT PANEL
		leftPanel.add(dimPane);
		leftPanel.add(attrPane);

		// CUBE PANEL *************************
		cubePanel = new JPanel();
		JTable cube = new JTable();

		// Populate Cube
		ArrayList columnNames = new ArrayList();
		ArrayList data = new ArrayList();
		ResultSet rs = sqlEngine.populateCentralCube();
		ResultSetMetaData md = rs.getMetaData();
		int columns = md.getColumnCount();

		// Get column names
		for (int i = 1; i <= columns; i++) {
			columnNames.add(md.getColumnName(i));
		}

		// Get row data
		while (rs.next()) {
			ArrayList row = new ArrayList(columns);
			for (int i = 1; i <= columns; i++) {
				row.add(rs.getObject(i));
			}
			data.add(row);
		}

		// Convert data into Vectors to construct JTable
		Vector colNamesVector = new Vector();
		Vector dataVector = new Vector();

		for (int i = 0; i < data.size(); i++) {
			ArrayList subArray = (ArrayList) data.get(i);
			Vector subVector = new Vector();
			for (int j = 0; j < subArray.size(); j++) {
				subVector.add(subArray.get(j));
			}
			dataVector.add(subVector);
		}

		for (int i = 0; i < columnNames.size(); i++) {
			colNamesVector.add(columnNames.get(i));
		}

		// Create table with database data
		cube = new JTable(dataVector, colNamesVector) {
			public Class getColumnClass(int column) {
				for (int row = 0; row < getRowCount(); row++) {
					Object o = getValueAt(row, column);
					if (o != null) {
						return o.getClass();
					}
				}
				return Object.class;
			}
		};

		JScrollPane cubePane = new JScrollPane(cube);
		cubePanel.add(cubePane);

		// ADD TO MAINFRAME
		mainFrame.add(leftPanel, BorderLayout.WEST);
		mainFrame.add(cubePanel, BorderLayout.EAST);

		mainFrame.setVisible(true);
	}

	public void populateDimensions() {
		ArrayList columnNames = new ArrayList();

	}

}
