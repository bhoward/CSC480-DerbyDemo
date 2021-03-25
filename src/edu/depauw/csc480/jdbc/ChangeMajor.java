package edu.depauw.csc480.jdbc;

import java.sql.*;

import org.apache.derby.jdbc.EmbeddedDriver;

public class ChangeMajor {
	public static void main(String[] args) {
		String url = "jdbc:derby:studentdb";
		String cmd = "update STUDENT set MajorId=30 where SName='amy'";

		Driver d = new EmbeddedDriver();
		try (Connection conn = d.connect(url, null); Statement stmt = conn.createStatement()) {
			int howmany = stmt.executeUpdate(cmd);
			System.out.println(howmany + " records changed.");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
