# 🚀 Inditex Promotions Test Automation Framework

[![Java](https://img.shields.io/badge/Java-11+-orange.svg)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-blue.svg)](https://maven.apache.org/)
[![Selenium](https://img.shields.io/badge/Selenium-4.26.0-green.svg)](https://selenium.dev/)
[![TestNG](https://img.shields.io/badge/TestNG-7.10.2-yellow.svg)](https://testng.org/)
[![Cucumber](https://img.shields.io/badge/Cucumber-7.20.1-brightgreen.svg)](https://cucumber.io/)
[![Allure](https://img.shields.io/badge/Allure-2.29.0-lightblue.svg)](https://docs.qameta.io/allure/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](https://www.docker.com/)

> **Enterprise-grade automation testing framework for Inditex promotional campaigns across all brands (Zara, Bershka, Pull&Bear, Massimo Dutti, Stradivarius, Oysho, Zara Home, Lefties) with 500+ comprehensive test scenarios.**

## 🌟 Features

### 🎯 **Comprehensive Test Coverage**
- **500+ promotional scenarios** across all 8 Inditex brands
- **Multi-dimensional testing**: 30+ countries, 7 customer types, seasonal campaigns
- **Advanced test types**: UI, API, Mobile, Accessibility, Security, Performance, Contract Testing

### 🏗️ **Enterprise Architecture**
- **Page Object Model** with proper separation of concerns
- **BDD Implementation** with Cucumber and Gherkin
- **Parallel execution** with ThreadLocal management
- **Advanced reporting** with Allure integration

### 🔧 **Modern Tech Stack**
- **Java 11+** with Maven build system
- **Selenium WebDriver 4.26.0** for UI automation
- **TestNG 7.10.2** for test orchestration
- **Cucumber 7.20.1** for BDD scenarios
- **REST Assured** for API testing
- **Pact** for contract testing

### 🛡️ **Quality Assurance**
- **Accessibility testing** with axe-core and WCAG compliance
- **Security testing** with OWASP ZAP integration
- **Performance testing** with JMeter
- **Visual regression testing** capabilities
- **Cross-browser testing** (Chrome, Firefox, Edge)

### ☁️ **Cloud & DevOps Ready**
- **Docker containerization** with multi-browser support
- **CI/CD integration** with Jenkins pipeline
- **Kubernetes deployment** ready
- **Monitoring integration** with Prometheus & Grafana

## 🚀 Quick Start

### Prerequisites
- Java 11+
- Maven 3.6+
- Docker (optional)

### Installation

```bash
# Clone the repository
git clone https://github.com/yourusername/InditexPromotionsTest.git
cd InditexPromotionsTest

# Run tests locally
mvn clean test

# Generate Allure report
mvn allure:serve
```

### Docker Deployment

```bash
# Build and run with Docker Compose
docker-compose up -d

# Run tests in containerized environment
docker-compose exec inditex-promotions-test mvn test
```

## 📊 Test Scenarios

### **Promotional Testing Coverage**

| Brand | Countries | Customer Types | Scenarios |
|-------|-----------|----------------|-----------|
| Zara | 30+ | Guest, Member, VIP, Premium, Student, Employee, Corporate | 150+ |
| Bershka | 25+ | Guest, Member, Student | 80+ |
| Pull & Bear | 20+ | Guest, Member, Student | 70+ |
| Massimo Dutti | 15+ | Guest, Member, Premium, VIP | 60+ |
| Stradivarius | 18+ | Guest, Member, Student | 55+ |
| Oysho | 12+ | Guest, Member | 40+ |
| Zara Home | 10+ | Guest, Member | 30+ |
| Lefties | 8+ | Guest, Member | 25+ |

### **Seasonal Campaigns**
- 🌸 **Spring Fashion Festival** (March-April)
- ☀️ **Summer Clearance** (June-August)
- 📚 **Back to School** (August-September)
- 🎃 **Halloween Special** (October-November)
- 🛍️ **Black Friday** (November) - up to 70% discounts
- 💻 **Cyber Monday** (December)
- 🎄 **Christmas Sale** (December)
- 🎊 **New Year Clearance** (January)
- 💕 **Valentine's Day** (February)

## 🏃‍♂️ Running Tests

### Test Suites

```bash
# Smoke tests (quick validation)
mvn test -Dgroups=smoke

# Regression tests (comprehensive)
mvn test -Dgroups=regression

# API tests only
mvn test -Dgroups=api

# Mobile tests
mvn test -Dgroups=mobile,responsive

# Accessibility tests
mvn test -Dgroups=accessibility,wcag

# Security tests
mvn test -Dgroups=security

# Performance tests
mvn test -Dgroups=performance

# Contract tests
mvn test -Dgroups=contract
```

### Environment Configuration

```bash
# Development environment
mvn test -Dtest.environment=dev

# Staging environment
mvn test -Dtest.environment=staging

# Production environment
mvn test -Dtest.environment=prod
```

### Browser Configuration

```bash
# Chrome (default)
mvn test -Dbrowser.type=chrome

# Firefox
mvn test -Dbrowser.type=firefox

# Edge
mvn test -Dbrowser.type=edge

# Headless mode
mvn test -Dheadless=true
```

### Parallel Execution

```bash
# Configure thread count
mvn test -Dparallel.thread.count=4

# Grid execution
mvn test -Dgrid.enabled=true -Dgrid.url=http://selenium-hub:4444/wd/hub
```

## 📁 Project Structure

```
InditexPromotionsTest/
├── src/
│   ├── main/java/com/inditex/test/
│   │   ├── config/           # Configuration management
│   │   ├── security/         # Security utilities
│   │   └── utils/            # Common utilities
│   └── test/java/com/inditex/test/
│       ├── accessibility/    # Accessibility testing
│       ├── api/             # API testing
│       ├── contract/        # Contract testing
│       ├── pages/           # Page Object Model
│       ├── performance/     # Performance testing
│       ├── runners/         # Test runners
│       ├── security/        # Security testing
│       └── stepdefinitions/ # Cucumber steps
├── src/test/resources/
│   ├── features/            # Cucumber feature files
│   ├── testdata/           # Test data (500+ scenarios)
│   └── config/             # Test configuration
├── docker-compose.yml       # Multi-browser testing setup
├── Dockerfile              # Container configuration
├── Jenkinsfile             # CI/CD pipeline
└── pom.xml                 # Maven dependencies
```

## 📈 Test Data

### **Comprehensive Test Data Generation**
- **100+ comprehensive promotion records** with diverse scenarios
- **80+ seasonal campaign records** covering major holidays
- **50+ bulk discount scenarios** with multi-buy deals
- **60+ edge case records** for security and boundary testing
- **30+ performance test records** for load testing

### **Data Management Features**
- 🔄 **Automated data generation** with realistic scenarios
- 🎯 **Geographic coverage** across 30+ countries
- 👥 **Customer segmentation** (guest, member, VIP, premium, student, employee, corporate)
- 🛡️ **Security validation** with XSS and SQL injection detection
- 📊 **Performance optimization** with cached data management

## 🔧 Configuration

### Application Properties

```properties
# Browser Configuration
browser.type=chrome
browser.timeout.explicit=15
browser.timeout.implicit=10
headless=false

# Grid Configuration
grid.enabled=false
grid.url=http://localhost:4444/wd/hub

# Parallel Execution
parallel.execution=true
parallel.thread.count=4

# Reporting
allure.results.directory=target/allure-results
generate.report=true

# Security Testing
security.testing.enabled=true
zap.proxy.port=8090

# Accessibility Testing
accessibility.testing.enabled=true
accessibility.wcag.level=AA
```

## 📊 Reporting

### **Allure Reports**
- 📈 **Comprehensive test results** with trends
- 🎯 **Test categorization** by feature and priority
- 📸 **Screenshots** on failure
- 📋 **Detailed step-by-step execution**
- 🔗 **Integration with CI/CD** systems

### **Custom Reports**
- 🚀 **Performance reports** with JMeter integration
- 🛡️ **Security reports** with vulnerability analysis
- ♿ **Accessibility reports** with WCAG compliance
- 📱 **Mobile testing reports** with device matrix

## 🐳 Docker Support

### **Multi-Browser Grid**
```yaml
# Selenium Hub with Chrome, Firefox, and Edge nodes
# Automatic scaling and load balancing
# VNC support for debugging
# Integrated test execution environment
```

### **Production Features**
- 🏗️ **Multi-stage builds** for optimization
- 🔒 **Security hardening** with non-root users
- 📊 **Health checks** and monitoring
- 🔄 **Auto-restart** capabilities

## 🔄 CI/CD Integration

### **Jenkins Pipeline**
```groovy
// Automated builds on code changes
// Parallel test execution
// Quality gates with test results
// Automated deployments
// Slack/Email notifications
```

### **GitHub Actions Support**
```yaml
# Cross-platform testing (Linux, Windows, macOS)
# Matrix builds for multiple Java versions
# Automatic report publishing
# Integration with GitHub Pages
```

## 🛡️ Security Features

### **Security Testing**
- 🔍 **OWASP ZAP integration** for vulnerability scanning
- 🛡️ **XSS and SQL injection** detection
- 🔒 **Authentication flow** testing
- 📋 **Security compliance** reporting

### **Data Security**
- 🔐 **Encrypted configuration** with Jasypt
- 🔑 **Secure credential management**
- 🛡️ **Input validation** and sanitization
- 📊 **Security audit trails**

## ♿ Accessibility Testing

### **WCAG Compliance**
- ✅ **WCAG 2.1 AA/AAA** validation
- 🎯 **axe-core integration** for automated scanning
- ⌨️ **Keyboard navigation** testing
- 🔊 **Screen reader compatibility**
- 🎨 **Color contrast** validation

### **Accessibility Features**
- 📋 **Comprehensive accessibility reports**
- 🔧 **Automated remediation suggestions**
- 📱 **Mobile accessibility** testing
- 🌐 **Multi-language** support

## 🚀 Performance Testing

### **JMeter Integration**
- 📈 **Load testing** with 1000+ concurrent users
- 💥 **Stress testing** to find failure points
- ⏰ **Endurance testing** for extended periods
- 📊 **Spike testing** for traffic bursts

### **Performance Metrics**
- ⚡ **Response time analysis**
- 📊 **Throughput measurements**
- 🎯 **Error rate monitoring**
- 💾 **Resource utilization** tracking

## 🔗 API Testing

### **REST Assured Framework**
- 🌐 **RESTful API** testing
- 📋 **JSON Schema** validation
- 🔒 **Authentication** testing
- 📊 **Response validation**

### **Contract Testing**
- 🤝 **Pact integration** for consumer-driven contracts
- 🔄 **Provider verification**
- 📋 **Contract evolution** management
- 🚀 **CI/CD integration**

## 🏆 Best Practices

### **Code Quality**
- 📝 **Clean code principles**
- 🧩 **SOLID design patterns**
- 🔧 **Dependency injection**
- 📋 **Comprehensive logging**

### **Test Design**
- 🎯 **Data-driven testing**
- 🔄 **Reusable components**
- 🛡️ **Error handling**
- 📊 **Test metrics** and KPIs

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📋 Requirements

### **System Requirements**
- **Java**: 11 or higher
- **Maven**: 3.6 or higher
- **Memory**: 4GB RAM minimum, 8GB recommended
- **Storage**: 2GB free space

### **Browser Requirements**
- **Chrome**: Latest stable version
- **Firefox**: Latest stable version
- **Edge**: Latest stable version

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

- 📧 **Email**: support@inditex-testing.com
- 💬 **Slack**: #inditex-automation
- 📖 **Wiki**: [Project Wiki](https://github.com/yourusername/InditexPromotionsTest/wiki)
- 🐛 **Issues**: [GitHub Issues](https://github.com/yourusername/InditexPromotionsTest/issues)

## 🙏 Acknowledgments

- **Inditex Development Team** for requirements and feedback
- **Open Source Community** for amazing testing tools
- **Selenium Contributors** for robust automation framework
- **TestNG Team** for powerful testing capabilities

---

**Made with ❤️ for enterprise-grade automation testing**

*Ensuring promotional campaigns work flawlessly across all Inditex brands worldwide* 🌍
