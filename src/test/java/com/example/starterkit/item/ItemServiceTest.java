package com.example.starterkit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    @Test
    void listAllReturnsAllItems() {
        Item item1 = new Item("Item 1", "Description 1");
        item1.setId(1L);
        Item item2 = new Item("Item 2", "Description 2");
        item2.setId(2L);
        when(itemRepository.findAll()).thenReturn(List.of(item1, item2));

        List<Item> result = itemService.listAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Item 1");
        assertThat(result.get(1).getName()).isEqualTo("Item 2");
        verify(itemRepository).findAll();
    }

    @Test
    void createSavesAndReturnsItem() {
        ItemRequest request = new ItemRequest("New item", "New description");
        Item saved = new Item("New item", "New description");
        saved.setId(1L);
        when(itemRepository.save(org.mockito.ArgumentMatchers.any(Item.class))).thenReturn(saved);

        Item result = itemService.create(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("New item");
        assertThat(result.getDescription()).isEqualTo("New description");
        verify(itemRepository).save(org.mockito.ArgumentMatchers.any(Item.class));
    }
}
