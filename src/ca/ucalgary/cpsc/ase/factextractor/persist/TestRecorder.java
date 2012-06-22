package ca.ucalgary.cpsc.ase.factextractor.persist;

import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import ca.ucalgary.cpsc.ase.FactManager.entity.Assertion;
import ca.ucalgary.cpsc.ase.FactManager.entity.ObjectType;

public interface TestRecorder {

	public void saveTestClazz(ITypeBinding binding, ObjectType type);
	
	public void saveTestMethod(IMethodBinding binding);
	
	public void saveMethodCall(IMethodBinding binding, List<Expression> arguments, Assertion assertion);

	public void saveXception(ITypeBinding binding);

	public void saveXceptions(ITypeBinding[] bindings);

	public void saveReference(String name, ITypeBinding referenceType, ITypeBinding declaringClass);
	
	public void saveAssertion(IMethodBinding binding);	
	
	public boolean visit(IVariableBinding binding, boolean isField);
	
	public boolean visit(IMethodBinding binding, List<Expression> arguments);	
	
}
