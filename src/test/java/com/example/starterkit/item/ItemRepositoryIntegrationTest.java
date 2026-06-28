package com.example.starterkit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ItemRepositoryIntegrationTest {

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void savesAndFindsItem() {
        Item item = new Item("Test item", "Test description");
        itemRepository.save(item);

        var found = itemRepository.findAll();

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("Test item");
        assertThat(found.get(0).getDescription()).isEqualTo("Test description");
    }
}
