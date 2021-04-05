package edu.depauw.csc480.projectv3.dao;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Properties;

import org.apache.derby.jdbc.EmbeddedDriver;

import edu.depauw.csc480.projectv3.model.Course;
import edu.depauw.csc480.projectv3.model.Dept;
import edu.depauw.csc480.projectv3.model.Enroll;
import edu.depauw.csc480.projectv3.model.Section;
import edu.depauw.csc480.projectv3.model.Student;

/**
 * This class mediates access to the student database, hiding the lower-level
 * DAO objects from the client. Based on Sciore, Section 9.1.
 * 
 * @author bhoward
 */
public class DatabaseManager {
	private Driver driver;
	private Connection conn;
	private DeptDAO deptDAO;
	private StudentDAO studentDAO;
	private CourseDAO courseDAO;
	private SectionDAO sectionDAO;
	private EnrollDAO enrollDAO;

	private final String url = "jdbc:derby:db/studentdb";

	public DatabaseManager() {
		driver = new EmbeddedDriver();

		Properties prop = new Properties();
		prop.put("create", "false");

		// try to connect to an existing database
		try {
			conn = driver.connect(url, prop);
			conn.setAutoCommit(false);
		} catch (SQLException e) {
			// database doesn't exist, so try creating it
			try {
				prop.put("create", "true");
				conn = driver.connect(url, prop);
				conn.setAutoCommit(false);
				create(conn);
			} catch (SQLException e2) {
				throw new RuntimeException("cannot connect to database", e2);
			}
		}

		deptDAO = new DeptDAO(conn, this);
		studentDAO = new StudentDAO(conn, this);
		courseDAO = new CourseDAO(conn, this);
		sectionDAO = new SectionDAO(conn, this);
		enrollDAO = new EnrollDAO(conn, this);
	}

	/**
	 * Initialize the tables in a newly created database
	 * 
	 * @param conn
	 * @throws SQLException
	 */
	private void create(Connection conn) throws SQLException {
		DeptDAO.create(conn);
		StudentDAO.create(conn);
		CourseDAO.create(conn);
		SectionDAO.create(conn);
		EnrollDAO.create(conn);
		conn.commit();
	}

	// ***************************************************************
	// Data retrieval functions -- find a model object given its key

	public Dept findDept(int dId) {
		return deptDAO.find(dId);
	}

	public Student findStudent(int sId) {
		return studentDAO.find(sId);
	}

	public Course findCourse(int cId) {
		return courseDAO.find(cId);
	}

	public Section findSection(int sectId) {
		return sectionDAO.find(sectId);
	}

	public Enroll findEnroll(int eId) {
		return enrollDAO.find(eId);
	}

	public Dept findDeptByName(String dName) {
		return deptDAO.findByName(dName);
	}

	public Student findStudentByName(String sName) {
		return studentDAO.findByName(sName);
	}

	public Course findCourseByTitle(String title) {
		return courseDAO.findByTitle(title);
	}

	// ***************************************************************
	// Data retrieval functions -- get collections of objects
	
	public Collection<Student> getStudents() {
		return studentDAO.getAll();
	}

	// ***************************************************************
	// Data insertion functions -- create new model object from attributes

	public Dept insertDept(int dId, String dName) {
		return deptDAO.insert(dId, dName);
	}

	public Student insertStudent(int sId, String sName, Dept major, int gradYear) {
		return studentDAO.insert(sId, sName, major, gradYear);
	}

	public Course insertCourse(int cId, String title, Dept dept) {
		return courseDAO.insert(cId, title, dept);
	}

	public Section insertSection(int sectId, Course course, String prof, int yearOffered) {
		return sectionDAO.insert(sectId, course, prof, yearOffered);
	}

	public Enroll insertEnroll(int eId, Student student, Section section, String grade) {
		return enrollDAO.insert(eId, student, section, grade);
	}

	// ***************************************************************
	// Utility functions

	/**
	 * Commit changes since last call to commit
	 */
	public void commit() {
		try {
			conn.commit();
		} catch (SQLException e) {
			throw new RuntimeException("cannot commit database", e);
		}
	}

	/**
	 * Abort changes since last call to commit, then close connection
	 */
	public void cleanup() {
		try {
			conn.rollback();
			conn.close();
		} catch (SQLException e) {
			System.out.println("fatal error: cannot cleanup connection");
		}
	}

	/**
	 * Close connection and shutdown database
	 */
	public void close() {
		try {
			conn.close();
		} catch (SQLException e) {
			throw new RuntimeException("cannot close database connection", e);
		}

		// Now shutdown the embedded database system -- this is Derby-specific
		try {
			Properties prop = new Properties();
			prop.put("shutdown", "true");
			conn = driver.connect(url, prop);
		} catch (SQLException e) {
			// This is supposed to throw an exception...
			System.out.println("Derby has shut down successfully");
		}
	}

	/**
	 * Clear out all data from database (but leave empty tables). Note that the
	 * order is the reverse in which the tables were created, because of referential
	 * integrity constraints.
	 */
	public void clearTables() {
		try {
			enrollDAO.clear();
			sectionDAO.clear();
			courseDAO.clear();
			studentDAO.clear();
			deptDAO.clear();
		} catch (SQLException e) {
			throw new RuntimeException("cannot clear tables", e);
		}
	}
}
