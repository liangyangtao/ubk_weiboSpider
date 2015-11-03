package com.unbank.test;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class YiZhuanWang {

	public static void main(String[] args) {

		double yi = 2964.97f;
		double  a=	yi*10000.0d;
	    DecimalFormat df1 = new DecimalFormat("###"); 
	    String b =df1.format(a);
		System.out.println(b);

	}
}
