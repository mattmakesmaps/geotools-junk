//http://docs.geotools.org/latest/userguide/tutorial/feature/csv2shp.html
package org.geotools.tutorial.feature;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.UIManager;

import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;


public class Csv2Shape {
    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

        File file = JFileDataStoreChooser.showOpenFile("csv",null);
        if (file == null) {
            return;
        }

        // CREATE FeatureType
        final SimpleFeatureType TYPE = DataUtilities.createType("Location",
                "the_geom:Point:srid=4326," + // <- the geometry attribute: Point type
                        "name:String," +   // <- a String attribute
                        "number:Integer"   // a number attribute
        );
        System.out.println("TYPE:"+TYPE);

        // Create Features

        List<SimpleFeature> features = new ArrayList<>();

        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();

        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);

        try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // get header from CSV
            String line = reader.readLine();
            System.out.println("Header: " + line);

            /**
             * Initialze `line` with reader.readLine()
             * Exit condition is when line != null
             * Increment loop by re-assigning `line` with
             *   new call to `reader.readLine()`
             */
            for (line = reader.readLine(); line != null; line = reader.readLine()) {
                if (line.trim().length() > 0) {
                    String tokens[] = line.split("\\,");

                    // parse stuff from CSV
                    double latitude = Double.parseDouble(tokens[0]);
                    double longitude = Double.parseDouble(tokens[1]);
                    String name = tokens[2].trim();
                    int number = Integer.parseInt(tokens[3].trim());

                    // build Point from lon/lat
                    Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));

                    // build feature from Point + tabular attributes.
                    featureBuilder.add(point);
                    featureBuilder.add(name);
                    featureBuilder.add(number);
                    SimpleFeature feature = featureBuilder.buildFeature(null);
                    features.add(feature);
                }
            }
        }

        // Create a shapefile from FeatureCollection

        File newFile = getNewShapeFile(file);

        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

        // MK TODO: Review `Map` syntax.
        Map<String, Serializable> params = new HashMap<>();
        params.put("url", newFile.toURI().toURL());
        params.put("create spatial index", Boolean.TRUE);

        // MK TODO: what's going on with (ShapefileDataStore)?
        ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);

        newDataStore.createSchema(TYPE);

        // Write the feature data to the shapefile
    }
}
