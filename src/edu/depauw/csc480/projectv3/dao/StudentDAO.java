package edu.depauw.csc480.projectv3.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import edu.depauw.csc480.projectv3.model.Dept;
import edu.depauw.csc480.projectv3.model.Enroll;
import edu.depauw.csc480.projectv3.model.Student;

/**
 * Data Access Object for the Student table. Encapsulates all of the relevant
 * SQL commands. Based on Sciore, Section 9.1, with caching inspired by Section
 * 9.2.
 * 
 * @author bhoward
 */
public class StudentDAO {
	private Connection conn;
	private DatabaseManager dbm;
	private Map<Integer, Student> cache;

	public StudentDAO(Connection conn, DatabaseManager dbm) {
		this.conn = conn;
		this.dbm = dbm;
		this.cache = new HashMap<>();
	}

	/**
	 * Create the Student table.
	 * 
	 * @param conn
	 * @throws SQLException
	 */
	public static void create(Connection conn) throws SQLException {
		StringBuilder sb = new StringBuilder();
		sb.append("create table STUDENT(");
		sb.append("  SId int,");
		sb.append("  SName varchar(10) not null,");
		sb.append("  MajorId int,");
		sb.append("  GradYear int not null,");
		sb.append("  primary key (SId),");
		sb.append("  foreign key (MajorId) references DEPT on delete set null");
		sb.append(")");

		Statement stmt = conn.createStatement();
		stmt.executeUpdate(sb.toString());
	}

