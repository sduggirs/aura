/*
 * Copyright (C) 2013 salesforce.com, inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.auraframework.impl.java.type.converter;

import java.util.Date;

import org.auraframework.annotations.Annotations.ServiceComponent;
import org.auraframework.impl.java.type.LocalizedConverter;
import org.auraframework.util.AuraLocale;
import org.auraframework.util.date.DateService;
import org.auraframework.util.date.DateServiceImpl;

@ServiceComponent
public class LocalizedDateToStringConverter implements LocalizedConverter<Date, String> {
    @Override
    public String convert(Date value, AuraLocale locale) {
        DateService dateService = DateServiceImpl.get();
        return dateService.getDateTimeISO8601Converter().format(value, locale.getTimeZone());
    }

    @Override
    public String convert(Date value) {
        if (value == null) {
            return null;
        }
        DateService dateService = DateServiceImpl.get();
        return dateService.getDateTimeISO8601Converter().format(value);
    }

    @Override
    public Class<Date> getFrom() {
        return Date.class;
    }

    @Override
    public Class<String> getTo() {
        return String.class;
    }

    @Override
    public Class<?>[] getToParameters() {
        return null;
    }
}
