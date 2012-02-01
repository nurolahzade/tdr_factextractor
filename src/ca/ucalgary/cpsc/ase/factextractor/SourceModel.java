package ca.ucalgary.cpsc.ase.factextractor;

import java.util.Stack;

import org.omg.CORBA.NVList;

import ca.ucalgary.cpsc.ase.FactManager.entity.Assertion;
import ca.ucalgary.cpsc.ase.FactManager.entity.Invocation;
import ca.ucalgary.cpsc.ase.FactManager.entity.Method;
import ca.ucalgary.cpsc.ase.FactManager.entity.Project;
import ca.ucalgary.cpsc.ase.FactManager.entity.SourceFile;
import ca.ucalgary.cpsc.ase.FactManager.entity.Clazz;
import ca.ucalgary.cpsc.ase.FactManager.entity.TestMethod;

public class SourceModel {
	
	private static SourceModel instance;
	
	private Project project;
	private SourceFile sourceFile;
	private Stack<Clazz> testClazzStack;
	private TestMethod testMethod;
	private Stack<Invocation> invocations;
	private Assertion assertion;
	
	private SourceModel() {
		testClazzStack = new Stack<Clazz>();
		invocations = new Stack<Invocation>();
	}
	
	private static synchronized SourceModel getInstance() {
		if (instance == null) {
			instance = new SourceModel();
		}
		return instance;
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
		return testClazzStack.peek();
	}

	private TestMethod getTestMethod() {
		return testMethod;
	}

	private void setTestMethod(TestMethod testMethod) {
		this.testMethod = testMethod;
	}
	
	private Invocation popInvocation() {
		return invocations.pop();
	}
	
	private void pushInvocation(Invocation invocation) {
		invocations.push(invocation);
	}
		
	private Invocation peekInvocation() {
		return invocations.peek();
	}

	private Assertion getAssertion() {
		return assertion;
	}

	private void setAssertion(Assertion assertion) {
		this.assertion = assertion;
	}
	
	public static Project currentProject() {
		return getInstance().getProject();
	}
	
	public static void stepIntoProject(Project project) {
		getInstance().setProject(project);
	}
	
	public static SourceFile currentSourceFile() {
		return getInstance().getSourceFile();
	}
	
	public static void stepIntoSourceFile(SourceFile source) {
		getInstance().setSourceFile(source);
	}
	
	public static void stepIntoClazz(Clazz clazz) {
		getInstance().pushTestClazz(clazz);
	}
	
	public static void ignoreClazz() {
		stepIntoClazz(null);
	}
	
	public static Clazz stepOutOfClazz() {
		return getInstance().popTestClazz();
	}
	
	public static Clazz currentClazz() {
		return getInstance().peekTestClazz();
	}
	
	public static void stepIntoTestMethod(TestMethod method) {
		getInstance().setTestMethod(method);
	}
	
	public static void stepOutOfTestMethod() {
		getInstance().setTestMethod(null);
	}
	
	public static TestMethod currentTestMethod() {
		return getInstance().getTestMethod();
	}
	
	public static void stepIntoAssertion(Assertion assertion) {
		getInstance().setAssertion(assertion);
	}
	
	public static void stepOutOfAssertion() {
		stepIntoAssertion(null);
	}
	
	public static Assertion currentAssertion() {
		return getInstance().getAssertion();
	}
	
	public static Invocation currentInvocation() {
		return getInstance().peekInvocation();
	}
	
	public static void stepIntoInvocation(Invocation invocation) {
		getInstance().pushInvocation(invocation);
	}
	
	public static void ignoreInvocation() {
		stepIntoInvocation(null);
	}
	
	public static Invocation stepOutOfInvocation() {
		return getInstance().popInvocation();
	}
	
}
