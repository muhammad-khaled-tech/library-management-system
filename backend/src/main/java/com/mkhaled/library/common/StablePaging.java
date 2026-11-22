package com.mkhaled.library.common;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Makes page order deterministic. Sorting by a non-unique column (title, borrowedOn, ...) lets
 * the database return equal rows in any order, so a row can show up on two pages or on none.
 * Adding the primary key as the last sort key fixes that.
 */
public final class StablePaging {

    private StablePaging() {
    }

    public static Pageable withIdTiebreaker(Pageable pageable) {
        Sort sort = pageable.getSort();
        if (pageable.isUnpaged() || sort.getOrderFor("id") != null) {
            return pageable;
        }
        // Follow the last requested direction, so "newest first" also puts the newest row first on ties
        Sort.Direction direction = sort.stream()
                .reduce((first, last) -> last)
                .map(Sort.Order::getDirection)
                .orElse(Sort.Direction.ASC);
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort.and(Sort.by(direction, "id")));
    }
}
