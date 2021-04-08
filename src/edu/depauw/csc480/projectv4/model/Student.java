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
@Table(name = "STUDENT")
public class Student {
	@Id
	@Column(name = "SId")
	private int sId;
	
	@Basic(optional = false)
	@Column(name = "SName", length = 10)
	private String sName;
	
	@ManyToOne(optional = true)
	@JoinColumn(name = "MajorId")
	private Dept major;
	
	@Basic(optional = false)
	@Column(name = "GradYear")
	private int gradYear;
	
	@OneToMany(mappedBy = "student")
	private Collection<Enroll> enrollments;

	protected Student() {
		// No-argument constructor for JPA
	}

	public Student(int sId, String sName, Dept major, int gradYear) {
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
	}

	public int getGradYear() {
		return gradYear;
	}

	public void setGradYear(int gradYear) {
		this.gradYear = gradYear;
	}

	public Collection<Enroll> getEnrollments() {
		return enrollments;
	}
}
