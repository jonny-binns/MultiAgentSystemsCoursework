/**
 * @author jonny
 * represents an individual tutorial for a student, has a time slot (day/time) a module name and a group number
 * Doesnt contain the list of attendees as students do not need that info
 */

package Ontology.Elements;

import jade.content.Concept;
import jade.content.onto.annotations.Slot;

public class StudentTutorial extends TimeSlot {
	private String moduleName;
	private int groupNumber;
	
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
}
