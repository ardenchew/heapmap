import java.util.Random;
import java.math.BigInteger;

/**
 * Generate (or approximate in any case) universal hash functions. Thanks to
 * Kelvin Qian (Fall 2017) for a helpful question and follow-up bug report.
 */
public final class UniversalHashes {
    private static final Random RAND = new Random();

    // Make checkstyle happy.
    private UniversalHashes() {}

    /**
     * Force given integer positive. This is *not* abs, it just switches the
     * MSB off.
     */
    private static int pos(int i) {
        return i & ~(1 << 31);
    }

    /**
     * The classic Carter/Wegman construction. Works for any m &gt; 0 but uses
     * expensive modulo operations internally.
     *
     * @param m Size of the hash table, m &gt; 0.
     * @return Random hash function that yields 0..m-1.
     */
    public static HashFunction prime(int m) {
        if (m <= 0) {
            throw new IllegalArgumentException("m not positive!");
        }
        BigInteger bigM = BigInteger.valueOf(m);
        BigInteger bigP = bigM.nextProbablePrime(); // so that's a maybe
        int p = bigP.intValue();
        int b = RAND.nextInt(p);
        int a = RAND.nextInt(p);
        while (a == 0) {
            a = RAND.nextInt(p);
        }
        assert a > 0 && b >= 0;
        return prime(a, b, p, m);
    }

    // Separate method because of anonymous class restrictions.
    private static HashFunction prime(int a, int b, int p, int m) {
        return new HashFunction() {
            public int hash(int i) {
                return pos((a * i + b) % p) % m;
            }

            public String toString() {
                return "((" + a + " * x + " + b + ") % " + p + ") % " + m;
            }
        };
    }

    /**
     * The newer Dietzfelbinger construction. Works only for m = 2^M but should
     * be a lot faster to compute. (But note that 0 hashes to 0 which is sad.)
     *
     * @param m Size of the hash table, m = 2^M, 1 &lt; M &lt; 32.
     * @return Random hash function that yields 0..m-1.
     */
    public static HashFunction power(int m) {
        int bit = Integer.highestOneBit(m);
        if ((m & bit) != m || m <= 0) {
            throw new IllegalArgumentException("m not a positive power of 2!");
        }
        int bigM = Integer.numberOfTrailingZeros(bit);
        int w = 32;
        int exp = w - bigM;
        int b = RAND.nextInt(1 << exp); // too small for small inputs?
        int a = RAND.nextInt();
        while (a <= 0 || (a & 1) == 0) {
            a = RAND.nextInt();
        }
        assert a > 0 && b >= 0;
        return power(a, b, exp);
    }

    // Separate method because of anonymous class restrictions.
    private static HashFunction power(int a, int b, int exp) {
        return new HashFunction() {
            public int hash(int i) {
                return (a * i + b) >>> exp; // zero-fills high bits!
            }

            public String toString() {
                return "(" + a + " * x + " + b + ") >>> " + exp;
            }
        };
    }

    // Hash a few integers through given hash function.
    private static void hashSome(HashFunction h) {
        for (int i = 42; i < 47; i++) {
            System.out.printf("\t[%d => %d]", i, h.hash(i));
        }
        System.out.println();
    }

    // Hash a lot of integers through given hash function.
    private static void hashCollisions(HashFunction h, int n, int m) {
        int[] hits = new int[m];
        for (int j = 0; j < n; j++) {
            hits[pos(h.hash(RAND.nextInt()))] += 1;
        }
        for (int j = 0; j < hits.length; j++) {
            System.out.printf("\t%d",  hits[j]);
        }
        System.out.println();
    }

    /**
     * Main method demonstrates how to make and use hash functions.
     *
     * @param args Command line arguments (ignored).
     */
    public static void main(String[] args) {
        // Hash a few for a variety of tables sizes.
        for (int i = 4; i < 13; i += 2) {
            int size = 1 << i;

            System.out.printf(" size: %d\n", size);
            HashFunction pr = prime(size);
            System.out.printf("prime: %s\n", pr);
            hashSome(pr);

            HashFunction po = power(size);
            System.out.printf("power: %s\n", po);
            hashSome(po);

            System.out.println();
        }

        // Hash a lot and count collisions for small tables.
        System.out.println("prime collisions:");
        for (int i = 0; i < 8; i++) {
            HashFunction pr = prime(16);
            hashCollisions(pr, 32768, 16);
        }
        System.out.println("power collisions:");
        for (int i = 0; i < 8; i++) {
            HashFunction po = power(16);
            hashCollisions(po, 32768, 16);
        }
    }
}
