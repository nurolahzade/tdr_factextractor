package ca.ucalgary.cpsc.ase.factextractor.visitor;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import ca.ucalgary.cpsc.ase.common.entity.ObjectType;
import ca.ucalgary.cpsc.ase.factextractor.writer.TestRecorder;

public class TestVisitor extends ASTVisitor {
	
	private static Logger logger = Logger.getLogger(ASTVisitor.class);
	
	private TestRecorder recorder;
	private Model model;
	
	public TestVisitor(TestRecorder recorder) {
		super();
		this.recorder = recorder;
		this.model = recorder.getModel();
	}
	
	public TestRecorder getRecorder() {
		return this.recorder;
	}
	
	@Override
	public boolean visit(TypeDeclaration node) {
		try {
			if (!node.isInterface()) { // an interface cannot hold test methods
				ITypeBinding binding = node.resolveBinding();
				if (binding != null) { // if it is not bound then ignore it
					if (binding.isTopLevel()) { // if it is an inner-class then ignore it
						logger.debug("Class: " + binding.getQualifiedName());						
						Type superclass= node.getSuperclassType();
						if (superclass != null) { // if it extends any class
							if (ASTHelper.isSubClassOf(node, "junit.framework.TestCase")) { // if it is a JUnit 3.x class
								recorder.saveTestClazz(node, binding, ObjectType.JUNIT3);
								logger.debug("This is a junit 3 test class.");
								return super.visit(node);																	
							}
						}
						if (ASTHelper.isJunit4TestClass(binding) || model.hasTestAnnotation()) { // if it is a JUnit 4.x class
							recorder.saveTestClazz(node, binding, ObjectType.JUNIT4);
							logger.debug("This is a junit 4 test class: " + binding.getQualifiedName());
							return super.visit(node);
						}
						else { // not a JUnit 3.x or 4.x test class, ignore it
							model.ignoreClazz();
							logger.debug("This is not a junit test class: " + binding.getQualifiedName());
						}
					}
					else { // inner class, ignore it
						model.ignoreClazz();
						logger.debug("Ignoring inner class; junit does not run test methods in inner classes: " + binding.getQualifiedName());					
					}
				}					
				else { // binding is not resolved, ignore it
					model.ignoreClazz();
					logger.warn("TypeDeclaration node binding was not resolved: " + node.getName().getFullyQualifiedName());
				}
			}
			else { // interface, ignore it
				model.ignoreClazz();
				logger.debug("Ignoring interface definition: " + node.getName().getFullyQualifiedName());				
			}
		} catch (Throwable t) {
			logger.warn(t.getMessage());
		}
		return false;
	}
			
