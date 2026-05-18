#!/bin/bash
BASE="/app/books"
API="http://localhost:8080/api/knowledge/ingest"

upload_book() {
  local expert="$1"
  local file="$2"
  local source="$3"
  echo "Loading: $source for $expert..."
  RESULT=$(curl -s -X POST "$API" \
       -H "Content-Type: application/json" \
       -d "{\"expert\": \"$expert\", \"filePath\": \"$BASE/$file\", \"sourceName\": \"$source\"}")
  echo "$RESULT"
  echo "✅ Done: $source"
  echo "---"
}

upload_book "eric-evans" "Domain Driven Design Tackling Complexity in the Heart of Software - Eric Evans.pdf" "DDD Blue Book"
upload_book "eric-evans" "Domain-Driven Design Reference{Eric Evans}(2014, Eric Evans){113368680} libgen.li.pdf" "DDD Reference"
upload_book "vaughn-vernon" "Vaughn Vernon - Vaughn Vernon - Implementing Domain-Driven Design (2013, Addison-Wesley Professional) - libgen.li.pdf" "IDDD"
upload_book "vaughn-vernon" "Vaughn Vernon - Domain-Driven Design Distilled (2016, Addison-Wesley Professional) - libgen.li.pdf" "DDD Distilled"
upload_book "vaughn-vernon" "[Addison-Wesley Signature Series (Vernon)] Vaughn Vernon, Jaskula Tomasz - Strategic Monoliths and Microservices_ Driving Innovation Using Purposeful Architecture (2022, Addison-Wesley Publishing) - libgen.li.pdf" "Strategic Monoliths"
upload_book "vlad-khononov" "Vlad Khononov - Learning Domain-Driven Design_ Aligning Software Architecture and Business Strategy (2021, O'Reilly Media) - libgen.li.pdf" "Learning DDD"
upload_book "martin-fowler" "Refactoring_ Improving the Design of Existing Code.pdf" "Refactoring"
upload_book "martin-fowler" "[The Addison-Wesley signature series] Martin Fowler - Patterns of Enterprise Application Architecture (2015_2002, Addison-Wesley Professional) - libgen.li.pdf" "PoEAA"
upload_book "alberto-brandolini" "Alberto Brandolini - Introducing EventStorming - libgen.li.pdf" "EventStorming"
upload_book "nick-tune" "Nick Tune, Jean-Georges Perrin - Architecture Modernization_ Socio-technical alignment of software, strategy, and structure (2024, MANNING Publications) - libgen.li.pdf" "Architecture Modernization"
upload_book "sam-newman" "Sam Newman - Monolith to Microservices_ Evolutionary Patterns to Transform Your Monolith (2019, O'Reilly Media) - libgen.li.pdf" "Monolith to Microservices"
upload_book "sam-newman" "Sam Newman - Building Microservices_ Designing Fine-Grained Systems (2021, O'Reilly Media) - libgen.li.pdf" "Building Microservices"
upload_book "chris-richardson" "Chris Richardson - Microservices Patterns_ With examples in Java (2018, MANNING Publications) - libgen.li.pdf" "Microservices Patterns"
upload_book "martin-kleppmann" "Martin Kleppmann - Designing Data-Intensive Applications_ The Big Ideas Behind Reliable, Scalable, and Maintainable Systems-O'Reilly Media (2017).pdf" "DDIA"
upload_book "pramod-sadalage" "Scott J Ambler, Pramod J. Sadalage - Refactoring Databases_ Evolutionary Database Design (Addison-Wesley Signature Series (Fowler)) (2006, Addison Wesley) - libgen.li.pdf" "Refactoring Databases"
upload_book "neal-ford-mark-richards" "Neal Ford, Mark Richards, Pramod Sadalage, Zhamak Dehghani - Software Architecture_ The Hard Parts_ Modern Trade-Off Analyses for Distributed Architectures (2021, O'Reilly Media) - libgen.li.pdf" "Architecture Hard Parts"
upload_book "neal-ford-mark-richards" "Fundamentals of Software Architecture_ A Modern Engineering Approach{Mark Richards_ Neal Ford}(2025 March 25, O&_039_Reilly Media){111205155} libgen.li.pdf" "Fundamentals of Software Architecture"
upload_book "kent-beck" "Kent Beck - Test-Driven Development By Example (2002, Addison Wesley) - libgen.li.pdf" "TDD By Example"
upload_book "kent-beck" "Kent Beck - Tidy First__ A Personal Exercise in Empirical Software Design (2023, O'Reilly Media) - libgen.li.pdf" "Tidy First"
upload_book "kent-beck" "Extreme Programming Explained (Addison-Wesley) - libgen.li.pdf" "XP Explained"
upload_book "uncle-bob" "Clean Architecture_ A Craftsman's Guide to Software Structure and Design.pdf" "Clean Architecture"
upload_book "uncle-bob" "Clean Code_ A Handbook of Agile Software Craftmanship.pdf" "Clean Code"
upload_book "uncle-bob" "Robert C. Martin - Clean Craftsmanship_ Disciplines, Standards, and Ethics (Robert C. Martin Series) (2021, Addison-Wesley Professional) - libgen.li.pdf" "Clean Craftsmanship"
upload_book "michael-feathers" "Working effectively with legacy code.pdf" "Working with Legacy Code"

echo "Finished processing all books!"
