package ch.coachpoulpy.server.admin

import dev.forkhandles.values.StringValue
import dev.forkhandles.values.StringValueFactory
import dev.forkhandles.values.regex

/**
 * An email address that prints masked (`a***@example.com`), so it can't reach a log or an audit row
 * by accident. Read the real one with `value`, only where it is needed (values4k).
 */
class EmailAddress private constructor(value: String) : StringValue(value, { take(1) + "***@" + substringAfter('@') }) {
    companion object : StringValueFactory<EmailAddress>(::EmailAddress, "[^@\\s]+@[^@\\s]+".regex)
}
