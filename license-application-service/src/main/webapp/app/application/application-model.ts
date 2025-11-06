export class ApplicationDTO {

  constructor(data:Partial<ApplicationDTO>) {
    Object.assign(this, data);
  }

  id?: number|null;
  licenseType?: string|null;
  cadastralReference?: string|null;
  appliedAt?: string|null;
  changedAt?: string|null;
  status?: string|null;
  paymentStatus?: string|null;
  user?: string|null;

}
