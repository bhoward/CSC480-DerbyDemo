package edu.depauw.csc480.projectv3;

import java.io.PrintStream;
import java.util.Scanner;

import edu.depauw.csc480.projectv3.dao.DatabaseManager;
import edu.depauw.csc480.projectv3.model.Course;
import edu.depauw.csc480.projectv3.model.Dept;
import edu.depauw.csc480.projectv3.model.Enroll;
import edu.depauw.csc480.projectv3.model.Section;
import edu.depauw.csc480.projectv3.model.Student;


/**
 * This is an example of a menu-driven client for Sciore's student database. It
 * uses Data Access Objects (following Section 9.1) to manage in-memory objects
 * that correspond to the data in the database, and to encapsulate the SQL
 * queries and commands. As compared to the version in
 * edu.depauw.csc480.projectv2, this uses a more sophisticated implementation of
 * DAO, fixing some of the problems pointed out in Section 9.2.
 * 
 * @author bhoward
 */
public class Main {
	private static final Scanner in = new Scanner(System.in);
	private static final PrintStream out = System.out;

	public static void main(String[] args) {
		DatabaseManager dbm = new DatabaseManager();

		displayMenu();
		loop: while (true) {
			switch (requestString("Selection (0 to quit, 9 for menu)? ")) {
			case "0": // Quit
				break loop;

			case "1": // Reset
				resetTables(dbm);
				break;

			case "2": // List students
				listStudents(dbm);
				break;

			case "3": // Show transcript
				showTranscript(dbm);
				break;

			case "4": // Add student
				addStudent(dbm);
				break;

			case "5": // Add enrollment
				addEnrollment(dbm);
				break;

			case "6": // Change grade
				changeGrade(dbm);
				break;

			default:
				displayMenu();
				break;
			}
		}
		out.println("Done");
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

	private static int requestInt(String prompt) {
		out.print(prompt);
		out.flush();
		int result = in.nextInt();
		in.nextLine();
		return result;
	}

	/**
	 * Delete the contents of the tables, then reinsert the sample data from Sciore.
	 * Note that the order is important, so that foreign key references already
	 * exist before they are used.
	 * 
	 * @param dbm
	 */
	@SuppressWarnings("unused")
	private static void resetTables(DatabaseManager dbm) {
		dbm.clearTables();
		dbm.commit();

		Dept compsci = dbm.insertDept(10, "compsci");
		Dept math = dbm.insertDept(20, "math");
		Dept drama = dbm.insertDept(30, "drama");

		Student joe = dbm.insertStudent(1, "joe", compsci, 2004);
		Student amy = dbm.insertStudent(2, "amy", math, 2004);
		Student max = dbm.insertStudent(3, "max", compsci, 2005);
		Student sue = dbm.insertStudent(4, "sue", math, 2005);
		Student bob = dbm.insertStudent(5, "bob", drama, 2003);
		Student kim = dbm.insertStudent(6, "kim", math, 2001);
		Student art = dbm.insertStudent(7, "art", drama, 2004);
		Student pat = dbm.insertStudent(8, "pat", math, 2001);
		Student lee = dbm.insertStudent(9, "lee", compsci, 2004);

		Course db = dbm.insertCourse(12, "db systems", compsci);
		Course comp = dbm.insertCourse(22, "compilers", compsci);
		Course calc = dbm.insertCourse(32, "calculus", math);
		Course alg = dbm.insertCourse(42, "algebra", math);
		Course act = dbm.insertCourse(52, "acting", drama);
		Course eloc = dbm.insertCourse(62, "elocution", drama);

		Section db04 = dbm.insertSection(13, db, "turing", 2004);
		Section db05 = dbm.insertSection(23, db, "turing", 2005);
		Section calc00 = dbm.insertSection(33, calc, "newton", 2000);
		Section calc01 = dbm.insertSection(43, calc, "einstein", 2001);
		Section eloc01 = dbm.insertSection(53, eloc, "brando", 2001);

		dbm.insertEnroll(14, joe, db04, "A");
		dbm.insertEnroll(24, joe, calc01, "C");
		dbm.insertEnroll(34, amy, calc01, "B+");
		dbm.insertEnroll(44, sue, calc00, "B");
		dbm.insertEnroll(54, sue, eloc01, "A");
		dbm.insertEnroll(64, kim, eloc01, "A");

		dbm.commit();
	}

	/**
	 * Print a table of all students with their id number, name, graduation year,
	 * and major.
	 * 
	 * @param dbm
	 */
	private static void listStudents(DatabaseManager dbm) {
		out.printf("%-3s %-10s %-4s %-8s\n", "Id", "Name", "Year", "Major");
		out.println("----------------------------");

		for (Student student : dbm.getStudents()) {
			Dept major = student.getMajor();
			out.printf("%3d %-10s %-4d %-8s\n", student.getSId(), student.getSName(), student.getGradYear(),
					(major != null) ? major.getDName() : "unknown");
		}

		dbm.commit();
	}

	/**
	 * Request a student name and print a table of their course enrollments.
	 * 
	 * @param dbm
	 */
	private static void showTranscript(DatabaseManager dbm) {
		String sname = requestString("Student name? ");
		Student student = dbm.findStudentByName(sname);

		out.printf("%-3s %-8s %-20s %-4s %-8s %-5s\n", "Id", "Dept", "Course", "Year", "Prof", "Grade");
		out.println("-----------------------------------------------------");

		for (Enroll enroll : student.getEnrollments()) {
			Section section = enroll.getSection();
			Course course = section.getCourse();
			Dept dept = course.getDept();
			out.printf("%3d %-8s %-20s %-4d %-8s %-5s\n", enroll.getEId(), dept.getDName(), course.getTitle(),
					section.getYearOffered(), section.getProf(), enroll.getGrade());
		}

		dbm.commit();
	}

	/**
	 * Request information to add a new student to the database. The id number must
	 * be unique, and the major must be an existing department name.
	 * 
	 * @param dbm
	 */
	private static void addStudent(DatabaseManager dbm) {
		int sid = requestInt("Id number? ");
		String sname = requestString("Student name? ");
		int gyear = requestInt("Graduation year? ");
		String major = requestString("Major? ");

		Dept dept = dbm.findDeptByName(major);
		Student student = dbm.insertStudent(sid, sname, dept, gyear);
		dbm.commit();

		if (student != null) {
			out.println("1 student inserted");
		} else {
			out.println("0 students inserted");
		}
	}

	/**
	 * Request information to add an enrollment record for a student. The id number
	 * must be unique, the student name and course title must exist, and the course
	 * must have been offered (exactly once) in the given year. The grade is set to
	 * NULL.
	 * 
	 * @param dbm
	 */
	private static void addEnrollment(DatabaseManager dbm) {
		int eid = requestInt("Id number? ");
		String sname = requestString("Student name? ");
		String title = requestString("Course title? ");
		int year = requestInt("Year offered? ");

		Course course = dbm.findCourseByTitle(title);

		// Search sections of the course for one in the desired year
		// (This should really be done on the db server side)
		Section section = null;
		for (Section sect : course.getSections()) {
			if (sect.getYearOffered() == year) {
				section = sect;
			}
		}

		Student student = dbm.findStudentByName(sname);

		if (section == null) {
			out.println("0 records inserted: section not found");
		} else if (student == null) {
			out.println("0 records inserted: student not found");
		} else {
			Enroll enroll = dbm.insertEnroll(eid, student, section, null);
			dbm.commit();

			if (enroll != null) {
				out.println("1 record inserted");
			} else {
				out.println("0 records inserted");
			}
		}
	}

	/**
	 * Request an enrollment id and a new grade to be entered, then update the
	 * enrollment table accordingly.
	 * 
	 * @param dbm
	 */
	private static void changeGrade(DatabaseManager dbm) {
		int eid = requestInt("Enrollment id number? ");
		String grade = requestString("New grade? ");

		Enroll enroll = dbm.findEnroll(eid);
		enroll.setGrade(grade);
		dbm.commit();
	}
}
