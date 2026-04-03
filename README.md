# RPC Gateway

## API

### Public API

- `GET /`
  - 잔액 조회용 간단한 웹 페이지를 반환합니다.
- `GET /api/balances/{address}`
  - Ethereum Sepolia 지갑 주소의 ETH 잔액을 조회합니다.

### Internal API

- `GET /internal/cache/balance`
  - 인메모리 balance 캐시 상태를 확인하는 내부 확인용 API입니다.
- `GET /internal/rate-limit/buckets`
  - IP별 rate limit bucket 상태를 확인하는 내부 확인용 API입니다.

`/internal/**` 경로는 운영 로그 확인, 디버깅, 동작 검증을 위한 내부용 API입니다. 외부 공개 API로 사용하는 것을 전제로 하지 않았습니다.

## 프로젝트 소개

이 프로젝트는 Ethereum Sepolia 환경에서 지갑 잔액을 조회하는 간단한 웹 서비스입니다.

과제의 핵심은 단순히 `eth_getBalance`를 호출하는 것이 아니라, 클라이언트가 외부 RPC 노드를 직접 호출하지 않도록 백엔드가 중간 게이트웨이 역할을 하면서 RPC 자원 남용을 줄이는 것입니다.

프로젝트 이름을 `rpc-gateway`로 지은 이유도 여기에 있습니다. 이 서버는 사용자와 Ethereum RPC 사이의 중간 관문으로 동작하며, 요청을 그대로 전달하는 대신 검증, 캐시, rate limit, timeout, 예외 처리 정책을 적용합니다.

## 배경과 문제 정의

일반적인 client-side wallet 구조에서는 브라우저가 RPC endpoint를 직접 호출하므로, 네트워크 탭이나 번들 코드에서 RPC URL이 노출될 수 있습니다.

RPC URL을 서버 환경변수에 숨기면 endpoint 자체는 노출되지 않지만, 그것만으로는 충분하지 않습니다. 공격자는 이제 공개 RPC가 아니라 `우리 서버 API`를 반복 호출할 수 있고, 결국 서버가 계속 외부 RPC provider를 호출하게 되므로 최종적으로 RPC 자원은 계속 소모됩니다.

따라서 이 과제는 다음 질문에 답하는 문제라고 판단했습니다.

- 클라이언트가 RPC URL을 직접 알지 못하게 할 수 있는가
- 잘못된 요청을 외부 RPC 호출 전에 차단할 수 있는가
- 같은 요청 반복을 줄일 수 있는가
- 대량 요청 자체를 제한할 수 있는가
- 외부 RPC 장애가 우리 서버 전체로 번지지 않게 할 수 있는가

## 설계 요약

전체 흐름은 아래와 같습니다.

1. 사용자가 웹 페이지 또는 API로 지갑 주소를 보냅니다.
2. 서버는 먼저 주소 형식을 검증합니다.
3. 같은 주소 조회 결과가 캐시에 있으면 외부 RPC를 다시 호출하지 않습니다.
4. 캐시에 없으면 rate limit 통과 여부를 확인한 뒤 Sepolia RPC provider를 호출합니다.
5. 외부 RPC 응답을 ETH 잔액으로 변환해 반환합니다.
6. 외부 RPC가 느리거나 실패하면 timeout 및 예외 처리 정책으로 응답을 정리합니다.

## 적용한 방어 전략

### 1. RPC URL 비노출

RPC URL은 코드에 하드코딩하지 않고 `.env` 기반 환경변수로 관리합니다.

- `ETHEREUM_SEPOLIA_RPC_URL`

이 방식으로 클라이언트는 Sepolia provider endpoint를 직접 알 수 없고, 오직 우리 서버 API만 호출할 수 있습니다.

### 2. 입력값 검증

Ethereum 주소 형식은 서버에서 먼저 검증합니다.

- 형식: `0x` + 40자리 hex
- 잘못된 주소는 `400 Bad Request`
- 목적: 불필요한 외부 RPC 호출 차단

즉 비정상 요청은 provider까지 보내지 않고 서버 입구에서 바로 막습니다.

### 3. 인메모리 캐시

같은 주소에 대한 잔액 조회 결과는 Spring Cache + Caffeine 기반 인메모리 캐시에 저장합니다.

- 기본 TTL: `30초`
- 목적: 새로고침 연타, 동일 주소 반복 조회 완화

