package org.greencheek.spring.rest.benchmarks;

import org.greencheek.spring.rest.JettyHttpsTestServer;
import org.greencheek.spring.rest.SSLCachingHttpComponentsClientHttpRequestFactory;
import org.greencheek.utils.ClientSSLSetupUtil;
import org.openjdk.jmh.annotations.*;

import org.springframework.web.client.RestTemplate;


/**
 * User: dominictootell
 * Date: 04/08/2013
 * Time: 11:17
 *
 * Benchmark                                            Mode Thr    Cnt  Sec         Mean   Mean error    Units
 o.g.s.r.b.BenchmarkGet.benchmarkGetNonSSLCaching    thrpt   2      1    5        0.762          NaN ops/msec
 o.g.s.r.b.BenchmarkGet.benchmarkGetNonSSLCaching    thrpt   4      1    5        1.097          NaN ops/msec
 o.g.s.r.b.BenchmarkGet.benchmarkGetNonSSLCaching    thrpt   8      1    5        1.118          NaN ops/msec
 o.g.s.r.b.BenchmarkGet.benchmarkGetNonSSLCaching    thrpt  16      1    5        1.112          NaN ops/msec
 o.g.s.r.b.BenchmarkGet.benchmarkGetSSLCaching       thrpt   2      1    5        1.190          NaN ops/msec
 o.g.s.r.b.BenchmarkGet.benchmarkGetSSLCaching       thrpt   4      1    5        2.488          NaN ops/msec
 o.g.s.r.b.BenchmarkGet.benchmarkGetSSLCaching       thrpt   8      1    5        3.137          NaN ops/msec
 o.g.s.r.b.BenchmarkGet.benchmarkGetSSLCaching       thrpt  16      1    5        3.130          NaN ops/msec
 */
public class BenchmarkGet {

    static final private JettyHttpsTestServer testHttpsServer = new JettyHttpsTestServer();
    static final private ClientSSLSetupUtil clientSSL = new ClientSSLSetupUtil();
    static final private RestTemplate sslCachingTemplate;
    static final private RestTemplate nonSSLCachingTemplate;
    static final private String urlString;
    static final private SSLCachingHttpComponentsClientHttpRequestFactory sslCachingFactory;
    static final private SSLCachingHttpComponentsClientHttpRequestFactory nonSSLCachingFactory;

    static {
        setup();
        sslCachingFactory = new SSLCachingHttpComponentsClientHttpRequestFactory(true);
        nonSSLCachingFactory = new SSLCachingHttpComponentsClientHttpRequestFactory(false);
        sslCachingTemplate = new RestTemplate(sslCachingFactory);
        nonSSLCachingTemplate= new RestTemplate(nonSSLCachingFactory);

        urlString = testHttpsServer.getBaseUrl() + "/methods/get";
    }

    public static void setup() {
        try {
            testHttpsServer.startSSLServer();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        clientSSL.setup();


        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
               shutdown();
            }
        });

    }

    public static void shutdown() {
        testHttpsServer.stopHttpsServer();
        sslCachingFactory.destroy();
        nonSSLCachingFactory.destroy();
    }

    public static void main(String[] args) {
        sslCachingTemplate.getForObject(urlString,String.class);
        sslCachingTemplate.getForObject(urlString,String.class);

        nonSSLCachingTemplate.getForObject(urlString,String.class);
        nonSSLCachingTemplate.getForObject(urlString,String.class);
        shutdown();


    }

//    @TearDown
//    public void tearDown() {
//
//        clientSSL.tearDown();
//
//        try {
//            testHttpsServer.stopHttpsServer();
//        } catch (Exception e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
//
//
//    }

    @GenerateMicroBenchmark
    public String benchmarkGetSSLCaching() {
        return sslCachingTemplate.getForObject(urlString,String.class);
    }

    @GenerateMicroBenchmark
    public String benchmarkGetNonSSLCaching() {
        return nonSSLCachingTemplate.getForObject(urlString,String.class);
    }
}
