# AlgoArena 

Competitive algorithm-solving game with a **visual drag-and-drop logic board**.  
Players face off in short challenges (sorting, pathfinding, resource allocation) where **speed + efficiency** decide the winner.  

---

##  Tech Stack

- **Frontend:** JavaFX (UI, drag-and-drop, animations)  
- **Backend:** Spring Boot (REST + WebSockets, API for challenges and scoring)  
- **Databases:**  
  - PostgreSQL → player accounts, scores, leaderboards  
  - MongoDB → challenge data & replay logs  

---

##  Repository Structure

```plaintext
algoarena/
│── README.md                  # Project documentation
│── .gitignore                 # Ignore build files & IDE configs
│
├── backend/                   # Spring Boot backend (API + DB logic)
│   ├── build.gradle / pom.xml
│   ├── src/
│   │   ├── main/java/com/algoarena/server/
│   │   ├── main/resources/application.yml
│   │   └── test/java/
│   └── docs/                  # API docs, Postman collections
│
├── frontend/                  # JavaFX frontend (game client)
│   ├── build.gradle / pom.xml
│   ├── src/
│   │   ├── main/java/com/algoarena/client/
│   │   ├── main/resources/fxml/
│   │   ├── main/resources/css/
│   │   └── test/java/
│
└── docs/                      # Shared UML diagrams, design notes
