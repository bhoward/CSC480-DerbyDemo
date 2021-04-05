package edu.depauw.csc480.projectv1;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Scanner;

import org.apache.derby.jdbc.EmbeddedDriver;

/**
 * This is an example of a menu-driven client for Sciore's student database. It
 * uses straight JDBC to execute SQL queries and commands.
 * 
 * @author bhoward
 */
public class Main {
	private static final Scanner in = new Scanner(System.in);
	private static final PrintStream out = System.out;

	public static void main(String[] args) {
		try (Connection conn = getConnection("jdbc:derby:db/studentdb")) {
			displayMenu();
			loop: while (true) {
				switch (requestString("Selection (0 to quit, 9 for menu)? ")) {
				case "0": // Quit
					break loop;

				case "1": // Reset
					resetTables(conn);
					break;

				case "2": // List students
					listStudents(conn);
					break;

				case "3": // Show transcript
					showTranscript(conn);
					break;

				case "4": // Add student
					addStudent(conn);
					break;

				case "5": // Add enrollment
					addEnrollment(conn);
					break;

				case "6": // Change grade
					changeGrade(conn);
					break;

				default:
					displayMenu();
					break;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		out.println("Done");
	}

	/**
	 * Attempt to open a connection to an embedded Derby database at the given URL.
	 * If the database does not exist, create it with empty tables.
	 * 
	 * @param url
	 * @return
	 */
	private static Connection getConnection(String url) {
		Driver driver = new EmbeddedDriver();

		// try to connect to an existing database
		Properties prop = new Properties();
		prop.put("create", "false");
		try {
			Connection conn = driver.connect(url, prop);
			return conn;
		} catch (SQLException e) {
			// database doesn't exist, so try creating it
			try {
				prop.put("create", "true");
				Connection conn = driver.connect(url, prop);
				createTables(conn);
				return conn;
			} catch (SQLException e2) {
				throw new RuntimeException("cannot connect to database", e2);
			}
		}
	}

	private static void displayMenu() {
		out.println("0: Quit");
		out.println("1: Reset tables");
		out.println("2: List students");
		out.println("3: Show transcript");
		out.println("4: Add student");
		out.println("5: Add enrollment");
		out.println("6: Change grade");
	}

	private static String requestString(String prompt) {
		out.print(prompt);
		out.flush();
		return in.nextLine();
	}

	private static void createTables(Connection conn) {
		// First clean up from previous runs, if any
		dropTables(conn);

		// Now create the schema
		addTables(conn);
	}

	private static void doUpdate(Connection conn, String statement, String message) {
		try (Statement stmt = conn.createStatement()) {
			stmt.executeUpdate(statement);
			System.out.println(message);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void doUpdateNoError(Connection conn, String statement, String message) {
		try (Statement stmt = conn.createStatement()) {
			stmt.executeUpdate(statement);
			System.out.println(message);
		} catch (SQLException e) {
			// Ignore error
		}
	}

	/**
	 * Create the tables for the student database from Sciore. Note that the tables
	 * have to be created in a particular order, so that foreign key references
	 * point to already-created tables. This allows the simpler technique of
	 * creating the tables directly with their f.k. constraints, rather than
	 * altering the tables later to add constraints.
	 * 
	 * @param conn
	 */
	private static void addTables(Connection conn) {
		StringBuilder sb = new StringBuilder();
		sb.append("create table DEPT(");
		sb.append("  DId int,");
		sb.append("  DName varchar(8) not null,");
		sb.append("  primary key (DId)");
		sb.append(")");
		doUpdate(conn, sb.toString(), "Table DEPT created.");

		sb = new StringBuilder();
		sb.append("create table STUDENT(");
		sb.append("  SId int,");
		sb.append("  SName varchar(10) not null,");
		sb.append("  MajorId int,");
		sb.append("  GradYear int,");
		sb.append("  primary key (SId),");
		sb.append("  foreign key (MajorId) references DEPT on delete set null");
		sb.append(")");
		doUpdate(conn, sb.toString(), "Table STUDENT created.");

		sb = new StringBuilder();
		sb.append("create table COURSE(");
		sb.append("  CId int,");
		sb.append("  Title varchar(20) not null,");
		sb.append("  DeptId int not null,");
		sb.append("  primary key (CId),");
		sb.append("  foreign key (DeptId) references DEPT on delete cascade");
		sb.append(")");
		doUpdate(conn, sb.toString(), "Table COURSE created.");

		sb = new StringBuilder();
		sb.append("create table SECTION(");
		sb.append("  SectId int,");
		sb.append("  CourseId int not null,");
		sb.append("  Prof varchar(8) not null,");
		sb.append("  YearOffered int not null,");
		sb.append("  primary key (SectId),");
		sb.append("  foreign key (CourseId) references COURSE on delete cascade");
		sb.append(")");
		doUpdate(conn, sb.toString(), "Table SECTION created.");

		sb = new StringBuilder();
		sb.append("create table ENROLL(");
		sb.append("  EId int,");
		sb.append("  StudentId int not null,");
		sb.append("  SectionId int not null,");
		sb.append("  Grade varchar(2),");
		sb.append("  primary key (EId),");
		sb.append("  foreign key (StudentId) references STUDENT on delete cascade,");
		sb.append("  foreign key (SectionId) references SECTION on delete no action");
		sb.append(")");
		doUpdate(conn, sb.toString(), "Table ENROLL created.");
	}

	/**
	 * Delete the tables for the student database. Note that the tables are dropped
	 * in the reverse order that they were created, to satisfy referential integrity
	 * (foreign key) constraints.
	 * 
	 * @param conn
	 */
	private static void dropTables(Connection conn) {
		doUpdateNoError(conn, "drop table ENROLL", "Table ENROLL dropped.");
		doUpdateNoError(conn, "drop table SECTION", "Table SECTION dropped.");
		doUpdateNoError(conn, "drop table COURSE", "Table COURSE dropped.");
		doUpdateNoError(conn, "drop table STUDENT", "Table STUDENT dropped.");
		doUpdateNoError(conn, "drop table DEPT", "Table DEPT dropped.");
	}

	/**
	 * Delete the contents of the tables, then reinsert the sample data from Sciore.
	 * Again, note that the order is important, so that foreign key references
	 * already exist before they are used.
	 * 
	 * @param conn
	 */
	private static void resetTables(Connection conn) {
		try (Statement stmt = conn.createStatement()) {
			int count = 0;
			count += stmt.executeUpdate("delete from ENROLL");
			count += stmt.executeUpdate("delete from SECTION");
			count += stmt.executeUpdate("delete from COURSE");
			count += stmt.executeUpdate("delete from STUDENT");
			count += stmt.executeUpdate("delete from DEPT");
			System.out.println(count + " records deleted");

			String[] deptvals = {
					"(10, 'compsci')", "(20, 'math')", "(30, 'drama')"
			};
			count = 0;
			for (String val : deptvals) {
				count += stmt.executeUpdate("insert into DEPT(DId, DName) values " + val);
			}
			System.out.println(count + " DEPT records inserted.");

			String[] studvals = {
					"(1, 'joe', 10, 2004)",
					"(2, 'amy', 20, 2004)",
					"(3, 'max', 10, 2005)",
					"(4, 'sue', 20, 2005)",
					"(5, 'bob', 30, 2003)",
					"(6, 'kim', 20, 2001)",
					"(7, 'art', 30, 2004)",
					"(8, 'pat', 20, 2001)",
					"(9, 'lee', 10, 2004)"
			};
			count = 0;
			for (String val : studvals) {
				count += stmt.executeUpdate("insert into STUDENT(SId, SName, MajorId, GradYear) values " + val);
			}
			System.out.println(count + " STUDENT records inserted.");

			String[] coursevals = {
					"(12, 'db systems', 10)",
					"(22, 'compilers', 10)",
					"(32, 'calculus', 20)",
					"(42, 'algebra', 20)",
					"(52, 'acting', 30)",
					"(62, 'elocution', 30)"
			};
			count = 0;
			for (String val : coursevals) {
				count += stmt.executeUpdate("insert into COURSE(CId, Title, DeptId) values " + val);
			}
			System.out.println(count + " COURSE records inserted.");

			String[] sectvals = {
					"(13, 12, 'turing', 2004)",
					"(23, 12, 'turing', 2005)",
					"(33, 32, 'newton', 2000)",
					"(43, 32, 'einstein', 2001)",
					"(53, 62, 'brando', 2001)"
			};
			count = 0;
			for (String val : sectvals) {
				count += stmt.executeUpdate("insert into SECTION(SectId, CourseId, Prof, YearOffered) values " + val);
			}
			System.out.println(count + " SECTION records inserted.");

			String[] enrollvals = {
					"(14, 1, 13, 'A')",
					"(24, 1, 43, 'C')",
					"(34, 2, 43, 'B+')",
					"(44, 4, 33, 'B')",
					"(54, 4, 53, 'A')",
					"(64, 6, 53, 'A')"
			};
			count = 0;
			for (String val : enrollvals) {
				count += stmt.executeUpdate("insert into ENROLL(EId, StudentId, SectionId, Grade) values " + val);
			}
			System.out.println(count + " ENROLL records inserted.");
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Print a table of all students with their id number, name, graduation year,
	 * and major.
	 * 
	 * @param conn
	 */
	private static void listStudents(Connection conn) {
		StringBuilder query = new StringBuilder();
		query.append("select s.SId, s.SName, s.GradYear, d.DName");
		query.append("  from STUDENT s, DEPT d");
		query.append("  where s.MajorId = d.DId");

		try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query.toString())) {
			out.printf("%-3s %-10s %-4s %-8s\n", "Id", "Name", "Year", "Major");
			out.println("----------------------------");
			while (rs.next()) {
				int sid = rs.getInt("SId");
				String sname = rs.getString("SName");
				int gradYear = rs.getInt("GradYear");
				String dname = rs.getString("DName");

				out.printf("%3d %-10s %-4d %-8s\n", sid, sname, gradYear, dname);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Request a student name and print a table of their course enrollments.
	 * 
	 * @param conn
	 */
	private static void showTranscript(Connection conn) {
		String sname = requestString("Student name? ");

		StringBuilder query = new StringBuilder();
		query.append("select e.EId, d.DName, c.Title, s.YearOffered, s.Prof, e.Grade");
		query.append("  from DEPT d, COURSE c, SECTION s, ENROLL e, STUDENT t");
		query.append("  where c.DeptId = d.DId");
		query.append("    and s.CourseId = c.CId");
		query.append("    and e.SectionId = s.SectId");
		query.append("    and e.StudentId = t.SId");
		query.append("    and t.SName = ?");

		try (PreparedStatement pstmt = conn.prepareStatement(query.toString())) {
			pstmt.setString(1, sname);
			ResultSet rs = pstmt.executeQuery();

			out.printf("%-3s %-8s %-20s %-4s %-8s %-5s\n", "Id", "Dept", "Course", "Year", "Prof", "Grade");
			out.println("-----------------------------------------------------");
			while (rs.next()) {
				int eid = rs.getInt("EId");
				String dname = rs.getString("DName");
				String title = rs.getString("Title");
				int yearOffered = rs.getInt("YearOffered");
				String prof = rs.getString("Prof");
				String grade = rs.getString("Grade");

				out.printf("%3d %-8s %-20s %-4d %-8s %-5s\n", eid, dname, title, yearOffered, prof, grade);
			}

			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Request information to add a new student to the database. The id number must
	 * be unique, and the major must be an existing department name.
	 * 
	 * @param conn
	 */
	private static void addStudent(Connection conn) {
		String sid = requestString("Id number? ");
		String sname = requestString("Student name? ");
		String gyear = requestString("Graduation year? ");
		String major = requestString("Major? ");

		StringBuilder command = new StringBuilder();
		command.append("insert into STUDENT(SId, SName, MajorId, GradYear)");
		command.append("  select ?, ?, d.DId, ?");
		command.append("  from DEPT d");
		command.append("  where d.DName = ?");

		try (PreparedStatement pstmt = conn.prepareStatement(command.toString())) {
			pstmt.setString(1, sid);
			pstmt.setString(2, sname);
			pstmt.setString(3, gyear);
			pstmt.setString(4, major);
			int count = pstmt.executeUpdate();

			out.println(count + " student(s) inserted");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Request information to add an enrollment record for a student. The id number
	 * must be unique, the student name and course title must exist, and the course
	 * must have been offered (exactly once) in the given year. The grade is set to
	 * NULL.
	 * 
	 * @param conn
	 */
	private static void addEnrollment(Connection conn) {
		String eid = requestString("Id number? ");
		String sname = requestString("Student name? ");
		String title = requestString("Course title? ");
		String year = requestString("Year offered? ");

		StringBuilder command = new StringBuilder();
		command.append("insert into ENROLL(EId, StudentId, SectionId)");
		command.append("  select ?, s.SId, t.SectId");
		command.append("  from STUDENT s, SECTION t, COURSE c");
		command.append("  where s.SName = ?");
		command.append("    and c.Title = ?");
		command.append("    and t.CourseId = c.CId");
		command.append("    and t.YearOffered = ?");

		try (PreparedStatement pstmt = conn.prepareStatement(command.toString())) {
			pstmt.setString(1, eid);
			pstmt.setString(2, sname);
			pstmt.setString(3, title);
			pstmt.setString(4, year);
			int count = pstmt.executeUpdate();

			out.println(count + " record(s) inserted");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Request an enrollment id and a new grade to be entered, then update the
	 * enrollment table accordingly.
	 * 
	 * @param conn
	 */
	private static void changeGrade(Connection conn) {
		String eid = requestString("Enrollment id number? ");
		String grade = requestString("New grade? ");

		StringBuilder command = new StringBuilder();
		command.append("update ENROLL");
		command.append("  set Grade = ?");
		command.append("  where EId = ?");

		try (PreparedStatement pstmt = conn.prepareStatement(command.toString())) {
			pstmt.setString(1, grade);
			pstmt.setString(2, eid);
			int count = pstmt.executeUpdate();

			out.println(count + " record(s) updated");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
