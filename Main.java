/***

@author Saurabh Tomar
@version 1.00

Written for CPSC 331

This program accepts a properly formatted logical expression 
from the user and displays a generated truth table of the values. 

Please note that there is no error checking.
All inputs are assumed correct and sanitized.

*/

import java.util.*;

class Main {
	
	public static void main (String[] args) {
		
		//************************************ SECTION 1 ************************************
		/*
		Precondiiton P1: 
		The inputs are:
		expression: a string of expression provided by the user		
		*/
		
		//  Initialize Scanner
		Scanner keyboard = new Scanner(System.in);
		
		// Prompt User
		System.out.print("Please enter a logical expression: ");
		String expression = keyboard.next();
		
		// Clean any extra white spaces
		expression = expression.replaceAll("\\s","");
		
		// Parse the string
		Parser parseNew = new Parser(expression);
		
		// Outout the resulting truthtable
		System.out.println(parseNew.toString());
		
		/*
		Postcondition Q1: 
		There are no outputs.  
		Inputs do not change. 
		*/		
		//********************************** END SECTION 1 **********************************
		
	}
	
	
}
