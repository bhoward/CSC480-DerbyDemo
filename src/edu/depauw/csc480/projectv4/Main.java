package edu.depauw.csc480.projectv4;

import java.io.PrintStream;
import java.util.Scanner;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.RollbackException;
import javax.persistence.TypedQuery;

import edu.depauw.csc480.projectv4.model.Course;
import edu.depauw.csc480.projectv4.model.Dept;
import edu.depauw.csc480.projectv4.model.Enroll;
import edu.depauw.csc480.projectv4.model.Section;
import edu.depauw.csc480.projectv4.model.Student;

public class Main {
	private static final Scanner in = new Scanner(System.in);
	private static final PrintStream out = System.out;

	public static void main(String[] args) {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("studentdb");
		EntityManager em = emf.createEntityManager();

		displayMenu();
		loop: while (true) {
			switch (requestString("Selection (0 to quit, 9 for menu)? ")) {
			case "0": // Quit
				break loop;

			case "1": // Reset
				resetTables(em);
				break;

			case "2": // List students
				listStudents(em);
				break;

			case "3": // Show transcript
				showTranscript(em);
				break;

			case "4": // Add student
				addStudent(em);
				break;

			case "5": // List sections
				listSections(em);
				break;
				
			case "6": // Add enrollment
				addEnrollment(em);
				break;

			case "7": // Change grade
				changeGrade(em);
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
		out.println("5: List sections");
		out.println("6: Add enrollment");
		out.println("7: Change grade");
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
	 * @param em
	 */
	@SuppressWarnings("unused")
	private static void resetTables(EntityManager em) {
		// Clear the tables
		EntityTransaction tx = em.getTransaction();
		tx.begin();

		em.createQuery("delete from Enroll").executeUpdate();
		em.createQuery("delete from Student").executeUpdate();
		em.createQuery("delete from Section").executeUpdate();
		em.createQuery("delete from Course").executeUpdate();
		em.createQuery("delete from Dept").executeUpdate();
		
		tryCommit(tx);
		
		em.clear(); // flush any locally-persisted objects
		
		// Now create the sample data objects and persist them
		tx = em.getTransaction();
		tx.begin();

		Dept compsci = new Dept(10, "compsci");
		Dept math = new Dept(20, "math");
		Dept drama = new Dept(30, "drama");
		
		em.persist(compsci);
		em.persist(math);
		em.persist(drama);

		Student joe = new Student(1, "joe", compsci, 2004);
		Student amy = new Student(2, "amy", math, 2004);
		Student max = new Student(3, "max", compsci, 2005);
		Student sue = new Student(4, "sue", math, 2005);
		Student bob = new Student(5, "bob", drama, 2003);
		Student kim = new Student(6, "kim", math, 2001);
		Student art = new Student(7, "art", drama, 2004);
		Student pat = new Student(8, "pat", math, 2001);
		Student lee = new Student(9, "lee", compsci, 2004);
		
		em.persist(joe);
		em.persist(amy);
		em.persist(max);
		em.persist(sue);
		em.persist(bob);
		em.persist(kim);
		em.persist(art);
		em.persist(pat);
		em.persist(lee);

		Course db = new Course(12, "db systems", compsci);
		Course comp = new Course(22, "compilers", compsci);
		Course calc = new Course(32, "calculus", math);
		Course alg = new Course(42, "algebra", math);
		Course act = new Course(52, "acting", drama);
		Course eloc = new Course(62, "elocution", drama);
		
		em.persist(db);
		em.persist(comp);
		em.persist(calc);
		em.persist(alg);
		em.persist(act);
		em.persist(eloc);

		Section db04 = new Section(13, db, "turing", 2004);
		Section db05 = new Section(23, db, "turing", 2005);
		Section calc00 = new Section(33, calc, "newton", 2000);
		Section calc01 = new Section(43, calc, "einstein", 2001);
		Section eloc01 = new Section(53, eloc, "brando", 2001);
		
		em.persist(db04);
		em.persist(db05);
		em.persist(calc00);
		em.persist(calc01);
		em.persist(eloc01);

		em.persist(new Enroll(14, joe, db04, "A"));
		em.persist(new Enroll(24, joe, calc01, "C"));
		em.persist(new Enroll(34, amy, calc01, "B+"));
		em.persist(new Enroll(44, sue, calc00, "B"));
		em.persist(new Enroll(54, sue, eloc01, "A"));
		em.persist(new Enroll(64, kim, eloc01, "A"));

		tryCommit(tx);
	}

	private static void tryCommit(EntityTransaction tx) {
		try {
			tx.commit();
		} catch (RollbackException ex) {
			ex.printStackTrace();
			tx.rollback();
		}
	}

	/**
	 * Print a table of all students with their id number, name, graduation year,
	 * and major.
	 * 
	 * @param em
	 */
	private static void listStudents(EntityManager em) {
		out.printf("%-3s %-10s %-4s %-8s\n", "Id", "Name", "Year", "Major");
		out.println("----------------------------");

		EntityTransaction tx = em.getTransaction();
		tx.begin();

		String query = "select s from Student s";
		TypedQuery<Student> q = em.createQuery(query, Student.class);
		
		for (Student student : q.getResultList()) {
			Dept major = student.getMajor();
			out.printf("%3d %-10s %-4d %-8s\n", student.getSId(), student.getSName(), student.getGradYear(),
					(major != null) ? major.getDName() : "unknown");
		}

		tryCommit(tx);
	}

	/**
	 * Request a student name and print a table of their course enrollments.
	 * 
	 * @param em
	 */
	private static void showTranscript(EntityManager em) {
		String sname = requestString("Student name? ");

		out.printf("%-3s %-8s %-20s %-4s %-8s %-5s\n", "Id", "Dept", "Course", "Year", "Prof", "Grade");
		out.println("-----------------------------------------------------");

		EntityTransaction tx = em.getTransaction();
		tx.begin();

		String query = "select s from Student s where s.sName = ?1";
		TypedQuery<Student> q = em.createQuery(query, Student.class);
		q.setParameter(1, sname);
		Student student = q.getSingleResult();
		
		for (Enroll enroll : student.getEnrollments()) {
			Section section = enroll.getSection();
			Course course = section.getCourse();
			Dept dept = course.getDept();
			out.printf("%3d %-8s %-20s %-4d %-8s %-5s\n", enroll.getEId(), dept.getDName(), course.getTitle(),
					section.getYearOffered(), section.getProf(), enroll.getGrade());
		}

		tryCommit(tx);
	}

	/**
	 * Request information to add a new student to the database. The id number must
	 * be unique, and the major must be an existing department name.
	 * 
	 * @param em
	 */
	private static void addStudent(EntityManager em) {
		int sid = requestInt("Id number? ");
		String sname = requestString("Student name? ");
		int gyear = requestInt("Graduation year? ");
		String major = requestString("Major? ");

		EntityTransaction tx = em.getTransaction();
		tx.begin();

		String query = "select d from Dept d where d.dName = ?1";
		TypedQuery<Dept> q = em.createQuery(query, Dept.class);
		q.setParameter(1, major);
		Dept dept = q.getSingleResult();
		
		Student student = new Student(sid, sname, dept, gyear);
		em.persist(student);
		
		tryCommit(tx);
		
		// TODO not checking for failure ...
	}

	/**
	 * Print a table of all sections with their id number, department, title,
	 * professor, and year offered.
	 * 
	 * @param dbm
	 */
	private static void listSections(EntityManager em) {
		out.printf("%-3s %-8s %-20s %-8s %4s\n", "Id", "Dept", "Title", "Prof", "Year");
		out.println("-----------------------------------------------");

		EntityTransaction tx = em.getTransaction();
		tx.begin();

		String query = "select k from Section k";
		TypedQuery<Section> q = em.createQuery(query, Section.class);
		
		for (Section section : q.getResultList()) {
			Course course = section.getCourse();
			Dept dept = course.getDept();
			out.printf("%-3d %-8s %-20s %-8s %4d\n", section.getSectId(), dept.getDName(), course.getTitle(),
					section.getProf(), section.getYearOffered());
		}
		
		tryCommit(tx);
	}

	/**
	 * Request information to add an enrollment record for a student. The id number
	 * must be unique, the student name and course title must exist, and the course
	 * must have been offered (exactly once) in the given year. The grade is set to
	 * NULL.
	 * 
	 * @param em
	 */
	private static void addEnrollment(EntityManager em) {
		int eid = requestInt("Id number? ");
		String sname = requestString("Student name? ");
		String title = requestString("Course title? ");
		int year = requestInt("Year offered? ");

		EntityTransaction tx = em.getTransaction();
		tx.begin();

		String query = "select s from Section s where s.yearOffered = ?1 and s.course.title = ?2";
		TypedQuery<Section> q = em.createQuery(query, Section.class);
		q.setParameter(1, year);
		q.setParameter(2, title);
		Section section = q.getSingleResult();

		query = "select s from Student s where s.sName = ?1";
		TypedQuery<Student> q2 = em.createQuery(query, Student.class);
		q2.setParameter(1, sname);
		Student student = q2.getSingleResult();

		Enroll enroll = new Enroll(eid, student, section, null);
		em.persist(enroll);
		
		tryCommit(tx);
		
		// TODO not checking for failure ...
	}

	/**
	 * Request an enrollment id and a new grade to be entered, then update the
	 * enrollment table accordingly.
	 * 
	 * @param em
	 */
	private static void changeGrade(EntityManager em) {
		int eid = requestInt("Enrollment id number? ");
		String grade = requestString("New grade? ");

		EntityTransaction tx = em.getTransaction();
		tx.begin();

		Enroll enroll = em.find(Enroll.class, eid);
		enroll.setGrade(grade);
		
		tryCommit(tx);
		
		// TODO not checking for failure ...
	}
}
