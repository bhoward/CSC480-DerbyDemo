package edu.depauw.csc480.projectv2.model;

import edu.depauw.csc480.projectv2.dao.EnrollDAO;

public class Enroll {
	private EnrollDAO dao;
	
	private int eId;
	private Student student;
	private Section section;
	private String grade;
	
	public Enroll(EnrollDAO dao, int eId, Student student, Section section, String grade) {
		this.dao = dao;
		this.eId = eId;
		this.student = student;
		this.section = section;
		this.grade = grade;
	}

	public int getEId() {
		return eId;
	}

	public Student getStudent() {
		return student;
	}

	public Section getSection() {
		return section;
	}
	
	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
		dao.changeGrade(eId, grade);
	}
}
