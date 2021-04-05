package edu.depauw.csc480.projectv2.model;

import java.util.Collection;

import edu.depauw.csc480.projectv2.dao.StudentDAO;

public class Student {
	private StudentDAO dao;
	
	private int sId;
	private String sName;
	private Dept major;
	private int gradYear;
	
	private Collection<Enroll> enrollments;

	public Student(StudentDAO dao, int sId, String sName, Dept major, int gradYear) {
		this.dao = dao;
		this.sId = sId;
		this.sName = sName;
		this.major = major;
		this.gradYear = gradYear;
	}

	public int getSId() {
		return sId;
	}

	public String getSName() {
		return sName;
	}

	public Dept getMajor() {
		return major;
	}

	public void setMajor(Dept major) {
		this.major = major;
		dao.changeMajor(sId, major);
	}

	public int getGradYear() {
		return gradYear;
	}

	public void setGradYear(int gradYear) {
		this.gradYear = gradYear;
		dao.changeGradYear(sId, gradYear);
	}

	public Collection<Enroll> getEnrollments() {
		if (enrollments == null) {
			enrollments = dao.getEnrollments(sId);
		}
		return enrollments;
	}
}
