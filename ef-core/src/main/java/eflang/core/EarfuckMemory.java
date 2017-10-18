package eflang.core;
import java.util.HashMap;

/**
 * This class represents an abstract tape memory, composed of 
 * integer-valued cells.
 * There are infinitely many cells, each corresponding to an integer index.  
 * @author Stefan Kopieczek
 *
 */
public class EarfuckMemory {

    /**
     * We store the cell data as a HashMap of indices against values.
     * We use Integers rather than ints to avoid autoboxing errors.
     */
    private HashMap<Integer, Integer> memory;

    EarfuckMemory() {
         memory = new HashMap<>();
    }

    public Integer get(Integer idx) {
        return memory.getOrDefault(idx, 0);
    }

    void put(Integer idx, Integer value) {
        memory.put(idx, value);
    }

    public void dump(Integer left, Integer right) {
        Integer i = left;
        while (i <= right) {
            System.out.print(get(i)+" ");
            i += 1;
        }
        System.out.print("\n");
    }
}
