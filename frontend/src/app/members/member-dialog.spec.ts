import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { of } from 'rxjs';
import { MemberDialog } from './member-dialog';
import { MemberService } from './member.service';

describe('MemberDialog', () => {
  const memberService = { create: vi.fn(), update: vi.fn() };
  const dialogRef = { close: vi.fn() };

  beforeEach(() => {
    vi.clearAllMocks();
    TestBed.configureTestingModule({
      imports: [MemberDialog],
      providers: [
        { provide: MemberService, useValue: memberService },
        { provide: MatDialogRef, useValue: dialogRef },
        { provide: MAT_DIALOG_DATA, useValue: null },
      ],
    });
  });

  it('does not save an invalid email', () => {
    const dialog = TestBed.createComponent(MemberDialog).componentInstance;
    dialog['form'].setValue({ fullName: 'Sara Ali', email: 'not-an-email' });

    dialog.save();

    expect(memberService.create).not.toHaveBeenCalled();
  });

  it('registers a member and closes with it', () => {
    memberService.create.mockReturnValue(
      of({ id: 3, fullName: 'Sara Ali', email: 'sara@example.com' }),
    );
    const dialog = TestBed.createComponent(MemberDialog).componentInstance;
    dialog['form'].setValue({ fullName: 'Sara Ali', email: 'sara@example.com' });

    dialog.save();

    expect(memberService.create).toHaveBeenCalledWith({
      fullName: 'Sara Ali',
      email: 'sara@example.com',
    });
    expect(dialogRef.close).toHaveBeenCalledWith({
      id: 3,
      fullName: 'Sara Ali',
      email: 'sara@example.com',
    });
  });
});
