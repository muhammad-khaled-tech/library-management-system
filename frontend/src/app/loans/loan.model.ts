export type LoanStatus = 'ACTIVE' | 'OVERDUE' | 'RETURNED';

export interface Loan {
  id: number;
  bookId: number;
  bookTitle: string;
  memberId: number;
  memberName: string;
  borrowedOn: string;
  dueDate: string;
  returnedOn: string | null;
  lateFee: number | null;
  status: LoanStatus;
}

export interface BorrowRequest {
  bookId: number;
  memberId: number;
}

export interface LoanFilter {
  memberId?: number;
  bookId?: number;
  status?: LoanStatus;
}
