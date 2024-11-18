package de.sub.goobi.helper;

import java.time.LocalDateTime;

/**
 * Due to PowerMock mocking issues of LocalDateTime.now(), this class is used to retrieve local dates and times and mocked in testing.
 *
 * Feel free to extend this class with any DateTime related methods you might find useful.
 */
public class DateTimeHelper {
    public static LocalDateTime localDateTimeNow() {
        return LocalDateTime.now();
    }
}
