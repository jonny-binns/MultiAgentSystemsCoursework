/**
 * @author jonny
 * represents the timetable an agent attends
 */

package Ontology.Elements;

import jade.content.Predicate;
import jade.core.AID;

public class Attends implements Predicate {
	private AID student;
	private StudentTimetable timetable;
	
	public AID getStudent() {
		return student;
	}
	
	public void setStudent(AID student) {
		this.student = student;
	}
	
	public StudentTimetable getTimetable() {
		return timetable;
	}
	
	public void setTimetable(StudentTimetable timetable) {
		this.timetable = timetable;
	}
}
