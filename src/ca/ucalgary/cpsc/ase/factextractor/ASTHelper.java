 package ca.ucalgary.cpsc.ase.factextractor;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
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

	public static boolean isSubTypeOf(TypeDeclaration type, String supertype) {
		return isSubClassOf(type, supertype) || isSubInterfaceOf(type, supertype);
	}
	
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
	
	public static boolean isSubInterfaceOf(TypeDeclaration type, String supertype) {
		return false;
	}
	
	public static boolean isJunit4TestClass(ITypeBinding binding) {
		IMethodBinding[] methods = binding.getDeclaredMethods();
		for (IMethodBinding method : methods) {
			if (method != null && isJunit4TestMethod(method)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isJunit4TestMethod(IMethodBinding binding) {		
		for (IAnnotationBinding annotation : binding.getAnnotations()) {						
			if ("org.junit.Test".equals(annotation.getAnnotationType().getQualifiedName())) {
				return true;
			}
		}
		return false;
	}
	
	public static Clazz saveTestClazz(ITypeBinding binding, ObjectType type) {
		String packageName = binding.getPackage().getName();
		String className = binding.getName();
		String fqn = binding.getQualifiedName();		
		ClazzService clazzService = new ClazzService();
		Clazz testClazz = clazzService.createOrGet(className, packageName, fqn, SourceModel.getInstance().getSourceFile(), type);
		SourceModel.getInstance().pushTestClazz(testClazz);
		return testClazz;
	}
	
	public static Clazz loadClazz(ITypeBinding binding) {
		//todo: consider using ITypeBinding.getErasure()
		String className = binding.getName();
		String packageName = getPackageName(binding);
		String fqn = binding.getQualifiedName();
		ClazzService clazzService = new ClazzService();
		return clazzService.createOrGet(className, packageName, fqn, null, getObjectType(binding));
	}
	
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
	
	public static TestMethod saveTestMethod(IMethodBinding binding) {
		TestMethodService testMethodService = new TestMethodService();
		TestMethod method = testMethodService.create(binding.getName(), SourceModel.getInstance().peekTestClazz());
		SourceModel.getInstance().setTestMethod(method);
		SourceModel.getInstance().setTestMethod(method);
		return method;
	}
	
	public static Method saveMethodCall(IMethodBinding binding, List<Expression> arguments, Assertion assertion) {
		ITypeBinding declaringClass = binding.getDeclaringClass();
		Clazz clazz = loadClazz(declaringClass);
		String methodName = binding.getName();
		Clazz returnClazz = ASTHelper.loadClazz(binding.getReturnType());
		boolean isConstructor = binding.isConstructor();
		int argumentCount = arguments.size();
		int hash = hash(arguments);
		MethodService metthodService = new MethodService();
		TestMethod testMethod = SourceModel.getInstance().getTestMethod();
		Method method = metthodService.createOrGet(methodName, clazz, returnClazz, isConstructor, argumentCount, hash, testMethod, assertion);
		SourceModel.getInstance().pushInvocation(method);
		return method;
	}

	public static Xception saveXceotion(ITypeBinding binding) {
		Clazz clazz = loadClazz(binding);		
		XceptionService service = new XceptionService();
		TestMethod testMethod = SourceModel.getInstance().getTestMethod();
		Xception xception = service.createOrGet(clazz, testMethod);
		return xception;
	}

	public static Reference saveReference(String name, ITypeBinding referenceType, ITypeBinding declaringClass) {
		Clazz clazz = loadClazz(referenceType);
		Clazz declaringClazz = null;
		if (declaringClass != null)
			declaringClazz = loadClazz(declaringClass);
		ReferenceService service = new ReferenceService();
		Reference reference = service.createOrGet(name, clazz, declaringClazz, SourceModel.getInstance().getTestMethod());		
		return reference;
	}

	public static Assertion saveAssertion(IMethodBinding binding) {
		String name = binding.getName();
		AssertionType type = AssertionType.getType(name);
		TestMethod testMethod = SourceModel.getInstance().getTestMethod();
		AssertionService service = new AssertionService();
		Assertion assertion = service.createOrGet(type, testMethod);
		SourceModel.getInstance().pushInvocation(assertion);
		SourceModel.getInstance().setAssertion(assertion);
		return assertion;
	}

	
}
