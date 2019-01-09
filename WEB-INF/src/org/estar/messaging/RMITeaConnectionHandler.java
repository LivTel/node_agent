package org.estar.messaging;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;

import ngat.util.logging.LogManager;
import ngat.util.logging.Logger;

import org.estar.configuration.NodeAgentProperties;
import org.estar.rtml.RTMLDocument;
import org.estar.tea.EmbeddedAgentRequestHandler;
import org.estar.tea.EmbeddedAgentTestHarness;
import org.estar.tea.NodeAgentAsynchronousResponseHandler;
import org.estar.tea.TelescopeAvailability;
import org.estar.tea.TelescopeAvailabilityPredictor;
import org.estar.util.LoggerUtil;

/*
 * This singleton class used to invoke 'handle' RTML  calls on the TEA
 * It is initialised by the InitialisationServlet
 */
public class RMITeaConnectionHandler {
	
	static Logger traceLogger = LogManager.getLogger(LoggerUtil.TRACE_LOGGER_NAME);
	static Logger errorLogger = LogManager.getLogger(LoggerUtil.ERROR_LOGGER_NAME);
	
	private static RMITeaConnectionHandler instance = null;
	
	//instance variables
	private java.util.Date instantiationTimeStamp;	
	private static String embeddedAgentRequestHandlerURL;
	private static String telescopeAvailabilityPredictorRequestHandlerURL;
	
	//private EmbeddedAgentRequestHandler earh;
	private TelescopeAvailabilityPredictor tap;
	private RMIBindingPersistorRunnable persistenceRunnable;
	
	//return the singleton instance of the TeaConnectionHandler
	public static RMITeaConnectionHandler getInstance(){
		if (instance == null) {
			instance = new RMITeaConnectionHandler();
		}
		return instance;
	}
	
