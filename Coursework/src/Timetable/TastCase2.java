package Timetable;

import java.util.ArrayList;

import Ontology.Elements.TimeSlot;
import Ontology.Elements.TimetableTutorial;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class TastCase2 {
	/*
	 * Neither Agent wants to swap
	 */

	public static void main(String[] args) {
		//get timetable
		ArrayList<TimetableTutorial> classes = InitClasses();		
		TimetableTutorial[] classesObj = new TimetableTutorial[classes.size()];
		for(int i=0; i<classesObj.length; i++)
		{
			classesObj[i] = classes.get(i);
		}
		
		
		//get timeslot preferences for studentAgent
		ArrayList<TimeSlot> preferences = new ArrayList<TimeSlot>(); 
		TimeSlot wants = new TimeSlot();
		wants.setDay("Monday");
		wants.setTime(9);
		//TimeSlot preferNot = new TimeSlot();
		TimeSlot unable = new TimeSlot();
		unable.setDay("Friday");
		unable.setTime(17);
		preferences.add(wants);
		//preferences.add(preferNot);
		preferences.add(unable);
		TimeSlot[] preferencesObj= new TimeSlot[preferences.size()];
		for(int i=0; i<preferencesObj.length; i++)
		{
			preferencesObj[i] = preferences.get(i);
		}
		
		
		//get timeslot preferences for studentAgent1
		ArrayList<TimeSlot> preferences1 = new ArrayList<TimeSlot>(); 
		TimeSlot wants1 = new TimeSlot();
		wants1.setDay("Friday");
		wants1.setTime(17);
		//TimeSlot preferNot1 = new TimeSlot();
		TimeSlot unable1 = new TimeSlot();
		unable1.setDay("Monday");
		unable1.setTime(9);
		preferences1.add(wants1);
		//preferences1.add(preferNot1);
		preferences1.add(unable1);
		TimeSlot[] preferencesObj1 = new TimeSlot[preferences1.size()];
		for(int i=0; i<preferencesObj1.length; i++)
		{
			preferencesObj1[i] = preferences1.get(i);
		}
		
		
		//setup the jade environment
		Profile myProfile = new ProfileImpl();
		Runtime myRuntime = Runtime.instance();
		ContainerController myContainer = myRuntime.createMainContainer(myProfile);
		try {
			AgentController rma = myContainer.createNewAgent("rma", "jade.tools.rma.rma", null);
			rma.start();
			
			AgentController studentAgent = myContainer.createNewAgent("student", StudentAgent.class.getCanonicalName(), preferencesObj);
			studentAgent.start();
			
			AgentController studentAgent1 = myContainer.createNewAgent("student1", StudentAgent.class.getCanonicalName(), preferencesObj1);
			studentAgent1.start();
			
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
		
		//class 2
		TimetableTutorial tutorial2 = new TimetableTutorial();
		tutorial2.setModuleName("Multi-Agent Systems");
		tutorial2.setGroupNumber(2);
		tutorial2.setAttendees(null);
		TimeSlot timeSlot2 = new TimeSlot();
		timeSlot2.setDay("Friday");
		timeSlot2.setTime(17);
		tutorial2.setTimeSlot(timeSlot2);
		classes.add(tutorial2);
		
		return classes;
	}
}