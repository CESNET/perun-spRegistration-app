export class RequestSignature {

  constructor(item: any) {
    this.signerId = item.signerId;
    this.signerName = item.signerName;
    this.signedAt = item.signedAt;
    this.approved = item.approved;
  }

  signerId: number = null;
  signerName: string = '';
  signedAt: string = '';
  approved: boolean = false;

}
