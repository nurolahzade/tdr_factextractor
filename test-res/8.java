/*
 * Copyright (C) 2000-2004 Technical University of Ilmenau, Germany
 *                         Department of Electronic Circuits & Systems
 *
 * You may only use this file according to the terms of the license agreement included
 * within the file LICENSE which is distributed together with the software package.
 *
 * CVS Info:
 * $Author$
 * $Date$
 * $Revision$
 * $State$
 * $RCSfile$
 * 
 */
package org.evolvica.test.util;

import org.evolvica.util.BitVector;

import junit.framework.TestCase;

/** Documentation missing.
 *
 * @author Andreas Rummler
 * @since 0.6.0
 * @version $Revision$ $Date$
 */
public class BitVectorTest extends TestCase {

	public BitVectorTest( String arg0 ) {
		super( arg0 );
	}

	public static void main( String[] args ) {
		junit.textui.TestRunner.run( BitVectorTest.class );
	}

	final public void testBitVectorString() {
		BitVector vector = new BitVector( "10101010" );
		assertEquals( "10101010", vector.toString() );
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	final public void testAnd() {
		BitVector first = new BitVector( "10101010" );
		BitVector second = new BitVector( "11110000" );
		assertEquals( "10100000", first.and( second ).toString() );
		assertEquals( "10100000", second.and( first ).toString() );
	}
	
	final public void testGet() {
		BitVector vector = new BitVector( "10101010" );
		assertEquals( false, vector.get( 0 ) );
		assertEquals( true, vector.get( 1 ) );
		assertEquals( false, vector.get( 2 ) );
		assertEquals( true, vector.get( 3 ) );
		assertEquals( false, vector.get( 4 ) );
		assertEquals( true, vector.get( 5 ) );
		assertEquals( false, vector.get( 6 ) );
		assertEquals( true, vector.get( 7 ) );
	}

	final public void testOr() {
		BitVector first = new BitVector( "10101010" );
		BitVector second = new BitVector( "00001111" );
		assertEquals( "10101111", first.or( second ).toString() );
		assertEquals( "10101111", second.or( first ).toString() );
	}
	
	final public void testSetint() {
		BitVector vector = new BitVector( 8 );
		vector.set( 0 );
		assertEquals( "00000001", vector.toString() );
		vector.set( 2 );
		assertEquals( "00000101", vector.toString() );
		vector.set( 7 );
		assertEquals( "10000101", vector.toString() );
		vector = new BitVector( 40 );
		vector.set( 1 );
		vector.set( 7 );
		vector.set( 15 );
		vector.set( 32 );
		vector.set( 39 );
		assertEquals( "10000001 00000000 00000000 10000000 10000010", vector.toBlockString() );
	}

	final public void testSetintboolean() {
		BitVector vector = new BitVector( 8 );
		vector.set( 0, true );
		vector.set( 2, true );
		vector.set( 7, true );
		vector.set( 6, true );
		vector.set( 6, false );
		assertEquals( "10000101", vector.toString() );
	}
	
	final public void testShiftLeft() {
		BitVector vector = new BitVector( "101010101010101000000000000000000000000000000000" );
		vector.shiftLeft( 4 );
		assertEquals( "101010101010000000000000000000000000000000000000", vector.toString() );
		vector.shiftLeft( 6 );
		assertEquals( "101010000000000000000000000000000000000000000000", vector.toString() );
		vector = new BitVector( "101010101010101010101010101010101010101010101010" );
		vector.shiftLeft( 40 );
		assertEquals( "101010100000000000000000000000000000000000000000", vector.toString() );
	}
	
	final public void testShiftRight() {
		BitVector vector = new BitVector( "101010100000000000000000000000000000000000000000" );
		vector.shiftRight( 4 );
		assertEquals( "000010101010000000000000000000000000000000000000", vector.toString() );
		vector.shiftRight( 6 );
		assertEquals( "000000000010101010000000000000000000000000000000", vector.toString() );
		vector = new BitVector( "101010101010101010101010101010101010101010101010" );
		vector.shiftRight( 40 );
		assertEquals( "000000000000000000000000000000000000000010101010", vector.toString() );
	}
	
	final public void testToBlockString() {
		BitVector vector = new BitVector( 4 );
		assertEquals( "00000000", vector.toBlockString() );
		vector = new BitVector( 8 );
		assertEquals( "00000000", vector.toBlockString() );
		vector = new BitVector( 12 );
		assertEquals( "00000000 00000000", vector.toBlockString() );
		vector = new BitVector( 36 );
		assertEquals( "00000000 00000000 00000000 00000000 00000000", vector.toBlockString() );
	}

	final public void testToBlockStringint() {
		BitVector vector = new BitVector( 12 );
		assertEquals( "000 000 000 000", vector.toBlockString( 3 ) );
		assertEquals( "0000 0000 0000", vector.toBlockString( 4 ) );
		assertEquals( "00000000 00000000", vector.toBlockString( 8 ) );
		assertEquals( "000000000000", vector.toBlockString( 12 ) );
		assertEquals( "0000000000000000", vector.toBlockString( 16 ) );
	}
	
	final public void testToggle() {
		BitVector vector = new BitVector( "10101010" );
		vector.toggle( 0 );
		vector.toggle( 4 );
		vector.toggle( 7 );
		assertEquals( "00111011", vector.toString() );
	}
	
	final public void testToString() {
		BitVector vector = new BitVector( 4 );
		assertEquals( "0000", vector.toString() );
		vector = new BitVector( 8 );
		assertEquals( "00000000", vector.toString() );
		vector = new BitVector( 12 );
		assertEquals( "000000000000", vector.toString() );
		vector = new BitVector( 40 );
		assertEquals( "0000000000000000000000000000000000000000", vector.toString() );
	}

	final public void testXor() {
		BitVector first = new BitVector( "10101010" );
		BitVector second = new BitVector( "00001111" );
		assertEquals( "10100101", first.xor( second ).toString() );
		assertEquals( "10100101", second.xor( first ).toString() );
	}
	
}
