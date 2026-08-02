import { TestBed } from '@angular/core/testing';

import { ServiceListingService } from './service-listing';

describe('ServiceListingService', () => {
  let service: ServiceListingService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ServiceListingService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
