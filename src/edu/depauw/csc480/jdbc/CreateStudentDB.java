package edu.depauw.csc480.jdbc;

import java.sql.*;

import org.apache.derby.jdbc.EmbeddedDriver;

public class CreateStudentDB {
	public static void main(String[] args) {
		String url = "jdbc:derby:studentdb;create=true";
		Driver d = new EmbeddedDriver();

		try (Connection conn = d.connect(url, null)) {
			// First clean up from previous runs, if any
			dropConstraints(conn);
			dropTables(conn);

			// Now create the schema without constraints
			addTables(conn);

			// Insert some bulk data
			insertData(conn);
			
			// Finally add in the constraints
			addConstraints(conn);
		} catch (SQLException e) {
			e.printStackTrace();
		}
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

	private static void addTables(Connection conn) {
		doUpdate(conn, "create table STUDENT(SId int, SName varchar(10), MajorId int, GradYear int, primary key (SId))",
				"Table STUDENT created.");

		doUpdate(conn, "create table DEPT(DId int, DName varchar(8), primary key (DId))", "Table DEPT created.");

		doUpdate(conn, "create table COURSE(CId int, Title varchar(20), DeptId int, primary key (CId))",
				"Table COURSE created.");

		doUpdate(conn,
				"create table SECTION(SectId int, CourseId int, Prof varchar(8), YearOffered int, primary key (SectId))",
				"Table SECTION created.");

		doUpdate(conn,
				"create table ENROLL(EId int, StudentId int, SectionId int, Grade varchar(2), primary key (EId))",
				"Table ENROLL created.");
	}

	private static void addConstraints(Connection conn) {
		doUpdate(conn, "alter table STUDENT add constraint fk_student_major foreign key (MajorId) references DEPT",
				"Added foreign key STUDENT->DEPT.");

		doUpdate(conn, "alter table COURSE add constraint fk_course_dept foreign key (DeptId) references DEPT",
				"Added foreign key COURSE->DEPT.");

		doUpdate(conn, "alter table SECTION add constraint fk_section_course foreign key (CourseId) references COURSE",
				"Added foreign key SECTION->COURSE.");

		doUpdate(conn, "alter table ENROLL add constraint fk_enroll_student foreign key (StudentId) references STUDENT",
				"Added foreign key ENROLL->STUDENT.");

		doUpdate(conn, "alter table ENROLL add constraint fk_enroll_section foreign key (SectionId) references SECTION",
				"Added foreign key ENROLL->SECTION.");
	}

	private static void dropTables(Connection conn) {
		doUpdateNoError(conn, "drop table STUDENT", "Table STUDENT dropped.");
		doUpdateNoError(conn, "drop table DEPT", "Table DEPT dropped.");
		doUpdateNoError(conn, "drop table COURSE", "Table COURSE dropped.");
		doUpdateNoError(conn, "drop table SECTION", "Table SECTION dropped.");
		doUpdateNoError(conn, "drop table ENROLL", "Table ENROLL dropped.");
	}

	private static void dropConstraints(Connection conn) {
		doUpdateNoError(conn, "alter table STUDENT drop constraint fk_student_major",
				"Dropped foreign key STUDENT->DEPT.");
		doUpdateNoError(conn, "alter table COURSE drop constraint fk_course_dept", "Dropped foreign key COURSE->DEPT.");
		doUpdateNoError(conn, "alter table SECTION drop constraint fk_section_course",
				"Dropped foreign key SECTION->COURSE.");
		doUpdateNoError(conn, "alter table ENROLL drop constraint fk_enroll_student",
				"Dropped foreign key ENROLL->STUDENT.");
		doUpdateNoError(conn, "alter table ENROLL drop constraint fk_enroll_section",
				"Dropped foreign key ENROLL->SECTION.");
	}

	private static void insertData(Connection conn) {
		try (Statement stmt = conn.createStatement()) {
			String[] studvals = { "(1, 'joe', 10, 2021)", "(2, 'amy', 20, 2020)", "(3, 'max', 10, 2022)",
					"(4, 'sue', 20, 2022)", "(5, 'bob', 30, 2020)", "(6, 'kim', 20, 2020)", "(7, 'art', 30, 2021)",
					"(8, 'pat', 20, 2019)", "(9, 'lee', 10, 2021)" };
			for (String val : studvals) {
				stmt.executeUpdate("insert into STUDENT(SId, SName, MajorId, GradYear) values " + val);
			}
			System.out.println("STUDENT records inserted.");

			String[] deptvals = { "(10, 'compsci')", "(20, 'math')", "(30, 'drama')" };
			for (String val : deptvals) {
				stmt.executeUpdate("insert into DEPT(DId, DName) values " + val);
			}
			System.out.println("DEPT records inserted.");

			String[] coursevals = { "(12, 'db systems', 10)", "(22, 'compilers', 10)", "(32, 'calculus', 20)",
					"(42, 'algebra', 20)", "(52, 'acting', 30)", "(62, 'elocution', 30)" };
			for (String val : coursevals) {
				stmt.executeUpdate("insert into COURSE(CId, Title, DeptId) values " + val);
			}
			System.out.println("COURSE records inserted.");

			String[] sectvals = { "(13, 12, 'turing', 2018)", "(23, 12, 'turing', 2019)", "(33, 32, 'newton', 2019)",
					"(43, 32, 'einstein', 2017)", "(53, 62, 'brando', 2018)" };
			for (String val : sectvals) {
				stmt.executeUpdate("insert into SECTION(SectId, CourseId, Prof, YearOffered) values " + val);
			}
			System.out.println("SECTION records inserted.");

			String[] enrollvals = { "(14, 1, 13, 'A')", "(24, 1, 43, 'C' )", "(34, 2, 43, 'B+')", "(44, 4, 33, 'B' )",
					"(54, 4, 53, 'A' )", "(64, 6, 53, 'A' )" };
			for (String val : enrollvals) {
				stmt.executeUpdate("insert into ENROLL(EId, StudentId, SectionId, Grade) values " + val);
			}
			System.out.println("ENROLL records inserted.");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
