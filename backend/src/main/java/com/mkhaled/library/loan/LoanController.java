package com.mkhaled.library.loan;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Tag(name = "Loans")
@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @GetMapping
    public PagedModel<LoanResponse> list(
            @ParameterObject LoanSearchCriteria criteria,
            @ParameterObject @PageableDefault(size = 20, sort = "borrowedOn", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return new PagedModel<>(loanService.search(criteria, pageable));
    }

    @GetMapping("/{id}")
    public LoanResponse get(@PathVariable Long id) {
        return loanService.get(id);
    }

    @PostMapping
    public ResponseEntity<LoanResponse> borrow(@Valid @RequestBody BorrowRequest request) {
        LoanResponse loan = loanService.borrow(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(loan.id())
                .toUri();
        return ResponseEntity.created(location).body(loan);
    }

    @PostMapping("/{id}/return")
    public LoanResponse returnLoan(@PathVariable Long id) {
        return loanService.returnLoan(id);
    }
}
