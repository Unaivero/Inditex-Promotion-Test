package com.inditex.test.accessibility;

import com.deque.html.axecore.results.Results;
import com.deque.html.axecore.results.Rule;
import com.deque.html.axecore.selenium.AxeBuilder;
import com.inditex.test.config.ConfigManager;
import com.inditex.test.utils.WebDriverFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class AccessibilityHelper {
    private static final Logger logger = LoggerFactory.getLogger(AccessibilityHelper.class);
    
    private final WebDriver driver;
    private final boolean accessibilityTestingEnabled;
    private final String wcagLevel;
    private final String wcagStandards;
    
    public AccessibilityHelper() {
        this.driver = WebDriverFactory.getDriver();
        this.accessibilityTestingEnabled = ConfigManager.getBooleanProperty("accessibility.testing.enabled", true);
        this.wcagLevel = ConfigManager.getProperty("accessibility.wcag.level", "AA");
        this.wcagStandards = ConfigManager.getProperty("accessibility.standards", "WCAG21");
        
        if (!accessibilityTestingEnabled) {
            logger.warn("Accessibility testing is disabled in configuration");
        }
        
        logger.debug("AccessibilityHelper initialized - WCAG Level: {}, Standards: {}", wcagLevel, wcagStandards);
    }
    
    /**
     * Run a comprehensive accessibility scan using axe-core
     */
    public Map<String, Object> runAccessibilityScan() {
        if (!accessibilityTestingEnabled) {
            logger.info("Accessibility testing is disabled, skipping scan");
            return createEmptyResults();
        }
        
        try {
            logger.info("Running accessibility scan on current page");
            
            AxeBuilder builder = new AxeBuilder();
            Results results = builder.analyze(driver);
            
            Map<String, Object> scanResults = new HashMap<>();
            scanResults.put("violations", convertViolationsToMap(results.getViolations()));
            scanResults.put("passes", convertRulesToMap(results.getPasses()));
            scanResults.put("incomplete", convertRulesToMap(results.getIncomplete()));
            scanResults.put("inapplicable", convertRulesToMap(results.getInapplicable()));
            scanResults.put("url", driver.getCurrentUrl());
            scanResults.put("timestamp", new Date());
            
            logger.info("Accessibility scan completed - Found {} violations, {} passes", 
                results.getViolations().size(), results.getPasses().size());
            
            return scanResults;
            
        } catch (Exception e) {
            logger.error("Failed to run accessibility scan", e);
            throw new RuntimeException("Accessibility scan failed", e);
        }
    }
    
    /**
     * Run WCAG compliance check for specific version and level
     */
    public Map<String, Object> runWcagComplianceCheck(String wcagVersion, String complianceLevel) {
        if (!accessibilityTestingEnabled) {
            logger.info("Accessibility testing is disabled, skipping WCAG compliance check");
            return createEmptyResults();
        }
        
        try {
            logger.info("Running WCAG {} Level {} compliance check", wcagVersion, complianceLevel);
            
            AxeBuilder builder = new AxeBuilder();
            
            // Configure tags based on WCAG version and level
            List<String> tags = getWcagTags(wcagVersion, complianceLevel);
            builder.withTags(tags);
            
            Results results = builder.analyze(driver);
            
            Map<String, Object> complianceResults = new HashMap<>();
            complianceResults.put("violations", convertViolationsToMap(results.getViolations()));
            complianceResults.put("wcagVersion", wcagVersion);
            complianceResults.put("complianceLevel", complianceLevel);
            complianceResults.put("tags", tags);
            complianceResults.put("url", driver.getCurrentUrl());
            complianceResults.put("timestamp", new Date());
            
            boolean isCompliant = results.getViolations().isEmpty() || 
                hasOnlyMinorViolations(results.getViolations(), complianceLevel);
            complianceResults.put("isCompliant", isCompliant);
            
            logger.info("WCAG {} Level {} compliance check completed - Compliant: {}", 
                wcagVersion, complianceLevel, isCompliant);
            
            return complianceResults;
            
        } catch (Exception e) {
            logger.error("Failed to run WCAG compliance check", e);
            throw new RuntimeException("WCAG compliance check failed", e);
        }
    }
    
    /**
     * Test keyboard navigation accessibility
     */
    public boolean testKeyboardNavigation() {
        if (!accessibilityTestingEnabled) {
            logger.info("Accessibility testing is disabled, skipping keyboard navigation test");
            return true;
        }
        
        try {
            logger.info("Testing keyboard navigation accessibility");
            
            // Find all interactive elements
            List<WebElement> interactiveElements = findInteractiveElements();
            List<String> issues = new ArrayList<>();
            
            for (WebElement element : interactiveElements) {
                if (!isKeyboardAccessible(element)) {
                    String elementInfo = getElementInfo(element);
                    issues.add(elementInfo);
                    logger.warn("Keyboard inaccessible element found: {}", elementInfo);
                }
            }
            
            boolean isAccessible = issues.isEmpty();
            logger.info("Keyboard navigation test completed - Issues found: {}", issues.size());
            
            return isAccessible;
            
        } catch (Exception e) {
            logger.error("Failed to test keyboard navigation", e);
            throw new RuntimeException("Keyboard navigation test failed", e);
        }
    }
    
    /**
     * Test screen reader compatibility
     */
    public boolean testScreenReaderCompatibility() {
        if (!accessibilityTestingEnabled) {
            logger.info("Accessibility testing is disabled, skipping screen reader compatibility test");
            return true;
        }
        
        try {
            logger.info("Testing screen reader compatibility");
            
            List<String> issues = new ArrayList<>();
            
            // Check for proper heading structure
            if (!hasProperHeadingStructure()) {
                issues.add("Improper heading structure");
            }
            
            // Check for landmark elements
            if (!hasProperLandmarks()) {
                issues.add("Missing or improper landmark elements");
            }
            
            // Check for ARIA labels
            if (!hasProperAriaLabels()) {
                issues.add("Missing or improper ARIA labels");
            }
            
            // Check for alt text on images
            if (!hasProperImageAltText()) {
                issues.add("Missing or improper image alt text");
            }
            
            boolean isCompatible = issues.isEmpty();
            logger.info("Screen reader compatibility test completed - Issues found: {}", issues.size());
            
            return isCompatible;
            
        } catch (Exception e) {
            logger.error("Failed to test screen reader compatibility", e);
            throw new RuntimeException("Screen reader compatibility test failed", e);
        }
    }
    
    /**
     * Check color contrast ratios
     */
    public Map<String, Object> checkColorContrast() {
        if (!accessibilityTestingEnabled) {
            logger.info("Accessibility testing is disabled, skipping color contrast check");
            return createEmptyResults();
        }
        
        try {
            logger.info("Checking color contrast ratios");
            
            AxeBuilder builder = new AxeBuilder();
            builder.withTags(Arrays.asList("color-contrast"));
            
            Results results = builder.analyze(driver);
            
            Map<String, Object> contrastResults = new HashMap<>();
            contrastResults.put("violations", convertViolationsToMap(results.getViolations()));
            contrastResults.put("url", driver.getCurrentUrl());
            contrastResults.put("timestamp", new Date());
            
            logger.info("Color contrast check completed - Violations found: {}", results.getViolations().size());
            
            return contrastResults;
            
        } catch (Exception e) {
            logger.error("Failed to check color contrast", e);
            throw new RuntimeException("Color contrast check failed", e);
        }
    }
    
    /**
     * Validate image alt text
     */
    public boolean validateImageAltText() {
        if (!accessibilityTestingEnabled) {
            logger.info("Accessibility testing is disabled, skipping image alt text validation");
            return true;
        }
        
        try {
            logger.info("Validating image alt text");
            
            List<WebElement> images = driver.findElements(By.tagName("img"));
            List<String> issues = new ArrayList<>();
            
            for (WebElement img : images) {
                String altText = img.getAttribute("alt");
                String src = img.getAttribute("src");
                
                if (altText == null) {
                    issues.add("Image without alt attribute: " + src);
                } else if (altText.trim().isEmpty() && !isDecorativeImage(img)) {
                    issues.add("Image with empty alt text (not decorative): " + src);
                } else if (hasInappropriateAltText(altText)) {
                    issues.add("Image with inappropriate alt text: " + src + " (alt: " + altText + ")");
                }
            }
            
            boolean isValid = issues.isEmpty();
            logger.info("Image alt text validation completed - Issues found: {}", issues.size());
            
            return isValid;
            
        } catch (Exception e) {
            logger.error("Failed to validate image alt text", e);
            throw new RuntimeException("Image alt text validation failed", e);
        }
    }
    
    /**
     * Validate form accessibility
     */
    public boolean validateFormAccessibility() {
        if (!accessibilityTestingEnabled) {
            logger.info("Accessibility testing is disabled, skipping form accessibility validation");
            return true;
        }
        
        try {
            logger.info("Validating form accessibility");
            
            List<WebElement> formElements = driver.findElements(By.cssSelector("input, select, textarea"));
            List<String> issues = new ArrayList<>();
            
            for (WebElement element : formElements) {
                if (!hasProperLabel(element)) {
                    String elementInfo = getElementInfo(element);
                    issues.add("Form element without proper label: " + elementInfo);
                }
                
                if (!hasProperErrorHandling(element)) {
                    String elementInfo = getElementInfo(element);
                    issues.add("Form element without proper error handling: " + elementInfo);
                }
            }
            
            boolean isAccessible = issues.isEmpty();
            logger.info("Form accessibility validation completed - Issues found: {}", issues.size());
            
            return isAccessible;
            
        } catch (Exception e) {
            logger.error("Failed to validate form accessibility", e);
            throw new RuntimeException("Form accessibility validation failed", e);
        }
    }
    
    /**
     * Find keyboard inaccessible elements
     */
    public List<String> findKeyboardInaccessibleElements() {
        List<WebElement> interactiveElements = findInteractiveElements();
        return interactiveElements.stream()
            .filter(element -> !isKeyboardAccessible(element))
            .map(this::getElementInfo)
            .collect(Collectors.toList());
    }
    
    /**
     * Find images without alt text
     */
    public List<String> findImagesWithoutAltText() {
        List<WebElement> images = driver.findElements(By.tagName("img"));
        return images.stream()
            .filter(img -> {
                String altText = img.getAttribute("alt");
                return altText == null || (altText.trim().isEmpty() && !isDecorativeImage(img));
            })
            .map(img -> img.getAttribute("src"))
            .collect(Collectors.toList());
    }
    
    /**
     * Find images with inappropriate alt text
     */
    public List<String> findImagesWithInappropriateAltText() {
        List<WebElement> images = driver.findElements(By.tagName("img"));
        return images.stream()
            .filter(img -> {
                String altText = img.getAttribute("alt");
                return altText != null && hasInappropriateAltText(altText);
            })
            .map(img -> img.getAttribute("src") + " (alt: " + img.getAttribute("alt") + ")")
            .collect(Collectors.toList());
    }
    
    /**
     * Validate page structure
     */
    public boolean validatePageStructure() {
        return hasProperHeadingStructure() && hasProperLandmarks() && hasProperDocumentStructure();
    }
    
    /**
     * Validate focus indicators
     */
    public boolean validateFocusIndicators() {
        try {
            List<WebElement> focusableElements = findInteractiveElements();
            
            for (WebElement element : focusableElements) {
                // Focus on element and check if focus indicator is visible
                element.click();
                
                // Use JavaScript to check computed styles for focus indicators
                JavascriptExecutor js = (JavascriptExecutor) driver;
                String focusOutline = (String) js.executeScript(
                    "return window.getComputedStyle(arguments[0], ':focus').outline;", element);
                String focusBoxShadow = (String) js.executeScript(
                    "return window.getComputedStyle(arguments[0], ':focus').boxShadow;", element);
                
                if ((focusOutline == null || focusOutline.equals("none")) && 
                    (focusBoxShadow == null || focusBoxShadow.equals("none"))) {
                    logger.warn("Element without visible focus indicator: {}", getElementInfo(element));
                    return false;
                }
            }
            
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to validate focus indicators", e);
            return false;
        }
    }
    
    // Helper methods
    
    private Map<String, Object> createEmptyResults() {
        Map<String, Object> emptyResults = new HashMap<>();
        emptyResults.put("violations", Collections.emptyList());
        emptyResults.put("passes", Collections.emptyList());
        emptyResults.put("incomplete", Collections.emptyList());
        emptyResults.put("inapplicable", Collections.emptyList());
        return emptyResults;
    }
    
    private List<Map<String, Object>> convertViolationsToMap(List<Rule> violations) {
        return violations.stream().map(this::ruleToMap).collect(Collectors.toList());
    }
    
    private List<Map<String, Object>> convertRulesToMap(List<Rule> rules) {
        return rules.stream().map(this::ruleToMap).collect(Collectors.toList());
    }
    
    private Map<String, Object> ruleToMap(Rule rule) {
        Map<String, Object> ruleMap = new HashMap<>();
        ruleMap.put("id", rule.getId());
        ruleMap.put("description", rule.getDescription());
        ruleMap.put("impact", rule.getImpact());
        ruleMap.put("help", rule.getHelp());
        ruleMap.put("helpUrl", rule.getHelpUrl());
        ruleMap.put("tags", rule.getTags());
        return ruleMap;
    }
    
    private List<String> getWcagTags(String wcagVersion, String complianceLevel) {
        List<String> tags = new ArrayList<>();
        
        switch (wcagVersion.toLowerCase()) {
            case "wcag2a":
            case "wcag21":
                tags.add("wcag2a");
                if ("AA".equalsIgnoreCase(complianceLevel) || "AAA".equalsIgnoreCase(complianceLevel)) {
                    tags.add("wcag2aa");
                }
                if ("AAA".equalsIgnoreCase(complianceLevel)) {
                    tags.add("wcag2aaa");
                }
                break;
            default:
                tags.add("wcag2a");
                tags.add("wcag2aa");
        }
        
        return tags;
    }
    
    private boolean hasOnlyMinorViolations(List<Rule> violations, String complianceLevel) {
        for (Rule violation : violations) {
            String impact = violation.getImpact();
            if (isCriticalImpact(impact, complianceLevel)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isCriticalImpact(String impact, String complianceLevel) {
        if (impact == null) return false;
        
        switch (complianceLevel.toUpperCase()) {
            case "A":
                return "critical".equals(impact) || "serious".equals(impact);
            case "AA":
                return "critical".equals(impact) || "serious".equals(impact) || "moderate".equals(impact);
            case "AAA":
                return true; // All violations are considered critical for AAA
            default:
                return "critical".equals(impact);
        }
    }
    
    private List<WebElement> findInteractiveElements() {
        return driver.findElements(By.cssSelector(
            "a, button, input, select, textarea, [tabindex], [onclick], [role='button'], [role='link']"));
    }
    
    private boolean isKeyboardAccessible(WebElement element) {
        try {
            String tabIndex = element.getAttribute("tabindex");
            if (tabIndex != null && tabIndex.equals("-1")) {
                return false; // Explicitly not keyboard accessible
            }
            
            // Check if element can receive focus
            element.sendKeys("");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private String getElementInfo(WebElement element) {
        String tagName = element.getTagName();
        String id = element.getAttribute("id");
        String className = element.getAttribute("class");
        String text = element.getText();
        
        StringBuilder info = new StringBuilder(tagName);
        if (id != null && !id.isEmpty()) {
            info.append("#").append(id);
        }
        if (className != null && !className.isEmpty()) {
            info.append(".").append(className.replace(" ", "."));
        }
        if (text != null && !text.isEmpty() && text.length() < 50) {
            info.append(" ('").append(text).append("')");
        }
        
        return info.toString();
    }
    
    private boolean hasProperHeadingStructure() {
        List<WebElement> headings = driver.findElements(By.cssSelector("h1, h2, h3, h4, h5, h6"));
        
        if (headings.isEmpty()) {
            return false; // No headings found
        }
        
        // Check if there's at least one h1
        boolean hasH1 = headings.stream().anyMatch(h -> h.getTagName().equals("h1"));
        return hasH1;
    }
    
    private boolean hasProperLandmarks() {
        List<WebElement> landmarks = driver.findElements(By.cssSelector(
            "main, nav, header, footer, aside, section, [role='main'], [role='navigation'], [role='banner'], [role='contentinfo']"));
        return !landmarks.isEmpty();
    }
    
    private boolean hasProperAriaLabels() {
        List<WebElement> elementsNeedingLabels = driver.findElements(By.cssSelector(
            "button:not([aria-label]):not([aria-labelledby]), input:not([aria-label]):not([aria-labelledby]):not([id])"));
        return elementsNeedingLabels.isEmpty();
    }
    
    private boolean hasProperImageAltText() {
        return findImagesWithoutAltText().isEmpty() && findImagesWithInappropriateAltText().isEmpty();
    }
    
    private boolean hasProperDocumentStructure() {
        // Check for basic document structure elements
        List<WebElement> structureElements = driver.findElements(By.cssSelector("html, head, body, title"));
        return structureElements.size() >= 3; // Should have at least html, head, body
    }
    
    private boolean isDecorativeImage(WebElement img) {
        String role = img.getAttribute("role");
        return "presentation".equals(role) || "none".equals(role);
    }
    
    private boolean hasInappropriateAltText(String altText) {
        if (altText == null) return false;
        
        String lowerAlt = altText.toLowerCase().trim();
        String[] inappropriateTexts = {"image", "picture", "photo", "graphic", "img", "pic"};
        
        for (String inappropriate : inappropriateTexts) {
            if (lowerAlt.equals(inappropriate) || lowerAlt.startsWith(inappropriate + " of")) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean hasProperLabel(WebElement element) {
        String id = element.getAttribute("id");
        String ariaLabel = element.getAttribute("aria-label");
        String ariaLabelledBy = element.getAttribute("aria-labelledby");
        
        // Check if element has aria-label or aria-labelledby
        if (ariaLabel != null && !ariaLabel.trim().isEmpty()) {
            return true;
        }
        
        if (ariaLabelledBy != null && !ariaLabelledBy.trim().isEmpty()) {
            return true;
        }
        
        // Check if there's a label element for this input
        if (id != null && !id.trim().isEmpty()) {
            List<WebElement> labels = driver.findElements(By.cssSelector("label[for='" + id + "']"));
            if (!labels.isEmpty()) {
                return true;
            }
        }
        
        // Check if input is wrapped in a label
        try {
            WebElement parent = element.findElement(By.xpath(".."));
            return parent.getTagName().equals("label");
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean hasProperErrorHandling(WebElement element) {
        String ariaDescribedBy = element.getAttribute("aria-describedby");
        String ariaInvalid = element.getAttribute("aria-invalid");
        
        // For now, just check if error handling attributes are present
        // In a real implementation, you would validate the actual error messages
        return ariaDescribedBy != null || ariaInvalid != null;
    }
}
