package ca.ucalgary.cpsc.ase.factextractor.writer;

import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import ca.ucalgary.cpsc.ase.FactManager.entity.Assertion;
import ca.ucalgary.cpsc.ase.FactManager.entity.AssertionType;
import ca.ucalgary.cpsc.ase.FactManager.entity.ObjectType;
import ca.ucalgary.cpsc.ase.QueryManager.Query;
import ca.ucalgary.cpsc.ase.QueryManager.query.QueryAssertion;
import ca.ucalgary.cpsc.ase.QueryManager.query.QueryException;
import ca.ucalgary.cpsc.ase.QueryManager.query.QueryMethod;
import ca.ucalgary.cpsc.ase.QueryManager.query.QueryReference;
import ca.ucalgary.cpsc.ase.factextractor.visitor.ASTHelper;

public class QueryWriter extends TestRecorder {
	
	protected Query query;

	public QueryWriter() {
		query = new Query();
	}
	
	public Query getQuery() {
		return this.query;
	}
	
	@Override
	public void saveTestClazz(ITypeBinding binding, ObjectType type) {
		query.setClassName(binding.getName());		
	}

	@Override
	public void saveTestMethod(IMethodBinding binding) {
		query.setMethodName(binding.getName());
	}

	@Override
	public void saveMethodCall(IMethodBinding binding,
			List<Expression> arguments, Assertion assertion) {
		QueryMethod method = new QueryMethod();
		
		method.setName(binding.getName());
		method.setClazzFqn(binding.getDeclaringClass().getQualifiedName());
		method.setReturnTypeFqn(binding.getReturnType().getQualifiedName());
		method.setArguments(arguments.size());
		method.setHash(ASTHelper.hash(arguments));
		method.setConstructor(binding.isConstructor());
		
		query.add(method);
	}

	@Override
	public void saveXception(ITypeBinding binding) {
		QueryException exception = new QueryException();
		
		exception.setClazzFqn(binding.getQualifiedName());
		
		query.add(exception);
	}

	@Override
	public void saveXceptions(ITypeBinding[] bindings) {
		for (ITypeBinding binding : bindings) {
			saveXception(binding);
		}
	}

	@Override
	public void saveReference(String name, ITypeBinding referenceType,
			ITypeBinding declaringClass) {
		QueryReference reference = new QueryReference();
		
		reference.setName(name);
		reference.setClazzFqn(referenceType.getQualifiedName());
		reference.setDeclaringClazzFqn(declaringClass != null ? declaringClass.getQualifiedName() : null);
		
		query.add(reference);
	}

	@Override
	public void saveAssertion(IMethodBinding binding) {
		QueryAssertion assertion = new QueryAssertion();
		
		assertion.setType(AssertionType.getType(binding.getName()));
		
		query.add(assertion);
	}
	
}