package com.inditex.test.stepdefinitions;

import com.inditex.test.accessibility.AccessibilityHelper;
import com.inditex.test.pages.HomePage;
import com.inditex.test.pages.ProductPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class AccessibilitySteps {
    private static final Logger logger = LoggerFactory.getLogger(AccessibilitySteps.class);
    
    private AccessibilityHelper accessibilityHelper;
    private HomePage homePage;
    private ProductPage productPage;
    private Map<String, Object> accessibilityResults;

    @Given("I am on an accessible webpage")
    public void iAmOnAnAccessibleWebpage() {
        accessibilityHelper = new AccessibilityHelper();
        homePage = new HomePage();
        logger.info("Initialized accessibility testing on current webpage");
    }

    @Given("I navigate to the homepage for accessibility testing")
    public void iNavigateToTheHomepageForAccessibilityTesting() {
        homePage = new HomePage();
        homePage.navigateToBrandHomepage("Zara", "ES", "es");
        accessibilityHelper = new AccessibilityHelper();
        logger.info("Navigated to homepage for accessibility testing");
    }

    @Given("I navigate to a product page for accessibility testing")
    public void iNavigateToAProductPageForAccessibilityTesting() {
        // First go to homepage, then navigate to a product
        iNavigateToTheHomepageForAccessibilityTesting();
        productPage = homePage.searchForProduct("dress");
        logger.info("Navigated to product page for accessibility testing");
    }

    @When("I run accessibility scan")
    public void iRunAccessibilityScan() {
        try {
            accessibilityResults = accessibilityHelper.runAccessibilityScan();
            Assert.assertNotNull(accessibilityResults, "Accessibility scan results should not be null");
            logger.info("Accessibility scan completed successfully");
        } catch (Exception e) {
            logger.error("Failed to run accessibility scan", e);
            throw new RuntimeException("Accessibility scan failed", e);
        }
    }

    @When("I run WCAG {string} level {string} compliance check")
    public void iRunWcagComplianceCheck(String wcagVersion, String complianceLevel) {
        try {
            accessibilityResults = accessibilityHelper.runWcagComplianceCheck(wcagVersion, complianceLevel);
            Assert.assertNotNull(accessibilityResults, "WCAG compliance check results should not be null");
            logger.info("WCAG {} level {} compliance check completed", wcagVersion, complianceLevel);
        } catch (Exception e) {
            logger.error("Failed to run WCAG compliance check", e);
            throw new RuntimeException("WCAG compliance check failed", e);
        }
    }

    @When("I test keyboard navigation")
    public void iTestKeyboardNavigation() {
        try {
            boolean keyboardAccessible = accessibilityHelper.testKeyboardNavigation();
            Assert.assertTrue(keyboardAccessible, "Page should be keyboard accessible");
            logger.info("Keyboard navigation test completed successfully");
        } catch (Exception e) {
            logger.error("Keyboard navigation test failed", e);
            throw new RuntimeException("Keyboard navigation test failed", e);
        }
    }

    @When("I test screen reader compatibility")
    public void iTestScreenReaderCompatibility() {
        try {
            boolean screenReaderCompatible = accessibilityHelper.testScreenReaderCompatibility();
            Assert.assertTrue(screenReaderCompatible, "Page should be compatible with screen readers");
            logger.info("Screen reader compatibility test completed successfully");
        } catch (Exception e) {
            logger.error("Screen reader compatibility test failed", e);
            throw new RuntimeException("Screen reader compatibility test failed", e);
        }
    }

    @When("I check color contrast ratios")
    public void iCheckColorContrastRatios() {
        try {
            Map<String, Object> contrastResults = accessibilityHelper.checkColorContrast();
            Assert.assertNotNull(contrastResults, "Color contrast results should not be null");
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> violations = (List<Map<String, Object>>) contrastResults.get("violations");
            if (violations != null && !violations.isEmpty()) {
                logger.warn("Found {} color contrast violations", violations.size());
                for (Map<String, Object> violation : violations) {
                    logger.warn("Color contrast violation: {}", violation.get("description"));
                }
            }
            logger.info("Color contrast check completed");
        } catch (Exception e) {
            logger.error("Color contrast check failed", e);
            throw new RuntimeException("Color contrast check failed", e);
        }
    }

    @When("I validate alt text for images")
    public void iValidateAltTextForImages() {
        try {
            boolean altTextValid = accessibilityHelper.validateImageAltText();
            Assert.assertTrue(altTextValid, "All images should have appropriate alt text");
            logger.info("Image alt text validation completed successfully");
        } catch (Exception e) {
            logger.error("Image alt text validation failed", e);
            throw new RuntimeException("Image alt text validation failed", e);
        }
    }

    @When("I check form accessibility")
    public void iCheckFormAccessibility() {
        try {
            boolean formsAccessible = accessibilityHelper.validateFormAccessibility();
            Assert.assertTrue(formsAccessible, "Forms should be accessible");
            logger.info("Form accessibility validation completed successfully");
        } catch (Exception e) {
            logger.error("Form accessibility validation failed", e);
            throw new RuntimeException("Form accessibility validation failed", e);
        }
    }

    @Then("the page should have no accessibility violations")
    public void thePageShouldHaveNoAccessibilityViolations() {
        Assert.assertNotNull(accessibilityResults, "Accessibility results should be available");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> violations = (List<Map<String, Object>>) accessibilityResults.get("violations");
        
        if (violations != null && !violations.isEmpty()) {
            StringBuilder violationDetails = new StringBuilder("Accessibility violations found:\n");
            for (Map<String, Object> violation : violations) {
                violationDetails.append("- ").append(violation.get("description")).append("\n");
                violationDetails.append("  Impact: ").append(violation.get("impact")).append("\n");
                violationDetails.append("  Help: ").append(violation.get("helpUrl")).append("\n\n");
            }
            Assert.fail(violationDetails.toString());
        }
        
        logger.info("No accessibility violations found");
    }

    @Then("the page should pass WCAG {string} level {string} compliance")
    public void thePageShouldPassWcagCompliance(String wcagVersion, String complianceLevel) {
        Assert.assertNotNull(accessibilityResults, "WCAG compliance results should be available");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> violations = (List<Map<String, Object>>) accessibilityResults.get("violations");
        
        if (violations != null && !violations.isEmpty()) {
            // Filter violations by impact level based on compliance level
            long criticalViolations = violations.stream()
                .filter(v -> isCriticalForComplianceLevel(v, complianceLevel))
                .count();
            
            if (criticalViolations > 0) {
                StringBuilder violationDetails = new StringBuilder(
                    String.format("WCAG %s Level %s compliance failed with %d critical violations:\n", 
                        wcagVersion, complianceLevel, criticalViolations));
                
                violations.stream()
                    .filter(v -> isCriticalForComplianceLevel(v, complianceLevel))
                    .forEach(violation -> {
                        violationDetails.append("- ").append(violation.get("description")).append("\n");
                        violationDetails.append("  Impact: ").append(violation.get("impact")).append("\n");
                        violationDetails.append("  Help: ").append(violation.get("helpUrl")).append("\n\n");
                    });
                
                Assert.fail(violationDetails.toString());
            }
        }
        
        logger.info("Page passes WCAG {} Level {} compliance", wcagVersion, complianceLevel);
    }

    @Then("all interactive elements should be keyboard accessible")
    public void allInteractiveElementsShouldBeKeyboardAccessible() {
        try {
            List<String> inaccessibleElements = accessibilityHelper.findKeyboardInaccessibleElements();
            
            if (!inaccessibleElements.isEmpty()) {
                StringBuilder errorMessage = new StringBuilder("Found keyboard inaccessible elements:\n");
                for (String element : inaccessibleElements) {
                    errorMessage.append("- ").append(element).append("\n");
                }
                Assert.fail(errorMessage.toString());
            }
            
            logger.info("All interactive elements are keyboard accessible");
        } catch (Exception e) {
            logger.error("Failed to validate keyboard accessibility", e);
            throw new RuntimeException("Keyboard accessibility validation failed", e);
        }
    }

    @Then("all images should have meaningful alt text")
    public void allImagesShouldHaveMeaningfulAltText() {
        try {
            List<String> imagesWithoutAltText = accessibilityHelper.findImagesWithoutAltText();
            List<String> imagesWithBadAltText = accessibilityHelper.findImagesWithInappropriateAltText();
            
            StringBuilder errorMessage = new StringBuilder();
            
            if (!imagesWithoutAltText.isEmpty()) {
                errorMessage.append("Images without alt text:\n");
                for (String image : imagesWithoutAltText) {
                    errorMessage.append("- ").append(image).append("\n");
                }
            }
            
            if (!imagesWithBadAltText.isEmpty()) {
                errorMessage.append("Images with inappropriate alt text:\n");
                for (String image : imagesWithBadAltText) {
                    errorMessage.append("- ").append(image).append("\n");
                }
            }
            
            if (errorMessage.length() > 0) {
                Assert.fail(errorMessage.toString());
            }
            
            logger.info("All images have meaningful alt text");
        } catch (Exception e) {
            logger.error("Failed to validate image alt text", e);
            throw new RuntimeException("Image alt text validation failed", e);
        }
    }

    @Then("color contrast should meet accessibility standards")
    public void colorContrastShouldMeetAccessibilityStandards() {
        try {
            Map<String, Object> contrastResults = accessibilityHelper.checkColorContrast();
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> violations = (List<Map<String, Object>>) contrastResults.get("violations");
            
            if (violations != null && !violations.isEmpty()) {
                StringBuilder violationDetails = new StringBuilder("Color contrast violations found:\n");
                for (Map<String, Object> violation : violations) {
                    violationDetails.append("- ").append(violation.get("description")).append("\n");
                    violationDetails.append("  Contrast ratio: ").append(violation.get("contrastRatio")).append("\n");
                    violationDetails.append("  Required ratio: ").append(violation.get("requiredRatio")).append("\n\n");
                }
                Assert.fail(violationDetails.toString());
            }
            
            logger.info("Color contrast meets accessibility standards");
        } catch (Exception e) {
            logger.error("Failed to validate color contrast", e);
            throw new RuntimeException("Color contrast validation failed", e);
        }
    }

    @Then("page structure should be semantically correct")
    public void pageStructureShouldBeSemanticallyCorrect() {
        try {
            boolean semanticallyCorrect = accessibilityHelper.validatePageStructure();
            Assert.assertTrue(semanticallyCorrect, "Page structure should be semantically correct");
            logger.info("Page structure is semantically correct");
        } catch (Exception e) {
            logger.error("Failed to validate page structure", e);
            throw new RuntimeException("Page structure validation failed", e);
        }
    }

    @Then("focus indicators should be visible")
    public void focusIndicatorsShouldBeVisible() {
        try {
            boolean focusVisible = accessibilityHelper.validateFocusIndicators();
            Assert.assertTrue(focusVisible, "Focus indicators should be visible");
            logger.info("Focus indicators are visible");
        } catch (Exception e) {
            logger.error("Failed to validate focus indicators", e);
            throw new RuntimeException("Focus indicator validation failed", e);
        }
    }

    // Helper method to determine if a violation is critical for the specified compliance level
    private boolean isCriticalForComplianceLevel(Map<String, Object> violation, String complianceLevel) {
        String impact = (String) violation.get("impact");
        if (impact == null) return false;
        
        switch (complianceLevel.toUpperCase()) {
            case "A":
                return "critical".equals(impact) || "serious".equals(impact);
            case "AA":
                return "critical".equals(impact) || "serious".equals(impact) || "moderate".equals(impact);
            case "AAA":
                return true; // All violations are considered for AAA level
            default:
                return "critical".equals(impact);
        }
    }
}
