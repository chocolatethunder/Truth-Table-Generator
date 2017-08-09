/***

@author Saurabh Tomar
@version 1.00

Written for CPSC 331

This class parses a user defined string containing a properly formatted logical expression

Please note that there is no error checking.
All inputs are assumed correct and sanitized.

*/

import java.util.*;

class Parser {
	
	// Stores the initial expression entered by the user
	private String expr;
	// Very important string. This is the final result converting all the tokens strings 
	// when creating the subexpressions. 
	private String finalVal = "";
	
	// Contains the list of all the independent variables
	private LinkedList<Character> vars 						= new LinkedList<Character>();
	// Contains a list of temp subexpressions that contain tokens instead of subexpressions
	private LinkedList<String> subExprTemp 					= new LinkedList<String>();
	// This maps the token to the appropriate subexression
	private Map<String, String> subExpr 					= new LinkedHashMap<String, String>();
	// This maps the codename to the final non-token human readable subexpression
	private Map<String, String> subExprFinal 				= new LinkedHashMap<String, String>();
	// This contains the array of the independent variables, subexpressions, and their values.
	private HashMap<String, ArrayList<String>> testVars 	= new LinkedHashMap<String, ArrayList<String>>();
	// This is used to create an intermediate map of independent variables mapped to the current 
	// row of their values. For example row 2 will have {A=T,B=F} for expression with 2 independent vars.
	private HashMap<String, String> tempMap 				= new LinkedHashMap<String, String>();		
	
	// Counter for the codename
	int codenum = 1;
	
	// This is the codename used as a token in subExprTemp map
	String codename = "_";
	
	/***	
	Custom constructor which initializes this class with user's experssion and 
	calls all the appropriate function to process the expression.
	@param oper1 String containing the experession
	*/
	public Parser (String expression) {
		// Assign global access to user's subexpression
		this.expr = expression;
		
		// Filter the independent variables
		this.filterVars();
		
		// Filter all the sub experession in a temporary tokenized form
		this.filterSubExpr();
		
		// Convert all the subexpression from tokens to human readable format
		this.finalizeSubExpr();
		
		// Initilize and prefill and array containing data for all the independent variables
		this.arrayPrefill();
		
		// Create the 2D array containing the independent vars, codenmames, data. 
		this.createArray();
	}
	
	
	
	
	
	//************************************ SECTION 2 ************************************
	/*	
	Precondiiton P2: 
	The inputs are:
	this.expr: this is the string that is entered by the user.
	this.vars: an empty list
	*/
	
	/***	
	This function steps through the expression and finds the independent variable. It then 
	passes it to inVarList to check if the variable is not a duplicate and adds it to the list.
	*/	
	private void filterVars() {
		
		int step = 0;
		// Step through and find the variable.
		while (step < this.expr.length()) {			
			switch(this.expr.charAt(step)) {				
				case '(':
				case ')':
				case '*':
				case '+':
				case '-':
				break;
				default:
				// Since only variables,(,),*,+ this char must be a variable.
				// Check if this variable is not already in the list
				if (!this.inVarList(this.expr.charAt(step))) {
					// if not then add it
					this.vars.add(this.expr.charAt(step));
				}
			}		
			step++;
		}
		
	}
	
	/***	
	This function simply steps through the current list of independent variables and checks
	whether the argument is in it already or not. 
	@param element The independent variable to look for
	@return if the element is in the list or not
	*/	
	private boolean inVarList(char element) {		
		int step = 0;
		// Step through the current list of independent vars
		while (step < this.vars.size()) {
			// If found
			if (element == this.vars.get(step)) {
				return true;				
			}
			step++;
		}
		// Otherwise it is not in the list
		return false;		
	}
	
	/***	
	This function simply prints the independent variables
	*/	
	private void printVars() {
		
		// Print heading
		System.out.println("Set of independent variables:");
		
		// Initialize an interator on the list of independent variables
		Iterator<Character> varIterate = vars.iterator();
		
		// Iterate through and print them
		while (varIterate.hasNext()) {
			System.out.println(varIterate.next());
		}
		
	}
	
