package com.mkhaled.library.loan;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
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
