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
			//wants.add(preferences[0]);
			//preferNot.add(preferences[0]);
			unable.add(preferences[0]);
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
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("Student-Agent");
			template.addServices(sd);
			try {
				DFAgentDescription[] result = DFService.search(myAgent, template);
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
			
			/*
			for(int i=0; i<studentList.length; i++)
			{
				System.out.println(getAID().getName() + studentList[i]);
			}
			*/
			int step = 0;
			//check received messages in order to decide case
			
			ACLMessage incoming = myAgent.receive(incomingMT);
			if(incoming !=null)
			{
				//incoming query_ref message
				if(incoming.getPerformative() == ACLMessage.QUERY_REF)
				{
					step = 1;
					//System.out.println(getAID().getName() + "line 207 step = " + step);
				}
				//inform
				if(incoming.getPerformative() == ACLMessage.INFORM)
				{
					step = 2;
					//System.out.println(getAID().getName() + "line 213 step = " + step);
				}
				//propose
				if(incoming.getPerformative() == ACLMessage.PROPOSE)
				{
					step = 3;
					//System.out.println(getAID().getName() + "line 219 step = " + step);
				}
				//accept proposal
				if(incoming.getPerformative() == ACLMessage.ACCEPT_PROPOSAL)
				{
					step = 4;
					//System.out.println(getAID().getName() + "line 225 step = " + step);
				}
				//reject
				if(incoming.getPerformative() == ACLMessage.REJECT_PROPOSAL)
				{
					step = 5;
					//System.out.println(getAID().getName() + "line 231 step = " + step);
				}
			}
			//System.out.println(getAID().getName() + "line 234 step = " + step);
			
			switch (step) {
			//case 0 sends message asking for swap
			//find out if class is in its unable or prefer not lists
			//if yes send message
			//send query message for the time where other student attends class this student cant attend
			
			//other person replies with inform saying what time that class is
				//if that class time is the same as this student send reject then move on to another student
				//else calculate utility function of the new class 
					//if the utility function is higher/equal we want to swap, reply with propose message
						//if other person accepts swap message timetable and request an update, also swap time in timetable
						//else send reject move on to another student
					//else send reject move on to another student
			
			case 0: //agent decides to send message to other agents asking for swap
				//find out if class is in an unable or prefer not list
				//System.out.println(getAID().getName() + "Started case 0");
				//boolean unwantedClassBool = false;
				StudentTutorial unwantedClass = null;
				
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
				//System.out.println(timetable.getTimetable());
				
				ArrayList<StudentTutorial> timetableTemp = (ArrayList<StudentTutorial>) timetable.getTimetable();
				//while(unwantedClassBool == false)
				//{
					//ystem.out.println(timetableTemp.size());
					
					for(int i=0; i<timetableTemp.size(); i++)
					{
						for(int j=0; j<unable.size(); j++)
						{
							if(unable.get(j).getDay().equals(timetableTemp.get(i).getTimeSlot().getDay()) && unable.get(j).getTime() == timetableTemp.get(i).getTimeSlot().getTime());
							{
								//System.out.println("line 234");
								unwantedClass = timetableTemp.get(i);
								//unwantedClassBool = true;
								//System.out.println(unwantedClass.getModuleName() + unwantedClass.getGroupNumber());
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
									//msg.setConversationId("Swap");
									try {
										 // Let JADE convert from Java objects to string
										 getContentManager().fillContent(msg, attendsTemp);
										 send(msg);
										 //System.out.println(getAID().getName() + msg);
										 //System.out.println(getAID().getName() + "case 0 out = " + attendsTemp.getTutorial().getModuleName() + attendsTemp.getTutorial().getGroupNumber());
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
						 //change for prefer not add if else
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
					
					
				//}
				//System.out.println(unwantedClass.getModuleName() + unwantedClass.getGroupNumber());

				

				//System.out.println(getAID().getName() + "finished case 0");
				break;
				
			//case 1 receives query message
			//receives query message
			//replies with timeslot of that class
			case 1:
				//System.out.println(getAID().getName() + "Started case 1");
				//receiving message behaviour used from SellerAgent from practical 6
				//MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.QUERY_REF);
				//ACLMessage msg = receive(mt);
				//System.out.println(getAID().getName() + " line 337 " + incoming);
				if(incoming != null)
				{
					//System.out.println(getAID().getName() + "line 336");
					try { 
						//System.out.println(getAID().getName() + "line 338");
						ContentElement ce = null;
						//System.out.println(incoming.getContent());
						ce = getContentManager().extractContent(incoming);
						if(ce instanceof AttendsTutorial) {
							AttendsTutorial inAttendsTutorial = (AttendsTutorial) ce;
							StudentTutorial inTutorial = inAttendsTutorial.getTutorial();
							AID inStudent = inAttendsTutorial.getStudent();
							//System.out.println(getAID().getName() + "case 1 in = " + inAttendsTutorial.getTutorial().getModuleName() + inAttendsTutorial.getTutorial().getGroupNumber());
							//System.out.println(getAID().getName() + "line 345");
							//System.out.println(getAID().getName() + "inTutorial = " + inTutorial.getModuleName() + inTutorial.getGroupNumber());
							
							//get tutorial this agent attends
							ArrayList<StudentTutorial> timetableTemp2 = (ArrayList<StudentTutorial>) timetable.getTimetable();
							StudentTutorial outTutorial = null;
							for(int i=0; i<timetableTemp2.size(); i++) {
								if(timetableTemp2.get(i).getModuleName().equals(inTutorial.getModuleName()))
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
							//msg.setConversationId("Swap");
							// Let JADE convert from Java objects to string
							//System.out.println(getAID().getName() + "case 1 out = " + attendsTemp.getTutorial().getModuleName() + attendsTemp.getTutorial().getGroupNumber());
							getContentManager().fillContent(msg, attendsTemp);
							send(msg);
							//System.out.println(getAID().getName() + "line 378" + msg);
						}
					}
					catch (CodecException ce) {
						ce.printStackTrace();
					}
					catch (OntologyException oe) {
						oe.printStackTrace();
					}
				}
				//System.out.println(getAID().getName() + "Finished case 1");
				break;
				
				//case 2 receives inform message
				//if that class time is the same as this student send reject then move on to another student
				//else calculate utility function of the new class 
					//if the utility function is higher/equal we want to swap, reply with propose message
			
			case 2:
				//System.out.println(getAID().getName() + "Started case 2");
				//receiving message behaviour used from SellerAgent from practical 6
				//System.out.println(getAID().getName() + " line 401 " + incoming);
				if(incoming != null)
				{
					try { 
						//System.out.println(getAID().getName() + "line 338");
						ContentElement ce = null;
						//System.out.println(incoming.getContent());
						ce = getContentManager().extractContent(incoming);
						//System.out.println("407" + ce);
						if(ce instanceof AttendsTutorial)
						{
							AttendsTutorial inAttendsTutorial = (AttendsTutorial) ce;
							StudentTutorial inTutorial = inAttendsTutorial.getTutorial();
							AID inStudent = inAttendsTutorial.getStudent();
							//System.out.println(getAID().getName() + "case 2 in = " + inAttendsTutorial.getTutorial().getModuleName() + inAttendsTutorial.getTutorial().getGroupNumber());
							//System.out.println(getAID().getName() + "line 412");
							//System.out.println(getAID().getName() + "inTutorial = " + inTutorial.getModuleName() + inTutorial.getGroupNumber());
							
							//check that the incoming class is not the same as the current class
							ArrayList<StudentTutorial> timetableTemp2 = (ArrayList<StudentTutorial>) timetable.getTimetable();
							for(int i=0; i<timetableTemp2.size(); i++)
							{
								if(timetableTemp2.get(i).getModuleName().equals(inTutorial.getModuleName()) && timetableTemp2.get(i).getGroupNumber() != inTutorial.getGroupNumber())
								{
									//make timetable with new class for utility function
									ArrayList<StudentTutorial> replaceTimetable = (ArrayList<StudentTutorial>) timetable.getTimetable();
									replaceTimetable.set(i, inTutorial);
									//calculate utility function
									int newUtility = CalculateUtilityFunction(wants, preferNot, unable, replaceTimetable);
									
									if(newUtility >= utility)
									{
										//lets swap
										AttendsTutorial attendsTemp = new AttendsTutorial();
										attendsTemp.setStudent(getAID());
										attendsTemp.setTutorial(timetableTemp2.get(i));
										
										ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
										msg.addReceiver(inStudent);
										msg.setLanguage(codec.getName());
										msg.setOntology(ontology.getName());
										//msg.setConversationId("Swap");
										// Let JADE convert from Java objects to string
										//System.out.println(getAID().getName() + "case 2 out = " + attendsTemp.getTutorial().getModuleName() + attendsTemp.getTutorial().getGroupNumber());
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
								else if(timetableTemp2.get(i).getModuleName().equals(inTutorial.getModuleName()) && timetableTemp2.get(i).getGroupNumber() == inTutorial.getGroupNumber())
								{
									ACLMessage msg = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
									msg.addReceiver(inStudent);
									msg.setLanguage(codec.getName());
									msg.setOntology(ontology.getName());
									send(msg);
								}
								else{}
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
			//case 3 receives propose message
				//calculate utility function with proposed timeslot
					//if utility funciton is higher swap, reply with accept message, change timetable
					//else reply with reject message
			case 3:
				//code used to deal with messages is used from practical 6
				if(incoming != null)
				{
					try { 
						//System.out.println(getAID().getName() + "line 338");
						ContentElement ce = null;
						//System.out.println(incoming.getContent());
						ce = getContentManager().extractContent(incoming);
						//System.out.println("407" + ce);
						if(ce instanceof AttendsTutorial)
						{
							AttendsTutorial inAttendsTutorial = (AttendsTutorial) ce;
							StudentTutorial inTutorial = inAttendsTutorial.getTutorial();
							AID inStudent = inAttendsTutorial.getStudent();
							//System.out.println(getAID().getName() + "case 3 in = " + inAttendsTutorial.getTutorial().getModuleName() + inAttendsTutorial.getTutorial().getGroupNumber());
							
							ArrayList<StudentTutorial> timetableTemp2 = (ArrayList<StudentTutorial>) timetable.getTimetable();
							for(int i=0; i<timetableTemp2.size(); i++)
							{
								if(timetableTemp2.get(i).getModuleName().equals(inTutorial.getModuleName()) && timetableTemp2.get(i).getGroupNumber() != inTutorial.getGroupNumber())
								{
									//make timetable with new class for utility function
									ArrayList<StudentTutorial> replaceTimetable = (ArrayList<StudentTutorial>) timetable.getTimetable();
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
										//msg.setConversationId("Swap");
										// Let JADE convert from Java objects to string
										//System.out.println(getAID().getName() + "case 3 out = " + attendsTemp.getTutorial().getModuleName() + attendsTemp.getTutorial().getGroupNumber());
										getContentManager().fillContent(msg, attendsTemp);
										send(msg);
										
										//swap classes
										timetable.setTimetable(replaceTimetable);
										utility = newUtility;
										//System.out.println("Case 3 timetable update");
										ArrayList<StudentTutorial> tempTimetable = (ArrayList<StudentTutorial>) timetable.getTimetable();
										/*for(int j=0; j<tempTimetable.size(); j++)
										{
											System.out.println(getAID().getName() + " " + tempTimetable.get(j).getModuleName() + tempTimetable.get(j).getGroupNumber() + tempTimetable.get(j).getTimeSlot().getDay() + tempTimetable.get(j).getTimeSlot().getTime());
										}*/
										
									}
									else
									{
										//send reject
										ACLMessage msg = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
										msg.addReceiver(inStudent);
										msg.setLanguage(codec.getName());
										msg.setOntology(ontology.getName());
										send(msg);
										//step = 5;
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
				//case 4 receives accept_propose message
				//change timetable
			case 4:
				if(incoming != null)
				{
					try { 
						//System.out.println(getAID().getName() + "line 338");
						ContentElement ce = null;
						//System.out.println(incoming.getContent());
						ce = getContentManager().extractContent(incoming);
						//System.out.println("407" + ce);
						if(ce instanceof AttendsTutorial)
						{
							AttendsTutorial inAttendsTutorial = (AttendsTutorial) ce;
							StudentTutorial inTutorial = inAttendsTutorial.getTutorial();
							AID inStudent = inAttendsTutorial.getStudent();
							//System.out.println(getAID().getName() + "case 4 in = " + inAttendsTutorial.getTutorial().getModuleName() + inAttendsTutorial.getTutorial().getGroupNumber());
							
							ArrayList<StudentTutorial> timetableTemp2 = (ArrayList<StudentTutorial>) timetable.getTimetable();
							for(int i=0; i<timetableTemp2.size(); i++)
							{
								if(timetableTemp2.get(i).getModuleName().equals(inTutorial.getModuleName()))
								{
									//make timetable with new class for utility function
									ArrayList<StudentTutorial> replaceTimetable = (ArrayList<StudentTutorial>) timetable.getTimetable();
									replaceTimetable.set(i, inTutorial);
									//swap classes
									timetable.setTimetable(replaceTimetable);
									
									//System.out.println("Case 4 timetable update");
									ArrayList<StudentTutorial> tempTimetable = (ArrayList<StudentTutorial>) timetable.getTimetable();
									/*for(int j=0; j<tempTimetable.size(); j++)
									{
										System.out.println(getAID().getName() + " " + tempTimetable.get(j).getModuleName() + tempTimetable.get(j).getGroupNumber() + tempTimetable.get(j).getTimeSlot().getDay() + tempTimetable.get(j).getTimeSlot().getTime());
									}*/
									
									
									//calculate new utility function
									int newUtility = CalculateUtilityFunction(wants, preferNot, unable, replaceTimetable);
									utility = newUtility;
									//send swap to timetable
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
				System.out.println(getAID().getName() + " " + tempTimetable.get(i).getModuleName() + tempTimetable.get(i).getGroupNumber() + tempTimetable.get(i).getTimeSlot().getDay() + tempTimetable.get(i).getTimeSlot().getTime());
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
						//System.out.println("231 day Diff = " + dayDiff);
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
