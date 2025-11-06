export class LicenseDTO {

  constructor(data:Partial<LicenseDTO>) {
    Object.assign(this, data);
  }

  id?: number|null;
  licenseType?: string|null;
  licenseStatus?: string|null;
  issuedAt?: string|null;
  expiresAt?: string|null;
  user?: string|null;
  application?: number|null;

}
