---
license: Apache-2.0
module: http4k-format-jackson-csv
---

# http4k-format-jackson-csv Reference

CSV format module backed by Jackson's `jackson-dataformat-csv`. Works with lists of typed objects and CSV schemas.

## Construction

```kotlin
// Default singleton (includes withStandardMappings())
val csv = JacksonCsv

// Custom configuration — same pattern as other Jackson modules
```

## Schema

Writing requires a schema defining columns; generate one automatically from a data class. Reading defaults to an empty, header-driven schema, so columns can be reordered or contain extras not present on the target type:

```kotlin
val writeSchema = JacksonCsv.defaultWriteSchema<MyRecord>()  // CsvSchema with headers from class fields
val readSchema = JacksonCsv.defaultReadSchema()               // header-only schema, columns inferred from CSV
```

## Lens Integration

CSV lenses work with `List<T>` since CSV is inherently tabular:

```kotlin
// Typed body lens — note List<T>, not T
val lens = Body.auto<MyRecord>().toLens()
val records: List<MyRecord> = lens(request)
val response = Response(OK).with(lens of listOf(record1, record2))

// Convenience extensions
val response = Response(OK).csv(listOf(record1, record2))
val records: List<MyRecord> = request.csv<MyRecord>()

// BiDiMapping — separate schemas for reading and writing, both optional
val mapping = JacksonCsv.asBiDiMapping<MyRecord>(readSchema = readSchema, writeSchema = writeSchema)
```

## Read/Write Functions

For direct conversion without HTTP:

```kotlin
// Write objects to CSV string
val csvString: String = JacksonCsv.writeCsv(listOf(record1, record2), writeSchema)

// Read CSV string to objects
val records: List<MyRecord> = JacksonCsv.readCsv<MyRecord>(csvString, readSchema)

// Get reusable reader/writer functions
val writer: (List<MyRecord>) -> String = JacksonCsv.writerFor<MyRecord>(writeSchema)
val reader: (String) -> List<MyRecord> = JacksonCsv.readerFor<MyRecord>(readSchema)
```

## Column Ordering

Control CSV column order with `@JsonPropertyOrder`:

```kotlin
@JsonPropertyOrder("name", "age", "email")
data class Person(val name: String, val age: Int, val email: String)
```

## Gotchas

- **Content type is `TEXT_CSV`**: Default content type is `text/csv`.
- **Always List\<T\>**: CSV body lenses serialize/deserialize `List<T>`, not single objects.
- **Schema includes headers**: both `defaultWriteSchema<T>()` and `defaultReadSchema()` generate a schema with column headers. The first CSV row will be headers.
- **Empty lists produce headers only**: Writing an empty list outputs just the header row.
- **Column order matters for writing, not reading**: Use `@JsonPropertyOrder` to ensure consistent column ordering when writing. Reading uses `defaultReadSchema()` by default, which derives columns from the CSV's own header row, so input columns can be reordered or contain extra/unknown columns not present on the target type.
- **No JSON node manipulation**: Extends `AutoMarshalling` directly — CSV-only operations.
