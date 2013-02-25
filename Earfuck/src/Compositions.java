class Compositions {
	public static String test = "c4 b3 c4 r c4 d4 c4 b3 c4 r c4 d4 e4 d4 c4 b3 c4 r c4 d4 e4 f4 e4 d4 c4 b3 c4 r c4 d4 e4 f4 g4 f4 e4 d4 c4 b3 c4 r c4 d4 e4 f4 g4 c5 g4 f4 e4 d4 c4 b3 c4 r";
	public static String test2 = "c4 b3 c4 c4 c4 ( d4 c4 c4 )";
	public static String sadness_test = "d4 c4 c4 c4 c4 c4 c4 c4 c4 c4 c4 c4 c4 c4 c4 c4 c4 c4 c4 c4 c4 c4 c4 c4 c4 c4 c4 c4 c4";
	public static String eternalBliss = "c4 d4 d4 ( d4 c4 d4 r )";
	public static String multiplication = "c4 b3 r c4 d4 c4 r " +
					        "b3 ( b3 c4 " +
					        "( d4 c4 c4 d4 d4 e4 e4 d4 c4 ) " +
					        "d4 ( e4 d4 d4 c4 b3 c4 c4 d4 ) " +
					        "c4 b3 ) " +
					        "c4 e4 g4 r c5 ( have a banana )";
	public static String multiplication_jazz = "f4 eb4 r f4 g4 eb4 r " +
	        "b3 ( b3 c4 " +
	        "( f4 c4 c4 eb4 eb4 r f4 f4 d4 c4 ) " +
	        "d4 ( g4 eb4 eb4 c4 bb3 c4 c4 f4 ) " +
	        "c4 b3 ) " +
	        "g3 c4 g4 bb4 c5 r";
	public static String sum = 	"c4 e4 d4 r ( e4 f4 d4 r ) " + //Takes input until user enters 0
	        					"c4 b3 (  " + //If cell to your left not 0
	        					"c4 d4 c4 c4 " + //Subtract one
	        					"g3 ( g4 e4 c4 ) " + //Move left until you find a 0
	        					"d4 d4 " + //Move right & add one (to answer cell)
	        					"( e4 g4 c4 ) b3 a3 " + //Move right until you find a 0, then back one
	        					") c4 r"; //Else output answer
	public static String factorial = 	"c4 e4 g4 e4 c4 r " + //Takes input into first cell
	        							"( ( c4 e4 e4 g4 g4 e4 c4 ) ) " + //Duplicates it into the next two cells
	        							" e4 g4 c4 c4 " + //Moves along one and decrements
	        							"( d4 e4 ( e4 c4 d4 e4 ) e4 ( d4 c4 g4 ) a4 c5 c4 c4 ) " + //Creates decending list
	        							"d4 ( e4 g4 c4 ) " + //Move to the end of the list
	        							"g4 f4 e4 e4 d4 c4 " + //Delete the 1 at the end of the list
	        							"( ( c4 e4 g4 g4 e4 c4 ) " + 	//Duplicates the 2nd last element 
	        																//in the cell past the final element
										"d4 ( e4 d4 d4 e4 ( f4 f4 e4 e4 d4 c4 b3 c4 c4 d4 e4 ) d4 " + //Multiplication
										"( d4 e4 f4 ( g4 f4 f4 e4 d4 e4 e4 d4 c4 b3 c4 c4 d4 e4 f4 ) ) e4 d4 ) " + //Multiplication
										"e4 ( g4 e4 e4 ) f4 ( g4 f4 f4 ) c5 g4 f4 e4 d4 c4 d4 c4 ) " + //Reset temp counters and repeat
										"g4 c5 g4 c4 c6 r"; //output answer
										
	public static String salutations = "c5 b4 c5 c5 c5 c5 c5 c5 c5 c5 c5 c5 c5 " +
					                   "( ( ( ( d5 d5 d5 d5 d5 d5 d5 d5 " +
			                           "e5 e5 e5 e5 e5 e5 e5 e5 e5 e5 e5 " +
					                   "f5 f5 f5 f5 " +
			                           "g5 g5 " +
					                   "f5 e5 d5 c5 c5 ) ) ) ) " +
			                           "d5 d5 d5 r " +
					                   "e5 e5 r " +
			                           "e5 e5 e5 e5 e5 e5 e5 r " +
					                   "r " +
			                           "e5 e5 e5 r " +
					                   "f5 f5 f5 r " +
			                           "e5 d5 c5 d5   d5 d5 d5 d5 d5   d5 d5 d5 d5 d5   d5 d5 d5 d5 d5 c5 d5 r " +
					                   "e5 r " +
			                           "e5 e5 e5 r " +
					                   "f5 e5 e5 e5 e5 e5 e5 e5 d5 e5 r " +
			                           "f5 e5 e5 e5 e5 e5 e5 e5 e5 e5 c5 e5 r " +
					                   "f5 f5 r " +
			                           "g5 r c5 e5 g5 c6";
	public static String salutations_nicer = "c5 b4 c5 c5 d5 b4 c5 c5 g5 g4 c5 c5 c5 c5 c5 d5 b4 c5 c5 " +
            "( ( d5 d5 d5 d5 d5 d5 d5 d5 " +
            "e5 e5 e5 e5 e5 e5 e5 e5 e5 e5 e5 " +
            "f5 f5 f5 f5 " +
            "g5 g5 " +
            "f5 e5 d5 c5 c5 ) ) " +
            "d5 d5 d5 r " +
            "e5 e5 r " +
            "e5 e5 e5 e5 e5 e5 e5 r " +
            "r " +
            "e5 e5 e5 r " +
            "f5 f5 f5 r " +
            "e5 d5 c5 d5   d5 d5 d5 d5 d5   d5 d5 d5 d5 d5   d5 d5 d5 d5 d5 c5 d5 r " +
            "e5 r " +
            "e5 e5 e5 r " +
            "f5 e5 e5 e5 e5 e5 e5 e5 d5 e5 r " +
            "f5 e5 e5 e5 e5 e5 e5 e5 e5 e5 c5 e5 r " +
            "f5 f5 r " +
            "g5 r c5 e5 g5 c6";
	public static String ryan_fibonacci = "c4 d4 d4 r " +
                                          "( ( e4 e4 d4 d4 ) e4 " +
			                              "( f4 e4 e4 d4 c4 b3 c4 c4 d4 d4 e4 ) "+
                                          "d4 c4 b3 c4 r d4 c4 " +
			                              "( c4 d4 e4 e4 d4 c4 ) " +
                                          "d4 e4 ( f4 e4 e4 d4 c4 b3 c4 c4 d4 d4 e4 ) " +
			                              "d4 c4 d4 r )";
	public static String ryan_division = "c4 d4 c4 r d4 e4 f4 e4 r " +
			                              "d4 c4 ( ( c4 d4 d4 e4 f4 e4 e4 " +
			                              "( c5 b4 a4 g4 f4 e4 d4 ) f4 g4 a4 b4 c5 " +
			                              "d4 c4 ) " +
			                              "d4 ( ( e4 d4 d4 c4 b3 c4 c4 d4 ) " +
			                              "e4 f4 f4 e4 d4 ) c4 ) "+
			                              "c5 g4 e4 c4 b3 c4 r";
	
	public static String ryan_division_old = "c4 d4 c4 r d4 e4 f4 e4 r " +
            "d4 c4 ( ( c4 d4 d4 e4 f4 e4 e4 " +
            "( c5 b4 a4 g4 f4 e4 d4 ) f4 g4 a4 b4 c5 " +
            "d4 c4 ) " +
            "d4 ( ( e4 d4 d4 c4 b3 c4 c4 d4 ) " +
            "e4 f4 f4 e4 d4 ) c4 ) " +
            "c5 g4 e4 c4 b3 c4 r";
	
	public static String stefan_fibonacci = "c4 b3 c4 c4 r e4 e4 r c4 " +
			                                "( g4 ( c5 e4 e4 g4 g4 a4 a4 f4 c4 ) " +
			                                "e4 a4 c5 f4 " +
			                                "( f4 e4 d4 c4 d4 d4 f4 g4 c5 f4 ) " +
			                                "e4 d4 c4 " +
			                                "( c4 f4 a4 a4 f4 c4 ) d4 e4 r c4 )";
}