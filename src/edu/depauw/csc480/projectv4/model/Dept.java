package edu.depauw.csc480.projectv4.model;

import java.util.Collection;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "DEPT")
public class Dept {
	@Id
	@Column(name = "DId")
	private int dId;

	@Basic(optional = false)
	@Column(name = "DName", length = 8)
	private String dName;

	@OneToMany(mappedBy = "major")
	private Collection<Student> majors;

	@OneToMany(mappedBy = "dept")
	private Collection<Course> courses;

	protected Dept() {
		// No-argument constructor for JPA
	}

	public Dept(int dId, String dName) {
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
		return majors;
	}

	public Collection<Course> getCourses() {
		return courses;
	}
}
