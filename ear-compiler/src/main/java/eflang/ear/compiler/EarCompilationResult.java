package eflang.ear.compiler;

import java.util.List;

public class EarCompilationResult {
    private String efCode;
    private List<Long> lineStartPositions;

    public EarCompilationResult(String efCode, List<Long> lineStartPositions) {
        this.efCode = efCode;
        this.lineStartPositions = lineStartPositions;
    }

    public String getEfCode() {
        return efCode;
    }

    public List<Long> getLineStartPositions() {
        return lineStartPositions;
    }
}
