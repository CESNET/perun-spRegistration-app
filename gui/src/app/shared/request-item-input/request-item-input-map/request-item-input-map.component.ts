import { Component, Input, OnInit, ViewChild } from '@angular/core'
import { ApplicationItem } from '../../../core/models/ApplicationItem'
import { RequestItem } from '../../../core/models/RequestItem'
import { Attribute } from '../../../core/models/Attribute'
import { NgForm } from '@angular/forms'
import { RequestItemInputUtils } from '../request-item-input-utils/request-item-input.component'

@Component({
  selector: 'request-application-item-map',
  templateUrl: './request-item-input-map.component.html',
  styleUrls: ['./request-item-input-map.component.scss']
})
export class RequestItemInputMapComponent implements RequestItem, OnInit {
  constructor () {}

  @Input() newApp = false
  @Input() applicationItem: ApplicationItem
  @ViewChild('form', { static: false }) form: NgForm

  entries: Map<string, string> = new Map<string, string>()
  keys: string[] = []
  values: string[] = []
  indexes: number[] = []
  allowCustomKeys = false

  duplicateKeysError = false
  missingValueError = false
  expectedValueChangedError = false
  regexMismatchError = false

  private index = 0

  ngOnInit (): void {
    if (this.applicationItem.oldValue != null) {
      const map: Map<string, string> = this.applicationItem.oldValue
      for (const [key, value] of Object.entries(map)) {
        this.addValueNonEmpty(key, value)
      }
    }
    if (
      this.applicationItem.allowedKeys !== undefined &&
      this.applicationItem.allowedKeys !== null &&
      this.applicationItem.allowedKeys.length > 0
    ) {
      this.allowCustomKeys = true

      if (this.values.length === 0) {
        this.keys = this.applicationItem.allowedKeys

        for (let i = 0; i < this.keys.length; i++) {
          this.values.push('')
          this.indexes.push(this.index++)
        }
      }
    }

    if (this.applicationItem.required && this.keys.length === 0) {
      this.addValue()
    }
  }

  getAttribute (): Attribute {
    const map = new Map()

    for (let i = 0; i < this.values.length; i++) {
      map.set(this.keys[i], this.values[i])
    }

    const obj = Array.from(map.entries()).reduce(
      (main, [key, value]) => ({ ...main, [key]: value }),
      {}
    )

    return new Attribute(this.applicationItem.name, obj)
  }

  hasCorrectValue (): boolean {
    this.resetErrors()
    this.trimKeysAndValues()

    if (!this.newApp && !this.checkChangeMade()) {
      return false
    }

    if (!RequestItemInputUtils.hasValue(this.values)) {
      return this.checkValueRequired()
    } else {
      return (
        this.checkAllValuesFilled() &&
        this.checkRegex() &&
        this.checkDuplicates()
      )
    }
  }

  onFormSubmitted (): void {
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

  customTrackBy (index: number, _: any): any {
    return index
  }

  showErredValues (errIndexes: number[]) {
    for (let i = 0; i < errIndexes.length; i++) {
      const index = this.indexes[i]
      const input = this.form.form.controls['value-' + index]

      input.markAsTouched()
      input.setErrors({ incorrect: true })
    }
  }

  showErredKeys (errIndexes: number[]) {
    for (let i = 0; i < errIndexes.length; i++) {
      const index = this.indexes[i]
      const input = this.form.form.controls['key-' + index]

      input.markAsTouched()
      input.setErrors({ incorrect: true })
    }
  }

  hasError (): boolean {
    return (
      this.missingValueError ||
      this.duplicateKeysError ||
      this.missingValueError ||
      this.regexMismatchError
    )
  }

  removeValue (index: number) {
    this.values.splice(index, 1)
    this.keys.splice(index, 1)
    this.indexes.splice(index, 1)
  }

  addValue () {
    this.values.push('')
    this.keys.push('')
    this.indexes.push(this.index++)
  }

  addValueNonEmpty (key: string, value: string) {
    this.values.push(value)
    this.keys.push(key)
    this.indexes.push(this.index++)
  }

  private allValuesAreFilled (): boolean {
    if (this.values.length === 0) {
      return false
    }
    for (const value of this.values) {
      if (value === undefined || value === null || value.trim().length === 0) {
        return false
      }
    }

    return true
  }

  private trimKeysAndValues (): void {
    if (this.keys) {
      this.keys = this.keys.map((v) => v.trim())
    }
    if (this.values) {
      this.values = this.values.map((v) => v.trim())
    }
  }

  private checkValueRequired (): boolean {
    if (!RequestItemInputUtils.hasValue(this.values)) {
      if (this.applicationItem.required) {
        this.missingValueError = true
        return false
      }
    }
    return true
  }

  private checkDuplicates (): boolean {
    const errKeys = this.getDuplicateKeys()
    if (errKeys.length !== 0) {
      this.form.form.setErrors({ incorrect: true })
      this.showErredKeys(errKeys)
      this.duplicateKeysError = true
      return false
    }
    return true
  }

  private checkRegex (): boolean {
    const errValues = RequestItemInputUtils.checkRegex(
      this.applicationItem,
      this.values
    )
    if (errValues.length !== 0) {
      this.showErredValues(errValues)
      this.form.form.setErrors({ incorrect: true })
      this.regexMismatchError = true
      return false
    }
    return true
  }

  private checkChangeMade (): boolean {
    if (!this.requestedChangeHasBeenMade()) {
      this.form.form.setErrors({ incorrect: true })
      this.expectedValueChangedError = true
      return false
    }
    return true
  }

  private checkAllValuesFilled (): boolean {
    if (!this.allValuesAreFilled()) {
      this.form.form.setErrors({ incorrect: true })
      this.missingValueError = true
      return false
    }
    return true
  }

  private resetErrors (): void {
    this.expectedValueChangedError = false
    this.regexMismatchError = false
    this.missingValueError = false
    this.duplicateKeysError = false
  }

  private requestedChangeHasBeenMade () {
    if (this.applicationItem.hasComment()) {
      const map = this.applicationItem.oldValue
      for (let i = 0; i < this.indexes.length; i++) {
        const key = this.keys[i]
        if (!map.hasOwnProperty(key)) {
          return true
        } else if (map[key] !== this.values[i]) {
          return true
        }
      }
      return false
    }
    return true
  }

  private getDuplicateKeys (): number[] {
    const keysWithIndexes = new Map<string, number>()
    const duplicities = []
    for (let i = 0; i < this.values.length; i++) {
      const keySet = Array.from(keysWithIndexes.keys())
      const key = this.keys[i].trim()

      if (keySet.includes(key)) {
        duplicities.push(i)
      }

      keysWithIndexes.set(key, i)
    }
    return duplicities
  }
}
