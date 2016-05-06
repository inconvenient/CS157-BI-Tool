import java.sql.*;

public class DBConnection {

	String url = "jdbc:mysql://localhost:3306/starschema"; // CHANGE THIS!
	String userid = "root"; // CHANGE THIS!
	String password = ""; // CHANGE THIS!

	public DBConnection() {

	}

	public Connection getConnection() {
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url, userid, password);
		} catch (SQLException error) {
			System.out.println("Error: " + error);
		}
		return conn;
	}
}
