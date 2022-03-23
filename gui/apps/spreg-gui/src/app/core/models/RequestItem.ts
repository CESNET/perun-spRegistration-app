import {Attribute} from "./Attribute";

export interface RequestItem {

  getAttribute(): Attribute;

  hasCorrectValue(): boolean;

  onFormSubmitted(): void;
}
