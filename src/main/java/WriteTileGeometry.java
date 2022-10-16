import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.wdtinc.mapbox_vector_tile.VectorTile;
import com.wdtinc.mapbox_vector_tile.adapt.jts.*;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerBuild;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerParams;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerProps;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WriteTileGeometry {
  private static String TEST_LAYER_NAME = "layerNameHere";

  public static void main(String[] args) {

    // 1
    GeometryFactory geomFactory = new GeometryFactory();
    LineString line = geomFactory.createLineString(new Coordinate[]{
        new Coordinate(0, 0),
        new Coordinate(1, 0),
        new Coordinate(1, 1),
        new Coordinate(0, 1)
    });
    Map<String, Object> attributes = new LinkedHashMap<>();
    attributes.put("id", 1);
    attributes.put("name", "a");
    line.setUserData(attributes);

    // 2. TileGeomResult
    IGeometryFilter acceptAllGeomFilter = geometry -> true;
    Envelope tileEnvelope = new Envelope(0d, 100d, 0d, 100d); // TODO: Your tile extent here
    MvtLayerParams layerParams = new MvtLayerParams(); // Default extent

    TileGeomResult tileGeom = JtsAdapter.createTileGeom(
        line, // Your geometry
        tileEnvelope,
        geomFactory,
        layerParams,
        acceptAllGeomFilter);

    // 3. Features
    // Build MVT
    final VectorTile.Tile.Builder tileBuilder = VectorTile.Tile.newBuilder();

    {
      // Create MVT layer
      final VectorTile.Tile.Layer.Builder layerBuilder = MvtLayerBuild.newLayerBuilder(TEST_LAYER_NAME, layerParams);
      final MvtLayerProps layerProps = new MvtLayerProps();
      //IUserDataConverter ignoreUserData = new UserDataIgnoreConverter();
      IUserDataConverter userDataConverter = new UserDataKeyValueMapConverter("id");

      // MVT tile geometry to MVT features
      final List<VectorTile.Tile.Feature> features = JtsAdapter.toFeatures(tileGeom.mvtGeoms, layerProps, userDataConverter);
      layerBuilder.addAllFeatures(features);
      MvtLayerBuild.writeProps(layerBuilder, layerProps);

      System.out.println(layerProps.getKeys()); //[id, name]
      System.out.println(layerProps.getVals()); //[1, a]
      // Build MVT layer
      final VectorTile.Tile.Layer layer = layerBuilder.build();

      // Add built layer to MVT
      tileBuilder.addLayers(layer);
    }

    // 4. MVT
    VectorTile.Tile mvt = tileBuilder.build();
    try {
      Files.write(Path.of("test.mvt"), mvt.toByteArray());
    } catch (IOException e) {
      System.err.println(e.getMessage());
    }
  }

}
