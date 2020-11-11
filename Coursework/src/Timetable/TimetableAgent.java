package Timetable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import Ontology.TimetableOntology;
import Ontology.Elements.*;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

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
		
		//doWait(10000);
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
			
			addBehaviour(new NotifyStudents());
						
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
			}
			catch(FIPAException fe) {
				fe.printStackTrace();
			}
		}	
	}
	
	//notify students of their classes
	private class NotifyStudents extends OneShotBehaviour {
		@Override
		public void action() {
			/*
			ArrayList<TimetableTutorial> temp = (ArrayList<TimetableTutorial>) timetable.getTimetable();
			for(int i=0; i<temp.size(); i++)
			{
				System.out.println(temp.get(i).getModuleName() + "" + temp.get(i).getGroupNumber() + "" + temp.get(i).getAttendees());
			}
			*/
			
			//get list of students
			/*
			for(int i=0; i<studentList.length; i++)
			{
				System.out.println(studentList[i]);
			}
			*/
			
			//for each student
				//create student timetable
				//loop through timetable timetable and move classes where student attends to student timetable
				//message Student their timetable
				//prepare propose message
				//code used from practical 6, cautiousBuyerAgent
				//ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
			
			for(int i=0; i<studentList.length; i++)
			{
				StudentTimetable tempStudentTimetable = null;
				
				//loop through timetable
					//if student is in attendees
						//add that class to tempStudentTimetable
				//create message to student
				//send tempStudentTimetable
				
				ArrayList<TimetableTutorial> tempTimetable = (ArrayList<TimetableTutorial>) timetable.getTimetable();
				try {
					for(int j=0; j<tempTimetable.size(); j++)
					{
						//System.out.println(tempTimetable.get(j).getAttendees());
						if(tempTimetable.get(j).getAttendees().contains(studentList[i]))
						{
							System.out.println("Student " + studentList[i] + " attends " + tempTimetable.get(j).getModuleName() + tempTimetable.get(j).getGroupNumber());
						}
					}
				}
				catch (NullPointerException n)
				{
					n.printStackTrace();
				}
			}
		}
	}
	
}
