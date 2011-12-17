package ca.ucalgary.cpsc.ase.factextractor;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import ca.ucalgary.cpsc.ase.FactManager.entity.Assertion;
import ca.ucalgary.cpsc.ase.FactManager.entity.Clazz;
import ca.ucalgary.cpsc.ase.FactManager.entity.Invocation;
import ca.ucalgary.cpsc.ase.FactManager.entity.TestMethod;
import ca.ucalgary.cpsc.ase.FactManager.entity.ObjectType;
import ca.ucalgary.cpsc.ase.FactManager.service.TestMethodService;

public class TestVisitor extends ASTVisitor {
	
	private static Logger logger = Logger.getLogger(ASTVisitor.class);

	@Override
	public boolean visit(TypeDeclaration node) {
		try {
			if (!node.isInterface()) {
				ITypeBinding binding = node.resolveBinding();
				if (binding != null) {
					if (binding.isTopLevel()) {
						logger.debug("Class: " + binding.getQualifiedName());						
						Type superclass= node.getSuperclassType();
						if (superclass != null) {
							if (ASTHelper.isSubClassOf(node, "junit.framework.TestCase")) {
								ASTHelper.saveTestClazz(binding, ObjectType.JUNIT3);
								logger.debug("This is a junit 3 test class.");
								return super.visit(node);																	
							}
						}
						if (ASTHelper.isJunit4TestClass(binding)) {
							ASTHelper.saveTestClazz(binding, ObjectType.JUNIT4);
							logger.debug("This is a junit 4 test class.");
							return super.visit(node);
						}
						else {
							SourceModel.getInstance().pushTestClazz(null);
							logger.debug("This is not a junit test class.");
						}
					}
					else { // inner class
						SourceModel.getInstance().pushTestClazz(null);
						logger.debug("Ignoring inner class; junit does not run test methods in inner classes.");					
					}
				}					
				else { // binding not resolved
					SourceModel.getInstance().pushTestClazz(null);
					logger.warn("TypeDeclaration node binding was not resolved.");
				}
			}
			else { // is interface
				SourceModel.getInstance().pushTestClazz(null);
				logger.debug("Ignoring interface definition.");				
			}
		} catch (Throwable t) {
			logger.warn(t.getMessage());
		}
		return false;
	}
			
	@Override
	public void endVisit(TypeDeclaration node) {
		SourceModel.getInstance().popTestClazz();
		super.endVisit(node);
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		boolean isTestMethod = false;
		try {
			Clazz testClazz = SourceModel.getInstance().peekTestClazz(); 
			if (testClazz != null) {
				IMethodBinding binding = node.resolveBinding();
				if (binding != null) {
					if (testClazz.getType() == ObjectType.JUNIT3) {
						isTestMethod = true;
						TestMethod testMethod = ASTHelper.saveTestMethod(binding);
						SourceModel.getInstance().setTestMethod(testMethod);
						logger.debug("This is a junit 3 test method.");					
					}	
					else if (testClazz.getType() == ObjectType.JUNIT4) {						
						for (IAnnotationBinding annotation : binding.getAnnotations()) {						
							if ("org.junit.Test".equals(annotation.getAnnotationType().getQualifiedName())) {
								isTestMethod = true;
								TestMethod testMethod = ASTHelper.saveTestMethod(binding);
								SourceModel.getInstance().setTestMethod(testMethod);
								logger.debug("This is a junit 4 test method.");											
								for (IMemberValuePairBinding valuePair : annotation.getDeclaredMemberValuePairs()) {
									if ("expected".equals(valuePair.getName())) {
										ASTHelper.saveXceotion((ITypeBinding)valuePair.getValue());
										logger.debug("Test method expects exception.");
									}
								}
							}					
						}
					}
				}
				else {
					SourceModel.getInstance().setTestMethod(null);
					logger.warn("MethodDeclaration node binding was not resolved.");
					return false;													
				}
			}
		} catch (Throwable t) {
			logger.warn(t.getMessage());
		}
		return isTestMethod && super.visit(node);
	}		
	
	@Override
	public void endVisit(MethodDeclaration node) {
		SourceModel.getInstance().setTestMethod(null);
		super.endVisit(node);
	}

	@Override
	public boolean visit(MethodInvocation node) {
		try {
			if (SourceModel.getInstance().getTestMethod() != null) {
				IMethodBinding binding = node.resolveMethodBinding();
				if (binding != null) {
					Assertion assertion = SourceModel.getInstance().getAssertion();
					List<Expression> arguments = node.arguments();				
					if ("junit.framework.Assert".equals(binding.getDeclaringClass().getQualifiedName())) {
						if (assertion != null) {
							logger.error("New assertion reached while assertion flag is on.");
						}
						else {
							assertion = ASTHelper.saveAssertion(binding);
							logger.debug("Assertion in test method.");
						}
					}
					else {
						ASTHelper.saveMethodCall(binding, arguments, assertion);
						logger.debug("Method invocation in test method.");
					}				
				}
				else {
					SourceModel.getInstance().pushInvocation(null);
					logger.warn("MethodInvocation node binding was not resolved." + " ***" + node.getName() + "***");
					return false;								
				}				
			}
			else {
				SourceModel.getInstance().pushInvocation(null);
				logger.debug("Method invocation outside test method was ignored.");
				return false;
			}
		} catch (Throwable t) {
			logger.warn(t.getMessage());
		}
		return super.visit(node);
	}		

