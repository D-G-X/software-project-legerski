export class AppUserDTO {

  constructor(data:Partial<AppUserDTO>) {
    Object.assign(this, data);
  }

  id?: string|null;

}
