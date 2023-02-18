package hello.springcoremvc211.repository;

import hello.springcoremvc211.domain.Item;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

/**
 * 상품 저장소 - InMemory( HashMap )
 */
@Repository
public class ItemRepository {
    private final Map<Long, Item> store = new HashMap<>();
    private long sequence = 0L;

    /**
     * 상품 저장
     * 상품이 저장될 때마다 sequence field를 이용해 index를 1개씩 증가시킴
     *
     * @param item 저장할 상품 인스턴스
     * @return 저장된 상품 인스턴스
     */
    public Item save(
            Item item
    ) {
        item.setId(++sequence);
        store.put(item.getId(), item);
        return item;
    }

    /**
     * index를 이용해 상품 찾기
     *
     * @param id 찾고 싶은 상품의 index 값
     * @return 찾은 상품
     */
    @Nullable
    public Item findById(
            Long id
    ) {
        return store.get(id);
    }
}
