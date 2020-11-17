/**
 * @author jonny
 * timetable concept for the timetable agent, just a list of the TimetableTutorial concept
 */

package Ontology.Elements;
import java.util.List;

import jade.content.Concept;
import jade.content.onto.annotations.AggregateSlot;

public class Timetable implements Concept {
	private List<TimetableTutorial> timetable;

	@AggregateSlot(cardMin=1)
	public List<TimetableTutorial> getTimetable() {
		return timetable;
	}
	
	public void setTimetable(List<TimetableTutorial> timetable) {
		this.timetable = timetable;
	}
}
