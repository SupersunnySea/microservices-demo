package hipstershop;

import org.junit.Before;
import org.junit.Test;

import java.security.PublicKey;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;

public class AdServiceTest {
    private AdService adservice;

    @Before
    public void setUp() throws Exception{
        adservice = new AdService();
    }

    @Test
    public void testgetAdsByCategory() throws Exception{
        String category = "photography";
        hipstershop.Demo.Ad camera =
                hipstershop.Demo.Ad.newBuilder()
                        .setRedirectUrl("/product/2ZYFJ3GM2N")
                        .setText("Film camera for sale. 50% off.")
                        .build();
        hipstershop.Demo.Ad lens =
                hipstershop.Demo.Ad.newBuilder()
                        .setRedirectUrl("/product/66VCHSJNUP")
                        .setText("Vintage camera lens for sale. 20% off.")
                        .build();
        Collection<hipstershop.Demo.Ad> collection=new Collection<hipstershop.Demo.Ad>() {
            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean contains(Object o) {
                return false;
            }

            @Override
            public Iterator<hipstershop.Demo.Ad> iterator() {
                return null;
            }

            @Override
            public Object[] toArray() {
                return new Object[0];
            }

            @Override
            public <T> T[] toArray(T[] a) {
                return null;
            }

            @Override
            public boolean add(hipstershop.Demo.Ad ad) {
                return false;
            }

            @Override
            public boolean remove(Object o) {
                return false;
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                return false;
            }

            @Override
            public boolean addAll(Collection<? extends hipstershop.Demo.Ad> c) {
                return false;
            }

            @Override
            public boolean removeAll(Collection<?> c) {
                return false;
            }

            @Override
            public boolean retainAll(Collection<?> c) {
                return false;
            }

            @Override
            public void clear() {

            }
        };
        collection.add(camera);
        collection.add(lens);

//        assertEquals(collection,adservice.getAdsByCategory(category));
    }

//    @Test
//    public void

}
