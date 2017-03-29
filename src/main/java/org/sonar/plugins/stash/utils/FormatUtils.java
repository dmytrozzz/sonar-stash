package org.sonar.plugins.stash.utils;

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.ParseException;

public class FormatUtils {

    private static final Logger LOGGER = Loggers.get(FormatUtils.class);

    // Hiding implicit public constructor with an explicit private one (squid:S1118)
    private FormatUtils() {
    }

    /**
     * Format double with pattern #.#
     * For instance, 1.2345 => 1.2
     */
    public static double formatDouble(double d) {
        double result = d;

        try {
            DecimalFormat df = new DecimalFormat("0.0");
            String format = df.format(d);
            Number number = df.parse(format);

            result = number.doubleValue();

        } catch (ParseException e) {
            LOGGER.error(MessageFormat.format("Unable to format double {0}: {1}", d, e.getMessage()));
        }

        return result;
    }
}
