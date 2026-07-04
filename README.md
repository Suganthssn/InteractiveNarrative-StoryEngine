
# 📖 Branching Narrative Engine API

A high-performance backend simulation engine built with **Java and Spring Boot**, designed to power complex "Choose Your Own Adventure" and time-loop games. 

By modeling narrative structures as **Directed Weighted Graphs**, this engine proactively validates story integrity and dynamically calculates optimal gameplay routes.

## ✨ Core Features

* **Fail-Fast Graph Validation (Kosaraju’s Algorithm):** Automatically scans the entire narrative structure on server startup. If an inescapable time-loop (a Strongly Connected Component with no exit) is detected, the engine intentionally crashes to prevent runtime softlocks.
* **Dynamic Pathfinding (Dijkstra’s Algorithm):** Evaluates the narrative weight of player choices (e.g., main story vs. side quests) to calculate the absolute shortest "Canon Path" to every reachable ending in $O(V \log V + E)$ time.
* **Defensive Architecture:** Built with robust exception handling to safely parse malformed data, missing edge weights, and disconnected nodes without throwing `NullPointerExceptions`.

## 🛠️ Tech Stack

* **Language:** Java 17+
* **Framework:** Spring Boot
* **Algorithms:** Graph Theory (Kosaraju's, Dijkstra's, DFS)
* **Testing:** JUnit 5, Mockito (Unit Testing, Mocking)
* **Build Tool:** Maven

---

## 🏗️ Architecture & Data Modeling

> **Note to Interviewers:** The narrative is stored dynamically as a Graph, where each plot point is a **Vertex (Node)** and each player choice is a **Directed Edge** containing a narrative weight.

*(Write your architecture explanation here! Explain how you used `story.json` as your in-memory database, why you separated the `StoryService` from the `StoryAlgorithmService`, and how you used Spring's `@EventListener` to decouple the validation logic from the server startup!)*

---

## 🚀 Getting Started

### Prerequisites
* Java 17 or higher installed on your machine.
* Maven installed.

### Running Locally
1. Clone the repository:
   ```bash
   git clone [https://github.com/yourusername/branching-narrative-engine.git](https://github.com/yourusername/branching-narrative-engine.git)
   cd branching-narrative-engine

```

2. Build the project using Maven:
```bash
mvn clean package

```


3. Run the compiled executable JAR file:
```bash
java -jar target/story-engine-0.0.1-SNAPSHOT.jar

```


4. The API will now be running on `http://localhost:8080`.

---

## 🧪 Testing Strategy

This engine prioritizes critical business logic and defensive programming. The test suite utilizes **JUnit 5** and **Mockito** to achieve high coverage without requiring a live database connection.

To run the test suite:

```bash
mvn test

```

**Key Test Coverage Includes:**

1. Verifying Dijkstra's Algorithm bypasses expensive routes for lower-cost alternatives.
2. Ensuring Kosaraju's Algorithm correctly identifies and throws an `IllegalStateException` when a fatal time-loop is injected.
3. Edge-case handling for missing choice arrays and missing edge weights.

---

## 📡 API Usage

**Calculate Canon Paths**
Calculates the optimal route to all available endings based on choice weights.

* **URL:** `/api/engine/canon-paths`
* **Method:** `GET`
* **Success Response:**
* **Code:** `200 OK`
* **Content:**



```json
{
    "ending_escape": [
        "start",
        "bridge",
        "ending_escape"
    ],
    "ending_sacrifice": [
        "start",
        "reactor_room",
        "bridge",
        "ending_sacrifice"
    ]
}

```

```

Make sure to replace the `https://github.com/yourusername/branching-narrative-engine.git` URL with your actual repository link before pushing this to your main branch. Your project is now perfectly documented and ready to be shown off.

```
