package edu.depauw.csc480.projectv3.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import edu.depauw.csc480.projectv3.model.Enroll;
import edu.depauw.csc480.projectv3.model.Section;
import edu.depauw.csc480.projectv3.model.Student;

/**
 * Data Access Object for the Enroll table. Encapsulates all of the relevant SQL
 * commands. Based on Sciore, Section 9.1, with caching inspired by Section 9.2.
 * 
 * @author bhoward
 */
public class EnrollDAO {
	private Connection conn;
	private DatabaseManager dbm;
	private Map<Integer, Enroll> cache;

	public EnrollDAO(Connection conn, DatabaseManager dbm) {
		this.conn = conn;
		this.dbm = dbm;
		this.cache = new HashMap<>();
	}

	/**
	 * Create the Enroll table.
	 * 
	 * @param conn
	 * @throws SQLException
	 */
	public static void create(Connection conn) throws SQLException {
		StringBuilder sb = new StringBuilder();
		sb.append("create table ENROLL(");
		sb.append("  EId int,");
		sb.append("  StudentId int not null,");
		sb.append("  SectionId int not null,");
		sb.append("  Grade varchar(2),");
		sb.append("  primary key (EId),");
		sb.append("  foreign key (StudentId) references STUDENT on delete cascade,");
		sb.append("  foreign key (SectionId) references SECTION on delete no action");
		sb.append(")");

		Statement stmt = conn.createStatement();
		stmt.executeUpdate(sb.toString());
	}

	/**
	 * Retrieve an Enroll object given its key. Checks the cache to see if the
	 * desired object already exists in memory.
	 * 
	 * @param eId
	 * @return
	 */
	public Enroll find(int eId) {
		if (cache.containsKey(eId)) {
			return cache.get(eId);
		}

		try {
			StringBuilder sb = new StringBuilder();
			sb.append("select e.StudentId, e.SectionId, e.Grade");
			sb.append("  from ENROLL e");
			sb.append("  where e.EId = ?");

			PreparedStatement pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, eId);
			ResultSet rs = pstmt.executeQuery();

			// return null if enroll doesn't exist
			if (!rs.next())
				return null;

			int studentId = rs.getInt("StudentId");
			int sectionId = rs.getInt("SectionId");
			String grade = rs.getString("Grade");
			rs.close();

			Student student = dbm.findStudent(studentId);
			Section section = dbm.findSection(sectionId);
			Enroll enroll = new Enroll(this, eId, student, section, grade);
			cache.put(eId, enroll);

			return enroll;
		} catch (SQLException e) {
			dbm.cleanup();
			throw new RuntimeException("error finding enrollment", e);
		}
	}

	/**
	 * Add a new enrollment with the given attributes.
	 * 
	 * @param eId
	 * @param student
	 * @param section
	 * @param grade
	 * @return
	 */
	public Enroll insert(int eId, Student student, Section section, String grade) {
		try {
			// make sure that the cId is currently unused
			if (find(eId) != null) {
				return null;
			}

			StringBuilder sb = new StringBuilder();
			sb.append("insert into ENROLL(EId, StudentId, SectionId, Grade)");
			sb.append("  values (?, ?, ?, ?)");

			PreparedStatement pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, eId);
			pstmt.setInt(2, student.getSId());
			pstmt.setInt(3, section.getSectId());
			if (grade == null) {
				pstmt.setNull(4, Types.VARCHAR);
			} else {
				pstmt.setString(4, grade);
			}
			pstmt.executeUpdate();

			Enroll enroll = new Enroll(this, eId, student, section, grade);
			cache.put(eId, enroll);

			// Tell the Student and Section that they will need to recalculate their
			// enrollment lists
			student.invalidate();
			section.invalidate();

			return enroll;
		} catch (SQLException e) {
			dbm.cleanup();
			throw new RuntimeException("error inserting new enrollment", e);
		}
	}

	/**
	 * Grade was changed in the model object, so propagate the change to the
	 * database.
	 * 
	 * @param eId
	 * @param grade
	 */
	public void changeGrade(int eId, String grade) {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("update ENROLL");
			sb.append("  set Grade = ?");
			sb.append("  where EId = ?");

			PreparedStatement pstmt = conn.prepareStatement(sb.toString());
			if (grade == null) {
				pstmt.setNull(1, Types.VARCHAR);
			} else {
				pstmt.setString(1, grade);
			}
			pstmt.setInt(2, eId);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			dbm.cleanup();
			throw new RuntimeException("error changing grade", e);
		}
	}

	/**
	 * Clear all data from the Enroll table.
	 * 
	 * @throws SQLException
	 */
	void clear() throws SQLException {
		Statement stmt = conn.createStatement();
		String s = "delete from ENROLL";
		stmt.executeUpdate(s);
		cache.clear();
	}
}
