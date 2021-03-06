package com.ictpoker.ixi.Commons;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;

public class Properties extends java.util.Properties {

    private final static Logger LOGGER = LogManager.getLogger(Properties.class);
    private static final String LIST_DELIMITER = ",";

    // Property names
    private final static String NAME = "name";
    private final static String HOST = "host";
    private final static String REPORT_PORT = "reportPort";
    private final static String EXTERNAL_REPORT_PORT = "externalReportPort";
    private final static String NEIGHBORS = "neighbors";

    // Property defaults
    private final static String DEFAULT_NAME = "";
    private final static String DEFAULT_HOST = "0.0.0.0";
    private final static int    DEFAULT_REPORT_PORT = 1338;
    private final static int    DEFAULT_EXTERNAL_REPORT_PORT = -1;
    private final static String DEFAULT_NEIGHBORS = "";

    public Properties() {
        setRequiredProps();
    }

    public Properties(final String propertiesFilePath) {

        File propertiesLocation = new File(Constants.PROPERTIES_LOCATION);
        if (propertiesLocation.mkdirs()) {
            LOGGER.info(String.format("Created properties folder at: %s\n", propertiesLocation.getAbsolutePath()));
        }

        load(propertiesFilePath);
        setRequiredProps();
        validateProps();
    }

    /**
     * @return the reportPort
     */
    public int getReportPort() {
        return Integer.parseInt(getProperty(REPORT_PORT, Integer.toString(DEFAULT_REPORT_PORT)).trim());
    }

    /**
     * @param reportPort the reportPort to set
     */
    public void setReportPort(final int reportPort) {
        put(REPORT_PORT, Integer.toString(reportPort));
    }

    /**
     * @param externalReportPort the externalReportPort to set
     */
    public void setExternalReportPort(final int externalReportPort) {
        put(EXTERNAL_REPORT_PORT, Integer.toString(externalReportPort));
    }

    /**
     * * @return the externalReportPort
     */
    public int getExternalReportPort() {
        return Integer.parseInt(getProperty(EXTERNAL_REPORT_PORT, Integer.toString(DEFAULT_EXTERNAL_REPORT_PORT)).trim());
    }

    /**
     * @return the host
     */
    public String getHost() {
        return getProperty(HOST, DEFAULT_HOST).trim();
    }

    /**
     * @param host the host to set
     */
    public void setHost(final String host) {
        put(HOST, host);
    }

    /**
     * @return the name
     */
    public String getName() {
        return getProperty(NAME, DEFAULT_NAME).trim();
    }

    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        put(NAME, name);
    }

    public List<InetSocketAddress> getNeighborAddresses() {
        return neighborsFromString(getProperty(NEIGHBORS));
    }

    public void load(final String propertiesFilePath) {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(propertiesFilePath);
            load(inputStream);
        } catch (final FileNotFoundException e) {
            LOGGER.info(String.format("Could not read properties file '%s', therefore a new one will be created.",
                    propertiesFilePath));
        } catch (final IOException e) {
            LOGGER.error(String.format("Failed to open input stream of file: '%s'",
                    propertiesFilePath));
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LOGGER.error(String.format("Failed to close input stream of file: '%s'",
                            propertiesFilePath));
                    e.printStackTrace();
                }
            }
        }
    }

    public void store(final String propertiesFilePath) {
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(propertiesFilePath);
            store(outputStream, null);
        } catch (final FileNotFoundException e) {
            LOGGER.error(String.format("Failed to open output stream of file: '%s'",
                    propertiesFilePath));
            e.printStackTrace();
        } catch (final IOException e) {
            LOGGER.error(String.format("Failed to save properties to file: '%s'",
                    propertiesFilePath));
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    LOGGER.error(String.format("Failed to close output stream of file: '%s'",
                            propertiesFilePath));
                    e.printStackTrace();
                }
            }
        }
    }

    private void setRequiredProps() {
        if (get(NAME) == null) {
            put(NAME, DEFAULT_NAME);
        }
        if (get(REPORT_PORT) == null) {
            put(REPORT_PORT, Integer.toString(DEFAULT_REPORT_PORT));
        }
        if (get(NEIGHBORS) == null) {
            put(NEIGHBORS, DEFAULT_NEIGHBORS);
        }
    }

    private void validateProps() {
        if (!getName().matches(".+\\s\\(ict-\\d+\\)")) {
            try {
                throw new InvalidPropertiesFormatException(String.format("Unable to set %s. Please follow the naming convention: %s",
                        NAME,
                        "\"<name> (ict-<number>)\""));
            } catch (InvalidPropertiesFormatException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public Enumeration keys() {
        final Enumeration keysEnum = super.keys();
        final Vector<String> keyList = new Vector<>();
        while(keysEnum.hasMoreElements()){
            keyList.add((String)keysEnum.nextElement());
        }
        Collections.sort(keyList);
        return keyList.elements();
    }

    private static List<InetSocketAddress> neighborsFromString(final String string) {
        final List<String> addresses = stringListFromString(string);

        final List<InetSocketAddress> neighbors = new LinkedList<>();
        for (final String address : addresses) {
            try {
                neighbors.add(inetSocketAddressFromString(address));
            } catch (final Throwable t) {
                LOGGER.error(String.format("Invalid neighbor address: '%s'", address));
            }
        }
        return neighbors;
    }

    private static List<String> stringListFromString(final String string) {
        final List<String> stringList = new LinkedList<>();
        for (final String element : string.split(LIST_DELIMITER)) {
            if (element.length() == 0)
                continue;
            stringList.add(element);
        }
        return stringList;
    }

    private static InetSocketAddress inetSocketAddressFromString(final String address) {
        int portColonIndex;
        for (portColonIndex = address.length() - 1; address.charAt(portColonIndex) != ':'; portColonIndex--);
        final String hostString = address.substring(0, portColonIndex);
        final int port = Integer.parseInt(address.substring(portColonIndex + 1, address.length()));
        return new InetSocketAddress(hostString, port);
    }
}