	@Override
	public void endVisit(TypeDeclaration node) {
		model.stepOutOfClazz();
		super.endVisit(node);
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		boolean isTestMethod = false;
		try {
			if (model.insideAClass()) { // just making sure
				IMethodBinding binding = node.resolveBinding();
				if (binding != null) {
					if (model.isJUnit3TestClass()) { // if inside a JUnit 3.x test class
						//TODO - only public void methods are test methods, others are just helpers
						isTestMethod = true;
						recorder.saveTestMethod(node, binding);
						logger.debug("This is a junit 3 test method: " + binding.getName());					
					}	
					else if (model.isJUnit4TestClass()) { // if inside a JUnit 4.z test class						
//						for (IAnnotationBinding annotation : binding.getAnnotations()) {						
//							if ("org.junit.Test".equals(annotation.getAnnotationType().getQualifiedName())) { // if method is marked with @Test
								isTestMethod = true;
								recorder.saveTestMethod(node, binding);
								logger.debug("This is a junit 4 test method: " + binding.getName());											
//								for (IMemberValuePairBinding valuePair : annotation.getDeclaredMemberValuePairs()) {
//									if ("expected".equals(valuePair.getName())) { // if JUnit 4.x test method expects an exception to be thrown
//										recorder.saveXception((ITypeBinding)valuePair.getValue());
//										logger.debug("Test method expects exception: " + binding.getName());
//									}
//								}
//							}					
//						}
					}
					else {
						logger.debug("Non-test method declaration was ignored: " + binding.getName());
					}
				}
				else { // method binding cannot be resolved, ignore it
					model.ignoreTestMethod();
					logger.warn("MethodDeclaration node binding was not resolved: " + node.getName().getFullyQualifiedName());
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
		model.stepOutOfTestMethod();
		super.endVisit(node);
	}
				
	@Override
	public boolean visit(ImportDeclaration node) {
		// workaround for @Test annotation not detected by PPA
		if ("org.junit.Test".equals(node.getName().getFullyQualifiedName())) {
			model.importsJUnit4TestAnnotation();
			logger.debug("File imports org.junit.Test annotation.");
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(MethodInvocation node) {
		try {
			if (model.insideATestMethod()) { // if inside a JUnit test method
				IMethodBinding binding = node.resolveMethodBinding();
				recorder.visit(node, binding, node.arguments());
			}
			else { // method call is happening outside any test method, ignore it
				//TODO - test helper method have to be covered
				model.ignoreInvocation();
				logger.debug("Method invocation outside test method was ignored: " + node.getName().getFullyQualifiedName());
				return false;
			}
		} catch (Throwable t) {
			logger.warn(t.getMessage());
		}
		return super.visit(node);
	}		

	@Override
	public void endVisit(MethodInvocation node) {
//		if (model.insideAnAssertion()) { // turn off the assertion flag
//			model.stepOutOfAssertion();			
//		}
		model.stepOutOfInvocation();
		super.endVisit(node);
	}		

	@Override
	public boolean visit(SuperMethodInvocation node) {
		try {			
			if (model.insideATestMethod()) { // if inside a JUnit test method
				IMethodBinding binding = node.resolveMethodBinding();
				recorder.visit(node, binding, node.arguments());
			}
			else { // method call is happening outside any test method, ignore it
				//TODO - test helper method have to be covered
				model.ignoreInvocation();
				logger.debug("Super method invocation outside test method was ignored: " + node.getName().getFullyQualifiedName());
				return false;
			}
		} catch (Throwable t) {
			logger.warn(t.getMessage());
		}
		return super.visit(node);
	}

	@Override
	public void endVisit(SuperMethodInvocation node) {
		model.stepOutOfInvocation();
		super.endVisit(node);
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		try {
			if (model.insideATestMethod()) { // if inside a JUnit test method
				IMethodBinding binding = node.resolveConstructorBinding();
				if (binding != null) {
					List<Expression> arguments = node.arguments();				
					recorder.saveMethodCall(node, binding, arguments);
					logger.debug("Constructor invocation in test method: " + binding.getName());
				}
				else { // method call cannot be resolved, ignore it
					model.ignoreInvocation();
					logger.warn("ConstructorInvocation node binding was not resolved.");
					return false;								
				}				
			}
			else { // constructor call is happening outside any test method, ignore it
				//TODO - test helper methods and attribute initializations have to be covered
				model.ignoreInvocation();
				logger.debug("Constructor invocation outside test method was ignored.");
				return false;
			}			
		} catch (Throwable t) {
			logger.warn(t.getMessage());
		}
		return super.visit(node);
	}

	@Override
	public void endVisit(ClassInstanceCreation node) {
		model.stepOutOfInvocation();
		super.endVisit(node);
	}

	@Override
	public boolean visit(FieldAccess node) {
		try {
			if (model.insideATestMethod()) { // if inside a test method
				IVariableBinding binding = node.resolveFieldBinding();
				return recorder.visit(node, binding, true);
			}
			else { // field was accessed outside a test method, ignore it
				//TODO - if it is used for initializing a field that is later used in a test method, then we should not ignore it
				//TODO - test helper methods have to be covered
				logger.debug("Field access outside test method was ignored: " + node.getName().getFullyQualifiedName());
			}
		} catch (Throwable t) {
			logger.warn(t.getMessage());
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(SuperFieldAccess node) {
		try {
			if (model.insideATestMethod()) { // if inside a test method
				IVariableBinding binding = node.resolveFieldBinding();
				return recorder.visit(node, binding, true);
			}
			else { // field was accessed outside a test method, ignore it
				//TODO - if it is used for initializing a field that is later used in a test method, then we should not ignore it
				//TODO - test helper methods have to be covered
				logger.debug("Field access outside test method was ignored: " + node.getName().getFullyQualifiedName());
			}
		} catch (Throwable t) {
			logger.warn(t.getMessage());
		}
		return super.visit(node);
	}	
	
	@Override
	public boolean visit(SimpleName node) {
		try {
			if (model.insideATestMethod()) { // if inside a test method
				IBinding binding = node.resolveBinding();
				if (binding != null) {
					if (binding.getKind() == IBinding.VARIABLE) { // if it is a local variable 
						IVariableBinding variableBinding = (IVariableBinding) binding;
						recorder.visit(node, variableBinding, false);
					}				
				}
				else { // cannot resolve simple name, ignore it
					logger.warn("SimpleName node binding was not resolved: " + node.getFullyQualifiedName());
					return false;
				}				
			}
			else { // simple name was accessed outside a test method, ignore it
				//TODO - if it is used for initializing a field that is later used in a test method, then we should not ignore it
				//TODO - test helper methods have to be covered
				logger.debug("Variable access outside test method was ignored: " + node.getFullyQualifiedName());
			}
		} catch (Throwable t) {
			logger.warn(t.getMessage());
		}
		
		return super.visit(node);
	}

	@Override
	public boolean visit(QualifiedName node) {
		try {
			if (model.insideATestMethod()) { // if inside a test method
				IBinding binding = node.resolveBinding();
				if (binding != null) {
					if (binding.getKind() == IBinding.VARIABLE) { // if it is a field or local variable 
						IVariableBinding variableBinding = (IVariableBinding) binding;
						recorder.visit(node, variableBinding, false);
					}				
				}
				else { // cannot resolve qualified name, ignore it
					logger.warn("QualifiedName node binding was not resolved: " + node.getName().getFullyQualifiedName());
					return false;
				}				
			}
			else { // qualified name was accessed outside a test method, ignore it
				//TODO - if it is used for initializing a field that is later used in a test method, then we should not ignore it
				//TODO - test helper methods have to be covered
				logger.debug("Qualified name access outside test method was ignored: " + node.getName().getFullyQualifiedName());
			}
		} catch (Throwable t) {
			logger.warn(t.getMessage());
		}
		
		return super.visit(node);
	}

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		try {
			if (model.insideATestMethod()) { // if inside a test method
				IVariableBinding binding = node.resolveBinding();
				String variableName = node.getName().getFullyQualifiedName();
				if (binding != null) {
					ITypeBinding variableType = binding.getType();
					ITypeBinding declaringClass = binding.getDeclaringClass();
					recorder.saveReference(node, variableName, variableType, declaringClass);
					logger.debug("Variable declaration in test method: " + binding.getName());
				}
				else { // cannot resolve variable declaration, ignore it
					logger.warn("VariableDeclarationFragment node binding was not resolved: " + node.getName().getFullyQualifiedName());
				}				
			}
			else { // variable declaration is happening outside a test method, ignore it
				//TODO - if this field is used in the test cases and is initialized here, then we should not ignore it
				//TODO - test helper methods have to be covered
				logger.debug("Variable declaration fragment outside test method was ignored: " + node.getName().getFullyQualifiedName());
			}
		} catch (Throwable t) {
			logger.warn(t.getMessage());						
		}
		return super.visit(node);
	}
		
	@Override
	public boolean visit(CatchClause node) {
		try {
			if (model.insideATestMethod()) { // if inside a test method
				IVariableBinding binding = node.getException().resolveBinding();
				if (binding != null) {
					recorder.saveXception(binding.getType());
					logger.debug("Catch clause in test method.");				
				}
				else { // cannot resolve exception, ignore it
					logger.warn("CatchClause node binding was not resolved.");
				}
			}
			else { // catch clause is outside a test method, ignore it
				//TODO - test helper methods have to be covered 
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
			if (model.insideATestMethod()) { // if inside a test method
				 ITypeBinding binding = node.getExpression().resolveTypeBinding();
				if (binding != null) {
					recorder.saveXception(binding);
					logger.debug("Throw statement in test method");				
				}
				else { // cannot resolve exception type, ignore it
					logger.warn("ThrowStatement node binding was not resolved.");
				}
			}
			else { // throw statement is outside a test method, ignore it
				//TODO - test helper methods have to be covered
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
			//TODO - save boolean literal (for the sake of importing into the Solr index)
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
			//TODO - save character literal (for the sake of importing into the Solr index)
			logger.debug("Literal: " + literal);
		} catch (Throwable t) {
			logger.warn(t.getMessage());
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(NullLiteral node) {
		try {
			//TODO - save NULL literal (for the sake of importing into the Solr index)
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
			//TODO - save numeric literal (for the sake of importing into the Solr index)
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
			//TODO - save string literal (for the sake of importing into the Solr index)
			logger.debug("Literal: " + literal);
		} catch (Throwable t) {
			logger.warn(t.getMessage());
		}
		return super.visit(node);
	}

	
	
}
