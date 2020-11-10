/**
 * @author jonny
 * represents an agent attending a tutorial
 */

package Ontology.Elements;

import jade.content.Predicate;
import jade.core.AID;

public class Attends implements Predicate {
	private AID student;
	private StudentTutorial tutorial;
	
	public AID getStudent() {
		return student;
	}
	
	public void setStudent(AID student) {
		this.student = student;
	}
	
	public StudentTutorial getTutorial() {
		return tutorial;
	}
	
	public void setTutorial(StudentTutorial tutorial) {
		this.tutorial = tutorial;
	}
}