	/*
	Postcondition Q2:
	Output
	this.vars : The list now contains all the independent variables in the expression 
	*/	
	//********************************** END SECTION 2 **********************************
	
	
	
	
	
	
	//************************************ SECTION 3 ************************************
	/*	
	Precondiiton P3: 
	The inputs are:
	this.expr : this is the string that is entered by the user.
	this.subExprTemp : an empty list that will contain subexpression in tokenized format
	this.subExpr : an empty map that will contain the conversion from a token to the real subexpression
	this.subExprFinal : an empty map which will contain the codename to subexpression map
	*/
	
	/***	
	This function identifies and smaller subexperssions and passes them to addSubExpression to be tokenized and mapped.
	It then replaces the subExprTemp with the token where the subexpression was and continues untill all the subexpressions
	have been found.
	*/
	private void filterSubExpr() {		
		
		// String used to form the smaller subexpression
		String exprTmp = "";
		// Holds the conversion from char to string when stepping through user's experession
		String s;
		// Captures element being popped from subExprTemp.
		String elem;
		// Hold the current character from parent string
		char c;
		
		// Step backwards so the that stack points at the first word
		int step = this.expr.length()-1;
		
		while (step >= 0) {
			
			s = Character.toString(this.expr.charAt(step));
			c = this.expr.charAt(step);
			
			// Start forming the temporary substring
			this.subExprTemp.push(s);
			
			// Deal with -A
			// (-((A+B)*C)+(-C+D)) turns to 
			// (-((A+B)*C)+(_1_+D)) et al
			
			// If a - is found the next two characters are then start forming mini substring
			if (c == '-') {
				
				exprTmp += this.subExprTemp.pop();
				exprTmp += this.subExprTemp.pop();
				
				// pass it to addSubExpression which checks, adds, maps it to a token, and returns the token
				this.subExprTemp.push(addSubExpression(exprTmp));
				
				// Clear the mini substring buffer
				exprTmp = "";
				
			}
			
			// Deal with ( and )
			// (-((A+B)*C)+(-C+D)) turns to
			// (-(_2_*C)+_1_) et al
			
			// If a ( is found then start poping all the chars from subExprTemp into the mini substring buffer
			// and when a ) is found means an experssion is found. Send it addSubExpression to get the token. 
			if (c == '(') {
				
				// pop "("
				this.subExprTemp.pop();
				elem = this.subExprTemp.pop();
				
				if (step >= 0) {					
					
					// Keep poping until the matchin bracket at same depth is found
					while (!elem.equals(")")) {						
						exprTmp += elem;
						elem = this.subExprTemp.pop();						
					}			
					
					exprTmp = "(" + exprTmp + ")";
					
					// Get the token of the mini substring
					this.subExprTemp.push(addSubExpression(exprTmp));
					
					// Clear the mini substring buffer
					exprTmp = "";
				
				}
				
			}			
			step--;
			
		}
		
	}
	
	/***	
	This function matches the incoming subexpression to see if it already exists. It can detect a match between (C+D) and (D+C).
	It simply fills two lists with the expresions that needs to be compared. Then removes all the matching chars in the string 
	being compared and if there are characters left means it is not in the map. 
	@param subEx The experssion being compared.
	@reurn codename The token for the subexpression that is being compared
	*/
	private String addSubExpression(String subEx) {
		
		// Flag that is used to see if the experession is already mapped. 
		boolean inDictionary = false;
		codename = "_";
		int counter = 0;
		int subcounter = 0;
		boolean skiplast = false;
		
		// 2 Place holder lists to help compare
		LinkedList<Character> c1 = new LinkedList<Character>();
		LinkedList<Character> c2 = new LinkedList<Character>();	
		
		// Step through each key
		for (String value : this.subExpr.values()) {
			
			// DeTokenize the strings so the independent vars are in the subexpression
			// ReplaceFlags processes the string and stores it in finalVal
			this.replaceFlags(value);
			String test1 = this.finalVal;
			
			this.replaceFlags(subEx);
			String test2 = this.finalVal;
			
			// Reset the lists
			c1.clear();
			c2.clear();			
			
			// push the characters of the expression at current key into c1
			while (counter <= test1.length()-1) {
				// Skip the brackets
				if ((counter == 0 && test1.charAt(counter) == '(')) {
					counter++;
					skiplast = true;
					continue;
				} else if (skiplast == true && counter == test1.length()-1) {
					counter++;
					continue;
				} else {
					// Otherwise keep pushing
					c1.push(test1.charAt(counter));
					counter++;
				}
			}			
			counter = 0;
			skiplast = false;
			
			// push the characters of the expression being compared at current key into c2
			while (counter <= test2.length()-1) {
				// Skip the brackets
				if ((counter == 0 && test2.charAt(counter) == '(')) {
					counter++;
					skiplast = true;
					continue;
				} else if (skiplast == true && counter == test2.length()-1) {
					counter++;
					continue;
				} else {
					// Otherwise keep pushing
					c2.push(test2.charAt(counter));
					counter++;
				}
			}			
			counter = 0;
			skiplast = false;
			
			// Match and remove vars and operators in the second list
			while (counter < c1.size()) {				
				subcounter = 0;				
				while (subcounter < c2.size()) {
					// Match found
					if (c1.get(counter) == c2.get(subcounter)) {						
						c2.remove(subcounter);						
					}
					subcounter++;
				}
				counter++;
			}
			counter = 0;			
			
			// If the second list empty then this is not a new string otherwise
			// it is already in the dictionary. Fetch it's token name and return it. 
			if (c2.size() == 0) {
				inDictionary = true;
				codename = this.getKeyfromValue(this.subExpr, value).toString();
			}	
			
		}
		
		// If the inDictionary has not been tripped then this is a new value.
		if (inDictionary == false) {
			// create a new token
			codename += codenum;
			codename += "_";
			// push it in the dictionary
			this.subExpr.put(codename, subEx);			
			codenum++;
		}
		
		// return the token
		return codename;
		
	}
	
