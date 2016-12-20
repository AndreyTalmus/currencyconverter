package ru.homeproduction.andrey.currencyconverter;

import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class XmlParser {
    private static final String ns = null;

    public List<Entry> parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            in.close();
        }
    }

    private List<Entry> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Entry> entries = new ArrayList<Entry>();

        parser.require(XmlPullParser.START_TAG, ns, "ValCurs");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if (name.equals("Valute")) {
                entries.add(readEntry(parser));
            } else {
                skip(parser);
            }
        }
        return entries;
    }

    public static class Entry {
        public final String numcode;
        public final String charcode;
        public final String nominal;
        public final String name;
        public final String value;

        private Entry(String numcode, String charcode, String nominal, String name, String value) {
            this.numcode = numcode;
            this.charcode = charcode;
            this.nominal = nominal;
            this.name = name;
            this.value = value;
        }
    }

    private Entry readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "Valute");
        String numcode = null;
        String charcode = null;
        String nominal = null;
        String name = null;
        String value = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String line = parser.getName();
            if (line.equals("NumCode")) {
                numcode = readNumcode(parser);
            } else if (line.equals("CharCode")) {
                charcode = readCharcode(parser);
            } else if (line.equals("Nominal")) {
                nominal = readNominal(parser);
            } else if (line.equals("Name")) {
                name = readName(parser);
            } else if (line.equals("Value")) {
                value = readValue(parser);
            } else {
                skip(parser);
            }
        }
        return new Entry(numcode, charcode, nominal, name , value);
    }

    private String readNumcode(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "NumCode");
        String numcode = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "NumCode");
        return numcode;
    }

    private String readCharcode(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "CharCode");
        String charcode = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "CharCode");
        return charcode;
    }

    private String readNominal(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "Nominal");
        String nominal = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "Nominal");
        return nominal;
    }

    private String readName(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "Name");
        String name = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "Name");
        return name;
    }

    private String readValue(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "Value");
        String value = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "Value");
        return value;
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
            case XmlPullParser.END_TAG:
                    depth--;
                    break;
            case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
