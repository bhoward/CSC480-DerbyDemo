package edu.depauw.csc480.projectv2.model;

import java.util.Collection;

import edu.depauw.csc480.projectv2.dao.SectionDAO;

public class Section {
	private SectionDAO dao;
	
	private int sectId;
	private Course course;
	private String prof;
	private int yearOffered;
	
	private Collection<Enroll> enrollments;

	public Section(SectionDAO dao, int sectId, Course course, String prof, int yearOffered) {
		this.dao = dao;
		this.sectId = sectId;
		this.course = course;
		this.prof = prof;
		this.yearOffered = yearOffered;
	}

	public int getSectId() {
		return sectId;
	}

	public Course getCourse() {
		return course;
	}

	public String getProf() {
		return prof;
	}

	public int getYearOffered() {
		return yearOffered;
	}

	public Collection<Enroll> getEnrollments() {
		if (enrollments == null) {
			enrollments = dao.getEnrollments(sectId);
		}
		return enrollments;
	}
}