	/***	
	This function simply finds the key given a value is provided. Used to find the token name for experssions already stored in the dictionary
	@param mp : The map to search through
	@param value : the value whose key to look for
	@return : Either key if found or null if not. 
	*/
	private Object getKeyfromValue(Map mp, Object value) {
		
		for (Object key : mp.keySet()) {
			// if found return the key
			if (mp.get(key).equals(value)) {
				return key;
			}
		}
		// Otherwise not found
		return null;		
	}
	
	/***	
	This function stores the relationship between a codename to a subexpression and stores it in the subExprFinal map.
	*/
	private void finalizeSubExpr() {		
		
		String value 	= "";
		// Codename prefix
		String finalKey = "LE";
		int keyCounter 	= 1;
		
		for (String key : this.subExpr.keySet()) {
			finalKey = "LE";
			// Store it in a codename : subexpression format
			value = this.subExpr.get(key);
			this.replaceFlags(value);			
			finalKey += keyCounter;			
			this.subExprFinal.put(finalKey, this.finalVal);
			keyCounter++;
		}
		
	}
	
	/***	
	This function replaces the tokens in the subexression with the appropriate mapped subexpression.
	@param value : The tokenized subexperssion that needs to be converted to the original form
	*/
	private void replaceFlags(String value) {
		
		// Storage of the resulting string
		finalVal 		= "";		
		int counter 	= 0;
		String getKey 	= "";		
		String s 		= "";
		// Trigger to see if a token is found
		boolean has_	= false;
		
		// Search for a token in _number_ format
		while (counter <= value.length()-1) {			
			if (value.charAt(counter) == '_') {
				has_ = true;
				// Start forming the key to find the subexpression
				getKey += "_";				
				counter++;
				// Start storing the parts between the delimiters
				while (value.charAt(counter) != '_') {					
					s = Character.toString(value.charAt(counter));					
					getKey += s;					
					counter++;					
				}				
				getKey += "_";
				//finalVal += "(";
				// Get the value for this token using the key
				finalVal += this.subExpr.get(getKey);
				//finalVal += ")";
			} else {
				// Otherwise form the expression normally
				finalVal += value.charAt(counter);
			}			
			counter++;
			// reset the key for the next search
			getKey = "";
		}
		
		// If a token was found go back and recheck the string
		// Tail end recursion. Do it until there are no more tokens left. 
		if (has_ == true) {
			replaceFlags(finalVal);			
		}
		
	}
	
	/***	
	Print the subexperssions and their codenames
	*/
	private void printSubExpressions() {
		// Print heading
		System.out.println("Set of logical subexpressions and logical expression:");
		// Step through and print the subexperssions and their codenames
		for (String key : this.subExprFinal.keySet()) {
			System.out.println(key + ", " + this.subExprFinal.get(key));
		}		
	}
	
