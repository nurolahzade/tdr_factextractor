package ca.ucalgary.cpsc.ase.factextractor.persist;

import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import ca.ucalgary.cpsc.ase.FactManager.entity.Assertion;
import ca.ucalgary.cpsc.ase.FactManager.entity.ObjectType;

public class QueryComposer implements TestRecorder {

	@Override
	public void saveTestClazz(ITypeBinding binding, ObjectType type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveTestMethod(IMethodBinding binding) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveMethodCall(IMethodBinding binding,
			List<Expression> arguments, Assertion assertion) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveXception(ITypeBinding binding) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveXceptions(ITypeBinding[] bindings) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveReference(String name, ITypeBinding referenceType,
			ITypeBinding declaringClass) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveAssertion(IMethodBinding binding) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean visit(IVariableBinding binding, boolean isField) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean visit(IMethodBinding binding, List<Expression> arguments) {
		// TODO Auto-generated method stub
		return false;
	}

}
