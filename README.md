# 🚀 ASTROGENESIS

### NASA BioScience AI Dashboard & Knowledge Engine

**Description:**
Astrogenesis is an AI-powered knowledge management platform developed to analyze and summarize NASA’s bioscience research. It integrates **semantic search**, **RAG (Retrieval-Augmented Generation)**, and **LLM-driven reporting** to provide mission-relevant insights, identify knowledge gaps, and support scientific and operational decision-making.

---

## 📚 TABLE OF CONTENTS

1. [Project Overview](#1-project-overview)
2. [Technology Stack](#2-technology-stack)
3. [Architecture](#3-architecture)
4. [Database Model](#4-database-model)
5. [Service Layer](#5-service-layer)
6. [AI & Machine Learning Integration](#6-ai--machine-learning-integration)
7. [API Endpoints](#7-api-endpoints)
8. [Frontend Structure](#8-frontend-structure)
9. [Key Features](#9-key-features)
10. [Installation & Setup](#10-installation--setup)
11. [Performance & Optimization](#11-performance--optimization)
12. [Security](#12-security)
13. [Project Stats](#-project-statistics)
14. [Future Development](#-future-development)
15. [Acknowledgments](#-acknowledgments)

---

## 1. PROJECT OVERVIEW

**Astrogenesis** is an AI-driven dashboard and knowledge engine that consolidates **NASA bioscience research** for semantic discovery, analysis, and reporting.

### 🎯 Objectives

* Aggregate **608+ NASA bioscience publications**
* Provide **semantic search** across research datasets
* Generate **AI-assisted analytical summaries** (RAG pipeline)
* Support **audience-specific reporting** (scientists, managers, mission architects)
* Integrate with **NASA OSDR (Open Science Data Repository)**
* Export **professional PDF reports**

### 🚀 Problem Statement

NASA’s space biology data is scattered across various repositories and formats. Astrogenesis consolidates this knowledge into a searchable, AI-enhanced platform that:

* Identifies semantic relationships between studies
* Generates structured insights and summaries
* Highlights research gaps and consensus areas
* Supports mission planning with relevant context

---

## 2. TECHNOLOGY STACK

### 🖥 Backend

| Technology          | Version | Purpose               |
| ------------------- | ------- | --------------------- |
| **Java**            | 17      | Core language         |
| **Spring Boot**     | 3.3.5   | Backend framework     |
| **Spring Data JPA** | 3.3.5   | ORM / data layer      |
| **PostgreSQL**      | –       | Database              |
| **Thymeleaf**       | 3.3.5   | Server-side templates |
| **Maven**           | –       | Dependency management |

### 🧠 AI / ML

| Library             | Model                         | Purpose                         |
| ------------------- | ----------------------------- | ------------------------------- |
| **HuggingFace API** | `BAAI/bge-small-en-v1.5`      | Text embeddings (768-dim)       |
| **OpenRouter API**  | `Meta Llama 3.1 70B Instruct` | LLM-based reasoning & reporting |

### 📊 Visualization & Reporting

| Library        | Version  | Use                       |
| -------------- | -------- | ------------------------- |
| **iText PDF**  | 5.5.13.3 | PDF generation            |
| **JFreeChart** | 1.5.4    | Report charts             |
| **JSoup**      | 1.17.2   | NASA publication scraping |

### 🎨 Frontend

| Tech                    | Purpose               |
| ----------------------- | --------------------- |
| **HTML5 + CSS3**        | UI structure & style  |
| **Bootstrap 5**         | Responsive design     |
| **Vanilla JS**          | API interactions      |
| **Thymeleaf Templates** | Server-rendered pages |

### 🌐 External APIs

| API                      | Purpose                        |
| ------------------------ | ------------------------------ |
| **NASA OSDR API**        | Biological dataset ingestion   |
| **PubMed Central (PMC)** | Publication metadata retrieval |

---

## 3. ARCHITECTURE

Astrogenesis follows **Spring Boot MVC** with a **Layered Architecture**:

```
Presentation Layer (Controllers, Views, REST APIs)
       ↓
Service Layer (Business Logic, AI Integration)
       ↓
Repository Layer (Data Access)
       ↓
Database Layer (PostgreSQL + pgvector)
```

### Key Modules

* **Entity Layer:** Publication, OSDRDataset, ChatHistory
* **Service Layer:** SemanticSearchService, LLMService, EmbeddingService
* **Controller Layer:** AIController, ReportController, PublicationController
* **Config Layer:** API clients, database seeders

---

## 4. DATABASE MODEL

**PostgreSQL** schema with 3 core entities:

* `publications`
* `osdr_datasets`
* `chat_history`

Each entity stores metadata, embeddings, and relationships for semantic retrieval and RAG-based synthesis.

---

## 5. SERVICE LAYER

### 🧠 `LLMService`

Implements the **RAG pipeline**:

1. Query embedding (HuggingFace)
2. Semantic retrieval (cosine similarity)
3. Context assembly
4. LLM prompt construction (Llama 3.1 70B)
5. JSON response parsing

### 🔍 `SemanticSearchService`

Performs cosine similarity ranking between query and publication embeddings.

### 🧬 `EmbeddingService`

Converts text into 768-dim embeddings via HuggingFace API.

### 📄 `ReportService`

Generates styled **iText PDF reports** including LLM analysis, charts, and source metadata.

---

## 6. AI & MACHINE LEARNING INTEGRATION

Astrogenesis uses **Retrieval-Augmented Generation (RAG)**:

* Query → Embedding (768D)
* Retrieve Top-5 Similar Documents
* Assemble Context
* Generate Structured JSON via **Llama 3.1 70B**

**LLM Parameters:**

```json
{
  "model": "meta-llama/llama-3.1-70b-instruct",
  "temperature": 0.7,
  "max_tokens": 4500
}
```

**Audience Modes:** `scientist`, `manager`, `mission_architect`, `general`

---

## 7. API ENDPOINTS

| Method | Endpoint                  | Description                   | Response |
| ------ | ------------------------- | ----------------------------- | -------- |
| GET    | `/publications/search?q=` | Search publications           | JSON     |
| POST   | `/ai/chat`                | Generate AI analytical report | JSON     |
| GET    | `/report/generate?query=` | Generate PDF report           | PDF      |

---

## 8. FRONTEND STRUCTURE

**Templates:**

```
index.html
ai-dashboard.html
research.html
report.html
nasa.html
```

**Features:**

* Responsive Bootstrap 5 layout
* Dynamic AI chart rendering via Chart.js
* PDF report downloads
* Semantic publication search

---

## 9. KEY FEATURES

* ✅ **Semantic Search** (cosine similarity)
* ✅ **RAG-based AI analysis**
* ✅ **Audience-specific reports**
* ✅ **NASA OSDR integration**
* ✅ **Dynamic charts and tables**
* ✅ **Professional PDF generation**

---

## 10. INSTALLATION & SETUP

### Requirements

* Java 17+
* PostgreSQL 13+
* Maven 3.6+
* HuggingFace & OpenRouter API keys

### Quick Setup

```bash
git clone https://github.com/username/astrogenesis.git
cd astrogenesis
mvn clean install
mvn spring-boot:run
```

**Access:**

* Home: [http://localhost:8080](http://localhost:8080)
* AI Chat: `/ai`
* Publications: `/publications`

---

## 11. PERFORMANCE & OPTIMIZATION

* In-memory cosine similarity (planned pgvector)
* Lazy JPA fetch strategies
* Rate-limited API calls
* Planned caching with Redis
* Potential Elasticsearch hybrid search

---

## 12. SECURITY

* API keys via environment variables
* SQL injection-safe JPA
* HTTPS-ready
* Planned Spring Security authentication

---

## 📊 PROJECT STATISTICS

| Metric            | Value    |
| ----------------- | -------- |
| Java Classes      | 20+      |
| Total LOC         | ~3500    |
| Publications      | 608      |
| OSDR Datasets     | 500+     |
| Vector Dimensions | 850,000+ |

---

## 🧭 FUTURE DEVELOPMENT

**Short-Term:**

* Chat history dashboard
* Publication filters
* User authentication

**Mid-Term:**

* `pgvector` integration
* Redis caching
* WebSocket streaming chat

**Long-Term:**

* Knowledge graph (Neo4j)
* Hypothesis generation
* Mobile app version

---

## 🙏 ACKNOWLEDGMENTS

* **NASA OSDR** for open datasets
* **HuggingFace** for embedding models
* **OpenRouter** for LLM API access
* **Spring & PostgreSQL** communities

---

**Astrogenesis – NASA BioScience AI Dashboard**
*Exploring Science Through Artificial Intelligence*
Version 1.0 | © 2025

---

Would you like me to also generate a **GitHub-optimized README file** (with badges, installation commands, and usage examples) from this documentation? It’ll make your repository look more professional.
