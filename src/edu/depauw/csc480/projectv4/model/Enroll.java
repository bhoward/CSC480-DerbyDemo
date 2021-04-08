package edu.depauw.csc480.projectv4.model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "ENROLL")
public class Enroll {
	@Id
	@Column(name = "EId")
	private int eId;

	@ManyToOne(optional = false)
	@JoinColumn(name = "StudentId")
	private Student student;

	@ManyToOne(optional = false)
	@JoinColumn(name = "SectionId")
	private Section section;

	@Basic(optional = true)
	@Column(name = "Grade", length = 2)
	private String grade;

	protected Enroll() {
		// No-argument constructor for JPA
	}

	public Enroll(int eId, Student student, Section section, String grade) {
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
	}
}
