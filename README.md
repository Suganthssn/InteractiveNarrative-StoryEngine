
#  Branching Narrative Engine API

A high-performance backend simulation engine built with **Java and Spring Boot**, designed to power complex "Choose Your Own Adventure" and time-loop games. 

By modeling narrative structures as **Directed Weighted Graphs**, this engine proactively validates story integrity and dynamically calculates optimal gameplay routes.

##  Core Features

* **Fail-Fast Graph Validation (Kosaraju’s Algorithm):** Automatically scans the entire narrative structure on server startup. If an inescapable time-loop (a closed loop with no exit) is detected, the engine intentionally crashes to prevent runtime softlocks.
* **Dynamic Pathfinding (Dijkstra’s Algorithm):** Evaluates the narrative weight of player choices to calculate the absolute shortest "Canon Path" to every reachable ending in $O(V \log V + E)$ time.
* **Defensive Architecture:** Built with robust exception handling to safely parse malformed JSON data, missing edge weights, and disconnected nodes.

##  Tech Stack

* **Language:** Java 17+
* **Framework:** Spring Boot
* **Algorithms:** Graph Theory (Kosaraju's, Dijkstra's, DFS)
* **Testing:** JUnit 5, Mockito
* **Build Tool:** Maven

---

##  How It Works (Architecture)

Instead of using a standard SQL database, this engine uses **Graph Data Structures** to make the story work. 

### 1. The Story as a Graph
The entire game is loaded into memory as a math problem:
* **Nodes (Vertices):** Each part of the story or ending is a node.
* **Choices (Edges):** The options the player can pick are the paths connecting the nodes. 
* **Weights:** Each choice has a number attached to it (a weight) so the system knows the difference between the main story and side quests.

### 2. Clean Code Organization
The logic is completely separated into two main parts to follow strict backend design principles:
* **The Data Layer:** Only handles loading the story files and configuration.
* **The Algorithm Layer:** Only handles executing the complex graph mathematics.

### 3. Fail-Fast Security
To prevent a player from getting stuck in an infinite story loop with no way out, the engine checks itself the exact second it boots up:
* It runs **Kosaraju’s Algorithm** to scan the whole story.
* If it finds a closed loop with no exit (a softlock), the server intentionally crashes and throws an error *before* the game even starts. This guarantees the story is always 100% playable in production.

---

##  Getting Started

### Prerequisites
* Java 17 or higher installed on your machine.
* Maven installed.

### Running Locally
1. Clone the repository:
   ```bash
   git clone [https://github.com/Suganthssn/InteractiveNarrative-StoryEngine.git](https://github.com/Suganthssn/InteractiveNarrative-StoryEngine.git)
   cd InteractiveNarrative-StoryEngine

```

2. Build the project using Maven:
mvn clean package

```


3. Run the compiled executable JAR file:
```bash
java -jar target/story-engine-0.0.1-SNAPSHOT.jar

```


4. The API will now be running on `http://localhost:8080`.

---

## 🧪 Testing Strategy

This engine prioritizes critical business logic. The test suite utilizes **JUnit 5** and **Mockito** to verify the algorithms without requiring a live database connection.

To run the test suite:

```bash
mvn test

```

**Key Test Coverage Includes:**

1. Verifying Dijkstra's Algorithm bypasses expensive routes for lower-cost alternatives.
2. Ensuring Kosaraju's Algorithm correctly identifies and throws an `IllegalStateException` when a fatal time-loop is injected.

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

