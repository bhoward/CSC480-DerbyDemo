package edu.depauw.csc480.projectv2.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

import edu.depauw.csc480.projectv2.model.Course;
import edu.depauw.csc480.projectv2.model.Enroll;
import edu.depauw.csc480.projectv2.model.Section;

/**
 * Data Access Object for the Section table. Encapsulates all of the relevant
 * SQL commands. Based on Sciore, Section 9.1.
 * 
 * @author bhoward
 */
public class SectionDAO {
	private Connection conn;
	private DatabaseManager dbm;

	public SectionDAO(Connection conn, DatabaseManager dbm) {
		this.conn = conn;
		this.dbm = dbm;
	}

	/**
	 * Create the Section table.
	 * 
	 * @param conn
	 * @throws SQLException
	 */
	public static void create(Connection conn) throws SQLException {
		StringBuilder sb = new StringBuilder();
		sb.append("create table SECTION(");
		sb.append("  SectId int,");
		sb.append("  CourseId int not null,");
		sb.append("  Prof varchar(8) not null,");
		sb.append("  YearOffered int not null,");
		sb.append("  primary key (SectId),");
		sb.append("  foreign key (CourseId) references COURSE on delete cascade");
		sb.append(")");

		Statement stmt = conn.createStatement();
		stmt.executeUpdate(sb.toString());
	}

	/**
	 * Retrieve a Section object given its key. Note that this creates a new object
	 * in memory, even if another object for the same Section already exists.
	 * 
	 * @param sectId
	 * @return the Section object, or null if not found
	 */
	public Section find(int sectId) {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("select s.CourseId, s.Prof, s.YearOffered");
			sb.append("  from SECTION s");
			sb.append("  where s.SectId = ?");

			PreparedStatement pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, sectId);
			ResultSet rs = pstmt.executeQuery();

			// return null if section doesn't exist
			if (!rs.next())
				return null;

			int courseId = rs.getInt("CourseId");
			String prof = rs.getString("Prof");
			int yearOffered = rs.getInt("YearOffered");
			rs.close();

			Course course = dbm.findCourse(courseId);
			Section section = new Section(this, sectId, course, prof, yearOffered);

			return section;
		} catch (SQLException e) {
			dbm.cleanup();
			throw new RuntimeException("error finding section", e);
		}
	}

	/**
	 * Add a new Section with the given attributes.
	 * 
	 * @param sectId
	 * @param course
	 * @param prof
	 * @param yearOffered
	 * @return
	 */
	public Section insert(int sectId, Course course, String prof, int yearOffered) {
		try {
			// make sure that the cId is currently unused
			if (find(sectId) != null) {
				return null;
			}

			StringBuilder sb = new StringBuilder();
			sb.append("insert into SECTION(SectId, CourseId, Prof, YearOffered)");
			sb.append("  values (?, ?, ?, ?)");

			PreparedStatement pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, sectId);
			pstmt.setInt(2, course.getCId());
			pstmt.setString(3, prof);
			pstmt.setInt(4, yearOffered);
			pstmt.executeUpdate();

			Section section = new Section(this, sectId, course, prof, yearOffered);

			return section;
		} catch (SQLException e) {
			dbm.cleanup();
			throw new RuntimeException("error inserting new section", e);
		}
	}

	/**
	 * Retrieve a Collection of all enrollments for the given section. Backwards
	 * direction of Section foreign key from Enroll.
	 * 
	 * @param sectId
	 * @return the collection
	 */
	public Collection<Enroll> getEnrollments(int sectId) {
		try {
			Collection<Enroll> enrollments = new ArrayList<>();

			StringBuilder sb = new StringBuilder();
			sb.append("select e.EId");
			sb.append("  from ENROLL e");
			sb.append("  where e.SectionId = ?");

			PreparedStatement pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, sectId);
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				int eId = rs.getInt("EId");
				enrollments.add(dbm.findEnroll(eId));
			}
			rs.close();

			return enrollments;
		} catch (SQLException e) {
			dbm.cleanup();
			throw new RuntimeException("error getting section enrollments", e);
		}
	}

	/**
	 * Retrieve a Collection of all sections in the database. Note that this is
	 * pushing the limits of doing too much work on the client side and not in the
	 * database server, because it might be tempting to filter this collection with
	 * Java code. Instead, we should use this only when we really want all of the
	 * sections, and write other query methods to retrieve specific subsets. Also,
	 * as with find, this creates all new objects in memory, even if other Section
	 * objects already exist.
	 * 
	 * @return the collection
	 */
	public Collection<Section> getAll() {
		try {
			Collection<Section> sections = new ArrayList<>();

			StringBuilder sb = new StringBuilder();
			sb.append("select k.SectId, k.CourseId, k.Prof, k.YearOffered");
			sb.append("  from SECTION k");

			PreparedStatement pstmt = conn.prepareStatement(sb.toString());
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				int sectId = rs.getInt("SectId");
				int courseId = rs.getInt("CourseId");
				String prof = rs.getString("Prof");
				int year = rs.getInt("YearOffered");

				Course course = dbm.findCourse(courseId);
				Section section = new Section(this, sectId, course, prof, year);
				sections.add(section);
			}
			rs.close();

			return sections;
		} catch (SQLException e) {
			dbm.cleanup();
			throw new RuntimeException("error finding sections", e);
		}
	}

	/**
	 * Clear all data from the Section table.
	 * 
	 * @throws SQLException
	 */
	void clear() throws SQLException {
		Statement stmt = conn.createStatement();
		String s = "delete from SECTION";
		stmt.executeUpdate(s);
	}
}
