package lobecompiler;

public class LOBEInstruction {
	public LOBECommand mCommand;
	public String mArgs;
	public LOBEInstruction(LOBECommand cmd, String args) {
		mCommand = cmd;
		mArgs = args;
	}
}
