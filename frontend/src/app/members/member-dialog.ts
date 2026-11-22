import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { finalize } from 'rxjs';
import { Member } from './member.model';
import { MemberService } from './member.service';

/** Registers a member, or edits the one passed as dialog data. Stays open if the save fails. */
@Component({
  selector: 'app-member-dialog',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
  ],
  templateUrl: './member-dialog.html',
})
export class MemberDialog {
  private readonly memberService = inject(MemberService);
  private readonly dialogRef = inject<MatDialogRef<MemberDialog, Member>>(MatDialogRef);
  protected readonly member = inject<Member | null>(MAT_DIALOG_DATA);
  protected readonly saving = signal(false);

  protected readonly form = new FormGroup({
    fullName: new FormControl(this.member?.fullName ?? '', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/\S/), Validators.maxLength(150)],
    }),
    email: new FormControl(this.member?.email ?? '', {
      nonNullable: true,
      validators: [Validators.required, Validators.email, Validators.maxLength(255)],
    }),
  });

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const request = this.form.getRawValue();
    const call = this.member
      ? this.memberService.update(this.member.id, request)
      : this.memberService.create(request);

    this.saving.set(true);
    call.pipe(finalize(() => this.saving.set(false))).subscribe({
      next: (saved) => this.dialogRef.close(saved),
    });
  }
}
