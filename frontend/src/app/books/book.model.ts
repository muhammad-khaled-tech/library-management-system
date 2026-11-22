export interface Book {
  id: number;
  isbn: string;
  title: string;
  publishedYear: number | null;
  totalCopies: number;
  availableCopies: number;
  authorId: number;
  authorName: string;
}

export interface BookRequest {
  isbn: string;
  title: string;
  publishedYear: number | null;
  totalCopies: number;
  authorId: number;
}
