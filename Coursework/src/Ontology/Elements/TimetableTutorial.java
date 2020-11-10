/**
 * @author jonny
 * Tutorial concept for the timetable agent, adds a list of attendees to the standard concept
 */

package Ontology.Elements;

import java.util.List;
import jade.content.onto.annotations.AggregateSlot;
import jade.core.AID;

public class TimetableTutorial extends StudentTutorial {
	private List<AID> attendees;
	
	@AggregateSlot(cardMin=1)
	public List<AID> getAttendees() {
		return attendees;
	}
	
	public void setAttendees(List<AID> attendees) {
		this.attendees = attendees;
	}
	
}
