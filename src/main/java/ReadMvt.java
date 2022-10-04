import com.wdtinc.mapbox_vector_tile.adapt.jts.MvtReader;
import com.wdtinc.mapbox_vector_tile.adapt.jts.TagKeyValueMapConverter;
import com.wdtinc.mapbox_vector_tile.adapt.jts.model.JtsLayer;
import com.wdtinc.mapbox_vector_tile.adapt.jts.model.JtsMvt;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ReadMvt {
  public static void main(String[] args) throws IOException {
    GeometryFactory geomFactory = new GeometryFactory();

    for(String filename : Arrays.asList("data/10.pbf", "test.mvt")) {
      JtsMvt jtsMvt = MvtReader.loadMvt(
          new File(filename),
          geomFactory,
          new TagKeyValueMapConverter());

      System.out.println("======");
      System.out.println(filename);
      for (String layerName : jtsMvt.getLayersByName().keySet()) {
        System.out.println(layerName);
        JtsLayer layer = jtsMvt.getLayer(layerName);
        System.out.println(layer.getExtent());

        Collection<Geometry> coll = layer.getGeometries();
        for (Geometry geometry : coll) {
          System.out.println(geometry);
        }
        // attributes는 어디에 있지?
      }
    }


    /*
    // Allow negative-area exterior rings with classifier
    // (recommended for Mapbox compatibility)
    JtsMvt jtsMvt2 = MvtReader.loadMvt(
        new File("path/to/your.mvt"),
        geomFactory,
        new TagKeyValueMapConverter(),
        MvtReader.RING_CLASSIFIER_V1);
    */
  }
}
