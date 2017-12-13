/**
 * Written by Arden Chew.
 * JHED: achew4
 */
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A cuckoo single table implementation of HashMap.
 * More generally an implementation of a map.
 *
 * @param <K> element type of key.
 * @param <V> element type of value.
 */
public class HashMap<K, V> implements Map<K, V> {


    /**
     * An Entry object class for the values of our Hashmap.
     *
     * @param <K> element type of key.
     * @param <V> element type of value.
     */
    private class Entry<K, V> {

        /** The key */
        K key;
        /** The value for the key */
        V value;

        /** The constructor for our entry object. */
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

    // The initial size of the hash table
    private int hashSize = 512;
    // The hash table data holder
    private ArrayList<Entry<K, V>> data;
    // An empty Entry object for comparisons.
    private Entry<K, V> fake;
    // The number of objects added.
    private int size;
    // The first tables randomly generated hf.
    private HashFunction hashFunction1;
    // The second tables randomly generated hf.
    private HashFunction hashFunction2;
    // Int for trackign removals. 
    private int removeSlot;


    /** Constructor for HashMap. */
    public HashMap() {
        this.data = new ArrayList<>();
        for (int i = 0; i < (this.hashSize << 1); i++) {
            this.data.add(null);
        }
        this.hashFunction1 = this.getHashFunction(this.hashSize);
        this.hashFunction2 = this.getHashFunction(this.hashSize);
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

    /**
     * Generates a power based hash function based on hashmap size.
     * Uses given UniversalHashes class.
     *
     * @param int m size of the hashmap.
     * @param HashFunction the power hashfunction.
     */
    private HashFunction getHashFunction(int m) {
        HashFunction pr = UniversalHashes.power(m);
        return pr;
    }

    /**
     * Returns an absolute value of a given number.
     * Uses bit shifting for efficiency.
     * For making positive hashes.
     *
     * @param int i a positive or negative int.
     * @return int a positive int.
     */
    private int abs(int i) {
        int mask = i >> 31;
        return ((mask ^ i) - mask);
    }

    /**
     * Returns the hashcode for a String using djb style.
     * Changed to use bitshifting for speed.
     * Type casts k to a string.
     *
     * @param K k the input object.
     * @param H h the given hash function to use.
     * @return int the returned hashcode.
     */
    private int djbhash(K k, HashFunction h) {
        String o = (String) k;
        int hashBuilder = 5381;
        for (int i = 0; i < o.length(); i++) {
            hashBuilder += ((hashBuilder << 5) + hashBuilder)
                + h.hash((int) o.charAt(i));
        }
        return this.abs(hashBuilder);
    }

    /**
     * A slotting helper method for hashcode.
     *
     * @param int hashNum the hashcode.
     * @param int the corresponding array spot to use.
     */
    private int hash(int hashNum) {
        return hashNum % this.hashSize;
    }

    /**
     * A helper method for cuckoo to choose which hf to use.
     *
     * @param int i a counter that is odd or even.
     * @param HashFunction the respective hf to use.
     */
    private HashFunction chooseHf(int i) {
        if (!((i & 1) == 0)) {
            return this.hashFunction1;
        } else {
            return this.hashFunction2;
        }
    }

    /**
     * A method to get an entered object given the key.
     * Returns a null object if the object is not found.
     * The key may not be null.
     *
     * @param K k is the key of the node of interest.
     * @return Entry the entered object found.
     * @throws IllegalArgumentException if key is null.
     */
    private Entry<K, V> find(K k) throws IllegalArgumentException {
        if (k == null) {
            throw new IllegalArgumentException();
        }
        int slot = this.hash(this.djbhash(k, this.hashFunction1));
        this.fake.key = k;
        int numHashes = 1;
        HashFunction hf;
        while (numHashes < 10) {
            if ((numHashes & 1) == 0) {
                slot += this.hashSize;
            }
            if (this.data.get(slot) == null) {
                return null;
            } else if (this.data.get(slot).equals(this.fake)) {
                this.removeSlot = slot;
                return this.data.get(slot);
            } else {
                numHashes++;
                hf = this.chooseHf(numHashes);
                slot = this.hash(hf.hash(slot));
            }
        }
        this.rehash();
        return this.find(k);
    }

    /**
     * A helper method for find that does not accept nulls.
     *
     * @param K k the key of interest.
     * @return Entry returns the found object.
     * @throws IllegalArgumentException if the found object is null.
     */
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

    /**
     * A method to check the load of the hash table.
     * If the load exceeds 0.4 we resize the table.
     */
    private void checkLoad() {
        double load = ((double) this.size) / ((double) this.hashSize);
        double loadMax = 0.5;
        if (loadMax < load) {
            this.rehash();
        }
    }

    /**
     * A method to rehash the hashmap.
     * Performed when a cycle is detected of the load is too large.
     */
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

    /**
     * A nested class of the iterator for this specific implementation.
     */
    private class HashMapIterator implements Iterator<K> {
        /** A next tracker. */
        int count;
        /** For data storage. */
        ArrayList<Entry<K, V>> tempData = new ArrayList<>();

        /** The constructor */
        HashMapIterator() {
            this.count = 0;
            for (int i = 0; i < (HashMap.this.hashSize << 1); i++) {
                if (HashMap.this.data.get(i) != null) {
                    this.tempData.add(HashMap.this.data.get(i));
                }
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

    /**
     * An iterator for the given data structure.
     *
     * @return Iterator the iterator over element k.
     */
    public Iterator<K> iterator() {
        return new HashMapIterator();
    }

    /**
     * A toString representation of the data structure.
     *
     * @return String the representation.
     */
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