package com.example.starterkit.item;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public ResponseEntity<List<ItemResponse>> getItems() {
        List<ItemResponse> responses = itemService.listAll().stream()
                .map(item -> new ItemResponse(item.getId(), item.getName(), item.getDescription()))
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<ItemResponse> createItem(@Valid @RequestBody ItemRequest request) {
        Item item = itemService.create(request);
        ItemResponse response = new ItemResponse(item.getId(), item.getName(), item.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
