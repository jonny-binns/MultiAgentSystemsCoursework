package Timetable;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

public class StudentAgent extends Agent{

	protected void setup() {
		//doWait(10000);
		
		addBehaviour(new InitTimetable());
		
		System.out.println("Hello, Agent "+getAID().getName() + " is ready");
	}
	
	public class InitTimetable extends OneShotBehaviour {
		@Override
		public void action() {
			//when message received from timetable agent copy the timetable in the message over to the private student timetable
		}
	}
}
