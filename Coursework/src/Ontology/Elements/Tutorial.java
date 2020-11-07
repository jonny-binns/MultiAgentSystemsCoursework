/**
 * @author jonny
 * represents an individual tutorial, has a time slot (day/time) a module name and a group number
 */

package Ontology.Elements;

import jade.content.Concept;
import jade.content.onto.annotations.Slot;

public class Tutorial extends Timeslot {
	private String moduleName;
	private int groupNumber;
	private Timeslot timeslot;
	
	@Slot (mandatory = true)
	public String getModuleName() {
		return moduleName;
	}
	
	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}
	
	@Slot (mandatory = true)
	public int getGroupNumber() {
		return groupNumber;
	}
	
	public void setGroupNumber(int groupNumber) {
		this.groupNumber = groupNumber;
	}
	
	@Slot (mandatory = true)
	public Timeslot getTimeSlot() {
		return timeslot;
	}
	
	public void setTimeSlot(Timeslot timeslot) {
		this.timeslot = timeslot;
	}
}