	/*
	Postcondition Q3:
	Output
	this.expr : has not been changed
	this.subExprTemp : list now contains subexpression in tokenized format
	this.subExpr : map that contains the conversion from a token to the real subexpression
	this.subExprFinal : map which contains the codename to subexpression map
	*/
	//********************************** END SECTION 3 **********************************
	
	

	
	
	
	
	//************************************ SECTION 4 ************************************	
	/*	
	Precondiiton P4: 
	The inputs are:
	this.testVars: and empty map that will contain the independent var and codename to truth table values data
	*/
	
	/***	
	This function creates a prefil of T/F values for the independent variables based on how many there are using
	2^n where n is the number of independent vars. 
	*/
	private void arrayPrefill() {
		
		// get the 2^n value
		int n = (1<<this.vars.size());
		// Depcrement it so its 0 to n-1
		n--;
		int counter = 0;
		// Becomes the key for the testVars map
		String label;
		// Becomes the T/F for the array in the value for the testVars map
		String testval;
		
		while (counter < this.vars.size()) {
			
			// Start storing the T/F for an independent variable in an array
			ArrayList<String> truthVals = new ArrayList<String>();
			
			for (int i = n; i >= 0; i--) {
				
				String s = Integer.toBinaryString(i);
				
				// Prefill 0's
				while (s.length() != this.vars.size()) {
					s = '0'+s;
				}
				
				// convert 000 to FFF or 111 to TTT etc
				if (s.charAt(counter) == '1') { 
					testval = "T"; 
				} else { 
					testval = "F"; 
				}
				
				// Add this value to the array
				truthVals.add(testval);
				
			}
			
			// Store the summation of the array under it's independent variable 
			label = Character.toString(this.vars.get(counter));			
			testVars.put(label, truthVals);				
			counter++;
			
		}
		
	}
	
	/***	
	This function creates adds to the testVars map the logical subexpressions and their evaluations to their codenames. 
	*/
	private void createArray() {	
		
		int counter = 0;
		int subcounter = 0;
		int sub2counter = 1;
		String ss;
		
		// loop through all the subexperssions stored 
		while (sub2counter <= this.subExprFinal.size()) { 
			
			// Initialized an array to store the evaluated truth values
			ArrayList<String> truthVals = new ArrayList<String>();			
			counter = 0;
			
			// Loop through all the possible states of the independent variables. 
			while (counter < (1<<this.vars.size())) {		
				
				subcounter = 0;		
				
				// Collect the current states of the independent vars onto a tempMap
				while (subcounter < this.vars.size()) {  		
					
					ss = Character.toString(this.vars.get(subcounter));					
					tempMap.put(ss, this.testVars.get(ss).get(counter));				
					subcounter++;
					
				}
				
				// Send this tempMap and the expression to be evaluated to to evalExpr and then 
				// add the evaluation to an array.
				truthVals.add(evalExpr(tempMap, this.subExprFinal.get("LE"+sub2counter)));
				counter++;
				
				// clear the map buffer for next row of truth table values. 
				tempMap.clear();

			}		
			
			// add the array to the appropriate codename representing the subexpression
			this.testVars.put(("LE"+sub2counter), truthVals);
			sub2counter++;
			
		}	
		
	}
	
