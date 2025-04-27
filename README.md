<h1 align="center">📚 Social Blog System</h1>
<p align="center">
  Modern <b>Java</b> like-system demo – from CRUD to high-concurrency & observability  
  Spring Boot 3.4 • MyBatis-Plus • TiDB • Redis • Pulsar • Docker Compose • Grafana
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.4.x-brightgreen?logo=spring" />
  <img src="https://img.shields.io/badge/JDK-21-blue.svg?logo=java" />
  <img src="https://img.shields.io/badge/License-MIT-yellow.svg" />
</p>

---

## 1. Overview

A fully-featured **blog + like system** built with a production-grade stack.  
We start with clean CRUD, then add caching, message queues, batch flush, and finally monitoring / tracing – giving you a complete dev-to-ops loop.

### Key Features

| Category | Details |
| -------- | ------- |
| Basic    | • Blog list / detail / publish<br>• 🔥 Like & unlike with live counter<br>• Mock login |
| High concurrency | • Redis Lua + Bitmap deduplication<br>• Pulsar async queue, batch flush to TiDB<br>• Caffeine L1 cache + Redis L2 cache |
| Observability | • Prometheus metrics<br>• Grafana dashboards (import JSON)<br>• SkyWalking distributed tracing |
| DevOps | • All deps in one `docker-compose` file<br>• Production-ready Dockerfiles |

### Tech Stack

- **Backend**  Spring Boot 3.4, Java 21, MyBatis-Plus, TiDB  
- **Cache / MQ**  Redis 7, Caffeine, Apache Pulsar 4  
- **Observability**  Prometheus, Grafana, SkyWalking  
- **Infra**  Docker, Docker Compose, Nginx

---

## 2. Architecture

```mermaid
flowchart LR
  %% Client
  subgraph Client
    A[SPA] --Axios--> B(API Gateway)
  end
  %% Backend
  subgraph Backend[Spring Boot]
    B --HTTP--> C(Thumb Controller)
    C -->|Lua| R[Redis]
    C -->|Publish| MQ[Pulsar Topic]
    Worker[Sync Worker] --Batch insert--> DB[(TiDB)]
    MQ --Sub--> Worker
  end
  B <--Prometheus--> P[(Metrics)]
