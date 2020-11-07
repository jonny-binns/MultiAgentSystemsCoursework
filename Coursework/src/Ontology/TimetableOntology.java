package Ontology;

import jade.content.onto.BeanOntology;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.Ontology;

public class TimetableOntology extends BeanOntology {
	
	private static Ontology Instance = new TimetableOntology("my_ontology");
	
	public static Ontology getInstance() {
		return Instance;
	}
	
	//singleton pattern
	private TimetableOntology(String name) {
		super(name);
		try {
			add("Timetable.Ontology.Elements");
		}
		catch(BeanOntologyException e){
			e.printStackTrace();
		}
	}
}