	/***	
	This function evaluates the subexpression using the truth table values of the current row of independent variables
	@param mp : Map containing the current row of truth tables. i.e. A=T, B=T, C=F
	@param expression : expression that needs to be evaluated
	@return : return the result of the evaluation
	*/
	private String evalExpr(Map mp, String expression) {
		
		int counter = 0;
		
		// List used to build the string using the expression 
		LinkedList<String> vals 		= new LinkedList<String>();
		// List used to store the operators for the current evaluation
		LinkedList<String> operators 	= new LinkedList<String>();
		// List used to store all the values for the current evaluation
		LinkedList<String> values 		= new LinkedList<String>();		
		
		String s;
		String next = "";
		// Stores the temp result 
		String result = "";
		
		while (counter < expression.length()) {
			
			s = Character.toString(expression.charAt(counter));			
			
			// Store the next char
			if (counter < expression.length()-1) {
				next = Character.toString(expression.charAt(counter+1));
			}
			
			// if a - is found then the expression is evaluated and stored back into vals list that is being formed
			if (s.equals("-") && 
				!next.equals("(") && 
				!next.equals(")") && 
				!next.equals("+") && 
				!next.equals("-") && 
				!next.equals("*")) {
					
					// Store the next character into vals
					String getVal = mp.get(Character.toString(expression.charAt(counter+1))).toString();

					if (getVal.equals("F")) {
						vals.push("T");
					} else {
						vals.push("F");
					}
					
					// Push the counter forward so that next characters trace properly
					counter++;
				
				// If ) delimiter is found then start popping until ( is found. 
				} else if (s.equals(")")) {

					while (!s.equals("(")) {						
						s = vals.pop();
						
						if (!s.equals("-") && 
							!s.equals("+") && 
							!s.equals("*") && 
							!s.equals("(") && 
							!s.equals(")")) {
							
							// Pop all the values into a separate list
							values.add(s);
							
						} else if (s.equals("+") || s.equals("*")) {
							
							// Pop all the operators into a separate list
							operators.add(s);
							
						}
						
					}
					
					// If this is not the final evaluation
					if (values.size()>1) {
						
						// then keep looping through untill all expressions have been evaluated
						while (values.size() != 0) {
							
							// Store the first argument
							boolean val1 = false;
							// Store the second argument
							boolean val2 = false;
							// The result of the evaluation
							boolean bolResult = false;
							
							// This section assumes that an operator is accompanied by 2 values only
							// get the first 2 into the respective arguments.
							if (values.pop().equals("T")) {
								val1 = true;							
							} else {
								val1 = false;
							}
							
							if (values.pop().equals("T")) {
								val2 = true;							
							} else {
								val2 = false;
							}
							
							// grab the operator
							String currOp = operators.pop();
							
							// Evaluate what to do with the 2 arguments and 1 operator
							if (currOp.equals("+")) {
								bolResult = (val1 || val2);
							}
							
							if (currOp.toString().equals("*")) {
								bolResult = (val1 && val2);
							}
							
							// push the result into the 2D Array
							if (bolResult == true) {
								vals.push("T");
							} else {
								vals.push("F");
							}
							
						}
						
					} else {						
						
						while (values.size() != 0) {							
							vals.push(values.pop());							
						}
						
					}
					
					
				} else {					
					
					// Convert any independent variable in the expression to either a T/F given which row of the truth table the loop is on. 
					if (!s.equals("-") && !s.equals("+") && !s.equals("*") && !s.equals("(") && !s.equals(")")) {				
						vals.push(mp.get(s).toString());				
					} else {
						// push all non essentials to the list as well
						vals.push(s);	
					}
					
				}				
			
			counter++;
			
		}
		
		// In the final stage if there are 2 left in the vals it means a final negation needs to be evaluated. 
		if (vals.size() > 1) {
			
			// Evaulate the negation and return the result
			if (vals.pop().equals("T")) {
				return "F";
			} else {
				return "T";
			}
			
		}
		
		// Otherwise the final value of vals will sum evaluation so return that. 
		return vals.pop();
		
	}
	
	/***	
	This fucntion prints the 2D array containing the data in a human readable form
	*/
	private void printArray() {
		
		System.out.println("Truth Table: ");	
		
		// Print the table headings containing the independent variables and codenames
		for (String key : this.testVars.keySet()) {
			System.out.printf("%3s %3s", key.toString(), "");			
		}
		
		System.out.printf("\n");
		
		int counter = 0;
		
		// For each codename/expression print the data on the current truth table row
		while (counter < (1<<this.vars.size())) {
			
			for (String key : this.testVars.keySet()) {
				
				// Format it so it is center aligned
				if (key.length() == 1) {
					System.out.printf("%3s %3s", this.testVars.get(key).get(counter).toString(), "");
				} else {
					System.out.printf("%2s %4s", this.testVars.get(key).get(counter).toString(), "");
				}

			}
			System.out.printf("\n");
			counter++;
			
		}
		
	}
	
	/*
	Postcondition Q4:
	Output
	this.testVars changes and now contains the 2D data for the truth table
	*/
	//********************************** END SECTION 4 **********************************
	
	// Print the data
	public String toString() {		
		System.out.println();
		System.out.println("Input String: " + this.expr);
		System.out.println("Output:");
		this.printVars();
		this.printSubExpressions();
		this.printArray();
		return "";
	}
	
}
