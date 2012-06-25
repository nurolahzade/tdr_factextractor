package ca.ucalgary.cpsc.ase.factextractor.writer;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import ca.ucalgary.cpsc.ase.FactManager.entity.Assertion;
import ca.ucalgary.cpsc.ase.FactManager.entity.AssertionType;
import ca.ucalgary.cpsc.ase.FactManager.entity.Clazz;
import ca.ucalgary.cpsc.ase.FactManager.entity.Method;
import ca.ucalgary.cpsc.ase.FactManager.entity.ObjectType;
import ca.ucalgary.cpsc.ase.FactManager.entity.TestMethod;
import ca.ucalgary.cpsc.ase.FactManager.service.AssertionService;
import ca.ucalgary.cpsc.ase.FactManager.service.ClazzService;
import ca.ucalgary.cpsc.ase.FactManager.service.MethodService;
import ca.ucalgary.cpsc.ase.FactManager.service.ReferenceService;
import ca.ucalgary.cpsc.ase.FactManager.service.TestMethodService;
import ca.ucalgary.cpsc.ase.FactManager.service.XceptionService;
import ca.ucalgary.cpsc.ase.factextractor.visitor.ASTHelper;
import ca.ucalgary.cpsc.ase.factextractor.visitor.SourceModel;

public class DatabaseWriter extends TestRecorder {
	
	private static Logger logger = Logger.getLogger(DatabaseWriter.class);	

	/*
	 * Persist test class as a JUnit 3.x or JUnit 4.x test class.
	 * Test class might already have been persisted.
	 */
	@Override
	public void saveTestClazz(ITypeBinding binding, ObjectType type) {
		String packageName = binding.getPackage().getName();
		String className = binding.getName();
		String fqn = binding.getQualifiedName();		
		ClazzService clazzService = new ClazzService();
		Clazz testClazz = clazzService.createOrGet(className, packageName, fqn, SourceModel.currentSourceFile(), type);
		SourceModel.stepIntoClazz(testClazz);
	}
	
	/*
	 * Load or persists a type.
	 * Returns the persisted type.
	 */
	public Clazz loadClazz(ITypeBinding binding) {
		//todo: consider using ITypeBinding.getErasure() to better accommodate special cases 
		//      like parameterized types, wildcard types, type variables, arrays, etc.
		String className = binding.getName();
		String packageName = ASTHelper.getPackageName(binding);
		String fqn = binding.getQualifiedName();
		ClazzService clazzService = new ClazzService();
		return clazzService.createOrGet(className, packageName, fqn, null, ASTHelper.getObjectType(binding));
	}
	
	/*
	 * Persist test method. 
	 */
	@Override
	public void saveTestMethod(IMethodBinding binding) {
		TestMethodService testMethodService = new TestMethodService();
		TestMethod method = testMethodService.create(binding.getName(), SourceModel.currentClazz());
		ITypeBinding[] exceptions = binding.getExceptionTypes();
		SourceModel.stepIntoTestMethod(method);
		saveXceptions(exceptions);
	}
	
	/*
	 * Persist method call.
	 */
	@Override
	public void saveMethodCall(IMethodBinding binding, List<Expression> arguments, Assertion assertion) {
		ITypeBinding declaringClass = binding.getDeclaringClass();
		Clazz clazz = loadClazz(declaringClass);
		String methodName = binding.getName();
		Clazz returnClazz = loadClazz(binding.getReturnType());
		boolean isConstructor = binding.isConstructor();
		int argumentCount = arguments.size();
		int hash = ASTHelper.hash(arguments);
		
		MethodService metthodService = new MethodService();
		TestMethod testMethod = SourceModel.currentTestMethod();
		Method method = metthodService.createOrGet(methodName, clazz, returnClazz, isConstructor, argumentCount, hash, testMethod, assertion);
		SourceModel.stepIntoInvocation(method);
	}

	/*
	 * Persist exception class.
	 */
	@Override
	public void saveXception(ITypeBinding binding) {
		Clazz clazz = loadClazz(binding);		
		XceptionService service = new XceptionService();
		TestMethod testMethod = SourceModel.currentTestMethod();
		service.createOrGet(clazz, testMethod);
	}

	/*
	 * Persist method exception classes.
	 * Returns the persisted exceptions.
	 */
	@Override
	public void saveXceptions(ITypeBinding[] bindings) {
		for (int i = 0; i < bindings.length; ++i) {
			saveXception(bindings[i]);
		}
	}

	/*
	 * Persist a reference to referenceType by (optional) name that is an attribute of (optional) declaringClass.
	 */
	@Override
	public void saveReference(String name, ITypeBinding referenceType, ITypeBinding declaringClass) {
		Clazz clazz = loadClazz(referenceType);
		Clazz declaringClazz = null;
		if (declaringClass != null) {
			declaringClazz = loadClazz(declaringClass);			
		}
		ReferenceService service = new ReferenceService();
		service.createOrGet(name, clazz, declaringClazz, SourceModel.currentTestMethod());		
	}

	/*
	 * Persist assertion.
	 */
	@Override
	public void saveAssertion(IMethodBinding binding) {
		String name = binding.getName();
		AssertionType type = AssertionType.getType(name);
		TestMethod testMethod = SourceModel.currentTestMethod();
		AssertionService service = new AssertionService();
		Assertion assertion = service.createOrGet(type, testMethod);
		SourceModel.stepIntoInvocation(assertion);
		SourceModel.stepIntoAssertion(assertion);
	}
			
}