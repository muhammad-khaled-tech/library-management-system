package com.mkhaled.library.common;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

class StablePagingTest {

    @Test
    void addsIdAfterTheRequestedSortInTheSameDirection() {
        Pageable newestFirst = PageRequest.of(2, 10, Sort.by(Sort.Direction.DESC, "borrowedOn"));

        Pageable stable = StablePaging.withIdTiebreaker(newestFirst);

        assertThat(stable.getPageNumber()).isEqualTo(2);
        assertThat(stable.getPageSize()).isEqualTo(10);
        assertThat(stable.getSort()).isEqualTo(Sort.by(Sort.Order.desc("borrowedOn"), Sort.Order.desc("id")));
    }

    @Test
    void sortsByIdWhenNoSortWasRequested() {
        Pageable stable = StablePaging.withIdTiebreaker(PageRequest.of(0, 20));

        assertThat(stable.getSort()).isEqualTo(Sort.by("id"));
    }

    @Test
    void leavesASortThatAlreadyUsesId() {
        Pageable byId = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "id"));

        assertThat(StablePaging.withIdTiebreaker(byId)).isSameAs(byId);
    }
}
