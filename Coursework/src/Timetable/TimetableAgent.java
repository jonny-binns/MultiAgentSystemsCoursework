package Timetable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import Ontology.TimetableOntology;
import Ontology.Elements.*;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.abs.AbsContentElement;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class TimetableAgent extends Agent{
	//import codec and ontology
	//code used is from the music shop example in practical 6
	private Codec codec = new SLCodec();
	private Ontology ontology = TimetableOntology.getInstance();
	//create timetable
	private Timetable timetable = new Timetable();
	//create student list
	AID[] studentList;
	
	protected void setup() {
		doWait(20000);
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		
		//register agent with DF agent
		//code used from 1.2.2 in the practical textbook
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Timetable-Agent");
		sd.setName(getLocalName() + "-Timetable-Agent");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch(FIPAException e) {
			e.printStackTrace();
		}
		
		Object[] args = getArguments();
		if(args != null && args.length > 0)
		{
			ArrayList<TimetableTutorial> classes = new ArrayList<TimetableTutorial>();
			TimetableTutorial[] classesObj = (TimetableTutorial[]) args;
			for(int i=0; i<classesObj.length; i++)
			{
				 classes.add(classesObj[i]);
			}
			timetable.setTimetable(classes);
			
			
			addBehaviour(new InitStudents());	
			
			addBehaviour(new NotifyStudents(this, 10000));
						
		}
		else
		{
			System.out.println("No Arguements are Being passed");
		}
		
		System.out.println("Timetable Agent " + getAID().getName() + " is ready");
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
		
		//printout takedown message
		System.out.println("Timetable Agent " + getAID().getName() + " is terminating");
	}
	
	
	//assign students to their classes
	private class InitStudents extends OneShotBehaviour {
		
		@Override
		public void action() {
			//get list of students
			//code used from bookBuyerAgent in practical 2
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("Student-Agent");
			template.addServices(sd);
			
			try {
				DFAgentDescription[] result = DFService.search(myAgent, template);
				studentList = new AID[result.length];
				for(int i=0; i<result.length; i++)
				{
					studentList[i] = result[i].getName();
				}
			
				ArrayList<TimetableTutorial> tempTimetable = (ArrayList<TimetableTutorial>) timetable.getTimetable();
				
				//get number of tutorial groups
				int noOfTutorialGroups = 0;
				for(int i=0; i<tempTimetable.size(); i++)
				{
					String tutorialName = tempTimetable.get(0).getModuleName();
					if(tempTimetable.get(i).getModuleName() == tutorialName)
					{
						noOfTutorialGroups++;
					}
				}
			
				//decide which tutorial group the student will be in
				int groupNo = 1;
				
				for(int i=0; i<studentList.length; i++)
				{					
					//loop through timetable and add student to list of attendees
					for(int j=0; j<tempTimetable.size(); j++)
					{
						if(tempTimetable.get(j).getGroupNumber() == groupNo)
						{
							if(tempTimetable.get(j).getAttendees() == null)
							{
								ArrayList<AID> tempAttendees = new ArrayList<AID>();
								tempAttendees.add(studentList[i]);
								tempTimetable.get(j).setAttendees(tempAttendees);
							}
							else
							{
								ArrayList<AID> tempAttendees = (ArrayList<AID>) tempTimetable.get(j).getAttendees();
								tempAttendees.add(studentList[i]);
								tempTimetable.get(j).setAttendees(tempAttendees);
							}
						}
					}
					
					if(groupNo == noOfTutorialGroups)
					{
						groupNo = 1;
					}
					else
					{
						groupNo++;
					}
				}
				//updates timetable
				timetable.setTimetable(tempTimetable);
				/*
				ArrayList<TimetableTutorial>tmp = (ArrayList<TimetableTutorial>) timetable.getTimetable();
				for(int i=0; i<tmp.size(); i++)
				{
					System.out.println(tmp.get(i).getModuleName() + " " + tmp.get(i).getGroupNumber() + tmp.get(i).getAttendees());
				}
				*/
			}
			catch(FIPAException fe) {
				fe.printStackTrace();
			}
		}	
	}

	//notify students of their classes
	private class NotifyStudents extends TickerBehaviour {
		
		public NotifyStudents(Agent a, long period) {
			super(a, period);
		}
		
		private boolean finished;
		
		@Override
		public void onTick() {
			if(!finished)
			{
				//for each student
				//create student timetable
				//loop through timetable timetable and move classes where student attends to student timetable
				//create attends predicate
				
				//message student their timetable
				//prepare propose message
				//code from practical 6
				
				//on receipt of an accept proposal message
				//move to next student
				
			
			ArrayList<TimetableTutorial> tempTimetable = (ArrayList<TimetableTutorial>) timetable.getTimetable();
			for(int i=0; i<studentList.length; i++)
			{
				ArrayList<StudentTutorial> tempStudentTimetable = new ArrayList<StudentTutorial>();
				for(int j=0; j<tempTimetable.size(); j++)
				{
					if(tempTimetable.get(j).getAttendees().contains(studentList[i]))
					{
						//add to tempStudentTimetable
						StudentTutorial tempTutorial = new StudentTutorial();
						tempTutorial.setModuleName(tempTimetable.get(j).getModuleName());
						tempTutorial.setGroupNumber(tempTimetable.get(j).getGroupNumber());
						tempTutorial.setTimeSlot(tempTimetable.get(j).getTimeSlot());
						tempStudentTimetable.add(tempTutorial);
					}
				}
				
				//convert to studentTimetable object
				StudentTimetable tempStudentTimetableObj = new StudentTimetable();
				tempStudentTimetableObj.setTimetable(tempStudentTimetable);
				
				//convert to Attends for messaging
				Attends attendsTemp = new Attends();
				attendsTemp.setStudent(studentList[i]);
				attendsTemp.setTimetable(tempStudentTimetableObj);
				
				//send message
				//prepare the propose message
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.addReceiver(studentList[i]);
				msg.setLanguage(codec.getName());
				msg.setOntology(ontology.getName());
				msg.setConversationId("Student-Timetable");
				try {
					 // Let JADE convert from Java objects to string
					 getContentManager().fillContent(msg, attendsTemp);
					 send(msg);
				}
				catch (CodecException ce) {
					 ce.printStackTrace();
				}
				catch (OntologyException oe) {
					 oe.printStackTrace();
				} 
				
				/*
				//waits for accept proposal message
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
				ACLMessage reply = receive(mt);
				if(reply != null)
				{
					System.out.println("timetable received accept proposal");
					i++;
				}
				*/
			}
			finished = true;

			}
		}
	}
	
}