	//constructor
	private RMITeaConnectionHandler() {
		traceLogger.log(5, RMITeaConnectionHandler.class.getName(), "... instantiating RMITeaConnectionHandler"); 
		
		embeddedAgentRequestHandlerURL 	= NodeAgentProperties.RMI_PREFIX;
		embeddedAgentRequestHandlerURL  += NodeAgentProperties.getInstance().getProperty(NodeAgentProperties.TEA_HOST_NAME_PROPERTY);
		embeddedAgentRequestHandlerURL  += "/" + NodeAgentProperties.getInstance().getProperty(NodeAgentProperties.TEA_REQUEST_HANDLER_RMI_NAME);
		
		telescopeAvailabilityPredictorRequestHandlerURL 	= NodeAgentProperties.RMI_PREFIX;
		telescopeAvailabilityPredictorRequestHandlerURL  += NodeAgentProperties.getInstance().getProperty(NodeAgentProperties.TEA_HOST_NAME_PROPERTY);
		telescopeAvailabilityPredictorRequestHandlerURL  += "/" + NodeAgentProperties.getInstance().getProperty(NodeAgentProperties.TEA_AVAILABILITY_PREDICTOR_RMI_NAME);
		
		String dnaarhBindingName =  NodeAgentProperties.getInstance().getProperty(NodeAgentProperties.NODE_AGENT_RESPONSE_HANDLER_RMI_NAME);
		try {
			//bind and keep alive an asychronous response handler
			DefaultNodeAgentAsynchronousResponseHandler aRH = new DefaultNodeAgentAsynchronousResponseHandler();
			/*
			showDebugBlock();
			*/
			persistenceRunnable = new RMIBindingPersistorRunnable(dnaarhBindingName, aRH);
			new Thread(persistenceRunnable).start();
			
			/*
			traceLogger.log(5, RMITeaConnectionHandler.class.getName(), "... ... What:");
			try {
				String where = "localhost";
				String[] which = Naming.list("rmi://"+where);
				for (int i = 0; i < which.length; i++ ){
					System.err.println("["+i+"] "+which[i]);
				}
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
			*/
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		instantiationTimeStamp = new Date();	
	}
	
	public synchronized RTMLDocument handleScore(RTMLDocument rtmlDocument) throws MalformedURLException, RemoteException, NotBoundException {
		traceLogger.log(5, RMITeaConnectionHandler.class.getName(), "handleScore() invoked");
		RTMLDocument replyDocument = null;
		
		//locate reference to EmbeddedAgentRequestHandler (in the TEA)
		traceLogger.log(5, RMITeaConnectionHandler.class.getName(), "looking up EmbeddedAgentRequestHandler on TEA using URL: " + embeddedAgentRequestHandlerURL);
		EmbeddedAgentRequestHandler earh = (EmbeddedAgentRequestHandler)Naming.lookup(embeddedAgentRequestHandlerURL);
		traceLogger.log(5, RMITeaConnectionHandler.class.getName(), "... located EmbeddedAgentRequestHandler: "+earh);
		
		//invoke required method on earh
		traceLogger.log(5, RMITeaConnectionHandler.class.getName(), "... calling EmbeddedAgentRequestHandler.handleScore(rtmlDocument)");
		replyDocument = earh.handleScore(rtmlDocument);

		return replyDocument;
	}
	
	public synchronized RTMLDocument handleRequest(RTMLDocument rtmlDocument) throws MalformedURLException, RemoteException, NotBoundException {
		traceLogger.log(5, RMITeaConnectionHandler.class.getName(), "handleRequest() invoked");
		RTMLDocument replyDocument = null;
		
		//locate reference to EmbeddedAgentRequestHandler (in the TEA)
		traceLogger.log(5, RMITeaConnectionHandler.class.getName(), "looking up EmbeddedAgentRequestHandler on TEA using URL: " + embeddedAgentRequestHandlerURL);
		EmbeddedAgentRequestHandler earh = (EmbeddedAgentRequestHandler)Naming.lookup(embeddedAgentRequestHandlerURL);
		
		traceLogger.log(5, RMITeaConnectionHandler.class.getName(), "... located EmbeddedAgentRequestHandler: "+earh);
		
		//invoke required method on earh
		traceLogger.log(5, RMITeaConnectionHandler.class.getName(), "... calling EmbeddedAgentRequestHandler.handleRequest(rtmlDocument)");
		replyDocument = earh.handleRequest(rtmlDocument);

		return replyDocument;
	}
	
	public synchronized void testUpdateCallback(RTMLDocument rtmlDocument, long howLong) throws MalformedURLException, RemoteException, NotBoundException {
		traceLogger.log(5, RMITeaConnectionHandler.class.getName(), "testUpdateCallback(<rtmlDocument>," + howLong+")");
		
		//locate reference to EmbeddedAgentRequestHandler (in the TEA)
		traceLogger.log(5, RMITeaConnectionHandler.class.getName(), "looking up EmbeddedAgentTestHarness on TEA using URL: " + embeddedAgentRequestHandlerURL);
		EmbeddedAgentTestHarness eath = (EmbeddedAgentTestHarness)Naming.lookup(embeddedAgentRequestHandlerURL);
		traceLogger.log(5, RMITeaConnectionHandler.class.getName(), "... located EmbeddedAgentTestHarness: "+eath);
		//eath.testThroughput()
		//invoke required method on earh
		traceLogger.log(5, RMITeaConnectionHandler.class.getName(), "... calling EmbeddedAgentTestHarness.testUpdateCallback(<rtmlDocument>, " + howLong + ")");
		eath.testUpdateCallback(rtmlDocument, howLong);
		traceLogger.log(5, RMITeaConnectionHandler.class.getName(), "... testUpdateCallback invoked on EmbeddedAgentTestHarness");
	}
	
	public synchronized void testThroughput() throws RemoteException, MalformedURLException, NotBoundException {
		traceLogger.log(5, RMITeaConnectionHandler.class.getName(), "testThroughput()");
		
		//locate reference to EmbeddedAgentRequestHandler (in the TEA)
		traceLogger.log(5, RMITeaConnectionHandler.class.getName(), "looking up EmbeddedAgentTestHarness on TEA using URL: " + embeddedAgentRequestHandlerURL);
		EmbeddedAgentTestHarness eath = (EmbeddedAgentTestHarness)Naming.lookup(embeddedAgentRequestHandlerURL);
		traceLogger.log(5, RMITeaConnectionHandler.class.getName(), "... located EmbeddedAgentTestHarness: "+eath);

		//invoke required method on earh
		traceLogger.log(5, RMITeaConnectionHandler.class.getName(), "... calling EmbeddedAgentTestHarness.testThroughput()");
		
		//throws RemoteException if the throughput had problems, this eventually results in a SOAP error in response to the SOAP ping()
		eath.testThroughput();
		traceLogger.log(5, RMITeaConnectionHandler.class.getName(), "... testUpdateCallback invoked on EmbeddedAgentTestHarness");
	}
	
	public synchronized TelescopeAvailability getAvailabilityPrediction() throws MalformedURLException, RemoteException, NotBoundException {
		traceLogger.log(5, RMITeaConnectionHandler.class.getName(), "getAvailabilityPrediction() invoked");
		
		//locate reference to EmbeddedAgentRequestHandler (in the TEA)
		traceLogger.log(5, RMITeaConnectionHandler.class.getName(), "looking up TelescopeAvailabilityPredictor on TEA using URL: " + telescopeAvailabilityPredictorRequestHandlerURL);
		tap = (TelescopeAvailabilityPredictor)Naming.lookup(telescopeAvailabilityPredictorRequestHandlerURL);
		traceLogger.log(5, RMITeaConnectionHandler.class.getName(), "... located TelescopeAvailabilityPredictor: "+tap);
		
		//invoke required method on tap
		
		TelescopeAvailability availability = tap.getAvailabilityPrediction();
		return availability;
	}
	
	 public synchronized void setAvailabilityPrediction(long startTime, long endTime, double prediction)throws MalformedURLException, RemoteException, NotBoundException {
		 traceLogger.log(5, RMITeaConnectionHandler.class.getName(), "getAvailabilityPrediction() invoked");
		
		//locate reference to EmbeddedAgentRequestHandler (in the TEA)
		traceLogger.log(5, RMITeaConnectionHandler.class.getName(), "looking up TelescopeAvailabilityPredictor on TEA using URL: " + telescopeAvailabilityPredictorRequestHandlerURL);
		tap = (TelescopeAvailabilityPredictor)Naming.lookup(telescopeAvailabilityPredictorRequestHandlerURL);
		traceLogger.log(5, RMITeaConnectionHandler.class.getName(), "... located TelescopeAvailabilityPredictor: "+tap);
		
		//invoke required method on tap
		tap.setAvailabilityPrediction(startTime, endTime, prediction);
	 }
	 
	public void destroy() {
		persistenceRunnable.stop();
	}
	
	public String toString() {
		return this.getClass().getName() + " [instantiationTimeStamp:" + instantiationTimeStamp + "]";
	}
	
	/*
	private static void showDebugBlock() {
		
		try {
			System.err.println(" ************************* DEBUG BLOCK ************************* ");
			TestClass1 tc1 = new TestClass1();
			System.err.println("tc1 = " + tc1);
			TestClass2 tc2 = new TestClass2();
			System.err.println("tc2 = " + tc2);
			TestClass3 tc3 = new TestClass3();
			System.err.println("tc3 = " + tc3);
			
			System.err.println(" ************************* *********** ************************* ");
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	//main method for execution of hack test from JVM outside Tomcat
	public static void main(String a[]) {
		showDebugBlock();
	}
	*/
}


class RMIBindingPersistorRunnable implements Runnable {
	
