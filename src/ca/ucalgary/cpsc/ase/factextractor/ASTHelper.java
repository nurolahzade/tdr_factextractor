 package ca.ucalgary.cpsc.ase.factextractor;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;

import ca.ucalgary.cpsc.ase.FactManager.entity.Assertion;
import ca.ucalgary.cpsc.ase.FactManager.entity.AssertionType;
import ca.ucalgary.cpsc.ase.FactManager.entity.Clazz;
import ca.ucalgary.cpsc.ase.FactManager.entity.Xception;
import ca.ucalgary.cpsc.ase.FactManager.entity.Method;
import ca.ucalgary.cpsc.ase.FactManager.entity.Reference;
import ca.ucalgary.cpsc.ase.FactManager.entity.TestMethod;
import ca.ucalgary.cpsc.ase.FactManager.entity.ObjectType;
import ca.ucalgary.cpsc.ase.FactManager.service.AssertionService;
import ca.ucalgary.cpsc.ase.FactManager.service.ClazzService;
import ca.ucalgary.cpsc.ase.FactManager.service.XceptionService;
import ca.ucalgary.cpsc.ase.FactManager.service.MethodService;
import ca.ucalgary.cpsc.ase.FactManager.service.ReferenceService;
import ca.ucalgary.cpsc.ase.FactManager.service.TestMethodService;

public class ASTHelper {
	
	private static Logger logger = Logger.getLogger(ASTHelper.class);

	/*
	 * Verifies if type is extends/implements supertype.
	 */
	public static boolean isSubTypeOf(TypeDeclaration type, String supertype) {
		return isSubClassOf(type, supertype) || implementsInterface(type, supertype);
	}
	
	/*
	 * Verifies if type is a subclass of supertype.
	 * Type may extend supertype either directly or indirectly.
	 */
	public static boolean isSubClassOf(TypeDeclaration type, String supertype) {
		Type st = type.getSuperclassType();
		if (st != null) {
			ITypeBinding binding = st.resolveBinding();
			while (binding != null) {
				if (binding.getQualifiedName().equals(supertype))
					return true;
				binding = binding.getSuperclass();				
			}
		}
		return false;
	}
	
	/*
	 * Verifies if type implements supertype.
	 * Type may implement supertype either directly or indirectly.
	 */
	public static boolean implementsInterface(TypeDeclaration type, String supertype) {
		//todo
		throw new UnsupportedOperationException();
	}
	
	/*
	 * Checks if this class is a JUnit 4.x test class.
	 * A class is a JUnit 4.x test class if it has a method marked with @Test annotation.
	 */
	public static boolean isJunit4TestClass(ITypeBinding binding) {
		IMethodBinding[] methods = binding.getDeclaredMethods();
		for (IMethodBinding method : methods) {
			if (method != null && isJunit4TestMethod(method)) {
				return true;
			}
		}
		return false;
	}
	
