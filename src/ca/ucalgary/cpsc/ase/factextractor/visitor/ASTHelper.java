 package ca.ucalgary.cpsc.ase.factextractor.visitor;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;

import ca.ucalgary.cpsc.ase.FactManager.entity.ObjectType;

public class ASTHelper {
	
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
		//TODO
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
	
	/*
	 * Checks if this method is a JUnit 3.x or 4.x Assert method.
	 */
	public static boolean isJunitAssertion(IMethodBinding binding, Model model) {
		String fqn = binding.getDeclaringClass().getQualifiedName();
		String name = binding.getName();
		return (model.isJUnit3TestClass() && isJunit3Assertion(name, fqn)) ||
			(model.isJUnit4TestClass() && isJunit4Assertion(name));
	}
	
	public static boolean isJunit3Assertion(String name, String fqn) {
		return "junit.framework.Assert".equals(fqn) || "org.junit.Assert".equals(fqn)
			|| ("junit.framework.TestCase".equals(fqn) && isAssertMethod(name));		
	}
	
	public static boolean isJunit4Assertion(String name) {
		return isAssertMethod(name);
	}
	
	private static boolean isAssertMethod(String name) {
		return name.startsWith("assert") || name.startsWith("fail");
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
	
}
