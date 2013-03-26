package ca.ucalgary.cpsc.ase.factextractor.visitor;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import ca.ucalgary.cpsc.ase.common.entity.ObjectType;
import ca.ucalgary.cpsc.ase.common.query.QueryInvocation;
import ca.ucalgary.cpsc.ase.common.query.QueryTestClass;
import ca.ucalgary.cpsc.ase.common.query.QueryTestMethod;

public class QueryModel implements Model {

	private Stack<QueryTestClass> testClassStack;
	private QueryTestMethod testMethod;
	private Stack<QueryInvocation> invocations;
//	private QueryAssertion assertion;
	// workaround for PPA's lack of annotations support
	private boolean jUnit4TestAnnotation;
	private String lhs;
	private Map<String, QueryInvocation> dataFlows;
	
	public QueryModel() {
		testClassStack = new Stack<QueryTestClass>();
		invocations = new Stack<QueryInvocation>();
		jUnit4TestAnnotation = false;
		lhs = null;
		dataFlows = new HashMap<String, QueryInvocation>();
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

//	private QueryAssertion getAssertion() {
//		return assertion;
//	}
//
//	private void setAssertion(QueryAssertion assertion) {
//		this.assertion = assertion;
//	}
	
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
	
//	public void stepIntoAssertion(QueryAssertion assertion) {
//		setAssertion(assertion);
//	}
	
//	public void stepOutOfAssertion() {
//		stepIntoAssertion(null);
//	}
	
//	public QueryAssertion currentAssertion() {
//		return getAssertion();
//	}
	
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

//	@Override
//	public boolean insideAnAssertion() {
////		return currentInvocation() instanceof QueryAssertion;	
//		return currentAssertion() != null;
//	}

	@Override
	public void importsJUnit4TestAnnotation() {
		this.jUnit4TestAnnotation = true;
	}

	@Override
	public boolean hasTestAnnotation() {
		return jUnit4TestAnnotation;
	}

	@Override
	public boolean insideAnInvocation() {
		return currentInvocation() != null;
	}

	@Override
	public void stepIntoAssignment(String assignee) {
		lhs = assignee;		
	}

	@Override
	public void stepOutOfAssignment() {
		lhs = null;
	}

	@Override
	public boolean insideAnAssignment() {
		return lhs != null;		
	}

	public String currentLHS() {
		return lhs;
	}
	
	public void registerDataFlow(QueryInvocation from, String to) {
		dataFlows.put(to, from);
	}
	
	public QueryInvocation dataFlowsInto(String variable) {
		return dataFlows.get(variable);
	}
	
}
