package edu.depauw.csc480.projectv4.model;

import java.util.Collection;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "COURSE")
public class Course {
	@Id
	@Column(name = "CId")
	private int cId;
	
	@Basic(optional = false)
	@Column(name = "Title", length = 20)
	private String title;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "DeptId")
	private Dept dept;

	@OneToMany(mappedBy = "course")
	private Collection<Section> sections;
	
	protected Course() {
		// No-argument constructor for JPA
	}
	
	public Course(int cId, String title, Dept dept) {
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
		return sections;
	}
}
