package de.doridian.logstashlogger.config;

import de.doridian.logstashlogger.LogstashLogger;

import java.io.FileNotFoundException;
import java.io.FileReader;

public class ConfigFileReader extends FileReader {
	public ConfigFileReader(String file) throws FileNotFoundException {
		super(LogstashLogger.instance.getDataFolder() + "/" + file);
	}
}
