# Mimari Komite Agent

15 yazılım mimarisi uzmanından oluşan RAG tabanlı AI danışma sistemi.

## Uzmanlar
Eric Evans, Vaughn Vernon, Vlad Khononov, Mathias Verraes, Alberto Brandolini, Nick Tune, Sam Newman, Chris Richardson, Martin Fowler, Michael Feathers, Martin Kleppmann, Pramod Sadalage, Ford & Richards, Kent Beck, Uncle Bob

## Özellikler
- 💬 **Danışma** — Mimari sorularınızı 15 uzmana sorun, sentez + bireysel cevaplar alın
- 🖼️ **Diagram Analizi** — Mevcut diyagramı yükle, As-Is doğrula, To-Be Mermaid diagram üret
- 📚 **Bilgi Tabanı** — Uzmanlara PDF veya makale yükle

## Teknolojiler
- **Backend**: Spring Boot 3.4, Java 21, Spring AI 1.0, ChromaDB
- **Frontend**: React + Vite, Tailwind CSS
- **AI**: Anthropic Claude (claude-haiku-4-5)
- **Vector DB**: ChromaDB 0.5.23

## Kurulum

### Gereksinimler
- Java 21+
- Node.js 18+
- Docker

### 1. ChromaDB başlat
```bash
docker run -d -p 8000:8000 \
  -v $(pwd)/chromadb-data:/chroma/chroma \
  chromadb/chroma:0.5.23
```

### 2. Environment ayarla
```bash
cp .env.example .env
# .env dosyasına Anthropic API key ekle
```

### 3. Backend başlat
```bash
export $(cat .env) && mvn spring-boot:run
```

### 4. Frontend başlat
```bash
cd frontend && npm install && npm run dev
```

### 5. Kitapları yükle
```bash
./load-books.sh
```

## Kullanım
- Backend: http://localhost:8080
- Frontend: http://localhost:5173

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
