package Ontology.Elements;

/**
 * @author jonny
 * represents the swapping of a tutorial between two agents
 */

import jade.content.AgentAction;
import jade.core.AID;

public class Swap implements AgentAction{
	private AID swapStudent; //student who is receiving the swap
	private StudentTutorial tutorial;
	
	public AID getSwapStudent() {
		return swapStudent;
	}
	
	public void setSwapStudent(AID swapStudent) {
		this.swapStudent = swapStudent;
	}
	
	public StudentTutorial getTutorial() {
		return tutorial;
	}
	
	public void setTutorial(StudentTutorial tutorial) {
		this.tutorial = tutorial;
	}
}
