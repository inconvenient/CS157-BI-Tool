import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
	private JPanel centerPanel;
	private JPanel northPanel;

	private JTextField userEntry;

	private JList dimensionList;
	private JList attrList;
	private JList selectList;

	private JButton selectBtn;
	private JButton removeBtn;
	private JButton clearBtn;
	private JButton goBtn;

	private JTable cube;
	JScrollPane cubePane;

	// Data Variables
	private ArrayList<String> attributes;
	// current selections
	private HashMap<String, String> selections = new HashMap<String, String>();

	// Control variable for central cube creation
	private Boolean firstLoad = true;

	public static void main(String[] args) throws SQLException {
		// Instantiate SQLEngine
		sqlEngine = new SQLEngine();
		// Do GUI stuff
		GUI gui = new GUI();
	}

	public GUI() throws SQLException {
		buildGUI();
	}

	public void buildGUI() throws SQLException {

		// MAIN WINDOW *************************
		mainFrame = new JFrame("Avalanche BI Tool");
		mainFrame.setSize(1600, 900);
		mainFrame.setLayout(new BorderLayout());
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// LEFT PANEL *************************
		leftPanel = new JPanel(new GridLayout(1, 4));

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

		// User entry area for custom input
		userEntry = new JTextField();

		// Add selection to list
		DefaultListModel<String> selectModel = new DefaultListModel<String>();
		selectBtn = new JButton("Add attribute");
		selectBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String tempDim = dimensionList.getSelectedValue().toString();
				String tempAttr = attrList.getSelectedValue().toString();
				selections.put(dimensionList.getSelectedValue().toString(), attrList.getSelectedValue().toString());
				selectModel.addElement(tempDim + "." + tempAttr);
			}
		});
		// Remove selection from list
		clearBtn = new JButton("Clear selected list");
		clearBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				selections.clear();
				selectModel.removeAllElements();
			}
		});
		// GO BUTTON!
		goBtn = new JButton("Execute query");
		goBtn.addActionListener(new ActionListener() {

			StringBuilder sb;

			@Override
			public void actionPerformed(ActionEvent e) {
				sb = new StringBuilder();
				Iterator iter = selections.entrySet().iterator();
				sb.append("SELECT ");
				while (iter.hasNext()) {
					Map.Entry pair = (Map.Entry) iter.next();
					sb.append(pair.getKey() + "." + pair.getValue() + ", ");
				}
				// Trim the last AND
				String sql = sb.toString().substring(0, sb.lastIndexOf(", "));
				// Finalize the sql statement
				sb.append("sum(sales_fact.unit_sales) as unit_sales");
				sb.append(" FROM store, product, time, sales_fact");
				sb.append(" WHERE time.year = 1994 AND store.store_key = sales_fact.store_key AND");
				sb.append(" product.product_key = sales_fact.product_key AND time.time_key = sales_fact.time_key");
				Iterator iter2 = selections.entrySet().iterator();
				sb.append(" GROUP BY ");
				while (iter2.hasNext()) {
					Map.Entry pair = (Map.Entry) iter2.next();
					sb.append(pair.getKey() + "." + pair.getValue() + ", ");
				}
				String finalSql = sb.toString().substring(0, sb.lastIndexOf(","));
				try {
					System.out.println(finalSql);
					cubePanel.remove(cubePane);
					cube = new JTable();
					populateNewCube(sqlEngine.executeQuery(finalSql));
					cubePane = new JScrollPane(cube);
					cubePanel.add(cubePane);
					cubePanel.validate();
					
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		removeBtn = new JButton("Remove attribute");
		removeBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				selections.remove(dimensionList.getSelectedValue());
				selectModel.removeElement(dimensionList.getSelectedValue().toString() + "." + attrList.getSelectedValue().toString());
			}
		});

		// CONFIG LEFT PANEL
		leftPanel.add(dimPane);
		leftPanel.add(attrPane);
		leftPanel.add(userEntry);

		// NORTH PANEL
		northPanel = new JPanel(new GridLayout(1, 1));
		selectList = new JList(selectModel);
		JScrollPane selectionPane = new JScrollPane(selectList);
		northPanel.add(selectionPane);

		// CENTER PANEL
		centerPanel = new JPanel(new GridLayout(1, 4));
		centerPanel.add(selectBtn);
		centerPanel.add(removeBtn);
		centerPanel.add(clearBtn);
		centerPanel.add(goBtn);

		// CUBE PANEL *************************
		cubePanel = new JPanel();
		cube = new JTable();

		// Populate Cube
		populateFirstCube();

		cubePane = new JScrollPane(cube);
		cubePanel.add(cubePane);

		// MAIN FRAME *************************
		mainFrame.add(northPanel, BorderLayout.CENTER);
		mainFrame.add(leftPanel, BorderLayout.WEST);
		mainFrame.add(cubePanel, BorderLayout.EAST);
		mainFrame.add(centerPanel, BorderLayout.SOUTH);

		mainFrame.setVisible(true);
	}

	public void populateFirstCube() throws SQLException {
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
	}

	public void populateNewCube(ResultSet rs) throws SQLException {
		ArrayList columnNames = new ArrayList();
		ArrayList data = new ArrayList();
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
	}
}
