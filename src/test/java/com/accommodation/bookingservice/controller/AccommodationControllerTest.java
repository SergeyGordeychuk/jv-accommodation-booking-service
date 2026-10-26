package com.accommodation.bookingservice.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.accommodation.bookingservice.dto.AccommodationRequestDto;
import com.accommodation.bookingservice.dto.AccommodationResponseDto;
import com.accommodation.bookingservice.model.Accommodation;
import com.accommodation.bookingservice.service.AccommodationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AccommodationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccommodationService accommodationService;

    @Test
    @DisplayName("cors: preflight OPTIONS request returns CORS headers")
    void corsPreflightReturnsAllowedHeaders() throws Exception {
        mockMvc.perform(options("/api/accommodations")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    @DisplayName("findAll: unauthenticated request returns 200 OK")
    void findAllUnauthenticatedReturnsOk() throws Exception {
        AccommodationResponseDto dto = new AccommodationResponseDto();
        dto.setId(1L);
        dto.setLocation("Kyiv");

        Mockito.when(accommodationService.findAll()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/accommodations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].location").value("Kyiv"));
    }

    @Test
    @DisplayName("create: unauthenticated request returns 401 Unauthorized")
    void createUnauthenticatedReturnsUnauthorized() throws Exception {
        AccommodationRequestDto dto = new AccommodationRequestDto();
        dto.setType(Accommodation.Type.HOUSE);
        dto.setLocation("Kyiv");
        dto.setSize("2BR");
        dto.setDailyRate(BigDecimal.valueOf(100));
        dto.setAvailability(1);

        mockMvc.perform(post("/api/accommodations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("create: authenticated customer returns 403 Forbidden")
    void createAsCustomerReturnsForbidden() throws Exception {
        AccommodationRequestDto dto = new AccommodationRequestDto();
        dto.setType(Accommodation.Type.HOUSE);
        dto.setLocation("Kyiv");
        dto.setSize("2BR");
        dto.setDailyRate(BigDecimal.valueOf(100));
        dto.setAvailability(1);

        mockMvc.perform(post("/api/accommodations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("create: authenticated manager returns 201 Created")
    void createAsManagerReturnsCreated() throws Exception {
        AccommodationRequestDto dto = new AccommodationRequestDto();
        dto.setType(Accommodation.Type.HOUSE);
        dto.setLocation("Kyiv");
        dto.setSize("2BR");
        dto.setDailyRate(BigDecimal.valueOf(100));
        dto.setAvailability(1);

        AccommodationResponseDto responseDto = new AccommodationResponseDto();
        responseDto.setId(1L);
        responseDto.setLocation("Kyiv");

        Mockito.when(accommodationService.create(Mockito.any(AccommodationRequestDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/api/accommodations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }
}
