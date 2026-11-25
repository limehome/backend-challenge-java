package de.limehome.backendchallengejava;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
class BackendChallengeIntegrationTests {

    BookingInput GUEST_A_UNIT_1 = new BookingInput(
            "guestA", "1", LocalDate.now(), 5
    );

    BookingInput GUEST_A_UNIT_2 = new BookingInput(
            "guestA", "2", LocalDate.now(), 5
    );

    BookingInput GUEST_B_UNIT_1 = new BookingInput(
            "guestB", "1", LocalDate.now(), 5
    );

    @Autowired
    private MockMvc mockMvc;

    @InjectMocks
    private BookingController controller;

    @MockitoBean
    private BookingRepository bookingRepository;

    static ObjectMapper objectMapper = new ObjectMapper();
    @Test
    void createFreshBooking() throws Exception {
        Mockito.when(bookingRepository.save(Mockito.any(Booking.class))).then(i -> i.getArguments()[0]);

        this.mockMvc.perform(MockMvcRequestBuilders.
                        post("/api/v1/booking").
                        content(objectMapper.writeValueAsString(GUEST_A_UNIT_1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void sameGuestSameUnitFails() throws Exception {
        Mockito.when(bookingRepository.save(Mockito.any(Booking.class))).then(i -> i.getArguments()[0]);

        this.mockMvc.perform(MockMvcRequestBuilders.
                        post("/api/v1/booking").
                        content(objectMapper.writeValueAsString(GUEST_A_UNIT_1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.when(bookingRepository.findAll()).thenReturn(List.of(new Booking(GUEST_A_UNIT_1)));

        this.mockMvc.perform(MockMvcRequestBuilders.
                        post("/api/v1/booking").
                        content(objectMapper.writeValueAsString(GUEST_A_UNIT_1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("The given guest name cannot book the same unit multiple times"));
    }

    @Test
    void sameGuestDifferentUnit() throws Exception {
        Mockito.when(bookingRepository.save(Mockito.any(Booking.class))).then(i -> i.getArguments()[0]);

        this.mockMvc.perform(MockMvcRequestBuilders.
                        post("/api/v1/booking").
                        content(objectMapper.writeValueAsString(GUEST_A_UNIT_1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.when(bookingRepository.findAll()).thenReturn(List.of(new Booking(GUEST_A_UNIT_1)));

        this.mockMvc.perform(MockMvcRequestBuilders.
                        post("/api/v1/booking").
                        content(objectMapper.writeValueAsString(GUEST_A_UNIT_2))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("The same guest cannot be in multiple units at the same time"));
    }

    @Test
    void differentGuestSameBooking() throws Exception {
        Mockito.when(bookingRepository.save(Mockito.any(Booking.class))).then(i -> i.getArguments()[0]);

        this.mockMvc.perform(MockMvcRequestBuilders.
                        post("/api/v1/booking").
                        content(objectMapper.writeValueAsString(GUEST_A_UNIT_1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.when(bookingRepository.findAll()).thenReturn(List.of(new Booking(GUEST_A_UNIT_1)));

        this.mockMvc.perform(MockMvcRequestBuilders.
                        post("/api/v1/booking").
                        content(objectMapper.writeValueAsString(GUEST_B_UNIT_1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("For the given check-in date, the unit is already occupied"));
    }

    @Test
    void differentGuestsSameUnitDifferentDifferentDays() throws Exception {
        Mockito.when(bookingRepository.save(Mockito.any(Booking.class))).then(i -> i.getArguments()[0]);

        // Create first booking
        this.mockMvc.perform(MockMvcRequestBuilders.
                        post("/api/v1/booking").
                        content(objectMapper.writeValueAsString(GUEST_A_UNIT_1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.when(bookingRepository.findAll()).thenReturn(List.of(new Booking(GUEST_A_UNIT_1)));

        // GuestB trying to book a unit that is already occupied
        BookingInput newGuest = new BookingInput(
                "guestB", GUEST_A_UNIT_1.unitID, GUEST_A_UNIT_1.checkInDate.plus(1, ChronoUnit.DAYS), 5
        );

        this.mockMvc.perform(MockMvcRequestBuilders.
                        post("/api/v1/booking").
                        content(objectMapper.writeValueAsString(newGuest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("For the given check-in date, the unit is already occupied"));
    }

}
