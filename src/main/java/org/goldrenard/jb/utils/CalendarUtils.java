/*
 * This file is part of Program JB.
 *
 * Program JB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Program JB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Program JB. If not, see <http://www.gnu.org/licenses/>.
 */
package org.goldrenard.jb.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class CalendarUtils {

    private static final Logger log = LoggerFactory.getLogger(CalendarUtils.class);

    public static String formatTime(String formatString, long msSinceEpoch) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(formatString);
        Calendar cal = Calendar.getInstance();
        dateFormat.setCalendar(cal);
        return dateFormat.format(new Date(msSinceEpoch));
    }

    public static int timeZoneOffset() {
        Calendar cal = Calendar.getInstance();
        return (cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET)) / (60 * 1000);
    }

    public static String year() {
        Calendar cal = Calendar.getInstance();
        return String.valueOf(cal.get(Calendar.YEAR));
    }

    public static String date() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMMMMMMM dd, yyyy");
        dateFormat.setCalendar(cal);
        return dateFormat.format(cal.getTime());
    }

    public static String date(String jformat, String locale, String timezone) {
        if (jformat == null) {
            jformat = "EEE MMM dd HH:mm:ss zzz yyyy";
        }
        if (locale == null) {
            locale = Locale.US.getISO3Country();
        }
        if (timezone == null) {
            timezone = TimeZone.getDefault().getDisplayName();
        }
        Date date = new Date();
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(jformat);
            return simpleDateFormat.format(date);
        } catch (Exception e) {
            log.error("CalendarUtils.date Bad date: [Format = {}, Locale = {}, Timezone = {}]", jformat, locale, timezone, e);
        }
        return date.toString();
    }
}
