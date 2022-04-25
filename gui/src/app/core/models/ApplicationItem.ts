export class ApplicationItem {
  constructor (item: any) {
    if (!item) {
      return
    }
    this.name = item.name

    this.description = new Map<string, string>()
    if ('description' in item && item.description) {
      for (const k of Object.keys(item.description)) {
        this.description.set(k, item.description[k])
      }
    }

    this.displayName = new Map<string, string>()
    if ('displayName' in item && item.displayName) {
      for (const k of Object.keys(item.displayName)) {
        this.displayName.set(k.toLowerCase(), item.displayName[k])
      }
    }

    this.required = item.required
    this.displayed = item.displayed
    this.editable = item.editable
    this.type = item.type
    this.allowedValues = item.allowedValues
    this.displayPosition = item.displayPosition
    this.regex = item.regex
    this.allowedKeys = item.allowedKeys
    if ('comment' in item) {
      this.comment = item.comment
    }
    if ('oldValue' in item) {
      this.oldValue = item.oldValue
    }
  }

  name: string
  displayName: Map<string, string>
  description: Map<string, string>
  required: boolean
  displayed: boolean
  editable = false
  type: string
  allowedValues: string[]
  displayPosition: number
  regex: string
  allowedKeys: string[]
  comment: string
  oldValue: any

  public hasComment (): boolean {
    return (
      this.comment !== undefined &&
      this.comment !== null &&
      this.comment.trim().length > 0
    )
  }

  public hasRegex (): boolean {
    return (
      this.regex !== undefined &&
      this.regex !== null &&
      this.regex.trim().length > 0 &&
      this.regex !== 'URL'
    )
  }

  public isSelect (): boolean {
    return this.allowedValues && this.allowedValues.length > 0
  }

  public isTypeString (): boolean {
    return this.type === 'java.lang.String'
  }

  public isTypeBoolean (): boolean {
    return this.type === 'java.lang.Boolean'
  }

  public isTypeArray (): boolean {
    return this.type === 'java.util.ArrayList'
  }

  public isTypeMap (): boolean {
    return this.type === 'java.util.LinkedHashMap'
  }

  public isTypeInteger (): boolean {
    return this.type === 'java.lang.Integer'
  }
}