	/**
	 * Retrieve a Student object given its key. Checks the cache to see if the
	 * desired object already exists in memory.
	 * 
	 * @param sId
	 * @return the Student object, or null if not found
	 */
	public Student find(int sId) {
		if (cache.containsKey(sId)) {
			return cache.get(sId);
		}

		try {
			StringBuilder sb = new StringBuilder();
			sb.append("select s.SName, s.MajorId, s.GradYear");
			sb.append("  from STUDENT s");
			sb.append("  where s.SId = ?");

			PreparedStatement pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, sId);
			ResultSet rs = pstmt.executeQuery();

			// return null if student doesn't exist
			if (!rs.next())
				return null;

			String sName = rs.getString("SName");
			int majorId = rs.getInt("MajorId");
			int gradYear = rs.getInt("GradYear");
			rs.close();

			Dept major = dbm.findDept(majorId);
			Student student = new Student(this, sId, sName, major, gradYear);
			cache.put(sId, student);

			return student;
		} catch (SQLException e) {
			dbm.cleanup();
			throw new RuntimeException("error finding student", e);
		}
	}

	/**
	 * Retrieve a Student object given its name. If there is more than one with the
	 * same name, this will return the first one found. Checks the cache to see if
	 * the desired object already exists in memory.
	 * 
	 * @param sName
	 * @return the Student object, or null if not found
	 */
	public Student findByName(String sName) {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("select s.SId, s.MajorId, s.GradYear");
			sb.append("  from STUDENT s");
			sb.append("  where s.SName = ?");

			PreparedStatement pstmt = conn.prepareStatement(sb.toString());
			pstmt.setString(1, sName);
			ResultSet rs = pstmt.executeQuery();

			// return null if student doesn't exist
			if (!rs.next())
				return null;

			int sId = rs.getInt("SId");
			int majorId = rs.getInt("MajorId"); // returns 0 if NULL
			int gradYear = rs.getInt("GradYear");
			rs.close();

			if (cache.containsKey(sId)) {
				return cache.get(sId);
			}

			Dept major = dbm.findDept(majorId); // null if majorId doesn't exist
			Student student = new Student(this, sId, sName, major, gradYear);
			cache.put(sId, student);

			return student;
		} catch (SQLException e) {
			dbm.cleanup();
			throw new RuntimeException("error finding student by name", e);
		}
	}

	/**
	 * Add a new Student with the given attributes.
	 * 
	 * @param sId
	 * @param sName
	 * @param major
	 * @param gradYear
	 * @return the new Student object, or null if the key already exists
	 */
	public Student insert(int sId, String sName, Dept major, int gradYear) {
		try {
			// make sure that the sId is currently unused
			if (find(sId) != null) {
				return null;
			}

			StringBuilder sb = new StringBuilder();
			sb.append("insert into STUDENT(SId, SName, MajorId, GradYear)");
			sb.append("  values (?, ?, ?, ?)");

			PreparedStatement pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, sId);
			pstmt.setString(2, sName);
			if (major == null) {
				pstmt.setNull(3, Types.INTEGER);
			} else {
				pstmt.setInt(3, major.getDId());
			}
			pstmt.setInt(4, gradYear);
			pstmt.executeUpdate();

			Student student = new Student(this, sId, sName, major, gradYear);
			cache.put(sId, student);

			// Tell the Dept that it will have to recalculate its majors list
			major.invalidate();

			return student;
		} catch (SQLException e) {
			dbm.cleanup();
			throw new RuntimeException("error inserting new student", e);
		}
	}

	/**
	 * Major was changed in the model object, so propagate the change to the
	 * database.
	 * 
	 * @param sId
	 * @param major
	 */
	public void changeMajor(int sId, Dept oldMajor, Dept major) {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("update STUDENT");
			sb.append("  set MajorId = ?");
			sb.append("  where SId = ?");

			PreparedStatement pstmt = conn.prepareStatement(sb.toString());
			if (major == null) {
				pstmt.setNull(1, Types.INTEGER);
			} else {
				pstmt.setInt(1, major.getDId());
			}
			pstmt.setInt(2, sId);
			pstmt.executeUpdate();

			// Tell the relevant Depts that they need to recalculate their majors lists
			if (oldMajor != null) {
				oldMajor.invalidate();
			}

			if (major != null) {
				major.invalidate();
			}
		} catch (SQLException e) {
			dbm.cleanup();
			throw new RuntimeException("error changing major", e);
		}
	}

	/**
	 * Graduation year was changed in the model object, so propagate the change to
	 * the database.
	 * 
	 * @param sId
	 * @param gradYear
	 */
	public void changeGradYear(int sId, int gradYear) {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("update STUDENT");
			sb.append("  set GradYear = ?");
			sb.append("  where SId = ?");

			PreparedStatement pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, gradYear);
			pstmt.setInt(2, sId);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			dbm.cleanup();
			throw new RuntimeException("error changing graduation year", e);
		}
	}

	/**
	 * Retrieve a Collection of all enrollments for the given student. Backwards
	 * direction of Student foreign key from Enroll.
	 * 
	 * @param sId
	 * @return the collection
	 */
	public Collection<Enroll> getEnrollments(int sId) {
		try {
			Collection<Enroll> enrollments = new ArrayList<>();

			StringBuilder sb = new StringBuilder();
			sb.append("select e.EId");
			sb.append("  from ENROLL e");
			sb.append("  where e.StudentId = ?");

			PreparedStatement pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, sId);
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				int eId = rs.getInt("EId");
				enrollments.add(dbm.findEnroll(eId));
			}
			rs.close();

			return enrollments;
		} catch (SQLException e) {
			dbm.cleanup();
			throw new RuntimeException("error getting student enrollments", e);
		}
	}

	/**
	 * Retrieve a Collection of all students in the database. Note that this is
	 * pushing the limits of doing too much work on the client side and not in the
	 * database server, because it might be tempting to filter this collection with
	 * Java code. Instead, we should use this only when we really want all of the
	 * students, and write other query methods to retrieve specific subsets. Also,
	 * as with find, this creates all new objects in memory, even if other Student
	 * objects already exist.
	 * 
	 * @return the collection
	 */
	public Collection<Student> getAll() {
		try {
			Collection<Student> students = new ArrayList<>();

			StringBuilder sb = new StringBuilder();
			sb.append("select s.SId, s.SName, s.MajorId, s.GradYear");
			sb.append("  from STUDENT s");

			PreparedStatement pstmt = conn.prepareStatement(sb.toString());
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				int sId = rs.getInt("SId");
				String sName = rs.getString("SName");
				int majorId = rs.getInt("MajorId");
				int gradYear = rs.getInt("GradYear");

				if (cache.containsKey(sId)) {
					students.add(cache.get(sId));
				} else {
					Dept major = dbm.findDept(majorId);
					Student student = new Student(this, sId, sName, major, gradYear);
					cache.put(sId, student);
					students.add(student);
				}
			}
			rs.close();

			return students;
		} catch (SQLException e) {
			dbm.cleanup();
			throw new RuntimeException("error finding student", e);
		}
	}

	/**
	 * Clear all data from the Student table.
	 * 
	 * @throws SQLException
	 */
	void clear() throws SQLException {
		Statement stmt = conn.createStatement();
		String s = "delete from STUDENT";
		stmt.executeUpdate(s);
		cache.clear();
	}
}
