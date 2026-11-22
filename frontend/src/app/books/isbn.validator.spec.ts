import { isValidIsbn13 } from './isbn.validator';

describe('isValidIsbn13', () => {
  it('accepts a correct ISBN-13 with or without hyphens', () => {
    expect(isValidIsbn13('9780306406157')).toBe(true);
    expect(isValidIsbn13('978-0-306-40615-7')).toBe(true);
  });

  it('rejects a wrong check digit', () => {
    expect(isValidIsbn13('9780306406158')).toBe(false);
  });

  it('rejects anything that is not 13 digits', () => {
    expect(isValidIsbn13('0306406152')).toBe(false);
    expect(isValidIsbn13('97803064061X7')).toBe(false);
  });
});
