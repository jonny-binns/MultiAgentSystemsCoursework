/**
 * @author jonny
 * represents tutorial's time and day
 */

package Ontology.Elements;

import jade.content.Concept;
import jade.content.onto.annotations.Slot;

public class TimeSlot implements Concept{
	private int time;
	private String day;
	
	@Slot (mandatory = true)
	public int getTime() {
		return time;
	}
	
	public void setTime(int time) {
		this.time = time;
	}
	
	
	@Slot (mandatory = true)
	public String getDay() {
		return day;
	}
	
	public void setDay(String day) {
		this.day = day;
	}
}
