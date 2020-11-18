package Timetable;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import Ontology.TimetableOntology;
import Ontology.Elements.*;
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
	//timeslots student wants
	ArrayList<TimeSlot> wants = new ArrayList<TimeSlot>();
	//timeslots student prefers not to have
	ArrayList<TimeSlot> preferNot = new ArrayList<TimeSlot>();
	//timeslots where student is unable to attend
	ArrayList<TimeSlot> unable = new ArrayList<TimeSlot>();
	//utility score
	int utility;
	
	

	protected void setup() {
		doWait(20000);
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
		
		addBehaviour(new InitTimetable());

		
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
	
	
	private int calculateUtilityFunction(ArrayList<TimeSlot> wants, ArrayList<TimeSlot> preferNot, ArrayList<TimeSlot> unable, StudentTimetable timetable){
		int utility = 0;
		
		//loop through timetable
			//if wants contains timeslot then +40
			//if prefers not contains timeslot then +1
			//if unable contains timeslot then +0
			//else calculate no of timeslots away from unable
		
		return utility;
		
		/*
		int utility = 0;
		
		//max score of 40 per class
		
		//loop through timetable
			//loop through wants
				//check if class has a timeslot the student wants, if yes then +40
			//loop through prefers not
				//check if class has a timeslot the student prefers not to have, if yes then +1
			//loop through unable
				//check if class has a timeslot the student cant attend, if yes then +0
				//else calculate no of timeslots away from when the student cant attend
		
		ArrayList<StudentTutorial> temp = (ArrayList<StudentTutorial>) timetable.getTimetable();
		for(int i=0; i<temp.size(); i++)
		{
			
			//rebuild timeslot for the tutorial
			TimeSlot tutorialTemp = new TimeSlot();
			tutorialTemp.setDay(temp.get(i).getDay());
			tutorialTemp.setTime(temp.get(i).getTime());
			
			for(int j=0; j<wants.size(); j++)
			{
				//check if a class has the timeslot the student wants, if yes then +40
				//rebuild timeslot
				TimeSlot wantTemp = new TimeSlot();
				wantTemp.setDay(wants.get(j).getDay());
				wantTemp.setTime(wants.get(j).getTime());
				
				if(wantTemp == tutorialTemp)
				{
					System.out.println("179");
				}
			}
			
			
		}
		return utility;
		*/
	}
	
	private class InitTimetable extends  CyclicBehaviour {
		/*
		public InitTimetable(Agent a, long period) {
			super(a, period);
		}
		*/
		private boolean finished;
		
		
		@Override
		public void action() {
			if(!finished)
			{
				//should only respond to propose messages
				//code used from pracitcal 06 sellerAgent
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
				ACLMessage msg = receive(mt);
				if(msg != null)
				{
					try {
						ContentElement ce = null;
						//System.out.println(msg.getContent());
					
						//let jade convert from string to java object
						ce = getContentManager().extractContent(msg);
						if (ce instanceof Attends)
						{
							Attends attends = (Attends) ce;
							StudentTimetable tempTimetable = attends.getTimetable();
							timetable.setTimetable(tempTimetable.getTimetable());
						}
						
					} 
					catch (CodecException | OntologyException e) {
						e.printStackTrace();
					}
					
					finished = true;
				}
			}

		}
		
	}
}