	@Override
	public void endVisit(MethodInvocation node) {
		Invocation invocation = SourceModel.getInstance().popInvocation();
		if (invocation instanceof Assertion)
			SourceModel.getInstance().setAssertion(null);
		super.endVisit(node);
	}

	@Override
	public boolean visit(FieldAccess node) {
		try {
			if (SourceModel.getInstance().getTestMethod() != null) {
				IVariableBinding binding = node.resolveFieldBinding();
				if (binding != null) {
					ITypeBinding fieldType = binding.getType(); 
					ITypeBinding declaringClass = binding.getDeclaringClass();
					String fieldName = binding.getName();
					if (declaringClass != null || fieldType.isPrimitive() || fieldType.isArray()) {
						//todo add assertion on field access tracking
						ASTHelper.saveReference(fieldName, fieldType, declaringClass);
						logger.debug("Field access in test method.");								
					}
					else 
						if (declaringClass == null) {
							logger.warn("FieldAccess declaring class binding was not resolved: " + fieldName);
						}					
				}
				else {
					logger.warn("FieldAccess node binding was not resolved.");
					return false;				
				}				
			}
			else {
				logger.debug("Field access outside test method was ignored.");
			}
		} catch (Throwable t) {
			logger.warn(t.getMessage());
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(QualifiedName node) {
		try {
			if (SourceModel.getInstance().getTestMethod() != null) {
				IBinding binding = node.resolveBinding();
				if (binding != null) {
					if (binding.getKind() == IBinding.VARIABLE) {
						IVariableBinding variableBinding = (IVariableBinding) binding;  
						if (variableBinding.isField()) { 
							String fieldName = binding.getName();
							ITypeBinding nameType = variableBinding.getType();
							ITypeBinding declaringClass = ((IVariableBinding)binding).getDeclaringClass();
							if (declaringClass != null || nameType.isPrimitive() || nameType.isArray()) {
								//todo add assertion on field access tracking
								ASTHelper.saveReference(fieldName, nameType, declaringClass);
								logger.debug("Qualified name access in test method.");											
							}
							else
								if (declaringClass == null) {
									logger.warn("QualifiedName declaring class binding was not resolved.");
								}
						}
					}				
				}
				else {
					logger.warn("QualifiedName node binding was not resolved.");
					return false;
				}				
			}
			else {
				logger.debug("Qualified name access outside test method was ignored.");
			}
		} catch (Throwable t) {
			logger.warn(t.getMessage());
		}
		
		return super.visit(node);
	}

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		try {
			if (SourceModel.getInstance().getTestMethod() != null) {
				IVariableBinding binding = node.resolveBinding();
				String variableName = node.getName().getFullyQualifiedName();
				if (binding != null) {
					ITypeBinding variableType = binding.getType();
					ITypeBinding declaringClass = binding.getDeclaringClass();
					ASTHelper.saveReference(variableName, variableType, declaringClass);
					logger.debug("Variable declaration in test method.");
				}
				else {
					logger.warn("VariableDeclarationFragment node binding was not resolved.");
				}				
			}
			else {
				logger.debug("Variable declaration fragment outside test method was ignored.");
			}
		} catch (Throwable t) {
			logger.warn(t.getMessage());						
		}
		return super.visit(node);
	}
		
	@Override
	public boolean visit(CatchClause node) {
		try {
			if (SourceModel.getInstance().getTestMethod() != null) {
				IVariableBinding binding = node.getException().resolveBinding();
				if (binding != null) {
					ASTHelper.saveXceotion(binding.getType());
					logger.debug("Catch clause in test method.");				
				}
				else {
					logger.warn("CatchClause node binding was not resolved.");
				}
			}
			else {
				logger.debug("Catch clause outside test method was ignored.");
			}
		} catch (Throwable t) {
			logger.warn(t.getMessage());			
		}
		return super.visit(node);
	}
		
	@Override
	public boolean visit(ThrowStatement node) {
		try {
			if (SourceModel.getInstance().getTestMethod() != null) {
				String name = node.getExpression().resolveTypeBinding().getQualifiedName();
				// todo save exception
				logger.debug("Throw: " + name);				
			}
			else {
				logger.debug("Throw statement outisde test method was ignored.");
			}
		} catch (Throwable t) {
			logger.warn(t.getMessage());
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(BooleanLiteral node) {
		try {
			boolean literal = node.booleanValue();
			logger.debug("Literal: " + literal);
		} catch (Throwable t) {
			logger.warn(t.getMessage());
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(CharacterLiteral node) {
		try {
			char literal = node.charValue();
			logger.debug("Literal: " + literal);
		} catch (Throwable t) {
			logger.warn(t.getMessage());
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(NullLiteral node) {
		try {
			logger.debug("Literal: null");
		} catch (Throwable t) {
			logger.warn(t.getMessage());
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(NumberLiteral node) {
		try {
			String literal = node.getToken();
			logger.debug("Literal: " + literal);
		} catch (Throwable t) {
			logger.warn(t.getMessage());
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(StringLiteral node) {
		try {
			String literal = node.getLiteralValue();
			logger.debug("Literal: " + literal);
		} catch (Throwable t) {
			logger.warn(t.getMessage());
		}
		return super.visit(node);
	}

	
	
}
