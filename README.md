# Mimari Komite Agent

15 yazılım mimarisi uzmanından oluşan RAG tabanlı AI danışma sistemi.

## Uzmanlar
Eric Evans, Vaughn Vernon, Vlad Khononov, Mathias Verraes, Alberto Brandolini, Nick Tune, Sam Newman, Chris Richardson, Martin Fowler, Michael Feathers, Martin Kleppmann, Pramod Sadalage, Ford & Richards, Kent Beck, Uncle Bob

## Özellikler
- 💬 **Danışma** — Mimari sorularınızı 15 uzmana sorun, sentez + bireysel cevaplar alın
- 🖼️ **Diagram Analizi** — Diyagramı yükle, As-Is doğrula, To-Be Mermaid diagram üret
- 📚 **Bilgi Tabanı** — Uzmanlara PDF veya makale yükle

## Teknolojiler
- **Backend**: Spring Boot 3.4, Java 21, Spring AI 1.0, ChromaDB
- **Frontend**: React + Vite, Tailwind CSS
- **AI**: Anthropic Claude (claude-haiku-4-5)
- **Vector DB**: ChromaDB 0.5.23

## Kurulum (Docker ile)

### Gereksinimler
- Docker + Docker Compose
- Anthropic API Key → https://console.anthropic.com

### 1. Kurulum

```bash
git clone https://github.com/kagantantan/MimariKomiteAgent.git
cd MimariKomiteAgent
cp .env.example .env
# .env dosyasına ANTHROPIC_API_KEY ekleyin
```

### 2. Kitapları hazırla

`books/` klasörü oluşturun ve aşağıdaki kitapları PDF olarak ekleyin:

| Uzman | Kitap |
|-------|-------|
| Eric Evans | Domain-Driven Design (Blue Book) |
| Vaughn Vernon | Implementing DDD, DDD Distilled, Strategic Monoliths |
| Vlad Khononov | Learning Domain-Driven Design |
| Martin Fowler | Refactoring, PoEAA |
| Alberto Brandolini | Introducing EventStorming |
| Nick Tune | Architecture Modernization |
| Sam Newman | Building Microservices, Monolith to Microservices |
| Chris Richardson | Microservices Patterns |
| Martin Kleppmann | Designing Data-Intensive Applications |
| Pramod Sadalage | Refactoring Databases |
| Ford & Richards | Software Architecture: The Hard Parts, Fundamentals |
| Kent Beck | TDD By Example, Tidy First, XP Explained |
| Uncle Bob | Clean Architecture, Clean Code, Clean Craftsmanship |
| Michael Feathers | Working Effectively with Legacy Code |

> ⚠️ `load-books.sh` içindeki dosya adlarını kendi PDF dosya adlarınıza göre güncelleyin.

### 3. Başlat

```bash
docker-compose up -d
sleep 30
docker cp books/ mimarikomiteagent-backend-1:/app/books/
./load-books.sh
```

### 4. Aç

- Frontend: http://localhost:5173
- Backend API: http://localhost:8080

## API

| Endpoint | Method | Açıklama |
|----------|--------|----------|
| `/api/consult` | POST | Mimari soru sor |
| `/api/diagram/analyze` | POST | Diagram analiz et |
| `/api/diagram/validate` | POST | As-Is doğrula |
| `/api/diagram/generate` | POST | To-Be üret |
| `/api/knowledge/ingest-pdf` | POST | PDF yükle |
| `/api/knowledge/ingest-text` | POST | Metin yükle |
| `/api/knowledge/status` | GET | Uzman listesi |

## Notlar
- `books/` klasörü telif hakkı nedeniyle repoya dahil edilmemiştir
- Kitaplar ChromaDB'ye yerel embedding modeli ile yüklenir (Anthropic API maliyeti yoktur)
