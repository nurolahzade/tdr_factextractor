package ca.ucalgary.cpsc.ase.factextractor.visitor;

import java.util.Stack;

import ca.ucalgary.cpsc.ase.FactManager.entity.ObjectType;
import ca.ucalgary.cpsc.ase.QueryManager.query.QueryAssertion;
import ca.ucalgary.cpsc.ase.QueryManager.query.QueryInvocation;
import ca.ucalgary.cpsc.ase.QueryManager.query.QueryTestClass;
import ca.ucalgary.cpsc.ase.QueryManager.query.QueryTestMethod;

public class QueryModel implements Model {

	private Stack<QueryTestClass> testClassStack;
	private QueryTestMethod testMethod;
	private Stack<QueryInvocation> invocations;
	private QueryAssertion assertion;
	
	public QueryModel() {
		testClassStack = new Stack<QueryTestClass>();
		invocations = new Stack<QueryInvocation>();
	}
	
	private QueryTestClass popTestClass() {
		return testClassStack.pop();
	}

	private void pushTestClazz(QueryTestClass testClass) {
		testClassStack.push(testClass);
	}
	
	private QueryTestClass peekTestClass() {
		return testClassStack.empty() ? null : testClassStack.peek();
	}

	private QueryTestMethod getTestMethod() {
		return testMethod;
	}

	private void setTestMethod(QueryTestMethod testMethod) {
		this.testMethod = testMethod;
	}
	
	private QueryInvocation popInvocation() {
		return invocations.pop();
	}
	
	private void pushInvocation(QueryInvocation invocation) {
		invocations.push(invocation);
	}
		
	private QueryInvocation peekInvocation() {
		return invocations.empty() ? null : invocations.peek();
	}

	private QueryAssertion getAssertion() {
		return assertion;
	}

	private void setAssertion(QueryAssertion assertion) {
		this.assertion = assertion;
	}
	
	public void stepIntoClazz(QueryTestClass clazz) {
		pushTestClazz(clazz);
	}
	
	public void ignoreClazz() {
		stepIntoClazz(null);
	}
	
	public void stepOutOfClazz() {
		popTestClass();
	}
	
	public QueryTestClass currentClazz() {
		return peekTestClass();
	}
	
	public void stepIntoTestMethod(QueryTestMethod method) {
		setTestMethod(method);
	}
	
	public void stepOutOfTestMethod() {
		setTestMethod(null);
	}
	
	public QueryTestMethod currentTestMethod() {
		return getTestMethod();
	}
	
	public void stepIntoAssertion(QueryAssertion assertion) {
		setAssertion(assertion);
	}
	
	public void stepOutOfAssertion() {
		stepIntoAssertion(null);
	}
	
	public QueryAssertion currentAssertion() {
		return getAssertion();
	}
	
	public QueryInvocation currentInvocation() {
		return peekInvocation();
	}
	
	public void stepIntoInvocation(QueryInvocation invocation) {
		pushInvocation(invocation);
	}
	
	public void ignoreInvocation() {
		stepIntoInvocation(null);
	}
	
	public void stepOutOfInvocation() {
		popInvocation();
	}

	@Override
	public boolean isJUnit3TestClass() {
		return currentClazz().getType() == ObjectType.JUNIT3;
	}

	@Override
	public boolean isJUnit4TestClass() {
		return currentClazz().getType() == ObjectType.JUNIT4;
	}

	@Override
	public boolean insideAClass() {
		return currentClazz() != null;
	}

	@Override
	public void ignoreTestMethod() {
		stepIntoTestMethod(null);
	}

	@Override
	public boolean insideATestMethod() {
		return currentTestMethod() != null;
	}

	@Override
	public boolean insideAnAssertion() {
		return currentInvocation() instanceof QueryAssertion;	
	}
	
}
