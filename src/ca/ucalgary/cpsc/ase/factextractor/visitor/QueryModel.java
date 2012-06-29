package ca.ucalgary.cpsc.ase.factextractor.visitor;

import java.util.Stack;

import ca.ucalgary.cpsc.ase.QueryManager.query.QueryAssertion;
import ca.ucalgary.cpsc.ase.QueryManager.query.QueryMethod;
import ca.ucalgary.cpsc.ase.QueryManager.query.QueryTestClass;
import ca.ucalgary.cpsc.ase.QueryManager.query.QueryTestMethod;

public class QueryModel {

	private static QueryModel instance;
	
	private Stack<QueryTestClass> testClassStack;
	private QueryTestMethod testMethod;
	private Stack<QueryMethod> invocations;
	private QueryAssertion assertion;
	
	private QueryModel() {
		testClassStack = new Stack<QueryTestClass>();
		invocations = new Stack<QueryMethod>();
	}
	
	private static synchronized QueryModel getInstance() {
		if (instance == null) {
			instance = new QueryModel();
		}
		return instance;
	}
	
	private QueryTestClass popTestClass() {
		return testClassStack.pop();
	}

	private void pushTestClazz(QueryTestClass testClass) {
		testClassStack.push(testClass);
	}
	
	private QueryTestClass peekTestClass() {
		return testClassStack.peek();
	}

	private QueryTestMethod getTestMethod() {
		return testMethod;
	}

	private void setTestMethod(QueryTestMethod testMethod) {
		this.testMethod = testMethod;
	}
	
	private QueryMethod popInvocation() {
		return invocations.pop();
	}
	
	private void pushInvocation(QueryMethod invocation) {
		invocations.push(invocation);
	}
		
	private QueryMethod peekInvocation() {
		return invocations.peek();
	}

	private QueryAssertion getAssertion() {
		return assertion;
	}

	private void setAssertion(QueryAssertion assertion) {
		this.assertion = assertion;
	}
	
	public static void stepIntoClazz(QueryTestClass clazz) {
		getInstance().pushTestClazz(clazz);
	}
	
	public static void ignoreClazz() {
		stepIntoClazz(null);
	}
	
	public static QueryTestClass stepOutOfClazz() {
		return getInstance().popTestClass();
	}
	
	public static QueryTestClass currentClazz() {
		return getInstance().peekTestClass();
	}
	
	public static void stepIntoTestMethod(QueryTestMethod method) {
		getInstance().setTestMethod(method);
	}
	
	public static void stepOutOfTestMethod() {
		getInstance().setTestMethod(null);
	}
	
	public static QueryTestMethod currentTestMethod() {
		return getInstance().getTestMethod();
	}
	
	public static void stepIntoAssertion(QueryAssertion assertion) {
		getInstance().setAssertion(assertion);
	}
	
	public static void stepOutOfAssertion() {
		stepIntoAssertion(null);
	}
	
	public static QueryAssertion currentAssertion() {
		return getInstance().getAssertion();
	}
	
	public static QueryMethod currentInvocation() {
		return getInstance().peekInvocation();
	}
	
	public static void stepIntoInvocation(QueryMethod invocation) {
		getInstance().pushInvocation(invocation);
	}
	
	public static void ignoreInvocation() {
		stepIntoInvocation(null);
	}
	
	public static QueryMethod stepOutOfInvocation() {
		return getInstance().popInvocation();
	}
	
}
