import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NicDetailsDialogComponent } from './nic-details-dialog.component';

describe('NicDetailsDialogComponent', () => {
  let component: NicDetailsDialogComponent;
  let fixture: ComponentFixture<NicDetailsDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NicDetailsDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NicDetailsDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
