import java.io.Console;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;

import org.jfugue.*;

class Parser {	
	Performer mPerformer = new Performer();
	String outputMode;
			
	public Parser() {
		outputMode = "numeric";
	}
	
	public void perform(String piece) {
		String notes[] = piece.split("\\s+");
		Integer optimism = 0;
		int excitement = 63;
		Integer mood = 0;
		Integer skipping = 0;
		ParserMemory ambience = new ParserMemory();
		Stack<Integer> brackets = new Stack<Integer>();
		float duration = 0.25f;
		
		String note = notes[0];
		String prev = null;
		if (isNote(note)) {
			mPerformer.enqueue(note, duration);
		}
		Integer place = 0;
				
		while (place < notes.length - 1) {
			//ambience.dump(-1,5);
			//System.out.println(mood);
			if (isNote(note)) {
				prev = note;
			}
			place += 1;
			note = notes[place];			
			
			if (note.equals("(")) {
				if (ambience.get(mood) == 0){
					skipping += 1;
					continue;
				}
				duration /= 2;
				brackets.push(place);
				continue;
			}
			else if (note.equals(")")) {
				if (skipping > 0) {
					skipping -= 1;
					continue;
				}
				Integer startPlace = brackets.pop();
				duration *= 2;
				if (ambience.get(mood) != 0) {
					place = startPlace - 1;					
				}
				continue;
			}
			
			if (skipping != 0) {
				continue;
			}
			
			if (note.equals("r")) {	
				//mPerformer.playQueue();
				mPerformer.enqueue("R", duration);

				if (optimism < 0) {
					System.out.print(":> ");
					Scanner sc = new Scanner(System.in);
					Integer x = sc.nextInt();
					ambience.set(mood, x);
				}
				else if (optimism > 0) {
					System.out.print(ambience.memory.toString() + "\n");
					if (outputMode.equals("numeric")) {
						System.out.println(ambience.get(mood));
					}
					else if (outputMode.equals("ascii")) {
						System.out.print((char)(ambience.get(mood).intValue()));
					}
				}
				continue;
			}
			
			if ((prev != null) && (mPerformer.getNoteValue(note) == mPerformer.getNoteValue(prev))) {				
				ambience.set(mood, ambience.get(mood) + optimism);
			}
			else if (prev != null) {
				boolean isHappy = mPerformer.getNoteValue(note) > mPerformer.getNoteValue(prev);
				optimism = isHappy? 1 : -1;
				mood += optimism;
			}
			
			excitement += optimism * 8;
			if ((optimism == -1) && (excitement > 79)) { excitement = 79; }
			if ((optimism == 1) && (excitement < 47)) { excitement = 47; }
			else if (excitement > 127) { excitement=127; }
			else if (excitement < 23) { excitement=23; }
			
			mPerformer.enqueue(note, duration, excitement);	
		}
		//mPerformer.outputQueueToFile();
		mPerformer.playQueue();
	}
	
	static boolean isNote(String text) {
		return (!(text.equals("(") || text.equals(")") || text.equals("r")));  
	}
	
	private class ParserMemory {
		
		private HashMap<Integer, Integer> memory;
		
		public ParserMemory() {
			 memory = new HashMap<Integer, Integer>();
		}
		
		private Integer get(Integer idx) {
			if (memory.containsKey(idx)) {
				return memory.get(idx);
			}
			else {
				return 0;
			}
		}
		
		private void set(Integer idx, Integer value) {
			memory.put(idx, value);
		}
		
		private void inc(Integer idx) {
			Integer current = get(idx);
			set(idx, current + 1);
		}
		
		private void dec(Integer idx) {
			Integer current = get(idx);
			set(idx, current - 1);
		}
		
		private void dump(Integer left, Integer right) {
			Integer i = left;
			while (i <= right) {
				System.out.print(get(i)+" ");
				i += 1;
			}
			System.out.print("\n");
		}
	}	
}