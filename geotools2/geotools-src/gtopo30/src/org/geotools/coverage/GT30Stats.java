/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.coverage;

import java.io.*;
import java.net.*;
import java.util.*;


/**
 * This class parses the STX GTopo30 statistics file and allows to retrieve its contents
 *
 * @author aaime
 */
class GT30Stats {
    int minimum;
    int maximum;
    double average;
    double stddev;

    /**
     * Creates a new instance of GT30Stats
     *
     * @param statsURL URL pointing to the statistics (STX) file
     *
     * @throws FileNotFoundException
     * @throws IOException
     * @throws NumberFormatException if the file content is not correct
     */
    public GT30Stats(URL statsURL) throws FileNotFoundException, IOException, NumberFormatException {
        String path = statsURL.getFile();
        File stats = new File(path);

        BufferedReader reader = new BufferedReader(new FileReader(stats));
        String line = reader.readLine();
        StringTokenizer stok = new StringTokenizer(line, " ");

        // just parse one byte. if the support for this format will
        // be extended, we'll need to add support for multiple bands
        Integer.parseInt(stok.nextToken()); // band
        minimum = Integer.parseInt(stok.nextToken());
        maximum = Integer.parseInt(stok.nextToken());
        average = Double.parseDouble(stok.nextToken());
        stddev = Double.parseDouble(stok.nextToken());
    }

    public int getMin() {
        return minimum;
    }

    public int getMax() {
        return maximum;
    }

    public double getAverage() {
        return average;
    }

    public double getStdDev() {
        return stddev;
    }
}
