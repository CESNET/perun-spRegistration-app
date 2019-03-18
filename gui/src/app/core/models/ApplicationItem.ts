export interface ApplicationItem {
  name: string;
  required: boolean;
  displayPosition: number;
  displayed: boolean;
  regex: string;
  description: Map<string, string>;
  displayName: Map<string, string>;
  type: string;
  allowedKeys: string[];
  allowedValues: string[];
  comment: string;
}
