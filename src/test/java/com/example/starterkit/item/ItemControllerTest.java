package com.example.starterkit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemService itemService;

    @Test
    void getItemsReturnsListOfItems() throws Exception {
        Item item1 = new Item("Item 1", "Description 1");
        item1.setId(1L);
        Item item2 = new Item("Item 2", "Description 2");
        item2.setId(2L);
        when(itemService.listAll()).thenReturn(List.of(item1, item2));

        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Item 1")))
                .andExpect(jsonPath("$[0].description", is("Description 1")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Item 2")));
    }

    @Test
    void createItemWithValidInputReturnsCreatedItem() throws Exception {
        Item item = new Item("New item", "New description");
        item.setId(1L);
        when(itemService.create(org.mockito.ArgumentMatchers.any(ItemRequest.class))).thenReturn(item);

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "New item",
                                    "description": "New description"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("New item")))
                .andExpect(jsonPath("$.description", is("New description")));
    }

    @Test
    void createItemWithInvalidInputReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "",
                                    "description": "Description"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
