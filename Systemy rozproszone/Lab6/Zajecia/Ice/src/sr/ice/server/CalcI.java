package sr.ice.server;
// **********************************************************************
//
// Copyright (c) 2003-2019 ZeroC, Inc. All rights reserved.
//
// This copy of Ice is licensed to you under the terms described in the
// ICE_LICENSE file included in this distribution.
//
// **********************************************************************

import Demo.NoInput;
import com.zeroc.Ice.Current;

import Demo.A;
import Demo.Calc;

import java.util.Arrays;

public class CalcI implements Calc
{
	private static final long serialVersionUID = -2448962912780867770L;
	long counter = 0;

	@Override
	public long add(int a, int b, Current __current) 
	{
		System.out.println("ADD: a = " + a + ", b = " + b + ", result = " + (a+b) + "(" + __current.id + ")");
		
		if(a > 1000 || b > 1000) {
			try { Thread.sleep(6000); } catch(java.lang.InterruptedException ex) { } 
		}
		
		if(__current.ctx.values().size() > 0) System.out.println("There are some properties in the context");
		
		return a + b;
	}
	
	@Override
	public long subtract(int a, int b, Current __current) {
		return a - b;
	}


	@Override
	public /*synchronized*/ void op(A a1, short b1, Current current) {
		System.out.println("OP" + (++counter));
		try { Thread.sleep(500); } catch(java.lang.InterruptedException ex) { }		
	}

	@Override
	public double avg(long[] numbers, Current current) throws NoInput {
		if(numbers.length == 0)
			throw new NoInput();
		return 1.0 * Arrays.stream(numbers).sum() / numbers.length;
	}
}