	private Remote object;
	private String bindName;
	private volatile boolean shouldPersist;
	private static Date bindTime;
	
	static Logger traceLogger = LogManager.getLogger(LoggerUtil.TRACE_LOGGER_NAME);
	static Logger errorLogger = LogManager.getLogger(LoggerUtil.ERROR_LOGGER_NAME);
	
	public RMIBindingPersistorRunnable(String bindName, Remote object) throws RemoteException, MalformedURLException {
		traceLogger.log(5, RMIBindingPersistorRunnable.class.getName(), "new RMIBindingPeristanceThread(" + bindName + "," + object.getClass().getName() + "[" + object  + "] )");
		this.object= object;
		this.bindName = bindName;
	}
	
	private void delay(int msD) {
		Object waiter = new Object();
		synchronized(waiter) { 
			try { 
				waiter.wait(msD); 
			} catch (InterruptedException e)  {} 
		}
	}
	
	public void stop() {
		shouldPersist = false;
	}
	
	public void run() {
		traceLogger.log(5, RMIBindingPersistorRunnable.class.getName(), "... run()");
		
		try {
			traceLogger.log(5, RMIBindingPersistorRunnable.class.getName(), "... doing initial rebind");
			traceLogger.log(5, RMIBindingPersistorRunnable.class.getName(), "... Naming.rebind(" + bindName + ", " + object.getClass().getName() + "[" + object + "] );");
			Naming.rebind(bindName, object);
			bindTime = new Date();
			traceLogger.log(5, RMIBindingPersistorRunnable.class.getName(), "... ... rebind successful at " + bindTime);
		} catch (Exception e1) {
			errorLogger.log(1, RMIBindingPersistorRunnable.class.getName(), "initial rebind failed, halting RMIBindingPersistorRunnable!");
			e1.printStackTrace();
			return;
		} 
		
		shouldPersist = true;
		while (shouldPersist) {
			delay(60000);
			try {
				Naming.lookup(bindName);
			} catch (NotBoundException e) {
				//Not bound - log the fact and rebind 
				Date nowD = new Date();
				errorLogger.log(1, RMITeaConnectionHandler.class.getName(), bindName + " found to be unbound at " +nowD + " (last bind was at " + bindTime + ")");
				try {
					Naming.rebind(bindName, object);
					bindTime = new Date();
					traceLogger.log(5, RMIBindingPersistorRunnable.class.getName(), "... ... rebind successful at " + bindTime);
				} catch (Exception e1) {
					errorLogger.log(1, RMIBindingPersistorRunnable.class.getName(), "rebind failed after NotBoundException, halting RMIBindingPersistorRunnable!");
					e1.printStackTrace();
					shouldPersist = false;
					return;
				} 
			} catch (Exception e) {
				errorLogger.log(1, RMIBindingPersistorRunnable.class.getName(), "lookup failed but no NotBoundException was thrown, halting RMIBindingPersistorRunnable!");
				e.printStackTrace();
				shouldPersist = false;
				return;
			}
		}
		traceLogger.log(5, RMIBindingPersistorRunnable.class.getName(), "shouldPersist = false, exiting run cycle");
	}
		
}
/*
class TestClass1 extends UnicastRemoteObject {
	public TestClass1()throws RemoteException {}
}

class TestClass2 implements NodeAgentAsynchronousResponseHandler {

	public void handleAsyncResponse(RTMLDocument arg0) throws RemoteException {
	}
}

class TestClass3 implements java.rmi.Remote {

}
*/