package edu.depauw.csc480.projectv2.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

import edu.depauw.csc480.projectv2.model.Course;
import edu.depauw.csc480.projectv2.model.Dept;
import edu.depauw.csc480.projectv2.model.Student;

/**
 * Data Access Object for the Dept table. Encapsulates all of the relevant SQL
 * commands. Based on Sciore, Section 9.1.
 * 
 * @author bhoward
 */
public class DeptDAO {
	private Connection conn;
	private DatabaseManager dbm;

	public DeptDAO(Connection conn, DatabaseManager dbm) {
		this.conn = conn;
		this.dbm = dbm;
	}

	/**
	 * Create the Dept table.
	 * 
	 * @param conn
	 * @throws SQLException
	 */
	public static void create(Connection conn) throws SQLException {
		StringBuilder sb = new StringBuilder();
		sb.append("create table DEPT(");
		sb.append("  DId int,");
		sb.append("  DName varchar(8) not null,");
		sb.append("  primary key (DId)");
		sb.append(")");

		Statement stmt = conn.createStatement();
		stmt.executeUpdate(sb.toString());
	}

	/**
	 * Retrieve a Dept object given its key. Note that this creates a new object in
	 * memory, even if another object for the same Dept already exists.
	 * 
	 * @param dId
	 * @return the Dept object, or null if not found
	 */
	public Dept find(int dId) {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("select d.DName");
			sb.append("  from DEPT d");
			sb.append("  where d.DId = ?");

			PreparedStatement pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, dId);
			ResultSet rs = pstmt.executeQuery();

			// return null if department doesn't exist
			if (!rs.next())
				return null;

			String dName = rs.getString("DName");
			rs.close();

			Dept dept = new Dept(this, dId, dName);

			return dept;
		} catch (SQLException e) {
			dbm.cleanup();
			throw new RuntimeException("error finding department", e);
		}
	}

	/**
	 * Retrieve a Dept object given its name. If there is more than one with the
	 * same name, this will return the first one found. Note that this creates a new
	 * object in memory, even if another object for the same Dept already exists.
	 * 
	 * @param dName
	 * @return the Dept object, or null if not found
	 */
	public Dept findByName(String dName) {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("select d.DId");
			sb.append("  from DEPT d");
			sb.append("  where d.DName = ?");

			PreparedStatement pstmt = conn.prepareStatement(sb.toString());
			pstmt.setString(1, dName);
			ResultSet rs = pstmt.executeQuery();

			// return null if department doesn't exist
			if (!rs.next())
				return null;

			int dId = rs.getInt("DId");
			rs.close();

			Dept dept = new Dept(this, dId, dName);

			return dept;
		} catch (SQLException e) {
			dbm.cleanup();
			throw new RuntimeException("error finding department by name", e);
		}
	}

	/**
	 * Add a new Dept with the given attributes.
	 * 
	 * @param dId
	 * @param dName
	 * @return the new Dept object, or null if the key already exists
	 */
	public Dept insert(int dId, String dName) {
		try {
			// make sure that the dId is currently unused
			if (find(dId) != null) {
				return null;
			}

			StringBuilder sb = new StringBuilder();
			sb.append("insert into DEPT(DId, DName)");
			sb.append("  values (?, ?)");

			PreparedStatement pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, dId);
			pstmt.setString(2, dName);
			pstmt.executeUpdate();

			Dept dept = new Dept(this, dId, dName);

			return dept;
		} catch (SQLException e) {
			dbm.cleanup();
			throw new RuntimeException("error inserting new department", e);
		}
	}

	/**
	 * Retrieve a Collection of all Students majoring in the given department.
	 * Backwards direction of Dept foreign key from Student.
	 * 
	 * @param dId
	 * @return the collection
	 */
	public Collection<Student> getMajors(int dId) {
		try {
			Collection<Student> majors = new ArrayList<>();

			StringBuilder sb = new StringBuilder();
			sb.append("select s.SId");
			sb.append("  from STUDENT s");
			sb.append("  where s.MajorId = ?");

			PreparedStatement pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, dId);
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				int sId = rs.getInt("SId");
				majors.add(dbm.findStudent(sId));
			}
			rs.close();

			return majors;
		} catch (SQLException e) {
			dbm.cleanup();
			throw new RuntimeException("error getting department majors", e);
		}
	}

	/**
	 * Retrieve a Collection of all Courses offered by the given department.
	 * Backwards direction of Dept foreign key from Course.
	 * 
	 * @param dId
	 * @return the collection
	 */
	public Collection<Course> getCourses(int dId) {
		try {
			Collection<Course> courses = new ArrayList<>();

			StringBuilder sb = new StringBuilder();
			sb.append("select c.CId");
			sb.append("  from COURSE c");
			sb.append("  where c.DeptId = ?");

			PreparedStatement pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, dId);
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				int cId = rs.getInt("CId");
				courses.add(dbm.findCourse(cId));
			}
			rs.close();

			return courses;
		} catch (SQLException e) {
			dbm.cleanup();
			throw new RuntimeException("error getting department courses", e);
		}
	}

	/**
	 * Clear all data from the Dept table.
	 * 
	 * @throws SQLException
	 */
	void clear() throws SQLException {
		Statement stmt = conn.createStatement();
		String s = "delete from DEPT";
		stmt.executeUpdate(s);
	}
}
