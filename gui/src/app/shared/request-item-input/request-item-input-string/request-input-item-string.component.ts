import { Component, Input, OnInit, ViewChild } from '@angular/core'
import { ApplicationItem } from '../../../core/models/ApplicationItem'
import { RequestItem } from '../../../core/models/RequestItem'
import { Attribute } from '../../../core/models/Attribute'
import { NgForm, NgModel } from '@angular/forms'
import { RequestItemInputUtils } from '../request-item-input-utils/request-item-input.component'

@Component({
  selector: 'request-item-input-string',
  templateUrl: './request-input-item-string.component.html',
  styleUrls: ['./request-input-item-string.component.scss']
})
export class RequestInputItemStringComponent implements RequestItem, OnInit {
  constructor () {}

  @Input() newApp = false
  @Input() applicationItem: ApplicationItem
  @ViewChild('form', { static: false }) form: NgForm
  @ViewChild('input', { static: false }) inputField: NgModel

  value = ''

  missingValueError = false
  expectedValueChangedError = false
  regexMismatchError = false

  ngOnInit (): void {
    this.value = this.applicationItem.oldValue
  }

  getAttribute (): Attribute {
    return new Attribute(this.applicationItem.name, this.value)
  }

  hasCorrectValue (): boolean {
    this.resetErrors()

    if (!this.newApp && !this.checkChangeMade()) {
      return false
    }

    if (!this.value) {
      return this.checkValueRequired()
    } else {
      return this.checkRegex()
    }
  }

  onFormSubmitted (): void {
    if (this.value) {
      this.value = this.value.trim()
    }
    if (!this.hasCorrectValue()) {
      this.form.form.controls[this.applicationItem.name].markAsTouched()
      this.form.form.controls[this.applicationItem.name].setErrors({
        incorrect: true
      })
      this.form.form.controls[
        this.applicationItem.name
      ].updateValueAndValidity()
    }
  }

  hasError (): boolean {
    return (
      this.expectedValueChangedError ||
      this.missingValueError ||
      this.regexMismatchError
    )
  }

  private checkValueRequired (): boolean {
    if (this.applicationItem.required) {
      this.form.form.setErrors({ incorrect: true })
      this.missingValueError = true
      return false
    }
    return true
  }

  private checkChangeMade (): boolean {
    if (
      !RequestItemInputUtils.requestedChangeHasBeenMadeSingleValue(
        this.applicationItem,
        this.value
      )
    ) {
      this.form.form.setErrors({ incorrect: true })
      this.expectedValueChangedError = true
      return false
    }
    return true
  }

  private checkRegex (): boolean {
    if (
      !RequestItemInputUtils.checkRegexSingleValue(
        this.applicationItem,
        this.value
      )
    ) {
      this.form.form.setErrors({ incorrect: true })
      this.regexMismatchError = true
      return false
    }
    return true
  }

  private resetErrors (): void {
    this.expectedValueChangedError = false
    this.regexMismatchError = false
    this.missingValueError = false
  }
}
