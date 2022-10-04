import com.wdtinc.mapbox_vector_tile.VectorTile;
import com.wdtinc.mapbox_vector_tile.adapt.jts.*;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerBuild;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerParams;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerProps;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class WriteTileGeometry {
  public static void main(String[] args) {

    GeometryFactory geomFactory = new GeometryFactory();
    LineString line = geomFactory.createLineString(new Coordinate[]{
        new Coordinate(0, 0),
        new Coordinate(1, 0),
        new Coordinate(1, 1),
        new Coordinate(0, 1)
    });

    IGeometryFilter acceptAllGeomFilter = geometry -> true;
    Envelope tileEnvelope = new Envelope(0d, 100d, 0d, 100d); // TODO: Your tile extent here
    MvtLayerParams layerParams = new MvtLayerParams(); // Default extent

    TileGeomResult tileGeom = JtsAdapter.createTileGeom(
        line, // Your geometry
        tileEnvelope,
        geomFactory,
        layerParams,
        acceptAllGeomFilter);

    VectorTile.Tile mvt = encodeMvt(layerParams, tileGeom);
    try {
      Files.write(Path.of("test.mvt"), mvt.toByteArray());
    } catch (IOException e) {
      System.err.println(e.getMessage());
    }
  }

  private static String TEST_LAYER_NAME = "layerNameHere";

  private static VectorTile.Tile encodeMvt(MvtLayerParams mvtParams, TileGeomResult tileGeom) {

    // Build MVT
    final VectorTile.Tile.Builder tileBuilder = VectorTile.Tile.newBuilder();

    // Create MVT layer
    final VectorTile.Tile.Layer.Builder layerBuilder = MvtLayerBuild.newLayerBuilder(TEST_LAYER_NAME, mvtParams);
    final MvtLayerProps layerProps = new MvtLayerProps();
    IUserDataConverter ignoreUserData = new UserDataIgnoreConverter();

    // MVT tile geometry to MVT features
    final List<VectorTile.Tile.Feature> features = JtsAdapter.toFeatures(tileGeom.mvtGeoms, layerProps, ignoreUserData);
    layerBuilder.addAllFeatures(features);
    MvtLayerBuild.writeProps(layerBuilder, layerProps);

    // Build MVT layer
    final VectorTile.Tile.Layer layer = layerBuilder.build();

    // Add built layer to MVT
    tileBuilder.addLayers(layer);

    /// Build MVT
    return tileBuilder.build();
  }
}
