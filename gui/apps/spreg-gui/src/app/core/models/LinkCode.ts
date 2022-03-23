export class LinkCode {

  constructor(obj: any) {
    this.invitedEmail = obj.recipientEmail;
    this.inviterName = obj.senderName;
    this.inviterEmail = obj.senderEmail;
    this.requestId = obj.requestId;
    this.facilityId = obj.facilityId;
  }

  invitedEmail: string;
  inviterName: string;
  inviterEmail: string;
  requestId: number;
  facilityId: number;

}
