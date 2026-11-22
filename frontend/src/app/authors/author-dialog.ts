import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { finalize } from 'rxjs';
import { Author } from './author.model';
import { AuthorService } from './author.service';

/** Creates an author, or edits the one passed as dialog data. Stays open if the save fails. */
@Component({
  selector: 'app-author-dialog',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
  ],
  templateUrl: './author-dialog.html',
})
export class AuthorDialog {
  private readonly authorService = inject(AuthorService);
  private readonly dialogRef = inject<MatDialogRef<AuthorDialog, Author>>(MatDialogRef);
  protected readonly author = inject<Author | null>(MAT_DIALOG_DATA);
  protected readonly saving = signal(false);

  protected readonly form = new FormGroup({
    name: new FormControl(this.author?.name ?? '', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/\S/), Validators.maxLength(150)],
    }),
  });

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const request = this.form.getRawValue();
    const call = this.author
      ? this.authorService.update(this.author.id, request)
      : this.authorService.create(request);

    this.saving.set(true);
    call.pipe(finalize(() => this.saving.set(false))).subscribe({
      next: (saved) => this.dialogRef.close(saved),
    });
  }
}
