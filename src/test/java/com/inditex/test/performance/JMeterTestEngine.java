package com.inditex.test.performance;

import com.inditex.test.config.ConfigManager;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class JMeterTestEngine {
    private static final Logger logger = LoggerFactory.getLogger(JMeterTestEngine.class);
    
    private StandardJMeterEngine jmeterEngine;
    private HashTree testPlanTree;
    private String jmeterHome;
    private String resultsDirectory;
    private boolean initialized = false;

    public JMeterTestEngine() {
        this.jmeterHome = System.getProperty("jmeter.home", "src/test/resources/jmeter");
        this.resultsDirectory = ConfigManager.getProperty("performance.results.directory", "target/jmeter-results");
        
        try {
            initializeJMeter();
        } catch (Exception e) {
            logger.error("Failed to initialize JMeter", e);
            throw new RuntimeException("JMeter initialization failed", e);
        }
    }

    private void initializeJMeter() throws Exception {
        // Create JMeter home directory if it doesn't exist
        Files.createDirectories(Paths.get(jmeterHome));
        Files.createDirectories(Paths.get(resultsDirectory));
        
        // Set JMeter properties
        JMeterUtils.setJMeterHome(jmeterHome);
        JMeterUtils.loadJMeterProperties(createJMeterProperties());
        JMeterUtils.initLocale();
        
        // Initialize JMeter engine
        jmeterEngine = new StandardJMeterEngine();
        
        initialized = true;
        logger.info("JMeter engine initialized successfully");
    }

    private String createJMeterProperties() throws Exception {
        String propertiesPath = jmeterHome + "/jmeter.properties";
        File propertiesFile = new File(propertiesPath);
        
        if (!propertiesFile.exists()) {
            // Create basic JMeter properties file
            String properties = """
                # JMeter Properties for Performance Testing
                jmeter.save.saveservice.output_format=xml
                jmeter.save.saveservice.response_data=false
                jmeter.save.saveservice.samplerData=false
                jmeter.save.saveservice.requestHeaders=false
                jmeter.save.saveservice.responseHeaders=false
                jmeter.save.saveservice.encoding=false
                jmeter.save.saveservice.label=true
                jmeter.save.saveservice.latency=true
                jmeter.save.saveservice.response_code=true
                jmeter.save.saveservice.response_message=true
                jmeter.save.saveservice.successful=true
                jmeter.save.saveservice.thread_name=true
                jmeter.save.saveservice.time=true
                jmeter.save.saveservice.subresults=true
                jmeter.save.saveservice.assertions=true
                jmeter.save.saveservice.bytes=true
                jmeter.save.saveservice.hostname=true
                jmeter.save.saveservice.thread_counts=true
                jmeter.save.saveservice.sample_count=true
                jmeter.save.saveservice.idle_time=true
                """;
            
            Files.write(Paths.get(propertiesPath), properties.getBytes());
        }
        
        return propertiesPath;
    }

    public PerformanceTestResult runLoadTest(LoadTestConfig config) {
        if (!initialized) {
            throw new IllegalStateException("JMeter engine not initialized");
        }
        
        try {
            logger.info("Starting load test: {}", config.getTestName());
            
            // Create test plan
            testPlanTree = createTestPlan(config);
            
            // Configure engine
            jmeterEngine.configure(testPlanTree);
            
            // Run test
            long startTime = System.currentTimeMillis();
            jmeterEngine.run();
            long endTime = System.currentTimeMillis();
            
            // Process results
            PerformanceTestResult result = processResults(config, startTime, endTime);
            
            logger.info("Load test completed: {} in {}ms", config.getTestName(), (endTime - startTime));
            return result;
            
        } catch (Exception e) {
            logger.error("Load test execution failed: {}", config.getTestName(), e);
            throw new RuntimeException("Load test failed", e);
        }
    }

    private HashTree createTestPlan(LoadTestConfig config) {
        // Create Test Plan
        TestPlan testPlan = new TestPlan(config.getTestName());
        testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
        testPlan.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());
        testPlan.setEnabled(true);
        testPlan.setUserDefinedVariables(config.getUserVariables());

        // Create Thread Group
        ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setName("Thread Group");
        threadGroup.setNumThreads(config.getNumThreads());
        threadGroup.setRampUp(config.getRampUpTime());
        threadGroup.setSamplerController(createLoopController(config.getLoopCount()));
        threadGroup.setProperty(TestElement.TEST_CLASS, ThreadGroup.class.getName());
        threadGroup.setProperty(TestElement.GUI_CLASS, ThreadGroupGui.class.getName());
        threadGroup.setEnabled(true);

        // Create HTTP Samplers
        HashTree threadGroupTree = new HashTree();
        for (HTTPRequestConfig requestConfig : config.getHttpRequests()) {
            HTTPSampler httpSampler = createHttpSampler(requestConfig);
            threadGroupTree.add(httpSampler);
        }

        // Add Result Collector
        ResultCollector resultCollector = createResultCollector(config);
        threadGroupTree.add(resultCollector);

        // Build test plan tree
        HashTree testPlanTree = new HashTree();
        testPlanTree.add(testPlan);
        testPlanTree.add(threadGroup, threadGroupTree);

        return testPlanTree;
    }

    private LoopController createLoopController(int loopCount) {
        LoopController loopController = new LoopController();
        loopController.setLoops(loopCount);
        loopController.setFirst(true);
        loopController.setProperty(TestElement.TEST_CLASS, LoopController.class.getName());
        loopController.setProperty(TestElement.GUI_CLASS, LoopControlPanel.class.getName());
        loopController.initialize();
        return loopController;
    }

    private HTTPSampler createHttpSampler(HTTPRequestConfig requestConfig) {
        HTTPSampler httpSampler = new HTTPSampler();
        httpSampler.setName(requestConfig.getName());
        httpSampler.setDomain(requestConfig.getDomain());
        httpSampler.setPort(requestConfig.getPort());
        httpSampler.setPath(requestConfig.getPath());
        httpSampler.setMethod(requestConfig.getMethod());
        httpSampler.setFollowRedirects(true);
        httpSampler.setAutoRedirects(false);
        httpSampler.setUseKeepAlive(true);
        httpSampler.setDoMultipart(false);
        
        // Add headers
        if (requestConfig.getHeaders() != null && !requestConfig.getHeaders().isEmpty()) {
            for (Map.Entry<String, String> header : requestConfig.getHeaders().entrySet()) {
                httpSampler.getHeaderManager().add(header.getKey(), header.getValue());
            }
        }
        
        // Add parameters
        if (requestConfig.getParameters() != null && !requestConfig.getParameters().isEmpty()) {
            for (Map.Entry<String, String> param : requestConfig.getParameters().entrySet()) {
                httpSampler.addArgument(param.getKey(), param.getValue());
            }
        }
        
        // Add body data for POST/PUT requests
        if (requestConfig.getBodyData() != null && !requestConfig.getBodyData().isEmpty()) {
            httpSampler.addNonEncodedArgument("", requestConfig.getBodyData(), "");
            httpSampler.setPostBodyRaw(true);
        }
        
        httpSampler.setProperty(TestElement.TEST_CLASS, HTTPSampler.class.getName());
        httpSampler.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());
        httpSampler.setEnabled(true);
        
        return httpSampler;
    }

    private ResultCollector createResultCollector(LoadTestConfig config) {
        String resultsFile = resultsDirectory + "/" + config.getTestName() + "_results.jtl";
        
        Summariser summer = new Summariser("summary");
        ResultCollector logger = new ResultCollector(summer);
        logger.setFilename(resultsFile);
        logger.setEnabled(true);
        
        return logger;
    }

    private PerformanceTestResult processResults(LoadTestConfig config, long startTime, long endTime) {
        String resultsFile = resultsDirectory + "/" + config.getTestName() + "_results.jtl";
        
        PerformanceTestResult result = new PerformanceTestResult();
        result.setTestName(config.getTestName());
        result.setStartTime(startTime);
        result.setEndTime(endTime);
        result.setDuration(endTime - startTime);
        result.setResultsFile(resultsFile);
        
        // Parse JTL file for detailed metrics
        try {
            if (Files.exists(Paths.get(resultsFile))) {
                PerformanceMetricsParser parser = new PerformanceMetricsParser();
                PerformanceMetrics metrics = parser.parseJTLFile(resultsFile);
                result.setMetrics(metrics);
            }
        } catch (Exception e) {
            logger.warn("Failed to parse performance results", e);
        }
        
        return result;
    }

    public PerformanceTestResult runStressTest(StressTestConfig config) {
        if (!initialized) {
            throw new IllegalStateException("JMeter engine not initialized");
        }
        
        try {
            logger.info("Starting stress test: {}", config.getTestName());
            
            // Convert stress test config to load test config
            LoadTestConfig loadConfig = convertStressToLoadConfig(config);
            
            return runLoadTest(loadConfig);
            
        } catch (Exception e) {
            logger.error("Stress test execution failed: {}", config.getTestName(), e);
            throw new RuntimeException("Stress test failed", e);
        }
    }

    private LoadTestConfig convertStressToLoadConfig(StressTestConfig stressConfig) {
        LoadTestConfig loadConfig = new LoadTestConfig();
        loadConfig.setTestName(stressConfig.getTestName());
        loadConfig.setNumThreads(stressConfig.getMaxUsers());
        loadConfig.setRampUpTime(stressConfig.getRampUpTime());
        loadConfig.setLoopCount(stressConfig.getDuration() / stressConfig.getThinkTime());
        loadConfig.setHttpRequests(stressConfig.getHttpRequests());
        loadConfig.setUserVariables(stressConfig.getUserVariables());
        
        return loadConfig;
    }

    public void shutdown() {
        if (jmeterEngine != null) {
            try {
                jmeterEngine.stopTest();
                jmeterEngine.exit();
                logger.info("JMeter engine shut down successfully");
            } catch (Exception e) {
                logger.warn("Error during JMeter engine shutdown", e);
            }
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    public String getResultsDirectory() {
        return resultsDirectory;
    }
}
