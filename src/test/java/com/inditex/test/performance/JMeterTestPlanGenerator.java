package com.inditex.test.performance;

import com.inditex.test.config.ConfigManager;
import com.inditex.test.utils.TestDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * JMeter test plan generator for comprehensive performance testing
 * of promotional pricing across all Inditex brands and markets.
 * 
 * Generates test plans for:
 * - Load testing (1000+ concurrent users)
 * - Stress testing to failure points
 * - Endurance testing for extended periods
 * - Spike testing for traffic bursts
 */
public class JMeterTestPlanGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(JMeterTestPlanGenerator.class);
    
    private static final String JMETER_PLANS_DIR = "src/test/resources/jmeter";
    private static final String PERFORMANCE_DATA_DIR = "src/test/resources/testdata/performance";
    
    /**
     * Generates comprehensive JMeter test plans for all performance scenarios
     */
    public static void generateAllTestPlans() {
        logger.info("Generating comprehensive JMeter test plans");
        
        try {
            createDirectories();
            
            // Generate different types of performance test plans
            generateLoadTestPlan();
            generateStressTestPlan();
            generateEnduranceTestPlan();
            generateSpikeTestPlan();
            generatePromotionalApiTestPlan();
            generateConcurrentUserTestPlan();
            
            // Generate supporting data files
            generatePerformanceTestData();
            
            logger.info("Successfully generated all JMeter test plans");
            
        } catch (Exception e) {
            logger.error("Failed to generate JMeter test plans", e);
            throw new RuntimeException("JMeter test plan generation failed", e);
        }
    }
    
    /**
     * Generates load test plan for 1000+ concurrent users
     */
    private static void generateLoadTestPlan() throws IOException {
        logger.info("Generating load test plan for 1000+ concurrent users");
        
        String testPlan = generateJMeterXML(
            "Promotional_Load_Test",
            "Load testing promotional pricing with 1000+ concurrent users",
            1000, // thread count
            300,  // ramp-up period (seconds)
            600,  // test duration (seconds)
            createPromotionalTestScenarios()
        );
        
        writeTestPlan("promotional_load_test.jmx", testPlan);
    }
    
    /**
     * Generates stress test plan to find failure points
     */
    private static void generateStressTestPlan() throws IOException {
        logger.info("Generating stress test plan to failure points");
        
        String testPlan = generateJMeterXML(
            "Promotional_Stress_Test",
            "Stress testing promotional pricing to find failure points",
            2000, // thread count
            180,  // ramp-up period (seconds)
            900,  // test duration (seconds)
            createStressTestScenarios()
        );
        
        writeTestPlan("promotional_stress_test.jmx", testPlan);
    }
    
    /**
     * Generates endurance test plan for extended periods
     */
    private static void generateEnduranceTestPlan() throws IOException {
        logger.info("Generating endurance test plan");
        
        String testPlan = generateJMeterXML(
            "Promotional_Endurance_Test",
            "Endurance testing promotional pricing over extended periods",
            500,  // thread count
            600,  // ramp-up period (seconds)
            3600, // test duration (1 hour)
            createEnduranceTestScenarios()
        );
        
        writeTestPlan("promotional_endurance_test.jmx", testPlan);
    }
    
    /**
     * Generates spike test plan for traffic bursts
     */
    private static void generateSpikeTestPlan() throws IOException {
        logger.info("Generating spike test plan");
        
        String testPlan = generateSpikeJMeterXML(
            "Promotional_Spike_Test",
            "Spike testing promotional pricing with sudden traffic bursts",
            100,  // baseline users
            1500, // spike users
            60,   // spike duration
            createSpikeTestScenarios()
        );
        
        writeTestPlan("promotional_spike_test.jmx", testPlan);
    }
    
    /**
     * Generates API-focused performance test plan
     */
    private static void generatePromotionalApiTestPlan() throws IOException {
        logger.info("Generating promotional API performance test plan");
        
        String testPlan = generateApiJMeterXML(
            "Promotional_API_Performance_Test",
            "API performance testing for promotional pricing endpoints",
            500,  // thread count
            120,  // ramp-up period
            300,  // test duration
            createApiTestScenarios()
        );
        
        writeTestPlan("promotional_api_performance.jmx", testPlan);
    }
    
    /**
     * Generates concurrent user simulation test plan
     */
    private static void generateConcurrentUserTestPlan() throws IOException {
        logger.info("Generating concurrent user simulation test plan");
        
        String testPlan = generateConcurrentUserJMeterXML(
            "Promotional_Concurrent_Users_Test",
            "Simulating realistic concurrent user behavior patterns",
            createConcurrentUserScenarios()
        );
        
        writeTestPlan("promotional_concurrent_users.jmx", testPlan);
    }
    
    /**
     * Generates comprehensive JMeter XML test plan
     */
    private static String generateJMeterXML(String testName, String description, 
                                          int threadCount, int rampUp, int duration,
                                          List<TestScenario> scenarios) {
        StringBuilder xml = new StringBuilder();
        
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<jmeterTestPlan version=\"1.2\" properties=\"5.0\" jmeter=\"5.6.3\">\n");
        xml.append("  <hashTree>\n");
        xml.append("    <TestPlan guiclass=\"TestPlanGui\" testclass=\"TestPlan\" testname=\"").append(testName).append("\">\n");
        xml.append("      <stringProp name=\"TestPlan.comments\">").append(description).append("</stringProp>\n");
        xml.append("      <boolProp name=\"TestPlan.functional_mode\">false</boolProp>\n");
        xml.append("      <boolProp name=\"TestPlan.serialize_threadgroups\">false</boolProp>\n");
        xml.append("      <elementProp name=\"TestPlan.arguments\" elementType=\"Arguments\" guiclass=\"ArgumentsPanel\">\n");
        xml.append("        <collectionProp name=\"Arguments.arguments\">\n");
        
        // Add test plan variables
        addTestPlanVariables(xml);
        
        xml.append("        </collectionProp>\n");
        xml.append("      </elementProp>\n");
        xml.append("    </TestPlan>\n");
        xml.append("    <hashTree>\n");
        
        // Add thread group
        addThreadGroup(xml, threadCount, rampUp, duration);
        
        // Add test scenarios
        for (TestScenario scenario : scenarios) {
            addTestScenario(xml, scenario);
        }
        
        // Add listeners and reporting
        addListeners(xml);
        
        xml.append("    </hashTree>\n");
        xml.append("  </hashTree>\n");
        xml.append("</jmeterTestPlan>\n");
        
        return xml.toString();
    }
    
    /**
     * Generates spike testing specific JMeter XML
     */
    private static String generateSpikeJMeterXML(String testName, String description,
                                                int baselineUsers, int spikeUsers, int spikeDuration,
                                                List<TestScenario> scenarios) {
        StringBuilder xml = new StringBuilder();
        
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<jmeterTestPlan version=\"1.2\" properties=\"5.0\" jmeter=\"5.6.3\">\n");
        xml.append("  <hashTree>\n");
        xml.append("    <TestPlan guiclass=\"TestPlanGui\" testclass=\"TestPlan\" testname=\"").append(testName).append("\">\n");
        xml.append("      <stringProp name=\"TestPlan.comments\">").append(description).append("</stringProp>\n");
        xml.append("    </TestPlan>\n");
        xml.append("    <hashTree>\n");
        
        // Add baseline load thread group
        addSpikeBaselineThreadGroup(xml, baselineUsers);
        
        // Add spike load thread group
        addSpikeLoadThreadGroup(xml, spikeUsers, spikeDuration);
        
        // Add test scenarios for both groups
        for (TestScenario scenario : scenarios) {
            addTestScenario(xml, scenario);
        }
        
        addListeners(xml);
        
        xml.append("    </hashTree>\n");
        xml.append("  </hashTree>\n");
        xml.append("</jmeterTestPlan>\n");
        
        return xml.toString();
    }
    
    /**
     * Generates API-specific JMeter XML test plan
     */
    private static String generateApiJMeterXML(String testName, String description,
                                             int threadCount, int rampUp, int duration,
                                             List<ApiTestScenario> scenarios) {
        StringBuilder xml = new StringBuilder();
        
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<jmeterTestPlan version=\"1.2\" properties=\"5.0\" jmeter=\"5.6.3\">\n");
        xml.append("  <hashTree>\n");
        xml.append("    <TestPlan guiclass=\"TestPlanGui\" testclass=\"TestPlan\" testname=\"").append(testName).append("\">\n");
        xml.append("      <stringProp name=\"TestPlan.comments\">").append(description).append("</stringProp>\n");
        xml.append("    </TestPlan>\n");
        xml.append("    <hashTree>\n");
        
        addThreadGroup(xml, threadCount, rampUp, duration);
        
        // Add API-specific scenarios
        for (ApiTestScenario scenario : scenarios) {
            addApiTestScenario(xml, scenario);
        }
        
        // Add API-specific assertions and monitoring
        addApiAssertions(xml);
        addApiListeners(xml);
        
        xml.append("    </hashTree>\n");
        xml.append("  </hashTree>\n");
        xml.append("</jmeterTestPlan>\n");
        
        return xml.toString();
    }
    
    /**
     * Generates concurrent user simulation JMeter XML
     */
    private static String generateConcurrentUserJMeterXML(String testName, String description,
                                                         List<ConcurrentUserScenario> scenarios) {
        StringBuilder xml = new StringBuilder();
        
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<jmeterTestPlan version=\"1.2\" properties=\"5.0\" jmeter=\"5.6.3\">\n");
        xml.append("  <hashTree>\n");
        xml.append("    <TestPlan guiclass=\"TestPlanGui\" testclass=\"TestPlan\" testname=\"").append(testName).append("\">\n");
        xml.append("      <stringProp name=\"TestPlan.comments\">").append(description).append("</stringProp>\n");
        xml.append("    </TestPlan>\n");
        xml.append("    <hashTree>\n");
        
        // Add multiple thread groups for different user behaviors
        for (ConcurrentUserScenario scenario : scenarios) {
            addConcurrentUserThreadGroup(xml, scenario);
            addConcurrentUserScenario(xml, scenario);
        }
        
        addListeners(xml);
        
        xml.append("    </hashTree>\n");
        xml.append("  </hashTree>\n");
        xml.append("</jmeterTestPlan>\n");
        
        return xml.toString();
    }
    
    // Helper methods for XML generation
    
    private static void addTestPlanVariables(StringBuilder xml) {
        String baseUrl = ConfigManager.getProperty("base.url", "http://localhost:8080");
        String apiVersion = ConfigManager.getProperty("api.version", "v1");
        
        xml.append("          <elementProp name=\"BASE_URL\" elementType=\"Argument\">\n");
        xml.append("            <stringProp name=\"Argument.name\">BASE_URL</stringProp>\n");
        xml.append("            <stringProp name=\"Argument.value\">").append(baseUrl).append("</stringProp>\n");
        xml.append("          </elementProp>\n");
        
        xml.append("          <elementProp name=\"API_VERSION\" elementType=\"Argument\">\n");
        xml.append("            <stringProp name=\"Argument.name\">API_VERSION</stringProp>\n");
        xml.append("            <stringProp name=\"Argument.value\">").append(apiVersion).append("</stringProp>\n");
        xml.append("          </elementProp>\n");
        
        xml.append("          <elementProp name=\"RESPONSE_TIMEOUT\" elementType=\"Argument\">\n");
        xml.append("            <stringProp name=\"Argument.name\">RESPONSE_TIMEOUT</stringProp>\n");
        xml.append("            <stringProp name=\"Argument.value\">5000</stringProp>\n");
        xml.append("          </elementProp>\n");
    }
    
    private static void addThreadGroup(StringBuilder xml, int threadCount, int rampUp, int duration) {
        xml.append("      <ThreadGroup guiclass=\"ThreadGroupGui\" testclass=\"ThreadGroup\" testname=\"Promotional Users\">\n");
        xml.append("        <stringProp name=\"ThreadGroup.on_sample_error\">continue</stringProp>\n");
        xml.append("        <elementProp name=\"ThreadGroup.main_controller\" elementType=\"LoopController\">\n");
        xml.append("          <boolProp name=\"LoopController.continue_forever\">false</boolProp>\n");
        xml.append("          <intProp name=\"LoopController.loops\">-1</intProp>\n");
        xml.append("        </elementProp>\n");
        xml.append("        <stringProp name=\"ThreadGroup.num_threads\">").append(threadCount).append("</stringProp>\n");
        xml.append("        <stringProp name=\"ThreadGroup.ramp_time\">").append(rampUp).append("</stringProp>\n");
        xml.append("        <stringProp name=\"ThreadGroup.duration\">").append(duration).append("</stringProp>\n");
        xml.append("        <boolProp name=\"ThreadGroup.scheduler\">true</boolProp>\n");
        xml.append("      </ThreadGroup>\n");
        xml.append("      <hashTree>\n");
    }
    
    private static void addSpikeBaselineThreadGroup(StringBuilder xml, int baselineUsers) {
        xml.append("      <ThreadGroup guiclass=\"ThreadGroupGui\" testclass=\"ThreadGroup\" testname=\"Baseline Load\">\n");
        xml.append("        <elementProp name=\"ThreadGroup.main_controller\" elementType=\"LoopController\">\n");
        xml.append("          <boolProp name=\"LoopController.continue_forever\">false</boolProp>\n");
        xml.append("          <intProp name=\"LoopController.loops\">-1</intProp>\n");
        xml.append("        </elementProp>\n");
        xml.append("        <stringProp name=\"ThreadGroup.num_threads\">").append(baselineUsers).append("</stringProp>\n");
        xml.append("        <stringProp name=\"ThreadGroup.ramp_time\">60</stringProp>\n");
        xml.append("        <stringProp name=\"ThreadGroup.duration\">600</stringProp>\n");
        xml.append("        <boolProp name=\"ThreadGroup.scheduler\">true</boolProp>\n");
        xml.append("      </ThreadGroup>\n");
        xml.append("      <hashTree>\n");
    }
    
    private static void addSpikeLoadThreadGroup(StringBuilder xml, int spikeUsers, int spikeDuration) {
        xml.append("      <ThreadGroup guiclass=\"ThreadGroupGui\" testclass=\"ThreadGroup\" testname=\"Spike Load\">\n");
        xml.append("        <elementProp name=\"ThreadGroup.main_controller\" elementType=\"LoopController\">\n");
        xml.append("          <boolProp name=\"LoopController.continue_forever\">false</boolProp>\n");
        xml.append("          <intProp name=\"LoopController.loops\">-1</intProp>\n");
        xml.append("        </elementProp>\n");
        xml.append("        <stringProp name=\"ThreadGroup.num_threads\">").append(spikeUsers).append("</stringProp>\n");
        xml.append("        <stringProp name=\"ThreadGroup.ramp_time\">10</stringProp>\n");
        xml.append("        <stringProp name=\"ThreadGroup.duration\">").append(spikeDuration).append("</stringProp>\n");
        xml.append("        <stringProp name=\"ThreadGroup.delay\">300</stringProp>\n");
        xml.append("        <boolProp name=\"ThreadGroup.scheduler\">true</boolProp>\n");
        xml.append("      </ThreadGroup>\n");
        xml.append("      <hashTree>\n");
    }
    
    private static void addConcurrentUserThreadGroup(StringBuilder xml, ConcurrentUserScenario scenario) {
        xml.append("      <ThreadGroup guiclass=\"ThreadGroupGui\" testclass=\"ThreadGroup\" testname=\"").append(scenario.name).append("\">\n");
        xml.append("        <elementProp name=\"ThreadGroup.main_controller\" elementType=\"LoopController\">\n");
        xml.append("          <boolProp name=\"LoopController.continue_forever\">false</boolProp>\n");
        xml.append("          <intProp name=\"LoopController.loops\">").append(scenario.loops).append("</intProp>\n");
        xml.append("        </elementProp>\n");
        xml.append("        <stringProp name=\"ThreadGroup.num_threads\">").append(scenario.users).append("</stringProp>\n");
        xml.append("        <stringProp name=\"ThreadGroup.ramp_time\">").append(scenario.rampUp).append("</stringProp>\n");
        xml.append("      </ThreadGroup>\n");
        xml.append("      <hashTree>\n");
    }
    
    private static void addTestScenario(StringBuilder xml, TestScenario scenario) {
        xml.append("        <HTTPSamplerProxy guiclass=\"HttpTestSampleGui\" testclass=\"HTTPSamplerProxy\" testname=\"").append(scenario.name).append("\">\n");
        xml.append("          <elementProp name=\"HTTPsampler.Arguments\" elementType=\"Arguments\">\n");
        xml.append("            <collectionProp name=\"Arguments.arguments\">\n");
        
        for (Map.Entry<String, String> param : scenario.parameters.entrySet()) {
            xml.append("              <elementProp name=\"").append(param.getKey()).append("\" elementType=\"HTTPArgument\">\n");
            xml.append("                <boolProp name=\"HTTPArgument.always_encode\">false</boolProp>\n");
            xml.append("                <stringProp name=\"Argument.value\">").append(param.getValue()).append("</stringProp>\n");
            xml.append("                <stringProp name=\"Argument.name\">").append(param.getKey()).append("</stringProp>\n");
            xml.append("              </elementProp>\n");
        }
        
        xml.append("            </collectionProp>\n");
        xml.append("          </elementProp>\n");
        xml.append("          <stringProp name=\"HTTPSampler.domain\">${BASE_URL}</stringProp>\n");
        xml.append("          <stringProp name=\"HTTPSampler.path\">").append(scenario.path).append("</stringProp>\n");
        xml.append("          <stringProp name=\"HTTPSampler.method\">").append(scenario.method).append("</stringProp>\n");
        xml.append("          <boolProp name=\"HTTPSampler.follow_redirects\">true</boolProp>\n");
        xml.append("          <stringProp name=\"HTTPSampler.connect_timeout\">${RESPONSE_TIMEOUT}</stringProp>\n");
        xml.append("          <stringProp name=\"HTTPSampler.response_timeout\">${RESPONSE_TIMEOUT}</stringProp>\n");
        xml.append("        </HTTPSamplerProxy>\n");
        xml.append("        <hashTree>\n");
        
        // Add assertions
        addResponseTimeAssertion(xml, scenario.maxResponseTime);
        addStatusCodeAssertion(xml, scenario.expectedStatusCode);
        
        xml.append("        </hashTree>\n");
    }
    
    private static void addApiTestScenario(StringBuilder xml, ApiTestScenario scenario) {
        xml.append("        <HTTPSamplerProxy guiclass=\"HttpTestSampleGui\" testclass=\"HTTPSamplerProxy\" testname=\"").append(scenario.name).append("\">\n");
        xml.append("          <stringProp name=\"HTTPSampler.domain\">${BASE_URL}</stringProp>\n");
        xml.append("          <stringProp name=\"HTTPSampler.path\">/api/${API_VERSION}").append(scenario.endpoint).append("</stringProp>\n");
        xml.append("          <stringProp name=\"HTTPSampler.method\">").append(scenario.method).append("</stringProp>\n");
        xml.append("          <stringProp name=\"HTTPSampler.postBodyRaw\">").append(scenario.requestBody).append("</stringProp>\n");
        xml.append("          <elementProp name=\"HTTPsampler.Header_manager\" elementType=\"HeaderManager\">\n");
        xml.append("            <collectionProp name=\"HeaderManager.headers\">\n");
        xml.append("              <elementProp name=\"Content-Type\" elementType=\"Header\">\n");
        xml.append("                <stringProp name=\"Header.name\">Content-Type</stringProp>\n");
        xml.append("                <stringProp name=\"Header.value\">application/json</stringProp>\n");
        xml.append("              </elementProp>\n");
        xml.append("            </collectionProp>\n");
        xml.append("          </elementProp>\n");
        xml.append("        </HTTPSamplerProxy>\n");
        xml.append("        <hashTree>\n");
        
        addApiAssertions(xml, scenario);
        
        xml.append("        </hashTree>\n");
    }
    
    private static void addConcurrentUserScenario(StringBuilder xml, ConcurrentUserScenario scenario) {
        for (UserAction action : scenario.actions) {
            xml.append("        <HTTPSamplerProxy guiclass=\"HttpTestSampleGui\" testclass=\"HTTPSamplerProxy\" testname=\"").append(action.name).append("\">\n");
            xml.append("          <stringProp name=\"HTTPSampler.domain\">${BASE_URL}</stringProp>\n");
            xml.append("          <stringProp name=\"HTTPSampler.path\">").append(action.path).append("</stringProp>\n");
            xml.append("          <stringProp name=\"HTTPSampler.method\">").append(action.method).append("</stringProp>\n");
            xml.append("        </HTTPSamplerProxy>\n");
            xml.append("        <hashTree>\n");
            
            // Add think time between actions
            xml.append("          <ConstantTimer guiclass=\"ConstantTimerGui\" testclass=\"ConstantTimer\" testname=\"Think Time\">\n");
            xml.append("            <stringProp name=\"ConstantTimer.delay\">").append(action.thinkTime).append("</stringProp>\n");
            xml.append("          </ConstantTimer>\n");
            
            xml.append("        </hashTree>\n");
        }
    }
    
    private static void addResponseTimeAssertion(StringBuilder xml, int maxResponseTime) {
        xml.append("          <DurationAssertion guiclass=\"DurationAssertionGui\" testclass=\"DurationAssertion\" testname=\"Response Time Assertion\">\n");
        xml.append("            <stringProp name=\"DurationAssertion.duration\">").append(maxResponseTime).append("</stringProp>\n");
        xml.append("          </DurationAssertion>\n");
    }
    
    private static void addStatusCodeAssertion(StringBuilder xml, int expectedStatusCode) {
        xml.append("          <ResponseAssertion guiclass=\"AssertionGui\" testclass=\"ResponseAssertion\" testname=\"Status Code Assertion\">\n");
        xml.append("            <collectionProp name=\"Asserion.test_strings\">\n");
        xml.append("              <stringProp name=\"response_code\">").append(expectedStatusCode).append("</stringProp>\n");
        xml.append("            </collectionProp>\n");
        xml.append("            <stringProp name=\"Assertion.test_field\">Assertion.response_code</stringProp>\n");
        xml.append("            <intProp name=\"Assertion.test_type\">1</intProp>\n");
        xml.append("          </ResponseAssertion>\n");
    }
    
    private static void addApiAssertions(StringBuilder xml) {
        xml.append("          <ResponseAssertion guiclass=\"AssertionGui\" testclass=\"ResponseAssertion\" testname=\"API Response Assertion\">\n");
        xml.append("            <collectionProp name=\"Asserion.test_strings\">\n");
        xml.append("              <stringProp name=\"json_response\">\"status\":\"success\"</stringProp>\n");
        xml.append("            </collectionProp>\n");
        xml.append("            <stringProp name=\"Assertion.test_field\">Assertion.response_data</stringProp>\n");
        xml.append("            <intProp name=\"Assertion.test_type\">2</intProp>\n");
        xml.append("          </ResponseAssertion>\n");
    }
    
    private static void addApiAssertions(StringBuilder xml, ApiTestScenario scenario) {
        xml.append("          <JSONPathAssertion guiclass=\"JSONPathAssertionGui\" testclass=\"JSONPathAssertion\" testname=\"JSON Path Assertion\">\n");
        xml.append("            <stringProp name=\"JSON_PATH\">").append(scenario.jsonPath).append("</stringProp>\n");
        xml.append("            <stringProp name=\"EXPECTED_VALUE\">").append(scenario.expectedValue).append("</stringProp>\n");
        xml.append("          </JSONPathAssertion>\n");
    }
    
    private static void addApiListeners(StringBuilder xml) {
        xml.append("        <ResultCollector guiclass=\"ViewResultsFullVisualizer\" testclass=\"ResultCollector\" testname=\"API Results Tree\">\n");
        xml.append("          <boolProp name=\"ResultCollector.error_logging\">false</boolProp>\n");
        xml.append("          <objProp>\n");
        xml.append("            <name>saveConfig</name>\n");
        xml.append("            <value class=\"SampleSaveConfiguration\">\n");
        xml.append("              <time>true</time>\n");
        xml.append("              <latency>true</latency>\n");
        xml.append("              <timestamp>true</timestamp>\n");
        xml.append("              <success>true</success>\n");
        xml.append("              <label>true</label>\n");
        xml.append("              <code>true</code>\n");
        xml.append("              <message>true</message>\n");
        xml.append("              <threadName>true</threadName>\n");
        xml.append("              <dataType>true</dataType>\n");
        xml.append("              <encoding>false</encoding>\n");
        xml.append("              <assertions>true</assertions>\n");
        xml.append("              <subresults>true</subresults>\n");
        xml.append("              <responseData>false</responseData>\n");
        xml.append("              <samplerData>false</samplerData>\n");
        xml.append("              <xml>false</xml>\n");
        xml.append("              <fieldNames>true</fieldNames>\n");
        xml.append("              <responseHeaders>false</responseHeaders>\n");
        xml.append("              <requestHeaders>false</requestHeaders>\n");
        xml.append("              <responseDataOnError>false</responseDataOnError>\n");
        xml.append("              <saveAssertionResultsFailureMessage>true</saveAssertionResultsFailureMessage>\n");
        xml.append("              <assertionsResultsToSave>0</assertionsResultsToSave>\n");
        xml.append("              <bytes>true</bytes>\n");
        xml.append("              <sentBytes>true</sentBytes>\n");
        xml.append("              <url>true</url>\n");
        xml.append("              <threadCounts>true</threadCounts>\n");
        xml.append("              <idleTime>true</idleTime>\n");
        xml.append("              <connectTime>true</connectTime>\n");
        xml.append("            </value>\n");
        xml.append("          </objProp>\n");
        xml.append("          <stringProp name=\"filename\">api_results.jtl</stringProp>\n");
        xml.append("        </ResultCollector>\n");
        xml.append("        <hashTree/>\n");
    }
    
    private static void addListeners(StringBuilder xml) {
        // Aggregate Report
        xml.append("        <ResultCollector guiclass=\"StatVisualizer\" testclass=\"ResultCollector\" testname=\"Aggregate Report\">\n");
        xml.append("          <boolProp name=\"ResultCollector.error_logging\">false</boolProp>\n");
        xml.append("          <objProp>\n");
        xml.append("            <name>saveConfig</name>\n");
        xml.append("            <value class=\"SampleSaveConfiguration\">\n");
        xml.append("              <time>true</time>\n");
        xml.append("              <latency>true</latency>\n");
        xml.append("              <timestamp>true</timestamp>\n");
        xml.append("              <success>true</success>\n");
        xml.append("              <label>true</label>\n");
        xml.append("              <code>true</code>\n");
        xml.append("              <message>true</message>\n");
        xml.append("              <threadName>true</threadName>\n");
        xml.append("              <dataType>true</dataType>\n");
        xml.append("              <encoding>false</encoding>\n");
        xml.append("              <assertions>true</assertions>\n");
        xml.append("              <subresults>true</subresults>\n");
        xml.append("              <responseData>false</responseData>\n");
        xml.append("              <samplerData>false</samplerData>\n");
        xml.append("              <xml>false</xml>\n");
        xml.append("              <fieldNames>true</fieldNames>\n");
        xml.append("              <responseHeaders>false</responseHeaders>\n");
        xml.append("              <requestHeaders>false</requestHeaders>\n");
        xml.append("              <responseDataOnError>false</responseDataOnError>\n");
        xml.append("              <saveAssertionResultsFailureMessage>true</saveAssertionResultsFailureMessage>\n");
        xml.append("              <assertionsResultsToSave>0</assertionsResultsToSave>\n");
        xml.append("              <bytes>true</bytes>\n");
        xml.append("              <sentBytes>true</sentBytes>\n");
        xml.append("              <url>true</url>\n");
        xml.append("              <threadCounts>true</threadCounts>\n");
        xml.append("              <idleTime>true</idleTime>\n");
        xml.append("              <connectTime>true</connectTime>\n");
        xml.append("            </value>\n");
        xml.append("          </objProp>\n");
        xml.append("          <stringProp name=\"filename\">results.jtl</stringProp>\n");
        xml.append("        </ResultCollector>\n");
        xml.append("        <hashTree/>\n");
        
        // Response Times Over Time
        xml.append("        <kg.apc.jmeter.vizualizers.CorrectedResultCollector guiclass=\"kg.apc.jmeter.vizualizers.ResponseTimesOverTimeGui\" testclass=\"kg.apc.jmeter.vizualizers.CorrectedResultCollector\" testname=\"Response Times Over Time\">\n");
        xml.append("          <boolProp name=\"ResultCollector.error_logging\">false</boolProp>\n");
        xml.append("          <stringProp name=\"filename\">response_times.jtl</stringProp>\n");
        xml.append("        </kg.apc.jmeter.vizualizers.CorrectedResultCollector>\n");
        xml.append("        <hashTree/>\n");
        
        // Transactions per Second
        xml.append("        <kg.apc.jmeter.vizualizers.CorrectedResultCollector guiclass=\"kg.apc.jmeter.vizualizers.TransactionsPerSecondGui\" testclass=\"kg.apc.jmeter.vizualizers.CorrectedResultCollector\" testname=\"Transactions per Second\">\n");
        xml.append("          <boolProp name=\"ResultCollector.error_logging\">false</boolProp>\n");
        xml.append("          <stringProp name=\"filename\">tps.jtl</stringProp>\n");
        xml.append("        </kg.apc.jmeter.vizualizers.CorrectedResultCollector>\n");
        xml.append("        <hashTree/>\n");
    }
    
    private static void createDirectories() throws IOException {
        File jmeterDir = new File(JMETER_PLANS_DIR);
        File performanceDir = new File(PERFORMANCE_DATA_DIR);
        
        if (!jmeterDir.exists()) {
            jmeterDir.mkdirs();
        }
        
        if (!performanceDir.exists()) {
            performanceDir.mkdirs();
        }
    }
    
    private static void writeTestPlan(String filename, String content) throws IOException {
        File file = new File(JMETER_PLANS_DIR, filename);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
        logger.info("Generated JMeter test plan: {}", file.getAbsolutePath());
    }
    
    // Test scenario creation methods
    
    private static List<TestScenario> createPromotionalTestScenarios() {
        // Load comprehensive test data and create scenarios
        List<Map<String, String>> testData = TestDataManager.getComprehensivePromotionData();
        return testData.stream()
                .limit(50) // Limit for load testing
                .map(JMeterTestPlanGenerator::createTestScenarioFromData)
                .collect(java.util.stream.Collectors.toList());
    }
    
    private static List<TestScenario> createStressTestScenarios() {
        // Similar to load test but with more aggressive scenarios
        List<Map<String, String>> testData = TestDataManager.getComprehensivePromotionData();
        return testData.stream()
                .limit(100) // More scenarios for stress testing
                .map(JMeterTestPlanGenerator::createStressTestScenarioFromData)
                .collect(java.util.stream.Collectors.toList());
    }
    
    private static List<TestScenario> createEnduranceTestScenarios() {
        // Optimized scenarios for long-running tests
        List<Map<String, String>> testData = TestDataManager.getComprehensivePromotionData();
        return testData.stream()
                .limit(25) // Fewer scenarios for endurance
                .map(JMeterTestPlanGenerator::createEnduranceTestScenarioFromData)
                .collect(java.util.stream.Collectors.toList());
    }
    
    private static List<TestScenario> createSpikeTestScenarios() {
        // Quick, intensive scenarios for spike testing
        List<Map<String, String>> testData = TestDataManager.getComprehensivePromotionData();
        return testData.stream()
                .limit(20) // Quick scenarios
                .map(JMeterTestPlanGenerator::createSpikeTestScenarioFromData)
                .collect(java.util.stream.Collectors.toList());
    }
    
    private static List<ApiTestScenario> createApiTestScenarios() {
        return java.util.Arrays.asList(
            new ApiTestScenario("Get Promotion", "/promotions", "GET", "", "$.status", "success"),
            new ApiTestScenario("Apply Discount", "/promotions/apply", "POST", 
                "{\"sku\":\"ZARA001ES\",\"customerId\":\"12345\"}", "$.discount", "20"),
            new ApiTestScenario("Validate Promotion", "/promotions/validate", "POST",
                "{\"code\":\"SUMMER20\"}", "$.valid", "true")
        );
    }
    
    private static List<ConcurrentUserScenario> createConcurrentUserScenarios() {
        return java.util.Arrays.asList(
            new ConcurrentUserScenario("Casual Browser", 100, 30, 5, java.util.Arrays.asList(
                new UserAction("Home Page", "/", "GET", 2000),
                new UserAction("Browse Products", "/products", "GET", 3000),
                new UserAction("View Product", "/products/ZARA001ES", "GET", 4000)
            )),
            new ConcurrentUserScenario("Active Shopper", 200, 60, 10, java.util.Arrays.asList(
                new UserAction("Home Page", "/", "GET", 1000),
                new UserAction("Search Products", "/search?q=dress", "GET", 2000),
                new UserAction("View Product", "/products/ZARA001ES", "GET", 3000),
                new UserAction("Add to Cart", "/cart/add", "POST", 1000),
                new UserAction("View Cart", "/cart", "GET", 2000)
            ))
        );
    }
    
    private static TestScenario createTestScenarioFromData(Map<String, String> data) {
        Map<String, String> params = new java.util.HashMap<>();
        params.put("sku", data.get("sku"));
        params.put("brand", data.get("brand"));
        params.put("country", data.get("country"));
        params.put("customerType", data.get("customer_type"));
        
        return new TestScenario(
            "Promotional Test - " + data.get("sku"),
            "/promotions/calculate",
            "POST",
            params,
            2000, // 2 second max response time
            200   // Expected status code
        );
    }
    
    private static TestScenario createStressTestScenarioFromData(Map<String, String> data) {
        Map<String, String> params = new java.util.HashMap<>();
        params.put("sku", data.get("sku"));
        params.put("brand", data.get("brand"));
        params.put("country", data.get("country"));
        params.put("customerType", data.get("customer_type"));
        
        return new TestScenario(
            "Stress Test - " + data.get("sku"),
            "/promotions/calculate",
            "POST",
            params,
            5000, // 5 second max response time for stress
            200   // Expected status code
        );
    }
    
    private static TestScenario createEnduranceTestScenarioFromData(Map<String, String> data) {
        Map<String, String> params = new java.util.HashMap<>();
        params.put("sku", data.get("sku"));
        params.put("brand", data.get("brand"));
        
        return new TestScenario(
            "Endurance Test - " + data.get("sku"),
            "/promotions/calculate",
            "POST",
            params,
            3000, // 3 second max response time
            200   // Expected status code
        );
    }
    
    private static TestScenario createSpikeTestScenarioFromData(Map<String, String> data) {
        Map<String, String> params = new java.util.HashMap<>();
        params.put("sku", data.get("sku"));
        
        return new TestScenario(
            "Spike Test - " + data.get("sku"),
            "/promotions/calculate",
            "POST",
            params,
            1000, // 1 second max response time for spikes
            200   // Expected status code
        );
    }
    
    private static void generatePerformanceTestData() throws IOException {
        // Generate CSV files for JMeter to use
        List<Map<String, String>> performanceData = TestDataManager.getPerformanceTestData();
        
        StringBuilder csv = new StringBuilder();
        csv.append("sku,brand,country,language,customer_type,promotion_code\n");
        
        for (Map<String, String> record : performanceData) {
            csv.append(record.get("sku")).append(",");
            csv.append(record.get("brand")).append(",");
            csv.append(record.get("country")).append(",");
            csv.append(record.get("language")).append(",");
            csv.append(record.get("customer_type")).append(",");
            csv.append("PERF").append(record.get("sku")).append("\n");
        }
        
        File dataFile = new File(PERFORMANCE_DATA_DIR, "jmeter_test_data.csv");
        try (FileWriter writer = new FileWriter(dataFile)) {
            writer.write(csv.toString());
        }
        
        logger.info("Generated performance test data file: {}", dataFile.getAbsolutePath());
    }
    
    // Data classes for test scenarios
    
    private static class TestScenario {
        final String name;
        final String path;
        final String method;
        final Map<String, String> parameters;
        final int maxResponseTime;
        final int expectedStatusCode;
        
        TestScenario(String name, String path, String method, Map<String, String> parameters,
                    int maxResponseTime, int expectedStatusCode) {
            this.name = name;
            this.path = path;
            this.method = method;
            this.parameters = parameters;
            this.maxResponseTime = maxResponseTime;
            this.expectedStatusCode = expectedStatusCode;
        }
    }
    
    private static class ApiTestScenario {
        final String name;
        final String endpoint;
        final String method;
        final String requestBody;
        final String jsonPath;
        final String expectedValue;
        
        ApiTestScenario(String name, String endpoint, String method, String requestBody,
                       String jsonPath, String expectedValue) {
            this.name = name;
            this.endpoint = endpoint;
            this.method = method;
            this.requestBody = requestBody;
            this.jsonPath = jsonPath;
            this.expectedValue = expectedValue;
        }
    }
    
    private static class ConcurrentUserScenario {
        final String name;
        final int users;
        final int rampUp;
        final int loops;
        final List<UserAction> actions;
        
        ConcurrentUserScenario(String name, int users, int rampUp, int loops, List<UserAction> actions) {
            this.name = name;
            this.users = users;
            this.rampUp = rampUp;
            this.loops = loops;
            this.actions = actions;
        }
    }
    
    private static class UserAction {
        final String name;
        final String path;
        final String method;
        final int thinkTime;
        
        UserAction(String name, String path, String method, int thinkTime) {
            this.name = name;
            this.path = path;
            this.method = method;
            this.thinkTime = thinkTime;
        }
    }
    
    /**
     * Main method for standalone execution
     */
    public static void main(String[] args) {
        logger.info("Starting JMeter test plan generation");
        try {
            generateAllTestPlans();
            logger.info("JMeter test plan generation completed successfully");
        } catch (Exception e) {
            logger.error("JMeter test plan generation failed", e);
            System.exit(1);
        }
    }
}