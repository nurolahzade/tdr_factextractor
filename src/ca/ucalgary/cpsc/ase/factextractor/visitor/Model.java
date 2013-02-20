package ca.ucalgary.cpsc.ase.factextractor.visitor;

public interface Model {

	public void ignoreClazz();
	public void stepOutOfClazz();
	public void importsJUnit4TestAnnotation();
	public boolean hasTestAnnotation();
	public boolean isJUnit3TestClass();
	public boolean isJUnit4TestClass();
	public boolean insideAClass();
	public void ignoreTestMethod();
	public void stepOutOfTestMethod();
	public boolean insideATestMethod();
	public void ignoreInvocation();
	public void stepOutOfInvocation();
//	public boolean insideAnAssertion();
//	public void stepOutOfAssertion();	
	
}
