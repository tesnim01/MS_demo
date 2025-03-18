package com.example.microservice2;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(Controller.class)  // Only loads the controller, not the full application
class Microservice2ApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testShowMessageEndpoint() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/serviceB/displayMessage"))
                .andExpect(status().isOk())  // Expect HTTP 200 OK
                .andExpect(content().string("Microservice 2 controller executed")); // Validate response content
    }
}
