package Timetable;

import jade.core.Agent;

public class SimpleAgent extends Agent {
	
	protected void setup() {
		doWait(10000);
		System.out.println("Hello, Agent "+getAID().getName() + " is ready");
	}
}
