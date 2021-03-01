package org.brunel.fyp.langserver;

import com.github.javaparser.printer.DefaultPrettyPrinter;
import com.github.javaparser.printer.configuration.DefaultConfigurationOption;
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration;
import com.github.javaparser.printer.configuration.PrinterConfiguration;

public class MJGAPrinterConfig {
    private static DefaultPrettyPrinter INSTANCE;

    public static PrinterConfiguration getConfig() {
        if (INSTANCE == null) {
            INSTANCE = new DefaultPrettyPrinter();
            INSTANCE.getConfiguration().addOption(new DefaultConfigurationOption(DefaultPrinterConfiguration.ConfigOption.ORDER_IMPORTS));
            INSTANCE.getConfiguration().addOption(new DefaultConfigurationOption(DefaultPrinterConfiguration.ConfigOption.PRINT_COMMENTS));
            INSTANCE.getConfiguration().addOption(new DefaultConfigurationOption(DefaultPrinterConfiguration.ConfigOption.PRINT_JAVADOC));
            INSTANCE.getConfiguration().addOption(new DefaultConfigurationOption(DefaultPrinterConfiguration.ConfigOption.SPACE_AROUND_OPERATORS));
        }

        return INSTANCE.getConfiguration();
    }
}
