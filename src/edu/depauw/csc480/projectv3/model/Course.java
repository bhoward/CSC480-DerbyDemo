package edu.depauw.csc480.projectv3.model;

import java.util.Collection;

import edu.depauw.csc480.projectv3.dao.CourseDAO;

public class Course {
	private CourseDAO dao;
	
	private int cId;
	private String title;
	private Dept dept;
	
	private Collection<Section> sections;

	public Course(CourseDAO dao, int cId, String title, Dept dept) {
		this.dao = dao;
		this.cId = cId;
		this.title = title;
		this.dept = dept;
	}

	public int getCId() {
		return cId;
	}

	public String getTitle() {
		return title;
	}

	public Dept getDept() {
		return dept;
	}

	public Collection<Section> getSections() {
		if (sections == null) {
			sections = dao.getSections(cId);
		}
		return sections;
	}

	public void invalidate() {
		sections = null;
	}
}
