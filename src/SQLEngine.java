import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.mysql.jdbc.ResultSetMetaData;

public class SQLEngine {

	// connection variables
	private static DBConnection dbc;
	private static Connection conn = null;

	// data variables
	public ResultSet rs;

	public SQLEngine() {
		dbc = new DBConnection();
		conn = dbc.getConnection();
		if (conn != null) {
			System.out.println("Connection successful!");
		}
	}

	public ArrayList<String> populateDimensions() throws SQLException {
		ArrayList<String> data = new ArrayList<String>();

		String sql = "SELECT TABLE_NAME FROM information_schema.tables WHERE TABLE_SCHEMA = 'starschema'";
		Statement stmt = conn.createStatement();
		rs = stmt.executeQuery(sql);
		while (rs.next()) {
			String tableName = rs.getString("TABLE_NAME");
			data.add(tableName);
		}
		return data;
	}

	public ArrayList<String> populateAttr(String dim) throws SQLException {
		ArrayList<String> data = new ArrayList<String>();
		String sql = "SELECT * FROM " + dim;
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		ResultSetMetaData md = (ResultSetMetaData) rs.getMetaData();
		int columns = md.getColumnCount();
		for (int i = 1; i <= columns; i++) {
			data.add(md.getColumnName(i));
		}
		return data;
	}

	public ResultSet populateCentralCube() throws SQLException {
		String sql = "SELECT store.store_state, product.brand, sum(sales_fact.unit_sales) as unit_sales"
				+ " FROM store, product, time, sales_fact"
				+ " WHERE time.year = 1994 AND store.store_key = sales_fact.store_key AND"
				+ " product.product_key = sales_fact.product_key AND time.time_key = sales_fact.time_key"
				+ " GROUP BY store.store_state, product.brand";
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		return rs;
	}

}
