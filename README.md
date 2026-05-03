# Java Text Editor QA Suite 🧪

[![Build](https://img.shields.io/badge/build-passing-brightgreen)](#)
[![Tests](https://img.shields.io/badge/tests-79%20passing-brightgreen)](#test-results)
[![JUnit](https://img.shields.io/badge/JUnit-5.10.2-blue)](#)
[![Java](https://img.shields.io/badge/Java-21-orange)](#)
[![Maven](https://img.shields.io/badge/Maven-3.8%2B-red)](#)

Automated testing framework and QA suite for **Real Editor** — a Java-based Arabic text editor with MariaDB storage and NLP capabilities (TF-IDF, PMI, PKL, POS Tagging, Stemming, Lemmatization, Root Extraction).

---

## About the Project

**Real Editor** is a powerful Arabic text editor built in Java. It integrates with MariaDB for document storage and provides advanced Arabic NLP processing features. This repository contains the complete automated test suite covering all major subsystems.

### Application Features

| Category | Feature |
|----------|---------|
| **File Management** | Create, Read, Update, Delete files in MariaDB |
| **Import** | Import `.txt` and `.md5` files from the filesystem |
| **NLP** | TF-IDF, PMI, PKL computation |
| **Linguistics** | POS Tagging, Stemming, Lemmatization, Root Extraction |
| **Search** | Full-text keyword search across documents |
| **Security** | MD5/SHA-1 hash integrity checks |

---

## Test Suite Overview

### 79 Tests · 6 Test Classes · 0 Failures

| Test Class | Tests | Issues Covered | Description |
|-----------|-------|---------------|-------------|
| `TFIDFCalculatorTest` | 14 | #2, #4 | TF-IDF positive & negative cases |
| `HashCalculatorTest` | 14 | #6 | MD5 hash integrity & known-value checks |
| `PreProcessTextTest` | 14 | — | Arabic text preprocessing pipeline |
| `SearchWordTest` | 10 | — | Keyword search & minimum-length enforcement |
| `EditorBOTest` | 21 | #1, #5 | Auto-save boundary tests & file import |
| `FileImporterTest` | 6 | #5 | Import command execution contract |

---

## Issues Resolved

### Issue #1 — Auto-Save Boundary Tests
- Successful create/update delegates to DB layer correctly
- DB failure propagated as `false` return value
- Empty file name / empty content boundary conditions
- Very large content (100k+ chars) handled without exception

### Issue #2 — TF-IDF Positive Case
- Valid Arabic corpus yields a finite, non-NaN score
- Score is reproducible across multiple calls (same input → same output)
- Single-document corpus handled correctly
- Manual tolerance check: score is a real finite number

### Issue #4 — TF-IDF Negative Case
- Empty string input — no crash, finite result
- Special characters only — no crash, finite result
- Whitespace-only input — no exception
- Empty corpus — no exception
- `null` corpus entry — `NullPointerException` thrown predictably
- Non-Arabic text (Latin, digits, symbols) — no crash

### Issue #5 — ImportCommand execute() Test
- Valid `.txt` file — content loaded into editor, file name preserved
- File content matches original bytes
- `.md5` extension accepted; `.pdf` rejected — DB not called
- Non-existent path — returns `false`, no exception
- Zero-byte file — no exception
- File without extension — rejected

### Issue #6 — Hash Integrity (MD5)
- MD5(`""`) = `D41D8CD98F00B204E9800998ECF8427E` (RFC-1321)
- MD5(`"hello"`) = `5D41402ABC4B2A76B9719D911017C592`
- MD5("The quick brown fox…") matches known reference
- Arabic text — always 32-char uppercase hex output
- Same input always yields same hash (idempotent)
- Different inputs produce different hashes (no collision)
- `null` input throws predictably

---

## Project Structure

```
Java-Text-Editor-QA-Suite/
├── src/                          # Main application source
│   ├── bll/                      # Business Logic Layer
│   ├── dal/                      # Data Access Layer
│   ├── dto/                      # Data Transfer Objects
│   └── pl/                       # Presentation Layer (Swing GUI)
│
├── Testing/                      # ✅ JUnit 5 Test Suite (79 tests)
│   ├── dal/
│   │   ├── TFIDFCalculatorTest.java   # Issues #2, #4
│   │   ├── HashCalculatorTest.java    # Issue #6
│   │   └── PreProcessTextTest.java
│   ├── bll/
│   │   ├── EditorBOTest.java          # Issues #1, #5
│   │   └── SearchWordTest.java
│   └── pl/
│       └── FileImporterTest.java      # Issue #5
│
├── resource/                     # Third-party JARs (Arabic NLP, MariaDB)
├── Report/                       # Test reports (surefire XML/TXT)
├── logs/                         # Application logs
├── pom.xml                       # Maven build configuration
├── config.properties             # Database connection settings
└── README.md
```

---

## Getting Started

### Prerequisites

| Tool | Version |
|------|---------|
| Java JDK | 17 or 21 |
| Maven | 3.8+ |
| MariaDB | 11.x (for running the application only) |

### Clone & Run Tests

```bash
git clone https://github.com/hashirrafique/Java-Text-Editor-QA-Suite.git
cd Java-Text-Editor-QA-Suite

# Run the full test suite (no database needed)
mvn test
```

Expected output:
```
Tests run: 79, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Run a specific test class

```bash
mvn test -Dtest=TFIDFCalculatorTest
mvn test -Dtest=HashCalculatorTest
mvn test -Dtest=EditorBOTest
```

### Build the application

```bash
mvn compile
mvn package -DskipTests
```

### Configure the Database

Edit `config.properties`:
```properties
db.url      = jdbc:mariadb://localhost:3306/realeditor
db.username = root
db.password = your_password
db.type     = dal.MariaDBDAOFactory
```

---

## Test Design Notes

**No live database required** — all tests use manual stubs (`StubFacadeDAO`, `StubEditorBO`) implementing the DAO/BO interfaces. The full suite runs without any MariaDB connection, making it CI-friendly.

**Test isolation** — each test class uses `@BeforeEach` to create fresh stubs, preventing state leakage between tests.

| Layer | Strategy |
|-------|---------|
| DAL (standalone) | Direct unit tests — `TFIDFCalculator`, `HashCalculator`, `PreProcessText` |
| DAL (DB-backed) | Manual stubs replace MariaDB |
| BLL | Manual `IFacadeDAO` stubs; tests verify delegation and return values |
| PL (GUI) | Constructor and contract tests; GUI interaction is untestable headlessly |

---

## Test Results

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running pl.FileImporterTest          Tests run: 6,  Failures: 0
Running dal.TFIDFCalculatorTest      Tests run: 14, Failures: 0
Running dal.PreProcessTextTest       Tests run: 14, Failures: 0
Running dal.HashCalculatorTest       Tests run: 14, Failures: 0
Running bll.SearchWordTest           Tests run: 10, Failures: 0
Running bll.EditorBOTest             Tests run: 21, Failures: 0

Tests run: 79, Failures: 0, Errors: 0, Skipped: 0

BUILD SUCCESS
```

---

## Installation (Full Application)

| Setup | Download |
|-------|---------|
| Single PC | [Download](https://drive.google.com/drive/folders/1aAInCbb5Oj6JfZfKciYHTXTgazPWUIy4?usp=sharing) |
| Server PC | [Download](https://drive.google.com/drive/folders/1w8qyK11KukpnU69mzSEO6ZzbApvQzco_?usp=drive_link) |
| Client PC | [Download](https://drive.google.com/drive/folders/16gFYIJnO1a_W3SbACUSC_Rz4xTG8jInc?usp=sharing) |

Documentation: [View Report](https://drive.google.com/drive/folders/185O5gpF0_EKI380CtnB6A0AK-Tph2Uz-?usp=sharing)

---

## Author

**Hashir Rafique** · BS Computer Science · FAST-NUCES CFD Campus · 22F-3294
