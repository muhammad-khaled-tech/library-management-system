import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { MemberService } from './member.service';

describe('MemberService', () => {
  let service: MemberService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(MemberService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('gets one member by id', () => {
    service.get(3).subscribe((member) => expect(member.email).toBe('sara@example.com'));

    http
      .expectOne('/api/members/3')
      .flush({ id: 3, fullName: 'Sara Ali', email: 'sara@example.com' });
  });
});
