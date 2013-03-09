package ca.ucalgary.cpsc.ase.factextractor;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import ca.ucalgary.cpsc.ase.common.service.MyTestServiceRemote;
import ca.ucalgary.cpsc.ase.common.service.RepositoryFileServiceRemote;

public class ClientTransactionTest {

	public static void main(String[] args) throws NamingException {
//		RepositoryFileServiceRemote service = ServiceProxy.getRepositoryFileService();
//		for (int i = 0; i < 10; ++i) {
//			service.create("Test" + i);
//		}
		try {
			Properties prop=new Properties();
			prop.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
			prop.setProperty(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
			prop.setProperty(Context.PROVIDER_URL, "jnp://localhost:1099");			
			Context context = new InitialContext(prop);		
				
			MyTestServiceRemote service = (MyTestServiceRemote) context.lookup("ejb/MyTestService");
			System.out.println(service.getCurrentTime());
			service.doSomeDbTask();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
