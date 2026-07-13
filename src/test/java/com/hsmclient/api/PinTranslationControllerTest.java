package com.hsmclient.api;

import com.hsmclient.domain.exception.HsmException;
import com.hsmclient.domain.model.PinBlock;
import com.hsmclient.domain.port.in.TranslatePinUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PinTranslationController.class)
class PinTranslationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TranslatePinUseCase translatePin;

    @Test
    void translateTpkReturnsDestinationPinBlock() throws Exception {
        when(translatePin.translateFromTpk(any(), any(), any(), any()))
                .thenReturn(PinBlock.iso0("1122334455667788"));

        mockMvc.perform(post("/api/v1/pin/translate/tpk")
                        .contentType("application/json")
                        .content("""
                                {
                                  "sourceTpk": "8699395CD2688A2CEFD97DAEF3BD4D61",
                                  "destinationZpk": "D3A8A50F20BD9B00",
                                  "sourcePinBlock": "8EF09CD0A28D7DB0",
                                  "pan": "976000000001"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pinBlock").value("1122334455667788"));
    }

    @Test
    void translateTpkReturns422WhenHsmRejectsTheRequest() throws Exception {
        when(translatePin.translateFromTpk(any(), any(), any(), any()))
                .thenThrow(new HsmException("20"));

        mockMvc.perform(post("/api/v1/pin/translate/tpk")
                        .contentType("application/json")
                        .content("""
                                {
                                  "sourceTpk": "8699395CD2688A2CEFD97DAEF3BD4D61",
                                  "destinationZpk": "D3A8A50F20BD9B00",
                                  "sourcePinBlock": "8EF09CD0A28D7DB0",
                                  "pan": "976000000001"
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("20"));
    }

    @Test
    void translateTpkReturns400WhenRequestIsIncomplete() throws Exception {
        mockMvc.perform(post("/api/v1/pin/translate/tpk")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
