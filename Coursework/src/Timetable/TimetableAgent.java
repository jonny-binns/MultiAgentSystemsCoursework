package Timetable;

import java.util.ArrayList;

import Ontology.TimetableOntology;
import Ontology.Elements.*;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

public class TimetableAgent extends Agent{
	//import codec and ontology
	//code used is from the music shop example in practical 6
	private Codec codec = new SLCodec();
	private Ontology ontology = TimetableOntology.getInstance();
	//create timetable
	private Timetable timetable = new Timetable();
	
	protected void setup() {
		//doWait(10000);
		Object[] args = getArguments();
		if(args != null && args.length > 0)
		{
			ArrayList<Tutorial> classes = new ArrayList<Tutorial>();
			Tutorial[] classesObj = (Tutorial[]) args;
			for(int i=0; i<classesObj.length; i++)
			{
				 classes.add(classesObj[i]);
			}
			timetable.setTimetable(classes);
			
			ArrayList<Tutorial> temp = (ArrayList<Tutorial>) timetable.getTimetable();
			for(int i=0; i<temp.size(); i++)
			{
				System.out.println("TimetableAgent.java");
				System.out.println(temp.get(i).getModuleName() + " " + temp.get(i).getTimeSlot().getTime() + " " + temp.get(i).getTimeSlot().getDay());
			}
			
			addBehaviour(new InitTimetable());	
		}
		else
		{
			System.out.println("No Arguements are Being passed");
		}
		
		System.out.println("Hello, Agent "+getAID().getName() + " is ready");
	}

	
	//Initialise the timetable for the week
	public class InitTimetable extends OneShotBehaviour {
		@Override
		public void action() {
		}
	}
}
