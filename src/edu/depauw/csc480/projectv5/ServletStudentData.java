package edu.depauw.csc480.projectv5;

import java.io.IOException;
import java.io.PrintWriter;

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
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class ServletStudentData extends HttpServlet {
	private EntityManagerFactory emf = Persistence.createEntityManagerFactory("studentdb");
	private EntityManager em = emf.createEntityManager();

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String studentName = request.getParameter("student_name");

		if (studentName == null) {
			displayStudents(response);
		} else {
			displayTranscript(response, studentName);
		}
	}

	private void displayStudents(HttpServletResponse response) throws IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<html>");
		out.println("<head> <title>Student Data</title> </head>");
		out.println("<body>");
		out.println("<p>Here is the student data</p>");

		EntityTransaction tx = em.getTransaction();
		tx.begin();

		String qry = "select s from Student s";
		TypedQuery<Student> q = em.createQuery(qry, Student.class);

		out.println("<p><table border=1>");
		out.println("<tr> <th>Name</th> <th>GradYear</th> <th>Major</th> </tr>");
		for (Student s : q.getResultList()) {
			String name = s.getSName();
			int year = s.getGradYear();
			Dept major = s.getMajor();
			out.print("<tr> <td>" + makeLink(name) + "</td> <td>" + year + "</td> <td>");
			if (major != null) {
				out.print(major.getDName());
			} else {
				out.print("unknown");
			}
			out.println("</td> </tr>");
		}
		out.println("</table></p>");
		out.println("</body> </html>");

		try {
			tx.commit();
		} catch (RollbackException ex) {
			ex.printStackTrace();
			tx.rollback();
		}

		out.close();
	}

	private String makeLink(String name) {
		return "<a href=\"/University/StudentData?student_name=" + name + "\">" + name + "</a>";
	}

	private void displayTranscript(HttpServletResponse response, String studentName) throws IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<html>");
		out.println("<head> <title>Student Data</title> </head>");
		out.println("<body>");
		out.println("<p>Here is the student transcript for " + studentName + "</p>");

		EntityTransaction tx = em.getTransaction();
		tx.begin();

		String qry = "select s from Student s where s.sName = ?1";
		TypedQuery<Student> q = em.createQuery(qry, Student.class);
		q.setParameter(1, studentName);
		Student student = q.getSingleResult();

		out.println("<p><table border=1>");
		out.println("<tr> <th>Department</th> <th>Course</th> <th>Year</th> <th>Prof</th> <th>Grade</th> </tr>");
		for (Enroll e : student.getEnrollments()) {
			Section s = e.getSection();
			Course c = s.getCourse();
			Dept d = c.getDept();

			String dName = d.getDName();
			String title = c.getTitle();
			int year = s.getYearOffered();
			String prof = s.getProf();
			String grade = e.getGrade();

			out.print("<tr> <td>" + dName + "</td> <td>" + title + "</td>  <td>" + year + "</td>");
			out.println("<td>" + prof + "</td> <td>" + grade + "</td> </tr>");
		}
		out.println("</table></p>");
		out.println("</body> </html>");

		try {
			tx.commit();
		} catch (RollbackException ex) {
			ex.printStackTrace();
			tx.rollback();
		}

		out.close();
	}
}