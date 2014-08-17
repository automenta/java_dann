/*
 * Encog(tm) Core v3.2 - Java Version
 * http://www.heatonresearch.com/encog/
 * https://github.com/encog/encog-java-core

 * Copyright 2008-2013 Heaton Research, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For more information on Heaton Research copyrights, licenses
 * and trademarks visit:
 * http://www.heatonresearch.com/copyright
 */
package syncleus.dann.data.market.loader;

import syncleus.dann.data.file.csv.CSVFormat;
import syncleus.dann.data.file.csv.ReadCSV;
import syncleus.dann.data.market.MarketDataType;
import syncleus.dann.data.market.TickerSymbol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.*;
import syncleus.dann.data.http.FormUtility;

/**
 * This class loads financial data from Yahoo. One caution on Yahoo data.
 *
 * @author jheaton
 */
public class YahooFinanceLoader implements MarketLoader {

    /**
     * This method builds a URL to load data from Yahoo Finance for a neural
     * network to train with.
     *
     * @param ticker The ticker symbol to access.
     * @param from   The beginning date.
     * @param to     The ending date.
     * @return The UEL
     * @throws IOException An error accessing the data.
     */
    private static URL buildURL(final TickerSymbol ticker, final Date from,
                                final Date to) throws IOException {
        // process the dates
        final Calendar calendarFrom = Calendar.getInstance();
        calendarFrom.setTime(from);
        final Calendar calendarTo = Calendar.getInstance();
        calendarTo.setTime(to);

        // construct the URL
        final OutputStream os = new ByteArrayOutputStream();
        final FormUtility form = new FormUtility(os, null);
        form.add("s", ticker.getSymbol().toUpperCase());
        form.add("a", String.valueOf(calendarFrom.get(Calendar.MONTH)));
        form.add("b", String.valueOf(calendarFrom.get(Calendar.DAY_OF_MONTH)));
        form.add("c", String.valueOf(calendarFrom.get(Calendar.YEAR)));
        form.add("d", String.valueOf(calendarTo.get(Calendar.MONTH)));
        form.add("e", String.valueOf(calendarTo.get(Calendar.DAY_OF_MONTH)));
        form.add("f", String.valueOf(calendarTo.get(Calendar.YEAR)));
        form.add("g", "d");
        form.add("ignore", ".csv");
        os.close();
        final String str = "http://ichart.finance.yahoo.com/table.csv?"
                + os.toString();
        return new URL(str);
    }

    /**
     * Load the specified financial data.
     *
     * @param ticker     The ticker symbol to load.
     * @param dataNeeded The financial data needed.
     * @param from       The beginning date to load data from.
     * @param to         The ending date to load data to.
     * @return A collection of LoadedMarketData objects that represent the data
     * loaded.
     */
    @Override
    public Collection<LoadedMarketData> load(final TickerSymbol ticker,
                                             final Set<MarketDataType> dataNeeded, final Date from, final Date to) {
        try {
            final Collection<LoadedMarketData> result = new ArrayList<>();
            final URL url = buildURL(ticker, from, to);
            final InputStream is = url.openStream();
            final ReadCSV csv = new ReadCSV(is, true, CSVFormat.ENGLISH);

            while (csv.next()) {
                final Date date = csv.getDate("date");
                final double adjClose = csv.getDouble("adj close");
                final double open = csv.getDouble("open");
                final double close = csv.getDouble("close");
                final double high = csv.getDouble("high");
                final double low = csv.getDouble("low");
                final double volume = csv.getDouble("volume");

                final LoadedMarketData data = new LoadedMarketData(date, ticker);
                data.setData(MarketDataType.ADJUSTED_CLOSE, adjClose);
                data.setData(MarketDataType.OPEN, open);
                data.setData(MarketDataType.CLOSE, close);
                data.setData(MarketDataType.HIGH, high);
                data.setData(MarketDataType.LOW, low);
                data.setData(MarketDataType.OPEN, open);
                data.setData(MarketDataType.VOLUME, volume);
                result.add(data);
            }

            csv.close();
            is.close();
            return result;
        } catch (final IOException e) {
            throw new LoaderError(e);
        }
    }
}
