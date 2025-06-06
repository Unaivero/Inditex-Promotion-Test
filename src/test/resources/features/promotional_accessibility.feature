Feature: Promotional Accessibility Testing
  As a QA Engineer
  I want to ensure promotional content is accessible to users with disabilities
  So that all users can access promotional pricing information regardless of their abilities

  Background:
    Given accessibility testing is enabled
    And I am on the homepage for brand "Zara" and country "ES" in "Spanish" language

  @accessibility @wcag @level_aa
  Scenario: WCAG 2.1 AA compliance for promotional content
    Given I navigate to a product with SKU "ZARA001ES" with active promotion
    When I run accessibility scan using axe-core
    Then there should be no WCAG 2.1 AA violations
    And promotional price elements should have proper accessibility attributes
    And color contrast should meet AA standards for all promotional text

  @accessibility @screen_reader
  Scenario: Screen reader compatibility for promotional pricing
    Given I am using screen reader simulation
    And I navigate to a product with SKU "ZARA001ES" with active promotion
    When I navigate using screen reader controls
    Then promotional price should be announced correctly
    And original price should be announced with "was" prefix
    And discount information should be clearly announced
    And promotional end date should be accessible

  @accessibility @keyboard_navigation
  Scenario: Keyboard navigation for promotional elements
    Given I am using keyboard navigation only
    And I navigate to a product with active promotion
    When I navigate using Tab key through promotional elements
    Then all promotional elements should be focusable via keyboard
    And focus indicators should be clearly visible
    And promotional code input should be keyboard accessible
    And "Add to Cart" button should be reachable via keyboard

  @accessibility @aria_labels
  Scenario: ARIA labels and roles for promotional content
    Given I navigate to a product with SKU "ZARA001ES" with active promotion
    When I inspect promotional elements for ARIA attributes
    Then promotional price should have aria-label describing the discount
    And original price should have aria-label "Original price"
    And discount badge should have role="img" with alt text
    And promotional details should have proper heading structure

  @accessibility @color_contrast
  Scenario Outline: Color contrast compliance for promotional elements
    Given I navigate to a product with active promotion
    When I test color contrast for promotional element "<element>"
    Then the contrast ratio should meet WCAG AA standard of 4.5:1
    And the element should remain readable with high contrast mode

    Examples:
      | element                |
      | promotional_price      |
      | original_price         |
      | discount_badge         |
      | promotional_text       |
      | save_amount           |

  @accessibility @focus_management
  Scenario: Focus management in promotional modals and overlays
    Given I navigate to a product with active promotion
    When I open promotional details modal using keyboard
    Then focus should be trapped within the modal
    And modal should have proper aria-describedby attributes
    When I press Escape key
    Then modal should close and focus should return to trigger element

  @accessibility @alternative_text
  Scenario: Alternative text for promotional images and icons
    Given I navigate to a product with active promotion
    When I inspect promotional images and icons
    Then all promotional images should have meaningful alt text
    And decorative images should have empty alt attributes
    And discount icons should have descriptive alt text
    And promotional badges should convey discount information

  @accessibility @semantic_markup
  Scenario: Semantic HTML markup for promotional content
    Given I navigate to a product with active promotion
    When I inspect the HTML structure
    Then promotional prices should use appropriate semantic elements
    And discount information should be in proper list structure
    And promotional headings should follow logical hierarchy
    And promotional sections should have landmark roles

  @accessibility @text_alternatives
  Scenario: Text alternatives for non-text promotional content
    Given I navigate to promotional product gallery
    When I inspect promotional video content
    Then promotional videos should have captions or transcripts
    And audio announcements should have text alternatives
    And visual promotional charts should have data tables
    And animated promotional content should have text descriptions

  @accessibility @language_attributes
  Scenario: Language attributes for promotional content
    Given I am browsing in multiple languages
    When I switch between Spanish and English promotional content
    Then lang attributes should be properly set for each language
    And promotional text should be correctly identified by screen readers
    And currency symbols should have proper language context

  @accessibility @error_messages
  Scenario: Accessible error messages for promotional codes
    Given I am on the cart page with items
    When I enter invalid promotional code "INVALID123"
    Then error message should be announced by screen reader
    And error should be associated with input field via aria-describedby
    And error message should have appropriate role and live region
    And focus should be moved to error message

  @accessibility @form_labels
  Scenario: Proper form labels for promotional code entry
    Given I am on the cart page with items
    When I inspect the promotional code form
    Then promotional code input should have associated label
    And label should be programmatically linked to input
    And placeholder text should not be the only form of labeling
    And required field indicators should be accessible

  @accessibility @responsive_accessibility
  Scenario: Accessibility on mobile devices
    Given I am using mobile device "iPhone 12"
    And I navigate to a product with active promotion
    When I test accessibility on mobile viewport
    Then touch targets should be minimum 44px for promotional elements
    And promotional content should be accessible via mobile screen readers
    And zoom functionality should work properly for promotional text
    And orientation changes should maintain accessibility

  @accessibility @high_contrast
  Scenario: High contrast mode compatibility
    Given I enable high contrast mode
    And I navigate to a product with active promotion
    When I inspect promotional elements in high contrast mode
    Then all promotional content should remain visible
    And promotional badges should have proper borders in high contrast
    And discount highlights should be distinguishable
    And promotional buttons should have clear boundaries

  @accessibility @reduced_motion
  Scenario: Reduced motion preferences for promotional content
    Given I have reduced motion preferences enabled
    And I navigate to promotional product page
    When promotional animations are present
    Then animations should respect prefers-reduced-motion setting
    And promotional carousels should not auto-advance
    And promotional transitions should be minimized
    And animated promotional badges should have static alternatives

  @accessibility @cognitive_accessibility
  Scenario: Cognitive accessibility for promotional information
    Given I navigate to a product with complex promotional offer
    When I review promotional terms and conditions
    Then promotional information should be presented clearly
    And complex promotional rules should be explained simply
    And promotional deadlines should be clearly stated
    And promotional calculations should be transparent

  @accessibility @multi_modal
  Scenario: Multi-modal access to promotional content
    Given I navigate to a product with active promotion
    When I access promotional information through different modalities
    Then promotional price should be available visually and auditorily
    And promotional details should be accessible via mouse, keyboard, and touch
    And promotional code entry should support voice input
    And promotional confirmations should provide multiple feedback types