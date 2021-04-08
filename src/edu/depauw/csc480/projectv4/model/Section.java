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
@Table(name = "SECTION")
public class Section {
	@Id
	@Column(name = "SectId")
	private int sectId;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "CourseId")
	private Course course;
	
	@Basic(optional = false)
	@Column(name = "Prof", length = 8)
	private String prof;
	
	@Basic(optional = false)
	@Column(name = "YearOffered")
	private int yearOffered;
	
	@OneToMany(mappedBy = "section")
	private Collection<Enroll> enrollments;
	
	protected Section() {
		// No-argument constructor for JPA
	}

	public Section(int sectId, Course course, String prof, int yearOffered) {
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
		return enrollments;
	}
}
