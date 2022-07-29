package org.aliyun;
import java.util.ArrayList;
import java.util.Arrays;

import org.aliyun.gsl_client.Decoder;
import org.aliyun.gsl_client.Query;
import org.aliyun.gsl_client.Value;
import org.aliyun.gsl_client.ValueBuilder;
import org.aliyun.gsl_client.exception.UserException;
import org.aliyun.gsl_client.parser.Plan;
import org.aliyun.gsl_client.predict.EgoGraph;
import org.aliyun.gsl_client.predict.TFPredictClient;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class PredictClientTest extends TestCase {
  private ArrayList<Integer> vtypes = new ArrayList<Integer>(Arrays.asList(0, 1, 1));
  private ArrayList<Integer> vops = new ArrayList<Integer>(Arrays.asList(2, 4, 4));
  private ArrayList<Integer> hops = new ArrayList<Integer>(Arrays.asList(1, 25, 10));
  private ArrayList<Integer> eops = new ArrayList<Integer>(Arrays.asList(0, 1, 3));

  public static Test suite() {
    return new TestSuite(PredictClientTest.class);
  }

  private Value generatedValue(Decoder d) {
    long inputVid = 0L;
    Plan plan = new Plan();
    Query query = new Query(plan);
    ValueBuilder builder = new ValueBuilder(query, 1 + 1 + 25 + 25 + 250, d, 0L);
    builder.addVopRes(2, (short)0, inputVid, 1);
    builder.addEopRes(1, (short)2, (short)0, (short)1, inputVid, 25);
    for (int i = 0; i < 25; ++i) {
      builder.addVopRes(4, (short)1, inputVid + i, 1);
    }
    for (int i = 0; i < 25; ++i) {
      builder.addEopRes(3, (short)3, (short)1, (short)1, inputVid + i, 10);
    }
    for (int i = 0; i < 25 * 10; ++i) {
      builder.addVopRes(4, (short)1, inputVid + i, 1);
    }
    Value val = builder.finish();
    return val;
  }

  public void testPredict() throws UserException {
    Decoder decoder = new Decoder();
    decoder.addFeatDesc(0,
                        new ArrayList<String>(Arrays.asList("float")),
                        new ArrayList<Integer>(Arrays.asList(1433)));
    decoder.addFeatDesc(1,
                        new ArrayList<String>(Arrays.asList("float")),
                        new ArrayList<Integer>(Arrays.asList(1433)));

    TFPredictClient client = new TFPredictClient(decoder, "localhost", 9000);
    Value content = generatedValue(decoder);
    EgoGraph egoGraph = content.getEgoGraph(vtypes, vops, hops, eops);
    ArrayList<Integer> phs = new ArrayList<Integer>(Arrays.asList(0, 2, 3));
    client.predict("ego_sage_model", 1, egoGraph);
  }
}
