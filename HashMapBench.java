import com.github.phf.jb.Bench;
import com.github.phf.jb.Bee;

public final class HashMapBench {
	private static final int SIZE = 1000;

	private HashMapBench() {}

	private static String[] createList() {
		String[] insertList = new String[SIZE];
		for (int i = 0; i < SIZE; i++) {
			insertList[i] = Integer.toString(i);
		}
		return insertList;
	}

	private static void insert(Map<String, Integer> m, Bee b) {
		b.stop();
		String[] insertList = createList();
		b.start();
		for (int i = 0; i < SIZE; i++) {
			m.insert(insertList[i], i);
		}
	}

	private static void remove(Map<String, Integer> m, Bee b) {
		b.stop();
		String[] insertList = createList();
		insert(m, b);
		b.start();
		for (int i = 0; i < SIZE; i++) {
			b.stop();
			if (m.has(insertList[i])) {
				b.start();
				m.remove(insertList[i]);
			}
		}
	}

	private static void lookup(Map<String, Integer> m, Bee b) {
		b.stop();
		String[] insertList = createList();
		insert(m, b);
		b.start();
		for (int i = 0; i < SIZE; i++) {
			m.has(insertList[i]);
		}
	}

	@Bench
	public static void insertHash(Bee b) {
		for (int n = 0; n < b.reps(); n++) {
			b.stop();
			Map<String, Integer> m = new HashMap<>();
			b.start();
			insert(m, b);
		}
	}

	@Bench
	public static void removeHash(Bee b) {
		for (int n = 0; n < b.reps(); n++) {
			b.stop();
			Map<String, Integer> m = new HashMap<>();
			b.start();
			remove(m, b);
		}
	}

	@Bench
	public static void lookupHash(Bee b) {
		for (int n = 0; n < b.reps(); n++) {
			b.stop();
			Map<String, Integer> m = new HashMap<>();
			b.start();
			lookup(m, b);
		}
	}

}