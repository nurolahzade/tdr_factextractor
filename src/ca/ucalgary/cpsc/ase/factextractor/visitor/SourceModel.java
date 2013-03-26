package ca.ucalgary.cpsc.ase.factextractor.visitor;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import ca.ucalgary.cpsc.ase.common.entity.MethodInvocation;
import ca.ucalgary.cpsc.ase.common.entity.ObjectType;
import ca.ucalgary.cpsc.ase.common.entity.Project;
import ca.ucalgary.cpsc.ase.common.entity.SourceFile;
import ca.ucalgary.cpsc.ase.common.entity.Clazz;
import ca.ucalgary.cpsc.ase.common.entity.TestMethod;

public class SourceModel implements Model {
	
	private Project project;
	private SourceFile sourceFile;
	private Stack<Clazz> testClazzStack;
	private TestMethod testMethod;
	private Stack<MethodInvocation> invocations;
//	private Assertion assertion;
	// workaround for PPA's lack of annotations support
	private boolean jUnit4TestAnnotation;
	private String lhs;
	private Map<String, MethodInvocation> dataFlows;
	
	public SourceModel() {
		testClazzStack = new Stack<Clazz>();
		invocations = new Stack<MethodInvocation>();
		jUnit4TestAnnotation = false;
		lhs = null;
		dataFlows = new HashMap<String, MethodInvocation>();
	}
	
	private Project getProject() {
		return project;
	}
	
	private void setProject(Project project) {
		this.project = project;
	}
	
	private SourceFile getSourceFile() {
		return sourceFile;
	}
	
	private void setSourceFile(SourceFile sourceFile) {
		this.sourceFile = sourceFile;
	}

	private Clazz popTestClazz() {
		return testClazzStack.pop();
	}

	private void pushTestClazz(Clazz testClazz) {
		testClazzStack.push(testClazz);
	}
	
	private Clazz peekTestClazz() {
		return testClazzStack.empty() ? null : testClazzStack.peek();
	}

	private TestMethod getTestMethod() {
		return testMethod;
	}

	private void setTestMethod(TestMethod testMethod) {
		this.testMethod = testMethod;
	}
	
	private MethodInvocation popInvocation() {
		return invocations.pop();
	}
	
	private void pushInvocation(MethodInvocation invocation) {
		invocations.push(invocation);
	}
		
	private MethodInvocation peekInvocation() {
		return invocations.empty() ? null : invocations.peek();
	}

//	private Assertion getAssertion() {
//		return assertion;
//	}
//
//	private void setAssertion(Assertion assertion) {
//		this.assertion = assertion;
//	}
	
	public Project currentProject() {
		return getProject();
	}
	
	public void stepIntoProject(Project project) {
		setProject(project);
	}
	
	public SourceFile currentSourceFile() {
		return getSourceFile();
	}
	
	public void stepIntoSourceFile(SourceFile source) {
		setSourceFile(source);
	}
	
	public void stepIntoClazz(Clazz clazz) {
		pushTestClazz(clazz);
	}
	
	@Override
	public void ignoreClazz() {
		stepIntoClazz(null);
	}
	
	@Override
	public void stepOutOfClazz() {
		popTestClazz();
	}
	
	public Clazz currentClazz() {
		return peekTestClazz();
	}
	
	public void stepIntoTestMethod(TestMethod method) {
		setTestMethod(method);
	}
	
	public void stepOutOfTestMethod() {
		setTestMethod(null);
		resetDataFlows();
	}
	
	private void resetDataFlows() {
		dataFlows.clear();
	}

	public TestMethod currentTestMethod() {
		return getTestMethod();
	}
	
//	public void stepIntoAssertion(Assertion assertion) {
//		setAssertion(assertion);
//	}
//	
//	public void stepOutOfAssertion() {
//		stepIntoAssertion(null);
//	}
	
//	public Assertion currentAssertion() {
//		return getAssertion();
//	}
	
	public MethodInvocation currentInvocation() {
		return peekInvocation();
	}
	
	public void stepIntoInvocation(MethodInvocation invocation) {
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
//		return currentInvocation().getAssertion() != null;
//	}

	@Override
	public void importsJUnit4TestAnnotation() {
		jUnit4TestAnnotation = true;		
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
	
	public void registerDataFlow(MethodInvocation from, String to) {
		dataFlows.put(to, from);
	}
	
	public MethodInvocation dataFlowsInto(String variable) {
		return dataFlows.get(variable);
	}
	
}
