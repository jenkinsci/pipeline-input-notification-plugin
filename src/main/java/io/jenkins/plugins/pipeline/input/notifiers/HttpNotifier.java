package io.jenkins.plugins.pipeline.input.notifiers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import hudson.Extension;
import io.jenkins.plugins.pipeline.input.models.InputNotificationEvent;
import jenkins.model.Jenkins;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author jasper
 */
public class HttpNotifier extends InputNotifier {
    private static final Logger LOGGER = Logger.getLogger(HttpNotifier.class.getName());

    private String httpEndpoint;
    private boolean ignoreSSL;

    @DataBoundConstructor
    public HttpNotifier() {
        super();
    }

    public String getHttpEndpoint() {
        return httpEndpoint;
    }

    @DataBoundSetter
    public void setHttpEndpoint(String httpEndpoint) {
        this.httpEndpoint = httpEndpoint;
    }

    /**
     * Gets whether to ignore SSL verify.
     *
     * @return true if ignore SSL is enabled
     */
    public boolean getIgnoreSSL() {
        return ignoreSSL;
    }

    @DataBoundSetter
    public void setIgnoreSSL(boolean ignoreSSL) {
        this.ignoreSSL = ignoreSSL;
    }

    @Override
    public void notifyInputNotification(InputNotificationEvent event) {
        String body = JSON.toJSONString(event, SerializerFeature.WriteMapNullValue);
        Request request = new Request.Builder()
            .url(getHttpEndpoint())
            .header("Referer", Jenkins.get().getRootUrl())
            .post(RequestBody.create(MediaType.parse("application/json"), body))
            .build();

        try (Response resp = getHttpClient(getIgnoreSSL()).newCall(request).execute()){
            if (resp.isSuccessful()) {
                LOGGER.log(Level.FINE, "Successfully sent data to {0}", getHttpEndpoint());
            } else {
                LOGGER.log(Level.WARNING, "Could not send data to HTTP endpoint - {0}", resp.code());
                LOGGER.log(Level.WARNING, "HTTP endpoint - {0}", getHttpEndpoint());
                LOGGER.log(Level.WARNING, "Data - {0}", body);
                LOGGER.log(Level.WARNING, "Reason - {0}", resp.body().string());
            }
        } catch (NoSuchAlgorithmException | KeyManagementException | IOException e) {
            LOGGER.log(Level.SEVERE, null, e);
        }
    }

    /**
     * Gets an HTTP client that can be used to make requests.
     * @return HTTP client
     */
    private OkHttpClient getHttpClient(boolean ignoreSSL) throws NoSuchAlgorithmException, KeyManagementException {
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS);
        if (ignoreSSL) {
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
            };
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            return builder
                .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0])
                .hostnameVerifier((hostname, session) -> true)
                .build();
        }
        return builder.build();
    }

    @Extension
    public static class DescriptorImpl extends InputNotifier.DescriptorImpl {

        @Override
        public String getDisplayName() {
            return "Http Notifier";
        }

        @Override
        public int ordinal() {
            return 20;
        }
    }

}
