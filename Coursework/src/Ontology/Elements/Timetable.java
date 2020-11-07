/**
 * @author jonny
 * represents a timetable, just a list of tutorials
 */

package Ontology.Elements;

import java.util.List;
import jade.content.onto.annotations.AggregateSlot;

public class Timetable extends Tutorial {
	private List<Tutorial> timetable;

	@AggregateSlot(cardMin=1)
	public List<Tutorial> getTimetable() {
		return timetable;
	}
	
	public void setTimetable(List<Tutorial> timetable) {
		this.timetable = timetable;
	}
}
