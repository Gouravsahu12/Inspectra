# 🔍 INSPECTRA

### AI-Powered Smart Inspection & Compliance Platform

<p align="center">
  <b>Transforming product inspections from manual verification into intelligent, evidence-driven compliance.</b>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/SIH%202026-Project-blue?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Platform-Android-green?style=for-the-badge&logo=android" />
  <img src="https://img.shields.io/badge/AI-Powered-purple?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Backend-Supabase-3ECF8E?style=for-the-badge&logo=supabase&logoColor=white" />
  <img src="https://img.shields.io/badge/Status-In%20Development-orange?style=for-the-badge" />
</p>

---

## 🚀 What is INSPECTRA?

**INSPECTRA** is an AI-powered mobile inspection and compliance platform designed to help field inspectors perform faster, smarter, and more reliable product inspections.

Instead of relying entirely on manual inspection, paperwork, and disconnected tools, INSPECTRA combines:

> 📱 Mobile Inspection + 📷 Computer Vision + 🤖 AI/OCR + ⚖️ Compliance Rules + 📊 Analytics

into a single platform.

An inspector can capture product/package images using a smartphone, extract relevant information using AI, automatically evaluate applicable compliance requirements, identify potential violations, and generate an evidence-backed inspection record.

---

## 🎯 The Problem

Traditional field inspections can involve:

- Manual data entry
- Paper-based records
- Repetitive verification
- Reading information from product packaging
- Manual comparison against regulations
- Difficulty maintaining consistent inspection records
- Limited access to historical inspection data
- Time-consuming report preparation
- Human errors during data collection and verification

This makes inspections slower, harder to standardize, and difficult to scale.

### INSPECTRA aims to change that.

---

# 💡 Our Solution

INSPECTRA provides a complete digital workflow for field inspections.

```text
┌─────────────────────┐
│   👮 Inspector      │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│  📋 New Inspection  │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│   📷 Capture Images │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│    🤖 AI + OCR      │
│ Information Extract │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ ⚖️ Compliance Engine│
│   Rule Evaluation   │
└──────────┬──────────┘
           │
      ┌────┴────┐
      ▼         ▼
┌──────────┐ ┌──────────┐
│Compliant │ │Violation │
└────┬─────┘ └────┬─────┘
     │             │
     └──────┬──────┘
            ▼
┌─────────────────────┐
│ 📊 Compliance Score │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ 📄 Inspection Report│
└─────────────────────┘


✨ Key Features
🔐 Secure Authentication
Email/password authentication
Secure session management
Role-based user profiles
Protected user data
Row Level Security
📋 Digital Inspections

Create and manage inspections digitally with:

Automatically generated inspection reference numbers
Product information
Brand information
Store/facility details
Inspection status
Inspector notes
Compliance score
📷 Real-Time Camera Integration

Capture product/package images directly from the Android device.

Supported inspection views include:

Front
Back
Side
Other

Images are securely associated with the corresponding inspection.

🤖 AI-Powered Information Extraction

INSPECTRA is designed to analyze product/package images and extract important information such as:

Product name
Brand
MRP
Net quantity
Manufacturer
Consumer care information
Country of origin
Other relevant declarations
⚖️ Automated Compliance Checking

Extracted product information can be evaluated against predefined compliance rules.

The system can identify:

Missing declarations
Incorrect information
Potential violations
Severity levels
Compliance status
📊 Compliance Scoring

Each inspection can receive a structured compliance score based on applicable checks.

📄 Digital Reports

Generate structured inspection reports containing:

Inspection details
Captured evidence
Extracted information
Detected violations
Compliance score
Inspector information
📈 Inspection History & Analytics

Inspectors and authorized users can access historical inspection information for monitoring and analysis.

🧠 AI Workflow

The AI pipeline is designed around an evidence-first approach.
Product Image
      │
      ▼
 Image Processing
      │
      ▼
 OCR / Vision Analysis
      │
      ▼
Structured Information
      │
      ▼
Validation
      │
      ▼
Compliance Rules
      │
      ▼
Violations + Evidence
      │
      ▼
Compliance Score

🏗️ Architecture

INSPECTRA follows a mobile-first cloud architecture.

                         ┌──────────────────┐
                         │   Android App    │
                         │ Kotlin + Compose │
                         └────────┬─────────┘
                                  │
                                  │ HTTPS
                                  ▼
                    ┌──────────────────────────┐
                    │        Supabase          │
                    │                          │
                    │ ┌────────┐ ┌──────────┐ │
                    │ │  Auth  │ │ Storage  │ │
                    │ └────────┘ └──────────┘ │
                    │                          │
                    │ ┌──────────────────────┐ │
                    │ │ PostgreSQL + RLS     │ │
                    │ └──────────────────────┘ │
                    │                          │
                    │ ┌──────────────────────┐ │
                    │ │   Edge Functions     │ │
                    │ └──────────────────────┘ │
                    └────────────┬─────────────┘
                                 │
                     ┌───────────┴───────────┐
                     │                       │
                     ▼                       ▼
              ┌──────────────┐       ┌──────────────┐
              │   Gemini AI  │       │ Compliance   │
              │  Vision/OCR  │       │    Engine    │
              └──────────────┘       └──────────────┘
