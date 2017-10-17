package lobecompiler;

public class LOBEInstruction {
	public LOBECommand mCommand;
	public Evaluable[] mArguments;
	
	public LOBEInstruction(LOBECommand cmd, Evaluable[] args) {
		mCommand = cmd;
		mArguments = args;
	}
}
