/**
 * Written by Arden Chew.
 * JHED: achew4
 *
 */
import java.util.ArrayList;
import java.util.Iterator;

public class HashMap3<K, V> implements Map<K, V> {

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
            return this.key.hashCode();
        }
    }

    private int hashSize = 1024;
    private ArrayList<Entry<K, V>> data;
    private Entry<K, V> fake;
    private int size;
    private HashFunction hashFunction1;
    private HashFunction hashFunction2;
    private int numHashes = 0;

    public HashMap3() {
        this.data = new ArrayList<>();
        for (int i = 0; i < (this.hashSize << 1); i++) {
            this.data.add(null);
        }
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
        return pr;
    }

    private int abs(int i) {
        int mask = i >> 31;
        return ((mask ^ i) - mask);
    }

    private int djbhash(K k, HashFunction h) {
        String o = (String) k;
        int hashBuilder = 5381;
        for (int i = 0; i < o.length(); i++) {
            hashBuilder += ((hashBuilder << 5) + hashBuilder) + h.hash((int) o.charAt(i));
        }
        return this.abs(hashBuilder);
    }

    private int hash(int hashNum) {
        return hashNum % this.hashSize;
    }

    private HashFunction chooseHf(int i) {
        if (!((i & 1) == 0)) {
            return this.hashFunction1;
        } else {
            return this.hashFunction2;
        }
    }
/*
    private ArrayList<Entry<K, V>> chooseDs(int i) {
        if (!((i & 1) == 0)) {
            return this.data1;
        } else {
            return this.data2;
        }
    }
*/

    private Entry<K, V> find(K k) {
        int slot = this.hash(this.djbhash(k, this.hashFunction1));
        this.fake.key = k;
        this.numHashes = 1;
        HashFunction hf;
        while (this.numHashes < 10) {
            if ((this.numHashes & 1) == 0) {
                slot += this.hashSize;
            }
            if (this.data.get(slot) == null) {
                return null;
            } else if (this.data.get(slot).equals(this.fake)) {
                return this.data.get(slot);
            } else {
                this.numHashes++;
                hf = this.chooseHf(this.numHashes);
                slot = this.hash(hf.hash(slot));
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

    public void put(K k, V v) throws IllegalArgumentException {
        Entry<K, V> e = this.findForSure(k);
        e.value = v;
    }

    public V get(K k) throws IllegalArgumentException {
        Entry<K, V> e = this.findForSure(k);
        return e.value;
    }

    public Entry<K, V> insertHelper(Entry<K, V> e, ArrayList<Entry<K, V>> a, int slot) {
        Entry<K, V> temp = a.get(slot);
        a.set(slot, e);
        return temp;
    }

    public void insert(K k, V v) throws IllegalArgumentException {
        if (this.has(k)) {
            throw new IllegalArgumentException();
        }
        int checker = 1;
        int slot = this.hash(this.djbhash(k, this.hashFunction1));
        Entry<K, V> e = new Entry<K, V>(k, v);
        Entry<K, V> temp;
        HashFunction hf;
        while (checker < 10) {
            if ((checker & 1) == 0) {
                slot += this.hashSize;
            }
            temp = this.data.get(slot);
            this.data.set(slot, e);
            if (temp == null) {
                this.size++;
                this.checkLoad();
                return;
            } else {
                e = temp;
                checker++;
                hf = this.chooseHf(checker);
                slot = this.hash(hf.hash(slot));
            }
        }
        this.rehash();
        this.insert(k, v);
    }

    private void checkLoad() {
        double load = ((double) this.size)/((double) this.hashSize);
        double loadMax = 0.4;
        if (loadMax < load) {
            this.rehash();
        }
    }

    private void rehash() {
        ArrayList<Entry<K, V>> emptyData = new ArrayList<>();
        ArrayList<Entry<K, V>> tempData = this.data;
        this.hashSize <<= 1;
        for (int i = 0; i < (this.hashSize << 1); i++) {
            emptyData.add(null);
        }
        this.data = emptyData;
        this.hashFunction1 = this.getHashFunction(this.hashSize);
        this.hashFunction2 = this.getHashFunction(this.hashSize);
        Entry<K, V> e;
        Iterator<Entry<K, V>> iter = tempData.iterator();
        while (iter.hasNext()) {
            e = iter.next();
            if (e != null) {
                this.insert(e.key, e.value);
            }
        } 
    }

    private int hashHelper(K k, int hashes) {
        int slot = this.hash(this.djbhash(k, this.hashFunction1));
        hashes--;
        while (hashes > 0) {
            if (!((hashes & 1) == 0)) {
                slot = this.hash(this.hashFunction1.hash(slot));
                slot += this.hashSize;
            } else {
                slot = this.hash(this.hashFunction2.hash(slot));
            }
            hashes--;
        }
        return slot;
    }

    public V remove(K k) throws IllegalArgumentException {
        Entry<K, V> e = this.findForSure(k);
        int slot = this.hashHelper(k, this.numHashes);
        this.data.set(slot, null);
        this.size--;
        return e.value;        
    }

    private class HashMapIterator implements Iterator<K> {
        int count = 0;
        ArrayList<Entry<K, V>> tempData = new ArrayList<>();

        HashMapIterator() {
            for (int i = 0; i < (HashMap3.this.hashSize << 1); i++) {
                if (HashMap3.this.data.get(i) != null) {
                    this.tempData.add(HashMap3.this.data.get(i));
                }
            }
        }

        public boolean hasNext() {
            return (count < this.tempData.size());
        }
        public K next() throws IndexOutOfBoundsException {
            if (!this.hasNext()) {
                throw new IndexOutOfBoundsException();
            }
            this.count++;
            return this.tempData.get(count - 1).key;
        }
    }

    public Iterator<K> iterator() {
        return new HashMapIterator();
    }


}