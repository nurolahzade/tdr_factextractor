//package ar.com.teracode.formula;

import junit.framework.TestCase;

public class FormulaLexicalTest extends TestCase {

	public void testLexical01()
	{
		try
		{
			String f = "[";
			new Formula(f);
			fail();
		}
		catch(SyntacticCheckException e)
		{
			assertEquals(e.getLine(),1);
			assertEquals(e.getColumn(),1);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			fail();
		}
	}

	// public void testLexical02()
	// {
		// try
		// {
			// String f = " [";
			// new Formula(f);
			// fail();
		// }
		// catch(SyntacticCheckException e)
		// {
			// assertEquals(e.getLine(),1);
			// assertEquals(e.getColumn(),2);
		// }
		// catch (Exception e)
		// {
			// e.printStackTrace();
			// fail();
		// }
	// }

	// public void testLexical03()
	// {
		// try
		// {
			// String f = "\n\n  [";
			// new Formula(f);
			// fail();
		// }
		// catch(SyntacticCheckException e)
		// {
			// assertEquals(e.getLine(),3);
			// assertEquals(e.getColumn(),3);
		// }
		// catch (Exception e)
		// {
			// e.printStackTrace();
			// fail();
		// }
	// }

	// public void testLexical04()
	// {
		// try
		// {
			// String f = "123a";
			// new Formula(f);
			// fail();
		// }
		// catch(SyntacticCheckException e)
		// {
			// assertEquals(e.getLine(),1);
			// assertEquals(e.getColumn(),4);
		// }
		// catch (Exception e)
		// {
			// e.printStackTrace();
			// fail();
		// }
	// }

	// public void testLexical05()
	// {
		// try
		// {
			// String f = "\"literal 'a' \"";
			// new Formula(f);
		// }
		// catch (Exception e)
		// {
			// e.printStackTrace();
			// fail();
		// }
	// }
	
	// public void testLexical06()
	// {
		// try
		// {
			// String f = "'literal \"a\"'";
			// new Formula(f);
		// }
		// catch (Exception e)
		// {
			// fail();
		// }
	// }

	// public void testLexical07()
	// {
		// try
		// {
			// String f = "0123652371";
			// new Formula(f);
		// }
		// catch (Exception e)
		// {
			// e.printStackTrace();
			// fail();
		// }
	// }

	// public void testLexical08()
	// {
		// try
		// {
			// String f = "123652371.88";
			// new Formula(f);
		// }
		// catch (Exception e)
		// {
			// e.printStackTrace();
			// fail();
		// }
	// }
	
	// public void testLexical09()
	// {
		// try
		// {
			// String f = "X:Numeric;X";
			// new Formula(f);
		// }
		// catch (Exception e)
		// {
			// e.printStackTrace();
			// fail();
		// }
	// }

	// public void testLexical10()
	// {
		// try
		// {
			// String f = "X1Y:Numeric;X1Y";
			// Formula formula = new Formula(f);
		// }
		// catch (Exception e)
		// {
			// e.printStackTrace();
			// fail();
		// }
	// }
	
	// public void testLexical11()
	// {
		// try
		// {
			// String f = "_X:Bool;_X";
			// new Formula(f);
		// }
		// catch (Exception e)
		// {
			// e.printStackTrace();
			// fail();
		// }
	// }

	// public void testLexical12()
	// {
		// try
		// {
			// String f = "X_1_Y:Date;X_1_Y";
			// new Formula(f);
		// }
		// catch (Exception e)
		// {
			// e.printStackTrace();
			// fail();
		// }
	// }

	// public void testLexical13()
	// {
		// try
		// {
			// String f = "true";
			// new Formula(f);
		// }
		// catch (Exception e)
		// {
			// e.printStackTrace();
			// fail();
		// }
	// }

	// public void testLexical14()
	// {
		// try
		// {
			// String f = "false";
			// new Formula(f);
		// }
		// catch (Exception e)
		// {
			// e.printStackTrace();
			// fail();
		// }
	// }
}