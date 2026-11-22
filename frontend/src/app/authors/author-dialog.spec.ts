import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { of } from 'rxjs';
import { AuthorDialog } from './author-dialog';
import { AuthorService } from './author.service';

describe('AuthorDialog', () => {
  const authorService = { create: vi.fn(), update: vi.fn() };
  const dialogRef = { close: vi.fn() };

  function setUp(data: { id: number; name: string } | null) {
    TestBed.configureTestingModule({
      imports: [AuthorDialog],
      providers: [
        { provide: AuthorService, useValue: authorService },
        { provide: MatDialogRef, useValue: dialogRef },
        { provide: MAT_DIALOG_DATA, useValue: data },
      ],
    });
    return TestBed.createComponent(AuthorDialog).componentInstance;
  }

  beforeEach(() => vi.clearAllMocks());

  it('does not save a blank name', () => {
    const dialog = setUp(null);
    dialog['form'].setValue({ name: '   ' });

    dialog.save();

    expect(authorService.create).not.toHaveBeenCalled();
    expect(dialogRef.close).not.toHaveBeenCalled();
  });

  it('creates a new author and closes with it', () => {
    authorService.create.mockReturnValue(of({ id: 5, name: 'Taha Hussein' }));
    const dialog = setUp(null);
    dialog['form'].setValue({ name: 'Taha Hussein' });

    dialog.save();

    expect(authorService.create).toHaveBeenCalledWith({ name: 'Taha Hussein' });
    expect(dialogRef.close).toHaveBeenCalledWith({ id: 5, name: 'Taha Hussein' });
  });

  it('updates the author it was opened with', () => {
    authorService.update.mockReturnValue(of({ id: 1, name: 'Naguib Mahfouz' }));
    const dialog = setUp({ id: 1, name: 'N. Mahfouz' });
    dialog['form'].setValue({ name: 'Naguib Mahfouz' });

    dialog.save();

    expect(authorService.update).toHaveBeenCalledWith(1, { name: 'Naguib Mahfouz' });
  });
});
