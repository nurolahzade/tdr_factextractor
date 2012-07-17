package ca.ucalgary.cpsc.ase.factextractor.visitor;

import java.util.Stack;

import ca.ucalgary.cpsc.ase.FactManager.entity.Assertion;
import ca.ucalgary.cpsc.ase.FactManager.entity.Invocation;
import ca.ucalgary.cpsc.ase.FactManager.entity.ObjectType;
import ca.ucalgary.cpsc.ase.FactManager.entity.Project;
import ca.ucalgary.cpsc.ase.FactManager.entity.SourceFile;
import ca.ucalgary.cpsc.ase.FactManager.entity.Clazz;
import ca.ucalgary.cpsc.ase.FactManager.entity.TestMethod;

public class SourceModel implements Model {
	
	private Project project;
	private SourceFile sourceFile;
	private Stack<Clazz> testClazzStack;
	private TestMethod testMethod;
	private Stack<Invocation> invocations;
	private Assertion assertion;
	// workaround for PPA's lack of annotations support
	private boolean jUnit4TestAnnotation;
	
	public SourceModel() {
		testClazzStack = new Stack<Clazz>();
		invocations = new Stack<Invocation>();
		jUnit4TestAnnotation = false;
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
	
	private Invocation popInvocation() {
		return invocations.pop();
	}
	
	private void pushInvocation(Invocation invocation) {
		invocations.push(invocation);
	}
		
	private Invocation peekInvocation() {
		return invocations.empty() ? null : invocations.peek();
	}

	private Assertion getAssertion() {
		return assertion;
	}

	private void setAssertion(Assertion assertion) {
		this.assertion = assertion;
	}
	
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
	}
	
	public TestMethod currentTestMethod() {
		return getTestMethod();
	}
	
	public void stepIntoAssertion(Assertion assertion) {
		setAssertion(assertion);
	}
	
	public void stepOutOfAssertion() {
		stepIntoAssertion(null);
	}
	
	public Assertion currentAssertion() {
		return getAssertion();
	}
	
	public Invocation currentInvocation() {
		return peekInvocation();
	}
	
	public void stepIntoInvocation(Invocation invocation) {
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
		return currentInvocation() instanceof Assertion;
	}

	@Override
	public void importsJUnit4TestAnnotation() {
		jUnit4TestAnnotation = true;		
	}

	@Override
	public boolean hasTestAnnotation() {
		return jUnit4TestAnnotation;
	}
	
}
