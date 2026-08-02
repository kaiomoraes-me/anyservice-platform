import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PaymentCancelComponent } from './payment-cancel';

describe('PaymentCancelComponent', () => {
  let component: PaymentCancelComponent;
  let fixture: ComponentFixture<PaymentCancelComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PaymentCancelComponent],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(PaymentCancelComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
