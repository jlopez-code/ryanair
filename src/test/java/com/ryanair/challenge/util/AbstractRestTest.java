package com.ryanair.challenge.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

public abstract class AbstractRestTest {

  @Autowired
  private WebApplicationContext context;

  protected ObjectMapper objectMapper;

  protected MockMvc mockMvc;

  @BeforeEach
  public void setUp() {
    this.mockMvc = MockMvcBuilders
        .webAppContextSetup(context)
        .build();

    this.objectMapper = new ObjectMapper();
    this.objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    this.objectMapper.registerModule(new JavaTimeModule());
  }

}
