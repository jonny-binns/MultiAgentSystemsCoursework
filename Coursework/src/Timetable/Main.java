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
		//initialise timetable to be passed as an argument to timetable agent
		ArrayList<String> moduleNames = new ArrayList<String>();
		moduleNames.add("Multi-Agent Systems");
		moduleNames.add("Software Architecture");
		int noOfGroups = 2;
		
		ArrayList<TimetableTutorial> classes = InitClasses(moduleNames, noOfGroups);
		
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
			
			AgentController studentAgent1 = myContainer.createNewAgent("student1", StudentAgent.class.getCanonicalName(), null);
			studentAgent1.start();
			
			AgentController timetableAgent = myContainer.createNewAgent("timetable agent", TimetableAgent.class.getCanonicalName(), classesObj);
			timetableAgent.start();
		}
		catch(Exception e) {
			System.out.println("Exception starting agent: " + e.toString());
		}
	}
	
	public static ArrayList<TimetableTutorial> InitClasses(ArrayList<String> moduleNames, int noOfGroups) {
		//ArrayList for days of the week
		ArrayList<String> weekdays = new ArrayList<String>();
		weekdays.add("Monday");
		weekdays.add("Tuesday");
		weekdays.add("Wedensday");
		weekdays.add("Thursday");
		weekdays.add("Friday");
		
		//creates the list of classes from the given info
		ArrayList<TimetableTutorial> classes = new ArrayList<TimetableTutorial>();
		for(int i=0; i<moduleNames.size(); i++)
		{
			for(int j=1; j<=noOfGroups; j++)
			{
				//generate random weekday for Tutorial
				//code used to generate random numbers is from: 
				///https://stackoverflow.com/questions/363681/how-do-i-generate-random-integers-within-a-specific-range-in-java
				int randDayNumber = ThreadLocalRandom.current().nextInt(1, 5 + 1);
				String randDay = weekdays.get(randDayNumber - 1);
				
				//generate random time for tutorial
				int randTime = ThreadLocalRandom.current().nextInt(9, 17 + 1);
				
				//create class and add to list
				TimetableTutorial tutorial = new TimetableTutorial();
				tutorial.setModuleName(moduleNames.get(i));
				tutorial.setGroupNumber(j);
				tutorial.setDay(randDay);
				tutorial.setTime(randTime);
				tutorial.setAttendees(null);
				classes.add(tutorial);
			}
		}
		return classes;
	}

}

