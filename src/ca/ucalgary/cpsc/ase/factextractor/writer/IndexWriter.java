package ca.ucalgary.cpsc.ase.factextractor.writer;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import ca.ucalgary.cpsc.ase.common.ServiceProxy;
import ca.ucalgary.cpsc.ase.common.entity.Assertion;
import ca.ucalgary.cpsc.ase.common.entity.AssertionType;
import ca.ucalgary.cpsc.ase.common.entity.Clazz;
import ca.ucalgary.cpsc.ase.common.entity.Method;
import ca.ucalgary.cpsc.ase.common.entity.MethodInvocation;
import ca.ucalgary.cpsc.ase.common.entity.ObjectType;
import ca.ucalgary.cpsc.ase.common.entity.Position;
import ca.ucalgary.cpsc.ase.common.entity.TestMethod;
import ca.ucalgary.cpsc.ase.common.service.AssertionServiceRemote;
import ca.ucalgary.cpsc.ase.common.service.ClazzServiceRemote;
import ca.ucalgary.cpsc.ase.common.service.MethodInvocationServiceRemote;
import ca.ucalgary.cpsc.ase.common.service.MethodServiceRemote;
import ca.ucalgary.cpsc.ase.common.service.PositionServiceRemote;
import ca.ucalgary.cpsc.ase.common.service.ReferenceServiceRemote;
import ca.ucalgary.cpsc.ase.common.service.ServiceWrapperRemote;
import ca.ucalgary.cpsc.ase.common.service.TestMethodServiceRemote;
import ca.ucalgary.cpsc.ase.common.service.XceptionServiceRemote;
import ca.ucalgary.cpsc.ase.factextractor.visitor.ASTHelper;
import ca.ucalgary.cpsc.ase.factextractor.visitor.SourceModel;

public class IndexWriter extends TestRecorder {
	
	protected SourceModel model;
	
	private ClazzServiceRemote clazzService;
	private TestMethodServiceRemote testMethodService;
	private MethodServiceRemote methodService;
	private MethodInvocationServiceRemote invocationService;
	private ReferenceServiceRemote referenceService;
	private XceptionServiceRemote xceptionService;
	private AssertionServiceRemote assertionService;
	private PositionServiceRemote positionService;
	
	public IndexWriter(SourceModel model) throws Exception {
		this.model = model;
		clazzService = ServiceProxy.getClazzService();
		testMethodService = ServiceProxy.getTestMethodService();
		methodService = ServiceProxy.getMethodService();
		invocationService = ServiceProxy.getMethodInvocationService();
		referenceService = ServiceProxy.getReferenceService();
		xceptionService = ServiceProxy.getXceptionService();
		assertionService = ServiceProxy.getAssertionService();
		positionService = ServiceProxy.getPositionService();
	}
	
	private static Logger logger = Logger.getLogger(IndexWriter.class);	

	/*
	 * Persist test class as a JUnit 3.x or JUnit 4.x test class.
	 * Test class might already have been persisted.
	 */
	@Override
	public void saveTestClazz(ASTNode node, ITypeBinding binding, ObjectType type) {
		String packageName = binding.getPackage().getName();
		String className = binding.getName();
		String fqn = binding.getQualifiedName();		
		Clazz testClazz = clazzService.createOrGet(className, packageName, fqn, model.currentSourceFile(), type);
		model.stepIntoClazz(testClazz);
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
		return clazzService.createOrGet(className, packageName, fqn, null, ASTHelper.getObjectType(binding));
	}
	
	/*
	 * Persist test method. 
	 */
	@Override
	public void saveTestMethod(ASTNode node, IMethodBinding binding) {
		TestMethod method = testMethodService.create(binding.getName(), model.currentClazz(), getPosition(node));
		ITypeBinding[] exceptions = binding.getExceptionTypes();
		model.stepIntoTestMethod(method);
		saveXceptions(exceptions);
	}
	
	/*
	 * Persist method call.
	 */
	@Override
	public void saveMethodCall(ASTNode node, IMethodBinding binding, List<Expression> arguments) {
		ITypeBinding declaringClass = binding.getDeclaringClass();
		Clazz clazz = loadClazz(declaringClass);
		String methodName = binding.getName();
		Clazz returnClazz = loadClazz(binding.getReturnType());
		boolean isConstructor = binding.isConstructor();
		int hash = ASTHelper.hash(arguments);
		
		TestMethod testMethod = model.currentTestMethod();
		Method method = methodService.createOrGet(methodName, clazz, returnClazz, isConstructor, 
				getMethodArguments(arguments), hash);
		MethodInvocation invocation = invocationService.create(testMethod, method, 
				null, getPosition(node));
		
		if (model.insideAnInvocation()) {
			MethodInvocation receiver = model.currentInvocation();
			invocationService.addDataFlowRelationship(invocation, receiver);
		}
		else if (model.insideAnAssignment()) {
			String to = model.currentLHS();
			model.registerDataFlow(invocation, to);
		}
		
		model.stepIntoInvocation(invocation);
	}

	/*
	 * Persist exception class.
	 */
	@Override
	public void saveXception(ITypeBinding binding) {
		Clazz clazz = loadClazz(binding);		
		TestMethod testMethod = model.currentTestMethod();
		xceptionService.createOrGet(clazz, testMethod);
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
	public void saveReference(ASTNode node, String name, ITypeBinding referenceType, ITypeBinding declaringClass) {
		Clazz clazz = loadClazz(referenceType);
		Clazz declaringClazz = null;
		if (declaringClass != null) {
			declaringClazz = loadClazz(declaringClass);			
		}
		referenceService.createOrGet(name, clazz, declaringClazz, model.currentTestMethod(), getPosition(node));
	}

	/*
	 * Persist assertion.
	 */
	@Override
	public void saveAssertion(ASTNode node, IMethodBinding binding) {
		String name = binding.getName();
		AssertionType type = AssertionType.getType(name);
		TestMethod testMethod = model.currentTestMethod();
		Assertion assertion = assertionService.createOrGet(type);
		MethodInvocation invocation = invocationService.create(testMethod, null, assertion, getPosition(node));
		model.stepIntoInvocation(invocation);
//		model.stepIntoAssertion(assertion);
	}

	@Override
	public SourceModel getModel() {
		return model;
	}
	
	protected Position getPosition(ASTNode node) {
		return positionService.create(node.getStartPosition(), node.getLength());
	}

	public List<Clazz> getMethodArguments(List<Expression> arguments) {
		List<Clazz> args = new ArrayList<Clazz>();
		for (Expression argument : arguments) {
			ITypeBinding binding = argument.resolveTypeBinding();
//			if (binding != null) {
				Clazz clazz = loadClazz(binding);
				args.add(clazz);
//			}
		}
		return args;
	}

	@Override
	protected void checkForPossibleDataFlows(String variable) {
		MethodInvocation origin = model.dataFlowsInto(variable);
		if (origin != null) {
			MethodInvocation receiver = model.currentInvocation();
			invocationService.addDataFlowRelationship(origin, receiver);			
		}
	}

}
