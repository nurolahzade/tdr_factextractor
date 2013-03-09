package ca.ucalgary.cpsc.ase.factextractor;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import ca.ucalgary.cpsc.ase.common.service.AssertionServiceRemote;
import ca.ucalgary.cpsc.ase.common.service.ClazzServiceRemote;
import ca.ucalgary.cpsc.ase.common.service.MethodInvocationServiceRemote;
import ca.ucalgary.cpsc.ase.common.service.MethodServiceRemote;
import ca.ucalgary.cpsc.ase.common.service.MyTestServiceRemote;
import ca.ucalgary.cpsc.ase.common.service.PositionServiceRemote;
import ca.ucalgary.cpsc.ase.common.service.ProjectServiceRemote;
import ca.ucalgary.cpsc.ase.common.service.ReferenceServiceRemote;
import ca.ucalgary.cpsc.ase.common.service.RepositoryFileServiceRemote;
import ca.ucalgary.cpsc.ase.common.service.ServiceDirectory;
import ca.ucalgary.cpsc.ase.common.service.SourceFileServiceRemote;
import ca.ucalgary.cpsc.ase.common.service.TestMethodServiceRemote;
import ca.ucalgary.cpsc.ase.common.service.XceptionServiceRemote;

public class ServiceProxy {
	
	private static Context context;
	
	private synchronized static Context getJNDIContext() throws NamingException {
		if (context == null) {
			context = init("org.jnp.interfaces.NamingContextFactory",
							"org.jboss.naming:org.jnp.interfaces",
							"jnp://localhost:1099");
		}
		return context;
	}
	
	private static Context init(String factory, String prefix, String provider) throws NamingException {
		Properties prop=new Properties();
		prop.put(Context.INITIAL_CONTEXT_FACTORY, factory);
		prop.setProperty(Context.URL_PKG_PREFIXES, prefix);
		prop.setProperty(Context.PROVIDER_URL, provider);			
		return new InitialContext(prop);		
	}
	
	private static Object lookup(String name) throws NamingException {
		return getJNDIContext().lookup(name);
	}
	
	public static AssertionServiceRemote getAssertionService() throws NamingException {
		return (AssertionServiceRemote) lookup(ServiceDirectory.ASSERTION_SERVICE);
	}
	
	public static ClazzServiceRemote getClazzService() throws NamingException {
		return (ClazzServiceRemote) lookup(ServiceDirectory.CLAZZ_SERVICE);
	}
	
	public static MethodInvocationServiceRemote getMethodInvocationService() throws NamingException {
		return (MethodInvocationServiceRemote) lookup(ServiceDirectory.METHOD_INVOCATION_SERVICE);
	}
	
	public static MethodServiceRemote getMethodService() throws NamingException {
		return (MethodServiceRemote) lookup(ServiceDirectory.METHOD_SERVICE);
	}
	
	public static PositionServiceRemote getPositionService() throws NamingException {
		return (PositionServiceRemote) lookup(ServiceDirectory.POSITION_SERVICE);
	}
	
	public static ProjectServiceRemote getProjectService() throws NamingException {
		return (ProjectServiceRemote) lookup(ServiceDirectory.PROJECT_SERVICE);
	}
	
	public static ReferenceServiceRemote getReferenceService() throws NamingException {
		return (ReferenceServiceRemote) lookup(ServiceDirectory.REFERENCE_SERVICE);
	}
	
	public static RepositoryFileServiceRemote getRepositoryFileService() throws NamingException {
		return (RepositoryFileServiceRemote) lookup(ServiceDirectory.REPOSITORY_FILE_SERVICE);
	}
	
	public static SourceFileServiceRemote getSourceFileService() throws NamingException {
		return (SourceFileServiceRemote) lookup(ServiceDirectory.SOURCE_FILE_SERVICE);
	}
	
	public static TestMethodServiceRemote getTestMethodService() throws NamingException {
		return (TestMethodServiceRemote) lookup(ServiceDirectory.TEST_METHOD_SERVICE);
	}
	
	public static XceptionServiceRemote getXceptionService() throws NamingException {
		return (XceptionServiceRemote) lookup(ServiceDirectory.XCEPTION_SERVICE);
	}

	public static MyTestServiceRemote getMyTestService() throws NamingException {
		return (MyTestServiceRemote) lookup("ejb/MyTestService");
	}	
	
}
