package com.mkhaled.library.member;

import com.mkhaled.library.common.StablePaging;
import com.mkhaled.library.common.exception.BusinessRuleException;
import com.mkhaled.library.common.exception.ResourceNotFoundException;
import com.mkhaled.library.loan.LoanRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final LoanRepository loanRepository;

    public MemberService(MemberRepository memberRepository, LoanRepository loanRepository) {
        this.memberRepository = memberRepository;
        this.loanRepository = loanRepository;
    }

    public Page<MemberResponse> search(String query, Pageable pageable) {
        Pageable stable = StablePaging.withIdTiebreaker(pageable);
        if (query == null || query.isBlank()) {
            return memberRepository.findAll(stable).map(MemberResponse::from);
        }
        String term = query.strip();
        return memberRepository
                .findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(term, term, stable)
                .map(MemberResponse::from);
    }

    public MemberResponse get(Long id) {
        return MemberResponse.from(findMember(id));
    }

    @Transactional
    public MemberResponse create(MemberRequest request) {
        String email = normalizeEmail(request.email());
        ensureEmailIsFree(email);
        Member member = memberRepository.save(new Member(request.fullName().strip(), email));
        return MemberResponse.from(member);
    }

    @Transactional
    public MemberResponse update(Long id, MemberRequest request) {
        Member member = findMember(id);
        String email = normalizeEmail(request.email());
        if (!email.equals(member.getEmail())) {
            ensureEmailIsFree(email);
        }
        member.updateDetails(request.fullName().strip(), email);
        return MemberResponse.from(member);
    }

    @Transactional
    public void delete(Long id) {
        if (!memberRepository.existsById(id)) {
            throw new ResourceNotFoundException("Member", id);
        }
        if (loanRepository.existsByMemberId(id)) {
            throw new BusinessRuleException("Member " + id + " has loan history and cannot be deleted");
        }
        memberRepository.deleteById(id);
    }

    private void ensureEmailIsFree(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new BusinessRuleException("A member with email " + email + " already exists");
        }
    }

    private Member findMember(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member", id));
    }

    private static String normalizeEmail(String email) {
        return email.toLowerCase(Locale.ROOT);
    }
}
