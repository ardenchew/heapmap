import java.util.ArrayList;
import java.util.Iterator;

public class HashMap4<K, V> implements Map<K, V> {

	private class Entry<K, V> {
		K key;
		V value;
		Entry(K k, V v) {
			this.key = k;
			this.value = v;
		}

		@Override
		public boolean equals(Object o) {
			return (o instanceof Entry) && (this.key.equals(((Entry) o).key));
		}

		@Override
		public int hashCode() {
			return this.key.hashCode();
		}

	}

	private int hashSize = 541;
	private ArrayList<Entry<K, V>> data;
	private Entry<K, V> fake;
	private int size;
	private HashFunction hashFunction;
	private int removeSlot;

	public HashMap4() {
		this.data = new ArrayList<>();
		for (int i = 0; i < this.hashSize; i++) {
			this.data.add(null);
		}
		//this.hashFunction = UniversalHashes.power(m);
		this.fake = new Entry<K, V>(null, null);
	}

	@Override
	public int size() {
		return this.size;
	}

	@Override
	public boolean has(K k) {
		return this.find(k) != null;
	}

	/*private int abs(int i) {
		int maxk = i >> 31;
		return ((mask ^ i) - mask);
	}*/

	/*private int djbhash(K k) {
		String o = (String) k;
		int hashBuilder = 5381;
		for (int i = 0; i < o.length(); i++) {
			hashBuilder += ((hashBuilder << 5) + hashBuilder)
				+ this.hashFunction.hash((int) o.charAt(i));
		}
		return this.abs(hashBuilder);
	}*/

	private int hash(int hashNum) {
		return hashNum % this.hashSize;
	}

	private Entry<K, V> find(K k) throws IllegalArgumentException {
		if (k == null) {
			throw new IllegalArgumentException();
		}
		int slot = this.hash(k.hashCode());
		this.fake.key = k;
		boolean filled = false;
		int i = 0;
		while (filled) {
			if (this.data.get(slot) == null) {
				return null;
			} else if (this.data.get(slot).equals(this.fake)) {
				this.removeSlot = slot;
				filled = true;
				return this.data.get(slot);
			} else {
				slot = this.hash(slot + (i * i));
				i++;
			}
		}
		this.rehash();
		return this.find(k);
	}

	private Entry<K, V> findForSure(K k) throws IllegalArgumentException {
		Entry<K, V> e = this.find(k);
		if (e == null) {
			throw new IllegalArgumentException();
		}
		return e;
	}

	@Override
	public void put(K k, V v) throws IllegalArgumentException {
		Entry<K, V> e = this.findForSure(k);
		e.value = v;
	}

	@Override
	public V get(K k) throws IllegalArgumentException {
		Entry<K, V> e = this.findForSure(k);
		return e.value;
	}

	@Override
	public void insert(K k, V v) throws IllegalArgumentException {
		if (this.has(k)) {
			throw new IllegalArgumentException();
		}
		int slot = this.hash(k.hashCode());
		Entry<K, V> e = new Entry<K, V>(k, v);
		int i = 0;
		while (this.data.get(slot) != null) {
			slot = this.hash(slot + (i * i));
			i++;
		}
		this.data.set(slot, e);
		this.size++;
		this.checkLoad();
	}

	private void checkLoad() {
		double load = ((double) this.size) / ((double) this.hashSize);
		if (load > 0.45) {
			this.rehash();
		}
	}

	private void rehash() {
		ArrayList<Entry<K, V>> emptyData = new ArrayList<>();
		ArrayList<Entry<K, V>> tempData = this.data;
		this.hashSize = (this.hashSize << 1) + 1;
		for (int i = 0; i < this.hashSize; i++) {
			emptyData.add(null);
		}
		this.data = emptyData;
		this.size = 0;
		Entry<K, V> e;
		Iterator<Entry<K, V>> iter = tempData.iterator();
		while (iter.hasNext()) {
			e = iter.next();
			if (e != null) {
				this.insert(e.key, e.value);
			}
		}
	}

	@Override
	public V remove(K k) throws IllegalArgumentException {
		Entry<K, V> e = this.findForSure(k);
		this.data.set(this.removeSlot, null);
		this.size--;
		return e.value;
	}

	private class HashMapIterator implements Iterator<K> {
		int count = 0;
		ArrayList<Entry<K, V>> tempData = new ArrayList<>();
		HashMapIterator() {
			this.count = 0;
			for (int i = 0; i < HashMap4.this.hashSize; i++) {
				this.tempData.add(HashMap4.this.data.get(i));
			}
		}

		@Override
		public boolean hasNext() {
			return (this.count < this.tempData.size());
		}

		@Override
		public K next() throws IndexOutOfBoundsException {
			if (!this.hasNext()) {
				throw new IndexOutOfBoundsException();
			}
			this.count++;
			return this.tempData.get(this.count - 1).key;
		}
	}

	public Iterator<K> iterator() {
		return new HashMapIterator();
	}

	public String toString() {
		Iterator<Entry<K, V>> iter = this.data.iterator();
		Entry<K, V> n;
		String s = "{";
		while (iter.hasNext()) {
			n = iter.next();
			if (n != null) {
				s += (n.key + ":" + n.value + ", ");
			}
		}
		if (s.length() > 1) {
			s = s.substring(0, (s.length() - 2));
		}
		s += "}";
		return s;
	}
}