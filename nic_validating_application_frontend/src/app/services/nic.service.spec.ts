import { TestBed } from '@angular/core/testing';

import { NicService } from './nic.service';

describe('NicService', () => {
  let service: NicService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(NicService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
