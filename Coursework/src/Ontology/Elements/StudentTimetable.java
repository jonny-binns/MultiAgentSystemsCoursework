/**
 * @author jonny
 * represents a timetable for a student agent
 * different to timeable.java as there is no list of attendees 
 */

package Ontology.Elements;

import java.util.List;

import jade.content.Concept;
import jade.content.onto.annotations.AggregateSlot;

public class StudentTimetable implements Concept {
	private List<StudentTutorial> timetable;

	@AggregateSlot(cardMin=1)
	public List<StudentTutorial> getTimetable() {
		return timetable;
	}
	
	public void setTimetable(List<StudentTutorial> timetable) {
		this.timetable = timetable;
	}
}
