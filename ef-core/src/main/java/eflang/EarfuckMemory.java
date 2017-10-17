package eflang;
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
		public HashMap<Integer, Integer> memory;
		
		public EarfuckMemory() {
			 memory = new HashMap<Integer, Integer>();
		}
		
		public Integer get(Integer idx) {
			if (memory.containsKey(idx)) {
				return memory.get(idx);
			}
			else {
				return 0;
			}
		}
		
		public void put(Integer idx, Integer value) {
			memory.put(idx, value);
		}
		
		public void inc(Integer idx) {
			Integer current = get(idx);
			put(idx, current + 1);
		}
		
		public void dec(Integer idx) {
			Integer current = get(idx);
			put(idx, current - 1);
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
