package com.mkhaled.library.member;

import com.mkhaled.library.common.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
class MemberControllerTest {

    private static final MemberResponse SARA = new MemberResponse(3L, "Sara Ali", "sara@example.com");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    @Test
    void listSortsByNameByDefault() throws Exception {
        PageRequest expectedPage = PageRequest.of(0, 20, Sort.by("fullName"));
        when(memberService.search(null, expectedPage))
                .thenReturn(new PageImpl<>(List.of(SARA), expectedPage, 1));

        mockMvc.perform(get("/api/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("sara@example.com"));
    }

    @Test
    void createReturns201WithLocation() throws Exception {
        when(memberService.create(any(MemberRequest.class))).thenReturn(SARA);

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName": "Sara Ali", "email": "sara@example.com"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/members/3"));
    }

    @Test
    void createRejectsInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName": "Sara Ali", "email": "not-an-email"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());

        verifyNoInteractions(memberService);
    }

    @Test
    void deleteReturns409WhenMemberHasLoans() throws Exception {
        doThrow(new BusinessRuleException("Member 3 has loan history and cannot be deleted"))
                .when(memberService).delete(3L);

        mockMvc.perform(delete("/api/members/3"))
                .andExpect(status().isConflict());
    }
}