	/*
	 * Checks if this method is a JUnit 4.x test method.
	 * A method is a JUnit 4.x test method if it is marked with @Test annotation.
	 */
	public static boolean isJunit4TestMethod(IMethodBinding binding) {		
		for (IAnnotationBinding annotation : binding.getAnnotations()) {						
			if ("org.junit.Test".equals(annotation.getAnnotationType().getQualifiedName())) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean visit(IVariableBinding binding) {
		if (binding != null) {
			ITypeBinding fieldType = binding.getType(); 
			ITypeBinding declaringClass = binding.getDeclaringClass();
			String fieldName = binding.getName();
			if (declaringClass != null || fieldType.isPrimitive() || fieldType.isArray()) { // if is a primitive or array, or a property of a known class
				//todo add assertion on field access tracking
				ASTHelper.saveReference(fieldName, fieldType, declaringClass);
				logger.debug("Reference access in test method: " + binding.getName());
			}
			else {				
				if (declaringClass == null) { // it is an object but we don't know the class it belongs to, ignore it
					logger.warn("Reference declaring class binding was not resolved: " + fieldName);
					return false;
				}					
			}
		}
		else { // cannot resolve field access, ignore it
			logger.warn("IVariableBinding node binding was not resolved.");
			return false;
		}
		return true;
	}
	
//	public static void visit(IVariableBinding binding) {
//		String variableName = binding.getName();
//		ITypeBinding nameType = binding.getType();
//		ITypeBinding declaringClass = ((IVariableBinding)binding).getDeclaringClass();
//		if (declaringClass != null || nameType.isPrimitive() || nameType.isArray()) { // if is primitive, array, or a property of a known class
//			//todo add assertion on field access tracking
//			ASTHelper.saveReference(variableName, nameType, declaringClass);
//			logger.debug("Variable access in test method: " + variableName);											
//		}
//		else {
//			if (declaringClass == null) { // it is an object but we don't know the class it belongs to, ignore it
//				logger.warn("Variable declaring class binding was not resolved: " + variableName);
//			}				
//		}
//	}
		
	public static boolean visit(IMethodBinding binding, List<Expression> arguments) {
		if (binding != null) {
			Assertion assertion = SourceModel.currentAssertion();
			if ("junit.framework.Assert".equals(binding.getDeclaringClass().getQualifiedName())) { // if this is an Assert method call
				if (assertion != null) { // nested assertions are not allowed
					logger.error("New assertion reached while assertion flag is on.");
				}
				else { // legitimate assertion
					assertion = ASTHelper.saveAssertion(binding);
					logger.debug("Assertion in test method.");
				}
			}
			else { // this is a non-Assert method call (may or may not have an assertion on it)
				ASTHelper.saveMethodCall(binding, arguments, assertion);
				logger.debug("Method invocation in test method.");
			}				
		}
		else { // method call cannot be resolved, ignore it
			SourceModel.ignoreInvocation();
			logger.warn("MethodInvocation node binding was not resolved.");
			return false;								
		}
		return true;
	}
	
	/*
	 * Persist test class as a JUnit 3.x or JUnit 4.x test class.
	 * Test class might already have been persisted.
	 * Returns the persisted test class.
	 */
	public static Clazz saveTestClazz(ITypeBinding binding, ObjectType type) {
		String packageName = binding.getPackage().getName();
		String className = binding.getName();
		String fqn = binding.getQualifiedName();		
		ClazzService clazzService = new ClazzService();
		Clazz testClazz = clazzService.createOrGet(className, packageName, fqn, SourceModel.currentSourceFile(), type);
		SourceModel.stepIntoClazz(testClazz);
		return testClazz;
	}
	
	/*
	 * Load or persists a type.
	 * Returns the persisted type.
	 */
	public static Clazz loadClazz(ITypeBinding binding) {
		//todo: consider using ITypeBinding.getErasure() to better accommodate special cases 
		//      like parameterized types, wildcard types, type variables, arrays, etc.
		String className = binding.getName();
		String packageName = getPackageName(binding);
		String fqn = binding.getQualifiedName();
		ClazzService clazzService = new ClazzService();
		return clazzService.createOrGet(className, packageName, fqn, null, getObjectType(binding));
	}
	
	/*
	 * Determine type package name.
	 * If type is an array then its element type package name is returned.
	 */
	public static String getPackageName(ITypeBinding binding) {
		String packageName = null;
		IPackageBinding packageBinding = binding.getPackage();
		if (packageBinding != null) {
			packageName = packageBinding.getName();			
		}
		else if (binding.isArray()) {
			packageName = getPackageName(binding.getElementType());
		}
		return packageName;
	}
	
	/*
	 * Assigns ObjectType.ARRAY, PRIMITIVE, ENUM, INTERFACE, or CLASS to the given type.
	 * If it is none of the above then ObjectType.UNKONOWN is returned.
	 */
	public static ObjectType getObjectType(ITypeBinding binding) {
		if (binding.isArray())
			return ObjectType.ARRAY;
		if (binding.isPrimitive())
			return ObjectType.PRIMITIVE;
		if (binding.isEnum())
			return ObjectType.ENUM;
		if (binding.isInterface())
			return ObjectType.INTERFACE;
		if (binding.isClass())
			return ObjectType.CLASS;
		return ObjectType.UNKNOWN;
	}
	
	/*
	 * Creates a hash code based on the number and types of arguments.
	 * If method has no arguments then 0 is returned.
	 */
	public static int hash(List<Expression> arguments) {
		int hash = 0;
		for (Expression argument : arguments) {
			ITypeBinding binding = argument.resolveTypeBinding();
			if (binding != null) {
				hash += 31 * hash + binding.getQualifiedName().hashCode();
			}
		}
		return hash;
	}
	
	/*
	 * Persist test method. 
	 * Returns the persisted test method.
	 */
	public static TestMethod saveTestMethod(IMethodBinding binding) {
		TestMethodService testMethodService = new TestMethodService();
		TestMethod method = testMethodService.create(binding.getName(), SourceModel.currentClazz());
		SourceModel.stepIntoTestMethod(method);
		return method;
	}
	
	/*
	 * Persist method call.
	 * Returns the persisted method call.
	 */
	public static Method saveMethodCall(IMethodBinding binding, List<Expression> arguments, Assertion assertion) {
		ITypeBinding declaringClass = binding.getDeclaringClass();
		Clazz clazz = loadClazz(declaringClass);
		String methodName = binding.getName();
		Clazz returnClazz = ASTHelper.loadClazz(binding.getReturnType());
		boolean isConstructor = binding.isConstructor();
		int argumentCount = arguments.size();
		int hash = hash(arguments);
		
		MethodService metthodService = new MethodService();
		TestMethod testMethod = SourceModel.currentTestMethod();
		Method method = metthodService.createOrGet(methodName, clazz, returnClazz, isConstructor, argumentCount, hash, testMethod, assertion);
		SourceModel.stepIntoInvocation(method);
		return method;
	}

	/*
	 * Persist exception class.
	 * Returns the persisted exception.
	 */
	public static Xception saveXceotion(ITypeBinding binding) {
		Clazz clazz = loadClazz(binding);		
		XceptionService service = new XceptionService();
		TestMethod testMethod = SourceModel.currentTestMethod();
		Xception xception = service.createOrGet(clazz, testMethod);
		return xception;
	}

	/*
	 * Persist a reference to referenceType by (optional) name that is an attribute of (optional) declaringClass.
	 * Returns the persisted reference.
	 */
	public static Reference saveReference(String name, ITypeBinding referenceType, ITypeBinding declaringClass) {
		Clazz clazz = loadClazz(referenceType);
		Clazz declaringClazz = null;
		if (declaringClass != null) {
			declaringClazz = loadClazz(declaringClass);			
		}
		ReferenceService service = new ReferenceService();
		Reference reference = service.createOrGet(name, clazz, declaringClazz, SourceModel.currentTestMethod());		
		return reference;
	}

	/*
	 * Persist assertion.
	 * Returns the persisted assertion.
	 */
	public static Assertion saveAssertion(IMethodBinding binding) {
		String name = binding.getName();
		AssertionType type = AssertionType.getType(name);
		TestMethod testMethod = SourceModel.currentTestMethod();
		AssertionService service = new AssertionService();
		Assertion assertion = service.createOrGet(type, testMethod);
		SourceModel.stepIntoInvocation(assertion);
		SourceModel.stepIntoAssertion(assertion);
		return assertion;
	}
	
}
