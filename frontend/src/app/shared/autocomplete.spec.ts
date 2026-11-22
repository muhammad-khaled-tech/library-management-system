import { FormControl } from '@angular/forms';
import { optionSelected } from './autocomplete';

describe('optionSelected', () => {
  it('fails for typed text and passes once an object is picked', () => {
    const control = new FormControl<{ id: number } | string | null>('mahf', optionSelected);
    expect(control.valid).toBe(false);

    control.setValue({ id: 1 });
    expect(control.valid).toBe(true);
  });
});