캐시만으로는 충분하지 않습니다. 공격자가 주소를 계속 바꿔가며 요청하면 캐시를 우회할 수 있기 때문입니다.

### 4. IP 기반 Rate Limiting

`/api/balances/**` 요청에는 IP 기준 rate limit을 적용했습니다.

- 기본 설정: `1분에 30회`
- 초과 시: `429 Too Many Requests`
- 구현 위치: Spring `OncePerRequestFilter`

캐시가 같은 요청 반복을 줄이는 장치라면, rate limit은 요청 폭주 자체를 막는 장치입니다.

### 5. Timeout / 외부 RPC 예외 처리

외부 RPC provider는 우리 시스템 밖에 있으므로 항상 정상이라고 가정할 수 없습니다. 그래서 timeout과 예외를 분리해서 처리했습니다.

- connect timeout: `20초`
- read timeout: `30초`

예외 처리 정책은 다음과 같습니다.

- 잘못된 주소: `400`
- rate limit 초과: `429`
- 외부 RPC 일반 실패: `502`
- 외부 RPC timeout: `504`
- 서버 설정 오류: `500`

이렇게 분리한 이유는 사용자에게 일관된 응답을 주면서도, 장애 원인을 구분해 설명할 수 있게 하기 위해서입니다.

## 왜 이렇게 설계했는가

이 과제에서 가장 중요한 포인트는 `최대한 RPC에 요청이 가지 않도록 처리하는 것`이라고 판단했습니다.

그래서 구현도 다음 우선순위로 진행했습니다.

1. 서버가 RPC URL을 대신 관리해서 직접 노출을 막는다.
2. 잘못된 입력은 RPC 호출 전에 차단한다.
3. 같은 요청은 캐시로 흡수한다.
4. 많은 요청 자체는 rate limit으로 차단한다.
5. 외부 장애는 timeout과 예외 처리로 격리한다.

즉 단순 프록시 서버가 아니라, 외부 RPC 자원을 안전하게 감싸는 게이트웨이 형태로 접근했습니다.

## 실행 방법

### 1. 환경변수 설정

프로젝트 루트의 `.env` 파일에 아래 값을 설정합니다.

```dotenv
ETHEREUM_SEPOLIA_RPC_URL=[RPC_URL 값을 채워주세욘]
```

선택적으로 아래 값도 조정할 수 있습니다.

```dotenv
BALANCE_CACHE_TTL_SECONDS=30
RATE_LIMIT_CAPACITY=30
RATE_LIMIT_REFILL_TOKENS=30
RATE_LIMIT_REFILL_MINUTES=1
ETHEREUM_RPC_CONNECT_TIMEOUT_MS=20000
ETHEREUM_RPC_READ_TIMEOUT_MS=30000
```

### 2. 실행

```bash
./gradlew bootRun
```

### 3. 테스트

```bash
./gradlew test
```

## API 예시

### 잔액 조회

```bash
curl http://localhost:8080/api/balances/0x742d35Cc6634C0532925a3b844Bc454e4438f44e
```

응답 예시:

```json
{
  "address": "0x742d35Cc6634C0532925a3b844Bc454e4438f44e",
  "balanceEth": "1"
}
```

### 내부 캐시 확인

```bash
curl http://localhost:8080/internal/cache/balance
```

### Rate limit bucket 확인

```bash
curl http://localhost:8080/internal/rate-limit/buckets
```

## 한계와 개선 방향

현재 구현은 과제 제출용 단일 인스턴스 환경을 기준으로 했습니다. 따라서 아래 한계가 있습니다.

- 캐시와 rate limit bucket이 모두 인메모리 기반이라 서버가 여러 대면 공유되지 않습니다.
- 서버 재시작 시 캐시와 bucket 상태가 사라집니다.
- IP 기반 rate limit은 프록시 환경이나 우회 시나리오에서 완전한 방어가 아닙니다.
- `/internal/**` API는 내부 확인용이므로 실제 운영 환경에서는 인증 또는 비활성화가 필요합니다.

실제 운영 환경으로 확장한다면 다음 방향이 적절합니다.

- Redis 기반 분산 캐시 및 분산 rate limit
- Prometheus / Grafana 기반 모니터링
- `/internal/**` 보호를 위한 인증/인가
- 요청 로그 및 차단 로그 고도화

## 기술 스택

- Java 17
- Spring Boot 3.5
- Thymeleaf
- Spring Cache
- Caffeine
- Bucket4j
- Ethereum Sepolia JSON-RPC
