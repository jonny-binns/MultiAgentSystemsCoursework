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
		
		TimeSlot wantsTS = new TimeSlot();
		wantsTS.setDay("Friday");
		wantsTS.setTime(17);
		unable.add(wantsTS);
		
		addBehaviour(new InitBehaviour());
	
		
		
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
	
		
	private class InitBehaviour extends CyclicBehaviour {

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
							ArrayList<StudentTutorial> tempTutorials = (ArrayList<StudentTutorial>) tempTimetable.getTimetable();  
							timetable.setTimetable(tempTutorials);
							/*
							ArrayList<StudentTutorial> temp = (ArrayList<StudentTutorial>) timetable.getTimetable();
							for(int i=0; i<temp.size(); i++)
							{
								System.out.println(temp.get(i).getModuleName());
							}
							*/
						}
						
					} 
					catch (CodecException | OntologyException e) {
						e.printStackTrace();
					}
					
					//calculate initial utility score
					utility = CalculateUtilityFunction(wants, preferNot, unable);
					System.out.println(utility);
					
					finished = true;
				}
			}			
		}
	}
	

	private int CalculateUtilityFunction(ArrayList<TimeSlot> wants, ArrayList<TimeSlot> preferNot, ArrayList<TimeSlot> unable){
		//loop through timetable
			//if wants contains timeslot then +40
			//if prefers not contains timeslot then +1
			//if unable contains timeslot then +0
			//else calculate no of timeslots away from unable
		
		//max score of 40
		int utility = 0;
		
		ArrayList<StudentTutorial> tempTimetable = (ArrayList<StudentTutorial>) timetable.getTimetable();
		/*
		for(int i=0; i<tempTimetable.size(); i++)
		{
			System.out.println("line 157 " + tempTimetable.get(i).getModuleName() + tempTimetable.get(i).getTimeSlot());
		}
		*/
		for (int i=0; i<tempTimetable.size(); i++)
		{	
			TimeSlot tempTS = tempTimetable.get(i).getTimeSlot();
			
			//wants
			for(int j=0; j<wants.size(); j++)
			{
				if(wants.get(j).getDay().equals(tempTS.getDay()) && wants.get(j).getTime() == tempTS.getTime())
				{
					System.out.println("wants contains " + tempTimetable.get(i).getModuleName());
					utility += 40;
				}
			}
			
			//prefer not
			for(int j=0; j<preferNot.size(); j++)
			{
				if(preferNot.get(j).getDay().equals(tempTS.getDay()) && preferNot.get(j).getTime() == tempTS.getTime())
				{
					System.out.println("preferNot contains " + tempTimetable.get(i).getModuleName());
					utility +=1;
				}
			}
			
			//unable
			for(int j=0; j<unable.size(); j++)
			{
				if(unable.get(j).getDay().equals(tempTS.getDay()) && unable.get(j).getTime() == tempTS.getTime())
				{
					utility +=0;
				}
				else
				{
					int timeDiff = 0;
					int dayDiff = 0;
					int tempTSDay = 0;
					int unableTempDay = 0;
					//get unable time
					
					TimeSlot unableTemp = unable.get(j);
					
					//calculate difference in time slots between unable time and time slot
					if(tempTS.getTime() >= unableTemp.getTime())
					{
						timeDiff = (tempTS.getTime() - unableTemp.getTime());
						System.out.println("205 time Diff = " + timeDiff);
					}
					else
					{
						timeDiff = (unableTemp.getTime() - tempTS.getTime());
						System.out.println("210 time Diff = " + timeDiff);
					}
					
					//calculate day numbers
					String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
					for(int k=0; k<days.length; k++)
					{
						if(days[k].equals(tempTS.getDay()))
						{
							tempTSDay = (k+1);

							System.out.println("TempTSDay = " + k + "+" + 1 + "=" + tempTSDay);
						}
					}
					
					for(int k=0; k<days.length; k++)
					{
						if(days[k].equals(unableTemp.getDay()))
						{
							unableTempDay = (k+1);
							System.out.println("UnableTempDay = " + k + "+" + 1 + "=" + unableTempDay);
						}
					}
					
					if(tempTSDay >= unableTempDay)
					{
						dayDiff = ((tempTSDay - unableTempDay)*8);
						System.out.println("231 day Diff = " + dayDiff);
					}
					else
					{
						dayDiff = ((unableTempDay - tempTSDay)*8);
						System.out.println("236 day Diff = " + dayDiff);
					}
					
					if(timeDiff == 0)
					{
						utility += dayDiff;
					}
					else
					{
						utility += timeDiff + dayDiff;
					}
				}
			}

		}
		
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
}
