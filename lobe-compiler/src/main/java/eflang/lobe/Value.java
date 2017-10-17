package eflang.lobe;

public interface Value extends Evaluable {
	public String getRef(LOBECompiler compiler);
}
