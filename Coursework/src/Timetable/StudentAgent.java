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
import jade.core.AID;
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
	//list of students
	AID[] studentList;
	//list of timetable agents
	AID[] timetableAgentList;
	
	

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
		
		//get preferences from main
		Object[] args = getArguments();
		if(args != null && args.length > 0)
		{
			TimeSlot[] preferences = (TimeSlot[]) args;
			wants.add(preferences[0]);
			//preferNot.add(preferences[1]);
			unable.add(preferences[1]);
		}
		else
		{
			System.out.println("No Arguements are Being passed");
		}

		addBehaviour(new InitBehaviour());
		doWait(40000);
		addBehaviour(new SwapBehaviour(this, 10000));
		
		
		System.out.println("Student Agent "+ getAID().getName() + " is ready");
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
					utility = CalculateUtilityFunction(wants, preferNot, unable, (ArrayList<StudentTutorial>) timetable.getTimetable());
					//System.out.println(utility);
					
					finished = true;
				}
			}			
		}
	}
	
	private class SwapBehaviour extends TickerBehaviour {
		MessageTemplate incomingMT;
		
		public SwapBehaviour(Agent a, long period) {
			super(a, period);
		}

		@Override
		protected void onTick() {
			//get list of other students
			//code used from bookBuyerAgent from practical 02
			DFAgentDescription studentTemplate = new DFAgentDescription();
			ServiceDescription studentSD = new ServiceDescription();
			studentSD.setType("Student-Agent");
			studentTemplate.addServices(studentSD);
			try {
				DFAgentDescription[] result = DFService.search(myAgent, studentTemplate);
				studentList = new AID[(result.length-1)];
				int j=0;
				String agentName = getAID().getName();
				for(int i=0; i<result.length; i++)
				{
					if(result[i].getName().equals(agentName) == false)
					{
						studentList[j] = result[i].getName();
						j++;
					}
				}
			}
			catch (FIPAException fe)
			{
				fe.printStackTrace();
			}
			
			//get list of timetable agents
			//code used from bookBuyerAgent from practical 2
			DFAgentDescription timetableTemplate = new DFAgentDescription();
			ServiceDescription timetableSD = new ServiceDescription();
			timetableSD.setType("Timetable-Agent");
			timetableTemplate.addServices(timetableSD);
			try {
				DFAgentDescription[] result = DFService.search(myAgent, timetableTemplate);
				timetableAgentList = new AID[(result.length)];
				String agentName = getAID().getName();
				for(int i=0; i<result.length; i++)
				{
					timetableAgentList[i] = result[i].getName();
				}
			}
			catch (FIPAException fe)
			{
				fe.printStackTrace();
			}
			

			int step = 0;
			//check received messages in order to decide case
			
			ACLMessage incoming = myAgent.receive(incomingMT);
			if(incoming !=null)
			{
				//incoming query_ref message
				if(incoming.getPerformative() == ACLMessage.QUERY_REF)
				{
					step = 1;
				}
				//inform
				if(incoming.getPerformative() == ACLMessage.INFORM)
				{
					step = 2;
				}
				//propose
				if(incoming.getPerformative() == ACLMessage.PROPOSE)
				{
					step = 3;
				}
				//accept proposal
				if(incoming.getPerformative() == ACLMessage.ACCEPT_PROPOSAL)
				{
					step = 4;
				}
				//reject
				if(incoming.getPerformative() == ACLMessage.REJECT_PROPOSAL)
				{
					step = 5;
				}
			}
			
			switch (step) {
			case 0: 
				//agent decides to send message to other agents asking for swap
				//loop through timetable
					//get class timeslot
					//loop through unable
						//if class timeslot = unable timeslot
							//get class 
							//break
					//loop through prefer not
						//if class timeslot == prefernot timeslot
							//get class
							//break
				
				StudentTutorial unwantedClass = null;
				ArrayList<StudentTutorial> timetableTemp = (ArrayList<StudentTutorial>) timetable.getTimetable();
					for(int i=0; i<timetableTemp.size(); i++)
					{
						for(int j=0; j<unable.size(); j++)
						{
							if(timetableTemp.get(i).getTimeSlot().getDay().equals(unable.get(j).getDay()) && timetableTemp.get(i).getTimeSlot().getTime() == unable.get(j).getTime())
							{
								unwantedClass = timetableTemp.get(i);
								for(int k=0; k<studentList.length; k++)
								{
									//convert to Attends for messaging
									AttendsTutorial attendsTemp = new AttendsTutorial();
									attendsTemp.setStudent(getAID());
									attendsTemp.setTutorial(unwantedClass);
									
									//send query message for class
									//code used from practical 06
									ACLMessage msg = new ACLMessage(ACLMessage.QUERY_REF);
									msg.addReceiver(studentList[k]);
									msg.setLanguage(codec.getName());
									msg.setOntology(ontology.getName());
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
								}
							}
						}
						 //change for prefer not add if else to check for null list
						/*
						for(int j=0; j<unable.size(); i++)
						{
							if(unable.get(j).getDay().equals(timetableTemp.get(i).getTimeSlot().getDay()) && unable.get(j).getTime() == timetableTemp.get(i).getTimeSlot().getTime());
							{
								unwantedClass = timetableTemp.get(i);
							}
						}
						*/
					}
				break;
				

			case 1:
				//case 1 receives query message
				//receives query message
				//replies with timeslot of that class
				//receiving message behaviour used from SellerAgent from practical 6
				if(incoming != null)
				{
					try {
						ContentElement ce = null;
						ce = getContentManager().extractContent(incoming);
						if(ce instanceof AttendsTutorial) {
							AttendsTutorial inAttendsTutorial = (AttendsTutorial) ce;
							StudentTutorial inTutorial = inAttendsTutorial.getTutorial();
							AID inStudent = inAttendsTutorial.getStudent();
							
							//get tutorial this agent attends
							ArrayList<StudentTutorial> timetableTemp2 = (ArrayList<StudentTutorial>) timetable.getTimetable();
							StudentTutorial outTutorial = null;
							for(int i=0; i<timetableTemp2.size(); i++) {
								if(timetableTemp2.get(i).getModuleName().equals(inTutorial.getModuleName()) && timetableTemp2.get(i).getGroupNumber() != inTutorial.getGroupNumber())
								{
									outTutorial = timetableTemp2.get(i);
								}
							}
							
							//send inform message as reply
							//convert to Attends for messaging
							AttendsTutorial attendsTemp = new AttendsTutorial();
							attendsTemp.setStudent(getAID());
							attendsTemp.setTutorial(outTutorial);
							
							//send query message for class
							ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
							msg.addReceiver(inStudent);
							msg.setLanguage(codec.getName());
							msg.setOntology(ontology.getName());
							// Let JADE convert from Java objects to string
							getContentManager().fillContent(msg, attendsTemp);
							send(msg);
						}
					}
					catch (CodecException ce) {
						ce.printStackTrace();
					}
					catch (OntologyException oe) {
						oe.printStackTrace();
					}
				}
				break;
				
				
			case 2:
				//case 2 receives inform message
				//if that class time is the same as this student send reject then move on to another student
				//else calculate utility function of the new class 
					//if the utility function is higher/equal we want to swap, reply with propose message
				//receiving message behaviour used from SellerAgent from practical 6
				if(incoming != null)
				{
					try {
						ContentElement ce = null;
						ce = getContentManager().extractContent(incoming);
						if(ce instanceof AttendsTutorial)
						{
							AttendsTutorial inAttendsTutorial = (AttendsTutorial) ce;
							StudentTutorial inTutorial = inAttendsTutorial.getTutorial();
							AID inStudent = inAttendsTutorial.getStudent();
							
							//check that the incoming class is not the same as the current class
							ArrayList<StudentTutorial> timetableTemp2 = (ArrayList<StudentTutorial>) timetable.getTimetable();
							for(int i=0; i<timetableTemp2.size(); i++)
							{
								if(timetableTemp2.get(i).getModuleName().equals(inTutorial.getModuleName()) && timetableTemp2.get(i).getGroupNumber() != inTutorial.getGroupNumber())
								{
									//make timetable with new class for utility function
									ArrayList<StudentTutorial> replaceTimetable = new ArrayList<>(timetable.getTimetable());
									replaceTimetable.set(i, inTutorial);
									//calculate utility function
									int newUtility = CalculateUtilityFunction(wants, preferNot, unable, replaceTimetable);
									
									if(newUtility >= utility)
									{
										//lets swap
										AttendsTutorial attendsTemp = new AttendsTutorial();
										attendsTemp.setStudent(getAID());
										attendsTemp.setTutorial(timetableTemp2.get(i));
										//attendsTemp.setTutorial(inTutorial);
										ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
										msg.addReceiver(inStudent);
										msg.setLanguage(codec.getName());
										msg.setOntology(ontology.getName());
										// Let JADE convert from Java objects to string
										getContentManager().fillContent(msg, attendsTemp);
										send(msg);
									}
									else
									{
										ACLMessage msg = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
										msg.addReceiver(inStudent);
										msg.setLanguage(codec.getName());
										msg.setOntology(ontology.getName());
										send(msg);
									}
								}
								if(timetableTemp2.get(i).getModuleName().equals(inTutorial.getModuleName()) && timetableTemp2.get(i).getGroupNumber() == inTutorial.getGroupNumber())
								{
									ACLMessage msg = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
									msg.addReceiver(inStudent);
									msg.setLanguage(codec.getName());
									msg.setOntology(ontology.getName());
									send(msg);
								}
							}
							
						}
					}
					catch (CodecException ce) {
						ce.printStackTrace();
					}
					catch (OntologyException oe) {
						oe.printStackTrace();
					}
				}
				
				break;
			
				
			case 3:
				//case 3 receives propose message
				//calculate utility function with proposed timeslot
					//if utility funciton is higher swap, reply with accept message, change timetable
					//else reply with reject message
				//code used to deal with messages is used from practical 6
				if(incoming != null)
				{
					try {
						ContentElement ce = null;
						ce = getContentManager().extractContent(incoming);
						if(ce instanceof AttendsTutorial)
						{
							AttendsTutorial inAttendsTutorial = (AttendsTutorial) ce;
							StudentTutorial inTutorial = inAttendsTutorial.getTutorial();
							AID inStudent = inAttendsTutorial.getStudent();
							
							ArrayList<StudentTutorial> timetableTemp2 = (ArrayList<StudentTutorial>) timetable.getTimetable();
							for(int i=0; i<timetableTemp2.size(); i++)
							{
								System.out.println(getAID().getName() + timetableTemp2.get(i).getModuleName() + inTutorial.getModuleName());
								System.out.println(getAID().getName() + timetableTemp2.get(i).getGroupNumber() + inTutorial.getGroupNumber());
								if(timetableTemp2.get(i).getModuleName().equals(inTutorial.getModuleName()) && timetableTemp2.get(i).getGroupNumber() != inTutorial.getGroupNumber())
								{
									//make timetable with new class for utility function
									ArrayList<StudentTutorial> replaceTimetable = new ArrayList<>(timetable.getTimetable());
									replaceTimetable.set(i, inTutorial);
									//calculate utility function
									int newUtility = CalculateUtilityFunction(wants, preferNot, unable, replaceTimetable);

									if(newUtility >= utility)
									{
										//lets swap
										AttendsTutorial attendsTemp = new AttendsTutorial();
										attendsTemp.setStudent(getAID());
										attendsTemp.setTutorial(timetableTemp2.get(i));
										
										ACLMessage msg = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
										msg.addReceiver(inStudent);
										msg.setLanguage(codec.getName());
										msg.setOntology(ontology.getName());
										// Let JADE convert from Java objects to string
										getContentManager().fillContent(msg, attendsTemp);
										send(msg);
										
										//swap classes
										timetable.setTimetable(replaceTimetable);
										utility = newUtility;
										//send swap to timetable
										//get timetable
										//create swap predicate
										Swap swap = new Swap();
										swap.setSwapStudent(inStudent);
										swap.setTutorial(inTutorial);
										
										//create inform message
										//send
										//send query message for class
										//code used from practical 06
										ACLMessage timetableMSG = new ACLMessage(ACLMessage.INFORM);
										timetableMSG.addReceiver(timetableAgentList[0]);
										timetableMSG.setLanguage(codec.getName());
										timetableMSG.setOntology(ontology.getName());
										// Let JADE convert from Java objects to string
										getContentManager().fillContent(timetableMSG, swap);
										send(timetableMSG);
									}
									else
									{
										//send reject
										ACLMessage msg = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
										msg.addReceiver(inStudent);
										msg.setLanguage(codec.getName());
										msg.setOntology(ontology.getName());
										send(msg);
									}
								}
							}
							
						}
						
					}
					catch (CodecException ce) {
						ce.printStackTrace();
					}
					catch (OntologyException oe) {
						oe.printStackTrace();
					}
				
			}
				break;

			case 4:
				//case 4 receives accept_propose message
				//change timetable
				if(incoming != null)
				{
					try {
						ContentElement ce = null;
						ce = getContentManager().extractContent(incoming);
						if(ce instanceof AttendsTutorial)
						{
							AttendsTutorial inAttendsTutorial = (AttendsTutorial) ce;
							StudentTutorial inTutorial = inAttendsTutorial.getTutorial();
							AID inStudent = inAttendsTutorial.getStudent();
							
							ArrayList<StudentTutorial> timetableTemp2 = (ArrayList<StudentTutorial>) timetable.getTimetable();
							for(int i=0; i<timetableTemp2.size(); i++)
							{
								if(timetableTemp2.get(i).getModuleName().equals(inTutorial.getModuleName()))
								{
									//make timetable with new class for utility function
									//swap classes
									timetable.getTimetable().set(i, inTutorial);
									//calculate new utility function
									int newUtility = CalculateUtilityFunction(wants, preferNot, unable, (ArrayList<StudentTutorial>)timetable.getTimetable());
									utility = newUtility;
									
								}
							}
						}
							
					}
					catch (CodecException ce) {
						ce.printStackTrace();
					}
					catch (OntologyException oe) {
						oe.printStackTrace();
					}
				
			}
				//case 5 reject message
				//stop
			case 5:
				break;
		}
			
			
			ArrayList<StudentTutorial> tempTimetable = (ArrayList<StudentTutorial>) timetable.getTimetable();
			for(int i=0; i<tempTimetable.size(); i++)
			{
				System.out.println(getAID().getName() + " " + step + " " + tempTimetable.get(i).getModuleName() + tempTimetable.get(i).getGroupNumber() + tempTimetable.get(i).getTimeSlot().getDay() + tempTimetable.get(i).getTimeSlot().getTime() + utility);
			}
			
		}
	}
	
	

	private int CalculateUtilityFunction(ArrayList<TimeSlot> wants, ArrayList<TimeSlot> preferNot, ArrayList<TimeSlot> unable, ArrayList<StudentTutorial> timetablePar){
		//loop through timetable
			//if wants contains timeslot then +40
			//if prefers not contains timeslot then +1
			//if unable contains timeslot then +0
			//else calculate no of timeslots away from unable
		
		//max score of 40
		int utility = 0;
		
		ArrayList<StudentTutorial> tempTimetable = timetablePar;
		for (int i=0; i<tempTimetable.size(); i++)
		{	
			TimeSlot tempTS = tempTimetable.get(i).getTimeSlot();
			
			//wants
			for(int j=0; j<wants.size(); j++)
			{
				if(wants.get(j).getDay().equals(tempTS.getDay()) && wants.get(j).getTime() == tempTS.getTime())
				{
					utility += 40;
				}
			}
			
			//prefer not
			for(int j=0; j<preferNot.size(); j++)
			{
				if(preferNot.get(j).getDay().equals(tempTS.getDay()) && preferNot.get(j).getTime() == tempTS.getTime())
				{
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
					}
					else
					{
						timeDiff = (unableTemp.getTime() - tempTS.getTime());
					}
					
					//calculate day numbers
					String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
					for(int k=0; k<days.length; k++)
					{
						if(days[k].equals(tempTS.getDay()))
						{
							tempTSDay = (k+1);
						}
					}
					
					for(int k=0; k<days.length; k++)
					{
						if(days[k].equals(unableTemp.getDay()))
						{
							unableTempDay = (k+1);
						}
					}
					
					if(tempTSDay >= unableTempDay)
					{
						dayDiff = ((tempTSDay - unableTempDay)*8);
					}
					else
					{
						dayDiff = ((unableTempDay - tempTSDay)*8);
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
	}
}
