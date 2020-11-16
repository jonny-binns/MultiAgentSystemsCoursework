package Timetable;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import Ontology.TimetableOntology;
import Ontology.Elements.StudentTimetable;
import Ontology.Elements.StudentTutorial;
import Ontology.Elements.TimetableTutorial;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class StudentAgent extends Agent{
	//import codec and ontology
	//code used is from the music shop example in practical 6
	private Codec codec = new SLCodec();
	private Ontology ontology = TimetableOntology.getInstance();
	//students timetable
	StudentTimetable timetable = new StudentTimetable();
	//timeslot student wants
	
	

	protected void setup() {
		//doWait(20000);
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		
		//register agent with DF agent
		//code used from 1.2.2 in the practical textbook
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Student-Agent");
		sd.setName(getLocalName() + "-Student-Agent");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch(FIPAException e) {
			e.printStackTrace();
		}
		
		//addBehaviour(new InitTimetable(this, 5000));
		
		//REMOVE THIS LATER
		ArrayList<String> moduleNames = new ArrayList<String>();
		moduleNames.add("Multi-Agent Systems");
		//moduleNames.add("Software Architecture");
		int noOfGroups = 1;
		
		timetable.setTimetable(InitClasses(moduleNames, noOfGroups));
		
		/*
		ArrayList<StudentTutorial> temp = (ArrayList<StudentTutorial>) timetable.getTimetable();
		for(int i=0; i<temp.size(); i++)
		{
			System.out.println(temp.get(i).getModuleName() + " " + temp.get(i).getGroupNumber() + " " + temp.get(i).getTime() + " " + temp.get(i).getDay());
		}
		*/
		
		System.out.println("Student Agent "+getAID().getName() + " is ready");
	}
	
	
	//shutdown method
	protected void takeDown() {
		//deregister from df agent
		//code used from 1.2.2. in practical textbook
		try {
			DFService.deregister(this);
		}
		catch(FIPAException e) {
			e.printStackTrace();
		}
		
		//printout a dismissal message
		System.out.println("Student Agent " + getAID().getName() + " is terminating");
	}
	
	//REMOVE THIS LATER
	public static ArrayList<StudentTutorial> InitClasses(ArrayList<String> moduleNames, int noOfGroups) {
		//ArrayList for days of the week
		ArrayList<String> weekdays = new ArrayList<String>();
		weekdays.add("Monday");
		weekdays.add("Tuesday");
		weekdays.add("Wedensday");
		weekdays.add("Thursday");
		weekdays.add("Friday");
		
		//creates the list of classes from the given info
		ArrayList<StudentTutorial> classes = new ArrayList<StudentTutorial>();
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
				StudentTutorial tutorial = new StudentTutorial();
				tutorial.setModuleName(moduleNames.get(i));
				tutorial.setGroupNumber(noOfGroups);
				tutorial.setDay(randDay);
				tutorial.setTime(randTime);
				classes.add(tutorial);
			}
		}
		return classes;
	}
	
	
	private class InitTimetable extends TickerBehaviour {
		
		public InitTimetable(Agent a, long period) {
			super(a, period);
		}
		
		private boolean finished = false;
		
		@Override
		public void onTick() {
			//when message received from timetable agent copy the timetable in the message over to the private student timetable
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF);
			ACLMessage msg = receive(mt);
			if(msg != null)
			{
				/**
				try {
					StudentTimetable tempStudentTimetableObj = (StudentTimetable) msg.getContentObject();
					ArrayList<StudentTutorial> tempStudentTimetable = (ArrayList<StudentTutorial>) tempStudentTimetableObj.getTimetable();
					System.out.println(getAID().getName());
					for(int i=0; i<tempStudentTimetable.size(); i++)
					{
						System.out.println(tempStudentTimetable.get(i).getModuleName() + tempStudentTimetable.get(i).getGroupNumber());
					}
				} 
				catch (UnreadableException e) {
					e.printStackTrace();
				}
				finished = true;
				*/
				
				try {
					ContentElement ce = null;
					System.out.println(msg.getContent());
					
					ce = getContentManager().extractContent(msg);	
					
					System.out.println(ce);
					if(ce instanceof StudentTutorial)
					{
						StudentTimetable tempTimetable = (StudentTimetable) ce;
						System.out.println(tempTimetable);
					}
				}
				catch (CodecException ce) {
					ce.printStackTrace();
				}
				catch (OntologyException oe) {
					oe.printStackTrace();
				}
			}
			finished = true;
		}
	}
}
