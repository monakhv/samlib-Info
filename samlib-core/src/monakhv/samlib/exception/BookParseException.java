/*
 * Copyright 2013 Dmitry Monakhov.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package monakhv.samlib.exception;

/**
 * Error parsing the book - skip it
 * @author monakhv
 */
public class BookParseException extends SamLibException {

    /**
     * Creates a new instance of
     * <code>BookParseException</code> without detail message.
     */
    public BookParseException() {
    }

    /**
     * Constructs an instance of
     * <code>BookParseException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public BookParseException(String msg) {
        super(msg);
    }
}
