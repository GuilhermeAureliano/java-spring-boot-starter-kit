package com.example.starterkit.item;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public List<Item> listAll() {
        return itemRepository.findAll();
    }

    @Transactional
    public Item create(ItemRequest request) {
        Item item = new Item(request.name(), request.description());
        return itemRepository.save(item);
    }
}
