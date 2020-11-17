package Timetable;

import java.util.ArrayList;
import Ontology.Elements.*;
import jade.core.AID;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.core.Runtime;

import java.util.concurrent.ThreadLocalRandom;

public class Main {

	public static void main(String[] args) {
		ArrayList<TimetableTutorial> classes = InitClasses();		
		
		TimetableTutorial[] classesObj = new TimetableTutorial[classes.size()];
		
		for(int i=0; i<classesObj.length; i++)
		{
			classesObj[i] = classes.get(i);
		}
		
		
		//setup the jade environment
		Profile myProfile = new ProfileImpl();
		Runtime myRuntime = Runtime.instance();
		ContainerController myContainer = myRuntime.createMainContainer(myProfile);
		try {
			AgentController rma = myContainer.createNewAgent("rma", "jade.tools.rma.rma", null);
			rma.start();
			
			AgentController studentAgent = myContainer.createNewAgent("student", StudentAgent.class.getCanonicalName(), null);
			studentAgent.start();
			
			//AgentController studentAgent1 = myContainer.createNewAgent("student1", StudentAgent.class.getCanonicalName(), null);
			//studentAgent1.start();
			
			AgentController timetableAgent = myContainer.createNewAgent("timetable agent", TimetableAgent.class.getCanonicalName(), classesObj);
			timetableAgent.start();
			
		}
		catch(Exception e) {
			System.out.println("Exception starting agent: " + e.toString());
		}
	}
	
	public static ArrayList<TimetableTutorial> InitClasses() {
		//creates the list of tutorials
		ArrayList<TimetableTutorial> classes = new ArrayList<TimetableTutorial>();
		
		//class 1
		TimetableTutorial tutorial1 = new TimetableTutorial();
		tutorial1.setModuleName("Multi-Agent Systems");
		tutorial1.setGroupNumber(1);
		tutorial1.setAttendees(null);
		TimeSlot timeSlot1 = new TimeSlot();
		timeSlot1.setDay("Monday");
		timeSlot1.setTime(9);
		tutorial1.setTimeSlot(timeSlot1);
		classes.add(tutorial1);
		
		/*
		//class 2
		TimetableTutorial tutorial2 = new TimetableTutorial();
		tutorial2.setModuleName("Multi-Agent Systems");
		tutorial2.setGroupNumber(2);
		tutorial2.setAttendees(null);
		TimeSlot timeSlot2 = new TimeSlot();
		timeSlot2.setDay("Monday");
		timeSlot2.setTime(11);
		tutorial2.setTimeSlot(timeSlot2);
		classes.add(tutorial2);
		*/
		
		return classes;
	}

}

