package com.mkhaled.library.member;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Page<Member> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String fullName, String email, Pageable pageable);

    boolean existsByEmail(String email);
}
