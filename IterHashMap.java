/**
 * Written by Arden Chew.
 * JHED: achew4
 */
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Iterator;

public class IterHashMap<K, V> implements Map<K, V> {

	private class Entry<K, V> {
		K key;
		V value;

		Entry(K k, V v) {
			this.key = k;
			this.value = v;
		}
		public boolean equals(Object o) {
			return (o instanceof Entry) && (this.key.equals(((Entry) o).key));
		}
		public int hashCode() {
		}
	}

	private int hashSize = 1024;
	private ArrayList<Entry<K, V>> data1;
	private ArrayList<Entry<K, V>> data2;
	private Entry<K, V> fake;
	private int size;
	private HashFunction hashFunction1;
	private HashFunction hashFunction2;
	//private int size1;
	//private int size2;

	public IterHashMap() {
		this.data1 = new ArrayList<Entry<K,V>>();
		this.data2 = new ArrayList<Entry<K,V>>();
		this.hashFunction1 = this.getHashFunction(this.hashSize);
		this.hashFunction2 = this.getHashFunction(this.hashSize);
		this.fake = new Entry<K, V>(null, null);
	}


	public int size() {
		return this.size;
	}

	public boolean has(K k) {
		return this.find(k) != null;
	}

	//power generated based on hash map size m, must be power of 2
	private HashFunction getHashFunction(int m) {
		HashFunction pr = UniversalHashes.power(m);
	}

	private int abs(int i) {
		if (i < 0) {
			return -i;
		} else {
			return i;
		}
	}

	private int djbhash(K k, HashFunction h) {
		String o = (String) k;
		int hashBuilder = 5381;
		for (int i = 0; i < o.length(); i++) {
			hashBuilder += ((hashBuilder << 5) + hashBuilder) + h.hash((int) o.charAt(i));
		}
		return this.abs(hashBuilder);
	}

//	private int addHash(int hashNum, HashFunction h) {
//		return h.hash(hashNum);
//	}


//	private int hashSize() {
//		return this.hashSize;
//	}

	private int hash(int hashNum) {
		return hashNum % this.hashSize;
	}



	private Entry<K, V> find(K k) {
		int hashNum = djbhash(k, this.hashFunction1);
		this.fake.key = k;
		int checker = 1;
		int slot;
		while (checker < 10) {
			slot = this.hash(hashNum);
			if ((checker % 2) == 1) {
				if (this.data1.get(slot).equals(this.fake)) {
					return this.data1.get(slot);
				} else if (this.data1.get(slot).key == null) {
					return null;
				} else {
					checker++;
					hashNum = this.hashFunction2.hash(hashNum);
				}
			} else {
				if (this.data2.get(slot).equals(this.fake)) {
					return this.data2.get(slot);
				} else if (this.data2.get(slot).key == null) {
					return null;
				} else {
					checker++;
					hashNum = this.hashFunction1.hash(hashNum);
				}
			}
		}
		this.rehash();  //CHECKNAME
		return this.find(k);
	}


	private void rehash() {
		this.hashSize <<= 1;
		HashMapIterator iter = this.iterator();
		ArrayList<Entry<K, V>> tempData1 = new ArrayList<>();
		ArrayList<Entry<K, V>> tempData2 = new ArrayList<>();
		this.HashFunction1 = this.getHashFunction(this.hashSize);
		this.HashFunction2 = this.getHashFunction(this.hashSize);
		while (iter.hasNext()) {
			this.insert(iter.Next().key, iter.Next().value);
		}

	}

	private class HashMapIterator implements Iterator<K> {
		private ArrayList<Entry<K, V>> data = new ArrayList<>();
		data = IterHashMap.data1;
		for (Entry<K, V> i : IterHashMap.data2) {
			data.add(i);
		}
		private Iterator<ArrayList<Entry<K, V>>> iter = data.iterator();
		public boolean hasNext() {
			return data.hasNext();
		}
		public Entry<K, V> next() {
			return data.next();
		}
	}

	public Iterator<K> iterator() {
		return new HashMapIterator();
	}




//	public int size1() {
//		return this.size1;
//	}
//
//	public int size2() {
//		return this.size2;
//	}



}