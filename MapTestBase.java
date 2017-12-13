import org.junit.Before;
import org.junit.Test;
import java.util.Iterator;
import java.util.Comparator;
import java.util.NoSuchElementException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;


public abstract class MapTestBase {
	public Map<String, Integer> map;

	protected abstract Map<String, Integer> createMap();

	@Before
	public void create() {
		map = this.createMap();
	}

	@Test
	public void insert() {
		map.insert("a", 2);
		assertEquals(map.size(), 1);
		assertTrue(map.get("a") == 2);
		map.insert("b", 3);
		map.insert("c", 4);
		map.insert("e", 5);
	}

	@Test(expected = IllegalArgumentException.class)
	public void insertNull() {
		map.insert(null, 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void insertDuplicate() {
		map.insert("a", 1);
		map.insert("a", 2);
	}

	@Test
	public void remove() {
		map.insert("a", 1);
		map.insert("b", 2);
		map.insert("c", 3);
		map.insert("d", 4);
		map.insert("e", 5);
		assertEquals(map.size(), 5);
		int check = map.remove("a");
		assertEquals(check, 1);
		assertEquals(map.size(), 4);
		map.remove("b");
	}

	@Test(expected = IllegalArgumentException.class)
	public void removeNull() {
		map.remove(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void removeUnknownKey() {
		map.remove("a");
	}

	@Test
	public void get() {
		map.insert("a", 1);
		assertTrue(map.get("a") == 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void getNull() {
		map.get(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void getUnknownKey() {
		map.get("a");
	}

	@Test(expected = IllegalArgumentException.class)
	public void getRemovedKey() {
		map.insert("a", 1);
		map.remove("a");
		map.get("a");
	}

	@Test
	public void put() {
		map.insert("a", 1);
		assertTrue(map.get("a") == 1);
		map.put("a", 2);
		assertTrue(map.get("a") == 2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void putNull() {
		map.put(null, 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void putUnknownKey() {
		map.put("a", 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void putRemovedKey() {
		map.insert("a", 1);
		map.remove("a");
		map.put("a", 2);
	}

	@Test
	public void hasTrue() {
		map.insert("a", 1);
		assertTrue(map.has("a"));
	}

	@Test
	public void hasFalseUnknownKey() {
		assertFalse(map.has("a"));
	}

	@Test
	public void hasFalseRemovedKey() {
		map.insert("a", 1);
		assertTrue(map.has("a"));
		map.remove("a");
		assertFalse(map.has("a"));
	}

	@Test
	public void size() {
		map.insert("a", 8);
		assertEquals(map.size(), 1);
		map.insert("b", 9);
		assertEquals(map.size(), 2);
		map.remove("a");
		assertEquals(map.size(), 1);
		map.remove("b");
		assertEquals(map.size(), 0);
	}

	@Test
	public void iteratorTest() {
		map.insert("a", 1);
		Iterator<String> iter = map.iterator();
		assertTrue(iter.hasNext());
		assertEquals(iter.next(), "a");
		assertFalse(iter.hasNext());
	}

	@Test
	public void toStringTest() {
		String empty = map.toString();
		assertEquals("{}", empty);
		map.insert("a", 1);
		map.insert("b", 2);
		map.remove("b");
		String full = map.toString();
		assertEquals("{a:1}", full);
	}

}