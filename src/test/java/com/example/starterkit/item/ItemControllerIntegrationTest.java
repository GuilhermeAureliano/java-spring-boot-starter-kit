package com.example.starterkit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ItemControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void getItemsReturnsPersistedItems() throws Exception {
        itemRepository.save(new Item("Integration item", "Integration description"));

        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Integration item")))
                .andExpect(jsonPath("$[0].description", is("Integration description")));
    }

    @Test
    void createItemPersistsAndReturnsItem() throws Exception {
        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Created item",
                                    "description": "Created description"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Created item")))
                .andExpect(jsonPath("$.description", is("Created description")));

        var items = itemRepository.findAll();
        assert items.size() == 1;
        assert items.get(0).getName().equals("Created item");
    }
}
