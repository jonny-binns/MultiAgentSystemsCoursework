package Ontology;

import jade.content.onto.BeanOntology;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.Ontology;

public class TimetableOntology extends BeanOntology {
	
	private static Ontology theInstance = new TimetableOntology("my_ontology");
	
	public static Ontology getInstance() {
		return theInstance;
	}
	
	//singleton pattern
	private TimetableOntology(String name) {
		super(name);
		try {
			add("Ontology.Elements");
		}
		catch(BeanOntologyException e){
			e.printStackTrace();
		}
	}
}
