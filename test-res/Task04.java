//package jp.co.iret.suz.s2.validator.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

//import jp.co.iret.suz.s2.validator.type.CreditcardType;

import org.junit.Test;

public class CreditcardUtilTest {

    @Test
    public void isValid() {
    	
    	Map<String, CreditcardType> map = new HashMap<String, CreditcardType>();
    	map.put("5555444455554442", CreditcardType.MASTERCARD);
    	map.put("5555555555554444", CreditcardType.MASTERCARD);
    	map.put("378282246310005", CreditcardType.AMEX);
    	map.put("4111111111111111", CreditcardType.VISA);
    	map.put("3528000000000007", CreditcardType.JCB);
    	map.put("3528000000000015", CreditcardType.JCB);
    	map.put("3528000000000023", CreditcardType.JCB);
    	map.put("36666666666660", CreditcardType.DINERS);
    	
    	for(String number : map.keySet()) {
    		for(CreditcardType type : CreditcardType.values()) {
 
       			String message = number + " : " + type + " : ";
     			TypeValidatorUtil util = new TypeValidatorUtil();
       			boolean result = util.isCreditcardNumber(number, type);
   
     			if(type == map.get(number)) {
    				assertTrue(message + "false", result);
    			} else {
    				assertFalse(message + "true", result);    				
    			}

    		}
    	}
    	
    }
	
}
