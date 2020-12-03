package Timetable;

import java.util.ArrayList;

import Ontology.Elements.TimeSlot;
import Ontology.Elements.TimetableTutorial;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class TestCase3 {

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
				wants.setDay("Tuesday");
				wants.setTime(17);
				//TimeSlot preferNot = new TimeSlot();
				TimeSlot unable = new TimeSlot();
				unable.setDay("Monday");
				unable.setTime(10);
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
				unable1.setDay("Thursday");
				unable1.setTime(13);
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
				timeSlot1.setDay("Tuesday");
				timeSlot1.setTime(15);
				tutorial1.setTimeSlot(timeSlot1);
				classes.add(tutorial1);
				
				//class 2
				TimetableTutorial tutorial2 = new TimetableTutorial();
				tutorial2.setModuleName("Multi-Agent Systems");
				tutorial2.setGroupNumber(2);
				tutorial2.setAttendees(null);
				TimeSlot timeSlot2 = new TimeSlot();
				timeSlot2.setDay("Wednesday");
				timeSlot2.setTime(12);
				tutorial2.setTimeSlot(timeSlot2);
				classes.add(tutorial2);
				
				//class 3
				TimetableTutorial tutorial3 = new TimetableTutorial();
				tutorial3.setModuleName("Software Archiecture");
				tutorial3.setGroupNumber(1);
				tutorial3.setAttendees(null);
				TimeSlot timeSlot3 = new TimeSlot();
				timeSlot3.setDay("Thursday");
				timeSlot3.setTime(10);
				tutorial3.setTimeSlot(timeSlot3);
				classes.add(tutorial3);
				
				//class 4
				TimetableTutorial tutorial4 = new TimetableTutorial();
				tutorial4.setModuleName("Software Archiecture");
				tutorial4.setGroupNumber(2);
				tutorial4.setAttendees(null);
				TimeSlot timeSlot4 = new TimeSlot();
				timeSlot4.setDay("Monday");
				timeSlot4.setTime(14);
				tutorial4.setTimeSlot(timeSlot4);
				classes.add(tutorial4);
				
				return classes;
			}
		
}
