package edu.depauw.csc480.projectv3.model;

import java.util.Collection;

import edu.depauw.csc480.projectv3.dao.DeptDAO;

public class Dept {
	private DeptDAO dao;
	
	private int dId;
	private String dName;

	private Collection<Student> majors;
	private Collection<Course> courses;

	public Dept(DeptDAO dao, int dId, String dName) {
		this.dao = dao;
		this.dId = dId;
		this.dName = dName;
	}

	public int getDId() {
		return dId;
	}

	public String getDName() {
		return dName;
	}

	public Collection<Student> getMajors() {
		if (majors == null) {
			majors = dao.getMajors(dId);
		}
		return majors;
	}

	public Collection<Course> getCourses() {
		if (courses == null) {
			courses = dao.getCourses(dId);
		}
		return courses;
	}

	public void invalidate() {
		majors = null;
		courses = null;
	}
}
