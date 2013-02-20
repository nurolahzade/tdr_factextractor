package ca.ucalgary.cpsc.ase.factextractor.writer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import ca.ucalgary.cpsc.ase.FactManager.entity.AssertionType;
import ca.ucalgary.cpsc.ase.FactManager.entity.ObjectType;
import ca.ucalgary.cpsc.ase.QueryManager.Query;
import ca.ucalgary.cpsc.ase.QueryManager.query.QueryAssertion;
import ca.ucalgary.cpsc.ase.QueryManager.query.QueryAssertionParameter;
import ca.ucalgary.cpsc.ase.QueryManager.query.QueryException;
import ca.ucalgary.cpsc.ase.QueryManager.query.QueryMethod;
import ca.ucalgary.cpsc.ase.QueryManager.query.QueryReference;
import ca.ucalgary.cpsc.ase.QueryManager.query.QueryTestClass;
import ca.ucalgary.cpsc.ase.QueryManager.query.QueryTestMethod;
import ca.ucalgary.cpsc.ase.factextractor.visitor.ASTHelper;
import ca.ucalgary.cpsc.ase.factextractor.visitor.QueryModel;

public class QueryWriter extends TestRecorder {
	
	protected Query query;
	protected QueryModel model;

	public QueryWriter(QueryModel model) {
		query = new Query();
		this.model = model;
	}
	
	public Query getQuery() {
		return this.query;
	}
	
	@Override
	public void saveTestClazz(ASTNode node, ITypeBinding binding, ObjectType type) {
		QueryTestClass testClass = new QueryTestClass();
		testClass.setName(binding.getName());
		testClass.setPackageName(ASTHelper.getPackageName(binding));
		testClass.setType(type);
		query.setTestClass(testClass);
		model.stepIntoClazz(testClass);
	}

	@Override
	public void saveTestMethod(ASTNode node, IMethodBinding binding) {
		QueryTestMethod testMethod = new QueryTestMethod();
		testMethod.setName(binding.getName());
		query.setTestMethod(testMethod);
		model.stepIntoTestMethod(testMethod);
	}

	@Override
	public void saveMethodCall(ASTNode node, IMethodBinding binding,
			List<Expression> arguments) {
		QueryMethod method = new QueryMethod();
		
		method.setName(binding.getName());
		method.setClazzFqn(binding.getDeclaringClass().getQualifiedName());
		method.setReturnTypeFqn(binding.getReturnType().getQualifiedName());
		method.setArguments(getMethodArguments(arguments));
		method.setHash(ASTHelper.hash(arguments));
		method.setConstructor(binding.isConstructor());
		
		query.add(method);
		model.stepIntoInvocation(method);
		
//		if (model.insideAnAssertion()) {
//			QueryAssertionParameter parameter = new QueryAssertionParameter();
//			
//			parameter.setAssertion(model.currentAssertion());
//			parameter.setMethod(method);
//			
//			query.add(parameter);
//		}
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
	public void saveReference(ASTNode node, String name, ITypeBinding referenceType,
			ITypeBinding declaringClass) {
		QueryReference reference = new QueryReference();		
		reference.setName(name);
		reference.setClazzFqn(referenceType.getQualifiedName());
		reference.setDeclaringClazzFqn(declaringClass != null ? declaringClass.getQualifiedName() : null);
		
		query.add(reference);
	}

	@Override
	public void saveAssertion(ASTNode node, IMethodBinding binding) {
		QueryAssertion assertion = new QueryAssertion();		
		assertion.setType(AssertionType.getType(binding.getName()));
		
		query.add(assertion);
//		model.stepIntoAssertion(assertion);
		model.stepIntoInvocation(assertion);
	}

	@Override
	public QueryModel getModel() {
		return model;
	}

	public List<String> getMethodArguments(List<Expression> arguments) {
		List<String> args = new ArrayList<String>();
		for (Expression argument : arguments) {
			ITypeBinding binding = argument.resolveTypeBinding();
//			if (binding != null) {
				args.add(binding.getQualifiedName());
//			}
		}
		return args;
	}
	
}
