import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { MemberList } from './member-list';
import { MemberService } from './member.service';

describe('MemberList', () => {
  const memberService = {
    search: vi.fn(() =>
      of({
        content: [{ id: 3, fullName: 'Sara Ali', email: 'sara@example.com' }],
        page: { size: 10, number: 0, totalElements: 1, totalPages: 1 },
      }),
    ),
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [MemberList],
      providers: [{ provide: MemberService, useValue: memberService }],
    });
  });

  it('loads members sorted by name and shows name and email', async () => {
    const fixture = TestBed.createComponent(MemberList);
    await fixture.whenStable();

    expect(memberService.search).toHaveBeenCalledWith('', {
      page: 0,
      size: 10,
      sort: 'fullName,asc',
    });
    const row = (fixture.nativeElement as HTMLElement).querySelector('tr.mat-mdc-row')!;
    expect(row.textContent).toContain('Sara Ali');
    expect(row.textContent).toContain('sara@example.com');
  });
});
