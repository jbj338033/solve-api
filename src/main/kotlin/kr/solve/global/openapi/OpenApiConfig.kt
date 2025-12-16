package kr.solve.global.openapi

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun openAPI(): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title("Solve API")
                    .version("1.0.0")
                    .description(
                        """
                        코딩 테스트 플랫폼 API

                        ## WebSocket Endpoints

                        ### Interactive Code Execution

                        **URL:** `ws://localhost:8080/ws/executions`

                        실시간 코드 실행을 위한 WebSocket 엔드포인트입니다. 터미널처럼 stdin/stdout을 실시간으로 주고받을 수 있습니다.

                        #### Message Types

                        **Client → Server:**

                        | Type | Data | Description |
                        |------|------|-------------|
                        | `INIT` | `{problemId, language, code}` | 실행 초기화 |
                        | `STDIN` | `string` | 표준 입력 전송 |
                        | `KILL` | - | 실행 중단 |

                        **Server → Client:**

                        | Type | Data | Description |
                        |------|------|-------------|
                        | `STDOUT` | `string` | 표준 출력 |
                        | `STDERR` | `string` | 표준 에러 |
                        | `COMPLETE` | `{exitCode, time, memory}` | 실행 완료 |
                        | `ERROR` | `string` | 에러 메시지 |

                        #### Example

                        ```javascript
                        const ws = new WebSocket('ws://localhost:8080/ws/executions');

                        // 초기화
                        ws.send(JSON.stringify({
                          type: 'INIT',
                          data: {
                            problemId: 1,
                            language: 'PYTHON',
                            code: 'name = input()\\nprint(f"Hello, {name}!")'
                          }
                        }));

                        // 입력 전송
                        ws.send(JSON.stringify({
                          type: 'STDIN',
                          data: 'World\\n'
                        }));

                        // 메시지 수신
                        ws.onmessage = (event) => {
                          const msg = JSON.parse(event.data);
                          switch (msg.type) {
                            case 'STDOUT':
                              console.log('Output:', msg.data);
                              break;
                            case 'COMPLETE':
                              console.log('Done:', msg.data);
                              break;
                          }
                        };
                        ```

                        #### Supported Languages

                        `C`, `CPP`, `JAVA`, `PYTHON`, `JAVASCRIPT`, `KOTLIN`, `GO`, `RUST`

                        ---

                        ### Code Judge (Individual Submission)

                        **URL:** `ws://localhost:8080/ws/judge`

                        개별 코드 제출 및 채점 결과를 실시간으로 수신합니다. 문제 풀이 페이지에서 사용합니다.

                        #### Message Types

                        **Client → Server:**

                        | Type | Data | Description |
                        |------|------|-------------|
                        | `INIT` | `{token, problemId, contestId?, language, code}` | 제출 및 채점 시작 |

                        **Server → Client:**

                        | Type | Data | Description |
                        |------|------|-------------|
                        | `CREATED` | `{submissionId}` | 제출 생성 완료 |
                        | `PROGRESS` | `{testcaseId, result, time, memory, score, progress}` | 테스트케이스 채점 진행 |
                        | `COMPLETE` | `{result, score, time, memory, error?}` | 채점 완료 |
                        | `ERROR` | `string` | 에러 메시지 |

                        #### Example

                        ```javascript
                        const ws = new WebSocket('ws://localhost:8080/ws/judge');

                        ws.onopen = () => {
                          ws.send(JSON.stringify({
                            type: 'INIT',
                            data: {
                              token: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
                              problemId: 1,
                              language: 'PYTHON',
                              code: 'print(sum(map(int, input().split())))'
                            }
                          }));
                        };

                        ws.onmessage = (event) => {
                          const msg = JSON.parse(event.data);
                          switch (msg.type) {
                            case 'CREATED':
                              console.log('Submission ID:', msg.data.submissionId);
                              break;
                            case 'PROGRESS':
                              console.log('Progress: ' + msg.data.progress + '%, Score: ' + msg.data.score);
                              break;
                            case 'COMPLETE':
                              console.log('Result:', msg.data.result, 'Score:', msg.data.score);
                              break;
                            case 'ERROR':
                              console.error('Error:', msg.data);
                              break;
                          }
                        };
                        ```

                        ---

                        ### Submission Feed (List Updates)

                        **URL:** `ws://localhost:8080/ws/submissions`

                        모든 제출의 생성 및 채점 상태 변경을 실시간으로 브로드캐스트합니다. 제출 목록 페이지에서 사용합니다.

                        #### Server → Client

                        | Type | Description |
                        |------|-------------|
                        | `NEW` | 새 제출 생성 |
                        | `UPDATE` | 채점 상태 변경 (JUDGING → COMPLETED) |

                        ```json
                        {
                          "type": "NEW" | "UPDATE",
                          "data": {
                            "id": 1,
                            "problem": { "id": 1, "title": "A+B" },
                            "contest": { "id": 1, "title": "2024 신입생 대회" } | null,
                            "user": { "id": 1, "username": "johndoe", "displayName": "John Doe", "profileImage": "https://..." },
                            "language": "PYTHON",
                            "status": "PENDING" | "JUDGING" | "COMPLETED",
                            "result": "ACCEPTED" | "WRONG_ANSWER" | ... | null,
                            "score": 100,
                            "time": 150,
                            "memory": 32,
                            "createdAt": "2025-01-01T12:00:00"
                          }
                        }
                        ```

                        #### Example

                        ```javascript
                        const ws = new WebSocket('ws://localhost:8080/ws/submissions');

                        ws.onmessage = (event) => {
                          const { type, data } = JSON.parse(event.data);
                          if (type === 'NEW') {
                            submissions.unshift(data);
                          } else if (type === 'UPDATE') {
                            const idx = submissions.findIndex(s => s.id === data.id);
                            if (idx >= 0) submissions[idx] = { ...submissions[idx], ...data };
                          }
                        };
                        ```
                        """.trimIndent(),
                    ),
            ).components(
                io.swagger.v3.oas.models
                    .Components()
                    .addSecuritySchemes(
                        "bearerAuth",
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT"),
                    ),
            )
}
