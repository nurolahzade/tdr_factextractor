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
	
	public static synchronized SourceModel getInstance() {
		if (instance == null) {
			instance = new SourceModel();
		}
		return instance;
	}
	
	public Project getProject() {
		return project;
	}
	
	public void setProject(Project project) {
		this.project = project;
	}
	
	public SourceFile getSourceFile() {
		return sourceFile;
	}
	
	public void setSourceFile(SourceFile sourceFile) {
		this.sourceFile = sourceFile;
	}

	public Clazz popTestClazz() {
		return testClazzStack.pop();
	}

	public void pushTestClazz(Clazz testClazz) {
		testClazzStack.push(testClazz);
	}
	
	public Clazz peekTestClazz() {
		return testClazzStack.peek();
	}

	public TestMethod getTestMethod() {
		return testMethod;
	}

	public void setTestMethod(TestMethod testMethod) {
		this.testMethod = testMethod;
	}
	
	public Invocation popInvocation() {
		return invocations.pop();
	}
	
	public void pushInvocation(Invocation invocation) {
		invocations.push(invocation);
	}
		
	public Invocation peekInvocation() {
		return invocations.peek();
	}

	public Assertion getAssertion() {
		return assertion;
	}

	public void setAssertion(Assertion assertion) {
		this.assertion = assertion;
	}
		
}
