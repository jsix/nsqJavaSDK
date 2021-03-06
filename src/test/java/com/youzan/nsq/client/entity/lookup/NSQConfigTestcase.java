package com.youzan.nsq.client.entity.lookup;

import com.fasterxml.jackson.databind.JsonNode;
import com.youzan.nsq.client.configs.ConfigAccessAgent;
import com.youzan.nsq.client.configs.DCCConfigAccessAgent;
import com.youzan.nsq.client.core.NSQConnection;
import com.youzan.nsq.client.entity.Address;
import com.youzan.nsq.client.entity.DesiredTag;
import com.youzan.nsq.client.entity.NSQConfig;
import com.youzan.nsq.client.exception.ConfigAccessAgentException;
import com.youzan.nsq.client.exception.NSQNoConnectionException;
import com.youzan.nsq.client.utils.ConnectionUtil;
import com.youzan.util.IOUtil;
import com.youzan.util.SystemUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import static com.youzan.nsq.client.entity.NSQConfig.Compression.DEFLATE;

/**
 * Created by lin on 16/12/21.
 */
public class NSQConfigTestcase {

    private final static Logger logger = LoggerFactory.getLogger(NSQConfigTestcase.class);
    private Properties props = new Properties();

    @BeforeClass
    public void init() throws IOException {
        logger.info("init of [NSQConfigTestcase].");
        logger.info("Initialize ConfigAccessAgent from system specified config.");
        System.setProperty("nsq.sdk.configFilePath", "src/main/resources/configClient.properties");
        try (final InputStream is = getClass().getClassLoader().getResourceAsStream("app-test.properties")) {
            props.load(is);
        }
        logger.info("init of [NSQConfigTestcase] ends.");
    }

    @Test
    public void testSetDCCUrlsInNSQConfig() throws ConfigAccessAgentException {
        String dumyUrl = "http://invalid.dcc.url:1234";
        ConfigAccessAgent.setConfigAccessRemotes(dumyUrl);
        NSQConfig config = new NSQConfig();
        DCCConfigAccessAgent agent = (DCCConfigAccessAgent) ConfigAccessAgent.getInstance();
        Assert.assertEquals(dumyUrl, DCCConfigAccessAgent.getUrls()[0]);
    }

    @Test
    public void testNSQConfig() {
        NSQConfig config = new NSQConfig();
        config
                /*
                开启／关闭顺序消费模式。默认状态下顺序消费模式为关闭状态。
                */
                .setOrdered(true)
                /*
                用于覆盖DCC返回的lookup address，配置为true时，ConfigAccessAgent将不会访问DCC，SDK
                使用｛@link NSQConfig#setLookupAddresses(String)｝传入的地址。
                @Deprecated
                */
                .setUserSpecifiedLookupAddress(true)
                /*
                指定Seed lookup address。UserSpecifiedLookupAddress 未指定或指定为{@link Boolean#FALSE}时
                传入地址将被忽略。在UserSpecifiedLookupAddress 为{@link Boolean#TRUE} 时有效。
                */
                .setLookupAddresses("sqs-qa.s.qima-inc.com:4161")
                /*
                指定Consumer消费的Channel，该配置对producer无效
                 */
                .setConsumerName("BaseConsumer")
                /*
                NSQd connection pool 的容量设置，默认值为10。
                 */
                .setConnectionPoolSize(10)
                /*
                NSQ消息超时配置, 默认值为60秒
                 */
                .setMsgTimeoutInMillisecond(10000)
                /*
                NSQ output_buffer_size 配置项，可选配置
                 */
                .setOutputBufferSize(10000)
                /*
                NSQ output_buffer_timeout 配置项，可选配置
                 */
                .setOutputBufferTimeoutInMillisecond(10000)
                /*
                SDK 同步操作时的等到超时设置，可选配置
                 */
                .setQueryTimeoutInMillisecond(30000)
                /*
                Consumer RDY最大值设置
                 */
                .setRdy(3)
                /*
                用户消息处理线程池容量配置项
                 */
                .setThreadPoolSize4IO(Runtime.getRuntime().availableProcessors());
    }

    @Test
    public void testIdentity() throws IOException, InterruptedException, TimeoutException, NSQNoConnectionException {
        NSQConfig config = new NSQConfig("default");
        String defaultIdentifyJson = config.identify(false);
        JsonNode idenJson = SystemUtil.getObjectMapper().readTree(defaultIdentifyJson);
        Assert.assertFalse(idenJson.get("extend_support").asBoolean());


        config.setHeartbeatIntervalInMillisecond(50000)
                .setOutputBufferSize(128)
                .setOutputBufferTimeoutInMillisecond(10)
                .setSampleRate(10)
                .setDeflateLevel(5)
                .setCompression(DEFLATE)
                .setConsumerDesiredTag(new DesiredTag("tag_123"));
        idenJson  = SystemUtil.getObjectMapper().readTree(config.identify(true));
        Assert.assertTrue(idenJson.get("extend_support").asBoolean());

        String lookupAddr = props.getProperty("lookup-addresses");
        String topicName = "JavaTesting-Producer-Base";
        JsonNode lookupResp = IOUtil.readFromUrl(new URL("http://" + lookupAddr + "/lookup?topic=" + topicName + "&access=r"));
        JsonNode partition = lookupResp.get("partitions").get("0");
        Address addr1 = new Address(partition.get("broadcast_address").asText(), partition.get("tcp_port").asText(), partition.get("version").asText(), topicName, 0, false);
        NSQConnection con = ConnectionUtil.connect(addr1, "BaseConsumer", config);
        Assert.assertTrue(con.isConnected());
        con.close();
    }

    @Test
    public void testDesiredTag() {
        DesiredTag tag = new DesiredTag("service-chain-demo-_123");
        try{
            new DesiredTag("Toooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo" +
                    "ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo" +
                    "ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo" +
                    "ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo" +
                    "ooooooooooooooooooooooooooLong");
            Assert.fail("desired tag should be too long");
        } catch (Exception e) {
            logger.info("too long tag detected.");
        }

        try{
            new DesiredTag("tag with space");
            Assert.fail("desired tag should be invalid");
        } catch (Exception e) {
            logger.info("tag with space detected.");
        }
    }

    @AfterMethod
    public void release(){
        System.clearProperty("nsq.sdk.configFilePath");
    }
}
