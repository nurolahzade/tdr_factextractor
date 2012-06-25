package ca.ucalgary.cpsc.ase.factextractor.writer;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import ca.ucalgary.cpsc.ase.FactManager.entity.Assertion;
import ca.ucalgary.cpsc.ase.FactManager.entity.ObjectType;
import ca.ucalgary.cpsc.ase.factextractor.visitor.SourceModel;

public abstract class TestRecorder {
	
	private static Logger logger = Logger.getLogger(TestRecorder.class);		

	public abstract void saveTestClazz(ITypeBinding binding, ObjectType type);
	
	public abstract void saveTestMethod(IMethodBinding binding);
	
	public abstract void saveMethodCall(IMethodBinding binding, List<Expression> arguments, Assertion assertion);

	public abstract void saveXception(ITypeBinding binding);

	public abstract void saveXceptions(ITypeBinding[] bindings);

	public abstract void saveReference(String name, ITypeBinding referenceType, ITypeBinding declaringClass);
	
	public abstract void saveAssertion(IMethodBinding binding);	
	
	public boolean visit(IVariableBinding binding, boolean isField) {
		if (binding != null) {
			ITypeBinding referenceType = binding.getType(); 
			ITypeBinding declaringClass = binding.getDeclaringClass();
			String referenceName = binding.getName();
			if (declaringClass != null || referenceType.isPrimitive() || referenceType.isArray()) { // if is a primitive or array, or a property of a known class
				//TODO add assertion on field access tracking
				saveReference(referenceName, referenceType, declaringClass);
				logger.debug("Reference access in test method: " + binding.getName());
			}
			else {				
				if (isField && declaringClass == null) { // it is an object field but we don't know the class it belongs to, ignore it
					logger.warn("Field declaring class binding was not resolved: " + referenceName);
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
	
	public boolean visit(IMethodBinding binding, List<Expression> arguments) {
		if (binding != null) {
			Assertion assertion = SourceModel.currentAssertion();
			if ("junit.framework.Assert".equals(binding.getDeclaringClass().getQualifiedName())) { // if this is an Assert method call
				if (assertion != null) { // nested assertions are not allowed
					logger.error("New assertion reached while assertion flag is on.");
				}
				else { // legitimate assertion
					saveAssertion(binding);
					logger.debug("Assertion in test method.");
				}
			}
			else { // this is a non-Assert method call (may or may not have an assertion on it)
				saveMethodCall(binding, arguments, assertion);
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
	
}