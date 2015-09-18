
import lobecompiler.InvalidOperationTokenException;
import lobecompiler.InvalidParameterException;
import lobecompiler.LOBECompiler;
import lobecompiler.LOBEInstruction;
import lobecompiler.LOBEParser;
import lobecompiler.LobeCompilationException;
import earcompiler.EARException;
import ef.Parser;

class Main {
	public static void main(String args[]) throws EARException {
		LOBECompiler lobeCompiler = new LOBECompiler();
		LOBEParser lobeParser = new LOBEParser();
		String LOBESTRINGOMG = "SET b7 1\n" +
				               "SET b6 0\n" +
				               "SET b5 0\n" +
				               "SET b4 1\n" +
				               "SET b3 1\n" +
				               "SET b2 0\n" +
				               "SET b1 0\n" +
				               "SET b0 1\n" +
				               "SET temp 1\n" +
				               "SET answer 0\n" +
				               "IF (b0==1)\n" +
				               "SET answer (answer+temp)\n" +
				               "ENDIF\n" +
				               "SET temp (temp*2)\n" +
				               "IF (b1==1)\n" +
				               "SET answer (answer+temp)\n" +
				               "ENDIF\n" +
				               "SET temp (temp*2)\n" +
				               "IF (b2==1)\n" +
				               "SET answer (answer+temp)\n" +
				               "ENDIF\n" +
				               "SET temp (temp*2)\n" +
				               "IF (b3==1)\n" +
				               "SET answer (answer+temp)\n" +
				               "ENDIF\n" +
				               "SET temp (temp*2)\n" +
				               "IF (b4==1)\n" +
				               "SET answer (answer+temp)\n" +
				               "ENDIF\n" +
				               "SET temp (temp*2)\n" +
				               "IF (b5==1)\n" +
				               "SET answer (answer+temp)\n" +
				               "ENDIF\n" +
				               "SET temp (temp*2)\n" +
				               "IF (b6==1)\n" +
				               "SET answer (answer+temp)\n" +
				               "ENDIF\n" +
				               "SET temp (temp*2)\n" +
				               "IF (b7==1)\n" +
				               "SET answer (answer+temp)\n" +
				               "ENDIF\n" +			               
				               "PRINT answer\n";
				               
				
		//String predTest = "SET a 3\nIF (a<3)\nPRINT 50\nENDIF";
		//String LOBESTRINGOMG = "SET a 3\nIF (a==3)\nPRINT 17\nENDIF";
		LOBEInstruction[] instructions = null;
        try {
            instructions = lobeParser.parseAll(LOBESTRINGOMG);
        }
        catch (InvalidOperationTokenException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
		for (LOBEInstruction instruction : instructions) {
			try {
				lobeCompiler.execute(instruction);
			} catch (LobeCompilationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println(lobeCompiler.mOutput);
	}
}
