package com.mkhaled.library.member;

import com.mkhaled.library.common.exception.BusinessRuleException;
import com.mkhaled.library.common.exception.ResourceNotFoundException;
import com.mkhaled.library.loan.LoanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private MemberService memberService;

    private final Pageable firstPage = PageRequest.of(0, 20);

    @Test
    void searchMatchesNameOrEmail() {
        when(memberRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase("sara", "sara", firstPage))
                .thenReturn(Page.empty());

        memberService.search(" sara ", firstPage);

        verify(memberRepository).findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase("sara", "sara", firstPage);
    }

    @Test
    void createStoresEmailInLowerCase() {
        when(memberRepository.existsByEmail("sara@example.com")).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MemberResponse created = memberService.create(new MemberRequest("Sara Ali", "Sara@Example.com"));

        assertThat(created.email()).isEqualTo("sara@example.com");
    }

    @Test
    void createRejectsEmailThatIsAlreadyRegistered() {
        when(memberRepository.existsByEmail("sara@example.com")).thenReturn(true);

        assertThatThrownBy(() -> memberService.create(new MemberRequest("Sara Ali", "sara@example.com")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("A member with email sara@example.com already exists");
        verify(memberRepository, never()).save(any());
    }

    @Test
    void updateChangesNameAndEmail() {
        Member existing = member(3L, "Sara Ali", "sara@example.com");
        when(memberRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(memberRepository.existsByEmail("sara.ali@example.com")).thenReturn(false);

        MemberResponse updated = memberService.update(3L, new MemberRequest("Sara M. Ali", "sara.ali@example.com"));

        assertThat(updated).isEqualTo(new MemberResponse(3L, "Sara M. Ali", "sara.ali@example.com"));
    }

    @Test
    void updateKeepingSameEmailSkipsDuplicateCheck() {
        when(memberRepository.findById(3L)).thenReturn(Optional.of(member(3L, "Sara Ali", "sara@example.com")));

        memberService.update(3L, new MemberRequest("Sara M. Ali", "SARA@example.com"));

        verify(memberRepository, never()).existsByEmail(any());
    }

    @Test
    void getThrowsWhenMemberDoesNotExist() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.get(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Member with id 99 not found");
    }

    @Test
    void deleteRejectsMemberWithLoanHistory() {
        when(memberRepository.existsById(3L)).thenReturn(true);
        when(loanRepository.existsByMemberId(3L)).thenReturn(true);

        assertThatThrownBy(() -> memberService.delete(3L))
                .isInstanceOf(BusinessRuleException.class);
        verify(memberRepository, never()).deleteById(3L);
    }

    @Test
    void deleteRemovesMemberWithoutLoans() {
        when(memberRepository.existsById(3L)).thenReturn(true);
        when(loanRepository.existsByMemberId(3L)).thenReturn(false);

        memberService.delete(3L);

        verify(memberRepository).deleteById(3L);
    }

    private static Member member(Long id, String fullName, String email) {
        Member member = new Member(fullName, email);
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }
}
