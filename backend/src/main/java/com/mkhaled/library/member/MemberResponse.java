package com.mkhaled.library.member;

public record MemberResponse(Long id, String fullName, String email) {

    static MemberResponse from(Member member) {
        return new MemberResponse(member.getId(), member.getFullName(), member.getEmail());
    }
}
