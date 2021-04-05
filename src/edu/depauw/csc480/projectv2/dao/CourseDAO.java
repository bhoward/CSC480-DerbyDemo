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
import edu.depauw.csc480.projectv2.model.Section;

/**
 * Data Access Object for the Course table. Encapsulates all of the relevant SQL
 * commands. Based on Sciore, Section 9.1.
 * 
 * @author bhoward
 */
public class CourseDAO {
	private Connection conn;
	private DatabaseManager dbm;

	public CourseDAO(Connection conn, DatabaseManager dbm) {
		this.conn = conn;
		this.dbm = dbm;
	}

	/**
	 * Create the Course table.
	 * 
	 * @param conn
	 * @throws SQLException
	 */
	public static void create(Connection conn) throws SQLException {
		StringBuilder sb = new StringBuilder();
		sb.append("create table COURSE(");
		sb.append("  CId int,");
		sb.append("  Title varchar(20) not null,");
		sb.append("  DeptId int not null,");
		sb.append("  primary key (CId),");
		sb.append("  foreign key (DeptId) references DEPT on delete cascade");
		sb.append(")");

		Statement stmt = conn.createStatement();
		stmt.executeUpdate(sb.toString());
	}

	/**
	 * Retrieve a Course object given its key. Note that this creates a new object
	 * in memory, even if another object for the same Course already exists.
	 * 
	 * @param cId
	 * @return the Course object, or null if not found
	 */
	public Course find(int cId) {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("select c.Title, c.DeptId");
			sb.append("  from COURSE c");
			sb.append("  where c.CId = ?");

			PreparedStatement pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, cId);
			ResultSet rs = pstmt.executeQuery();

			// return null if course doesn't exist
			if (!rs.next())
				return null;

			String title = rs.getString("Title");
			int deptId = rs.getInt("DeptId");
			rs.close();

			Dept dept = dbm.findDept(deptId);
			Course course = new Course(this, cId, title, dept);

			return course;
		} catch (SQLException e) {
			dbm.cleanup();
			throw new RuntimeException("error finding course", e);
		}
	}

	/**
	 * Retrieve a Course object given its title. If there is more than one with the
	 * same title, this will return the first one found. Note that this creates a
	 * new object in memory, even if another object for the same Course already
	 * exists.
	 * 
	 * @param title
	 * @return the Course object, or null if not found
	 */
	public Course findByTitle(String title) {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("select c.CId, c.DeptId");
			sb.append("  from COURSE c");
			sb.append("  where c.Title = ?");

			PreparedStatement pstmt = conn.prepareStatement(sb.toString());
			pstmt.setString(1, title);
			ResultSet rs = pstmt.executeQuery();

			// return null if course doesn't exist
			if (!rs.next())
				return null;

			int cId = rs.getInt("CId");
			int deptId = rs.getInt("DeptId");
			rs.close();

			Dept dept = dbm.findDept(deptId);
			Course course = new Course(this, cId, title, dept);

			return course;
		} catch (SQLException e) {
			dbm.cleanup();
			throw new RuntimeException("error finding course by title", e);
		}
	}

	/**
	 * Add a new Course with the given attributes.
	 * 
	 * @param cId
	 * @param title
	 * @param dept
	 * @return the new Course object, or null if the key already exists
	 */
	public Course insert(int cId, String title, Dept dept) {
		try {
			// make sure that the cId is currently unused
			if (find(cId) != null) {
				return null;
			}

			StringBuilder sb = new StringBuilder();
			sb.append("insert into COURSE(CId, Title, DeptId)");
			sb.append("  values (?, ?, ?)");

			PreparedStatement pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, cId);
			pstmt.setString(2, title);
			pstmt.setInt(3, dept.getDId());
			pstmt.executeUpdate();

			Course course = new Course(this, cId, title, dept);

			return course;
		} catch (SQLException e) {
			dbm.cleanup();
			throw new RuntimeException("error inserting new course", e);
		}
	}

	/**
	 * Retrieve a Collection of all sections for the given course. Backwards
	 * direction of Course foreign key from Section.
	 * 
	 * @param cId
	 * @return the collection
	 */
	public Collection<Section> getSections(int cId) {
		try {
			Collection<Section> sections = new ArrayList<>();

			StringBuilder sb = new StringBuilder();
			sb.append("select s.SectId");
			sb.append("  from SECTION s");
			sb.append("  where s.CourseId = ?");

			PreparedStatement pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, cId);
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				int sectId = rs.getInt("SectId");
				sections.add(dbm.findSection(sectId));
			}
			rs.close();

			return sections;
		} catch (SQLException e) {
			dbm.cleanup();
			throw new RuntimeException("error getting course sections", e);
		}
	}

	/**
	 * Clear all data from the Course table.
	 * 
	 * @throws SQLException
	 */
	void clear() throws SQLException {
		Statement stmt = conn.createStatement();
		String s = "delete from COURSE";
		stmt.executeUpdate(s);
	}
}
