package kr.solve.infra.init

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import kr.solve.domain.contest.domain.entity.Contest
import kr.solve.domain.contest.domain.entity.ContestParticipant
import kr.solve.domain.contest.domain.entity.ContestProblem
import kr.solve.domain.contest.domain.enums.ContestType
import kr.solve.domain.contest.domain.enums.ScoreboardType
import kr.solve.domain.contest.domain.enums.ScoringType
import kr.solve.domain.contest.domain.repository.ContestParticipantRepository
import kr.solve.domain.contest.domain.repository.ContestProblemRepository
import kr.solve.domain.contest.domain.repository.ContestRepository
import kr.solve.domain.problem.domain.entity.Problem
import kr.solve.domain.problem.domain.entity.ProblemExample
import kr.solve.domain.problem.domain.entity.ProblemStats
import kr.solve.domain.problem.domain.entity.ProblemTestCase
import kr.solve.domain.problem.domain.enums.ProblemDifficulty
import kr.solve.domain.problem.domain.enums.ProblemType
import kr.solve.domain.problem.domain.repository.ProblemExampleRepository
import kr.solve.domain.problem.domain.repository.ProblemRepository
import kr.solve.domain.problem.domain.repository.ProblemStatsRepository
import kr.solve.domain.problem.domain.repository.ProblemTagRepository
import kr.solve.domain.problem.domain.repository.ProblemTestCaseRepository
import kr.solve.domain.tag.domain.entity.Tag
import kr.solve.domain.tag.domain.repository.TagRepository
import kr.solve.domain.user.domain.entity.User
import kr.solve.domain.user.domain.entity.UserActivity
import kr.solve.domain.user.domain.entity.UserOAuth
import kr.solve.domain.user.domain.enums.UserOAuthProvider
import kr.solve.domain.user.domain.enums.UserRole
import kr.solve.domain.user.domain.repository.UserActivityRepository
import kr.solve.domain.user.domain.repository.UserOAuthRepository
import kr.solve.domain.user.domain.repository.UserRepository
import kr.solve.domain.workbook.domain.entity.Workbook
import kr.solve.domain.workbook.domain.entity.WorkbookProblem
import kr.solve.domain.workbook.domain.repository.WorkbookProblemRepository
import kr.solve.domain.workbook.domain.repository.WorkbookRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

@Component
@Profile("local")
class DataInitializer(
    private val userRepository: UserRepository,
    private val userOAuthRepository: UserOAuthRepository,
    private val userActivityRepository: UserActivityRepository,
    private val tagRepository: TagRepository,
    private val problemRepository: ProblemRepository,
    private val problemExampleRepository: ProblemExampleRepository,
    private val problemTestCaseRepository: ProblemTestCaseRepository,
    private val problemTagRepository: ProblemTagRepository,
    private val problemStatsRepository: ProblemStatsRepository,
    private val contestRepository: ContestRepository,
    private val contestProblemRepository: ContestProblemRepository,
    private val contestParticipantRepository: ContestParticipantRepository,
    private val workbookRepository: WorkbookRepository,
    private val workbookProblemRepository: WorkbookProblemRepository,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        runBlocking {
            if (userRepository.count() > 0) {
                logger.info { "Data already initialized, skipping..." }
                return@runBlocking
            }

            logger.info { "Initializing seed data..." }

            val users = initUsers()
            initUserOAuths(users)
            val tags = initTags()
            val problems = initProblems(users)
            initProblemExamples(problems)
            initProblemTestCases(problems)
            initProblemTags(problems, tags)
            initProblemStats(problems)
            val contests = initContests(users)
            initContestProblems(contests, problems)
            initContestParticipants(contests, users)
            val workbooks = initWorkbooks(users)
            initWorkbookProblems(workbooks, problems)
            initUserActivities(users)

            logger.info { "Seed data initialization completed!" }
        }
    }

    private suspend fun initUsers(): List<User> {
        val users =
            listOf(
                User(
                    username = "admin",
                    displayName = "Admin User",
                    email = "admin@solve.kr",
                    profileImage = "https://api.dicebear.com/7.x/identicon/svg?seed=admin",
                    bio = "Solve 플랫폼 관리자입니다.",
                    organization = "Solve",
                    problemRating = 2500,
                    contestRating = 2400,
                    role = UserRole.ADMIN,
                ),
                User(
                    username = "alice",
                    displayName = "Alice Kim",
                    email = "alice@example.com",
                    profileImage = "https://api.dicebear.com/7.x/identicon/svg?seed=alice",
                    bio = "알고리즘을 좋아하는 개발자입니다.",
                    organization = "Google",
                    problemRating = 1850,
                    contestRating = 1920,
                    role = UserRole.USER,
                ),
                User(
                    username = "bob",
                    displayName = "Bob Lee",
                    email = "bob@example.com",
                    profileImage = "https://api.dicebear.com/7.x/identicon/svg?seed=bob",
                    bio = "PS 초보입니다. 잘 부탁드려요!",
                    organization = "Samsung",
                    problemRating = 1200,
                    contestRating = 1150,
                    role = UserRole.USER,
                ),
                User(
                    username = "charlie",
                    displayName = "Charlie Park",
                    email = "charlie@example.com",
                    profileImage = "https://api.dicebear.com/7.x/identicon/svg?seed=charlie",
                    bio = "CP 마스터를 향해!",
                    organization = "Naver",
                    problemRating = 2100,
                    contestRating = 2050,
                    role = UserRole.USER,
                ),
                User(
                    username = "diana",
                    displayName = "Diana Choi",
                    email = "diana@example.com",
                    profileImage = "https://api.dicebear.com/7.x/identicon/svg?seed=diana",
                    bio = "코딩테스트 준비 중입니다.",
                    organization = "Kakao",
                    problemRating = 800,
                    contestRating = 750,
                    role = UserRole.USER,
                ),
            )
        return users.map { userRepository.save(it) }
    }

    private suspend fun initUserOAuths(users: List<User>) {
        val oauths =
            listOf(
                UserOAuth(userId = users[0].id!!, provider = UserOAuthProvider.GITHUB, providerId = "github_admin_123"),
                UserOAuth(userId = users[1].id!!, provider = UserOAuthProvider.GITHUB, providerId = "github_alice_456"),
                UserOAuth(userId = users[2].id!!, provider = UserOAuthProvider.GOOGLE, providerId = "google_bob_789"),
                UserOAuth(userId = users[3].id!!, provider = UserOAuthProvider.GITHUB, providerId = "github_charlie_012"),
                UserOAuth(userId = users[4].id!!, provider = UserOAuthProvider.GOOGLE, providerId = "google_diana_345"),
            )
        oauths.forEach { userOAuthRepository.save(it) }
    }

    private suspend fun initTags(): List<Tag> {
        val tagNames =
            listOf(
                "구현",
                "다이나믹 프로그래밍",
                "그래프",
                "문자열",
                "수학",
                "자료구조",
                "정렬",
                "이분탐색",
                "브루트포스",
                "그리디",
            )
        return tagNames.map { tagRepository.save(Tag(name = it)) }
    }

    private suspend fun initProblems(users: List<User>): List<Problem> {
        val admin = users[0]
        val alice = users[1]
        val charlie = users[3]

        val problems =
            listOf(
                Problem(
                    title = "A+B",
                    description = "두 정수 A와 B를 입력받아 A+B를 출력하는 프로그램을 작성하시오.",
                    inputFormat = "첫째 줄에 A와 B가 주어진다. (0 < A, B < 10)",
                    outputFormat = "첫째 줄에 A+B를 출력한다.",
                    difficulty = ProblemDifficulty.MOON_5,
                    timeLimit = 1000,
                    memoryLimit = 256,
                    authorId = admin.id!!,
                    isPublic = true,
                    type = ProblemType.STANDARD,
                ),
                Problem(
                    title = "피보나치 수",
                    description = """n번째 피보나치 수를 구하는 프로그램을 작성하시오.

피보나치 수는 다음과 같이 정의됩니다:
- F(0) = 0
- F(1) = 1
- F(n) = F(n-1) + F(n-2) (n ≥ 2)""",
                    inputFormat = "첫째 줄에 n이 주어진다. (0 ≤ n ≤ 45)",
                    outputFormat = "첫째 줄에 n번째 피보나치 수를 출력한다.",
                    difficulty = ProblemDifficulty.MOON_1,
                    timeLimit = 1000,
                    memoryLimit = 256,
                    authorId = admin.id!!,
                    isPublic = true,
                    type = ProblemType.STANDARD,
                ),
                Problem(
                    title = "팰린드롬 판별",
                    description = """주어진 문자열이 팰린드롬인지 판별하는 프로그램을 작성하시오.

팰린드롬이란 앞으로 읽으나 뒤로 읽으나 같은 문자열을 말합니다.""",
                    inputFormat = "첫째 줄에 문자열 S가 주어진다. (1 ≤ |S| ≤ 1000)",
                    outputFormat = "팰린드롬이면 \"YES\", 아니면 \"NO\"를 출력한다.",
                    difficulty = ProblemDifficulty.MOON_3,
                    timeLimit = 1000,
                    memoryLimit = 256,
                    authorId = alice.id!!,
                    isPublic = true,
                    type = ProblemType.STANDARD,
                ),
                Problem(
                    title = "최단 경로",
                    description = """방향 그래프가 주어지면 주어진 시작점에서 다른 모든 정점으로의 최단 경로를 구하는 프로그램을 작성하시오.

단, 모든 간선의 가중치는 10 이하의 자연수이다.""",
                    inputFormat = """첫째 줄에 정점의 개수 V와 간선의 개수 E가 주어진다. (1 ≤ V ≤ 20,000, 1 ≤ E ≤ 300,000)
둘째 줄에는 시작 정점의 번호 K가 주어진다.
셋째 줄부터 E개의 줄에 각 간선을 나타내는 세 개의 정수 (u, v, w)가 주어진다.""",
                    outputFormat = "첫째 줄부터 V개의 줄에 걸쳐, i번째 줄에 i번 정점으로의 최단 경로의 경로값을 출력한다.",
                    difficulty = ProblemDifficulty.COMET_4,
                    timeLimit = 2000,
                    memoryLimit = 256,
                    authorId = admin.id!!,
                    isPublic = true,
                    type = ProblemType.STANDARD,
                ),
                Problem(
                    title = "이진 탐색",
                    description = "오름차순으로 정렬된 N개의 정수가 들어있는 배열에서 특정 수 X가 존재하는지 확인하는 프로그램을 작성하시오.",
                    inputFormat = """첫째 줄에 N과 X가 주어진다. (1 ≤ N ≤ 100,000, -10^9 ≤ X ≤ 10^9)
둘째 줄에 N개의 정수가 주어진다.""",
                    outputFormat = "X가 존재하면 1, 존재하지 않으면 0을 출력한다.",
                    difficulty = ProblemDifficulty.STAR_4,
                    timeLimit = 1000,
                    memoryLimit = 256,
                    authorId = alice.id!!,
                    isPublic = true,
                    type = ProblemType.STANDARD,
                ),
                Problem(
                    title = "동전 교환",
                    description = """N가지 종류의 동전이 있다. 각각의 동전이 나타내는 가치는 다르다. 이 동전을 적당히 사용해서, 그 가치의 합이 K원이 되도록 하고 싶다.

각각의 동전은 몇 개라도 사용할 수 있다.""",
                    inputFormat = """첫째 줄에 N, K가 주어진다. (1 ≤ N ≤ 100, 1 ≤ K ≤ 10,000)
다음 N개의 줄에 각 동전의 가치가 주어진다.""",
                    outputFormat = "첫째 줄에 K원을 만드는데 필요한 동전 개수의 최솟값을 출력한다. 불가능한 경우에는 -1을 출력한다.",
                    difficulty = ProblemDifficulty.STAR_1,
                    timeLimit = 1000,
                    memoryLimit = 256,
                    authorId = admin.id!!,
                    isPublic = true,
                    type = ProblemType.STANDARD,
                ),
                Problem(
                    title = "구간 합 구하기",
                    description = "N개의 정수로 이루어진 수열이 있다. M개의 쿼리가 주어지고, 각 쿼리는 i번째 수부터 j번째 수까지의 합을 구하는 것이다.",
                    inputFormat = """첫째 줄에 N과 M이 주어진다. (1 ≤ N, M ≤ 100,000)
둘째 줄에 N개의 정수가 주어진다.
셋째 줄부터 M개의 줄에 i와 j가 주어진다.""",
                    outputFormat = "각 쿼리에 대해 구간 합을 출력한다.",
                    difficulty = ProblemDifficulty.STAR_5,
                    timeLimit = 1000,
                    memoryLimit = 256,
                    authorId = charlie.id!!,
                    isPublic = true,
                    type = ProblemType.STANDARD,
                ),
                Problem(
                    title = "소수 판별",
                    description = "주어진 정수 N이 소수인지 판별하는 프로그램을 작성하시오.",
                    inputFormat = "첫째 줄에 정수 N이 주어진다. (2 ≤ N ≤ 10^12)",
                    outputFormat = "N이 소수이면 \"YES\", 아니면 \"NO\"를 출력한다.",
                    difficulty = ProblemDifficulty.STAR_3,
                    timeLimit = 1000,
                    memoryLimit = 256,
                    authorId = admin.id!!,
                    isPublic = true,
                    type = ProblemType.STANDARD,
                ),
                Problem(
                    title = "문자열 압축",
                    description = """문자열을 1개 이상의 단위로 잘라서 압축하여 더 짧은 문자열로 표현할 수 있는지 알아보려 합니다.

예를 들어, "aabbaccc"의 경우 "2a2ba3c"로 표현할 수 있습니다.""",
                    inputFormat = "첫째 줄에 문자열 S가 주어진다. (1 ≤ |S| ≤ 1,000)",
                    outputFormat = "압축한 문자열의 최소 길이를 출력한다.",
                    difficulty = ProblemDifficulty.STAR_2,
                    timeLimit = 1000,
                    memoryLimit = 256,
                    authorId = charlie.id!!,
                    isPublic = true,
                    type = ProblemType.STANDARD,
                ),
                Problem(
                    title = "최소 스패닝 트리",
                    description = """그래프가 주어졌을 때, 그 그래프의 최소 스패닝 트리를 구하는 프로그램을 작성하시오.

최소 스패닝 트리는 주어진 그래프의 모든 정점들을 연결하는 부분 그래프 중에서 그 가중치의 합이 최소인 트리를 말한다.""",
                    inputFormat = """첫째 줄에 정점의 개수 V와 간선의 개수 E가 주어진다. (1 ≤ V ≤ 10,000, 1 ≤ E ≤ 100,000)
다음 E개의 줄에 각 간선을 나타내는 세 개의 정수 A, B, C가 주어진다.""",
                    outputFormat = "첫째 줄에 최소 스패닝 트리의 가중치를 출력한다.",
                    difficulty = ProblemDifficulty.COMET_2,
                    timeLimit = 2000,
                    memoryLimit = 256,
                    authorId = admin.id!!,
                    isPublic = true,
                    type = ProblemType.STANDARD,
                ),
            )
        return problems.map { problemRepository.save(it) }
    }

    private suspend fun initProblemExamples(problems: List<Problem>) {
        val examples =
            listOf(
                // A+B
                ProblemExample(problemId = problems[0].id!!, input = "1 2", output = "3", order = 0),
                ProblemExample(problemId = problems[0].id!!, input = "5 3", output = "8", order = 1),
                // 피보나치
                ProblemExample(problemId = problems[1].id!!, input = "10", output = "55", order = 0),
                ProblemExample(problemId = problems[1].id!!, input = "0", output = "0", order = 1),
                // 팰린드롬
                ProblemExample(problemId = problems[2].id!!, input = "level", output = "YES", order = 0),
                ProblemExample(problemId = problems[2].id!!, input = "hello", output = "NO", order = 1),
                // 최단 경로
                ProblemExample(
                    problemId = problems[3].id!!,
                    input = "5 6\n1\n5 1 1\n1 2 2\n1 3 3\n2 3 4\n2 4 5\n3 4 6",
                    output = "0\n2\n3\n7\nINF",
                    order = 0,
                ),
                // 이진 탐색
                ProblemExample(problemId = problems[4].id!!, input = "5 3\n1 2 3 4 5", output = "1", order = 0),
                ProblemExample(problemId = problems[4].id!!, input = "5 6\n1 2 3 4 5", output = "0", order = 1),
                // 동전 교환
                ProblemExample(problemId = problems[5].id!!, input = "3 15\n1\n5\n12", output = "3", order = 0),
            )
        examples.forEach { problemExampleRepository.save(it) }
    }

    private suspend fun initProblemTestCases(problems: List<Problem>) {
        val testCases =
            listOf(
                // A+B (50개 테스트 케이스)
                ProblemTestCase(problemId = problems[0].id!!, input = "1 1", output = "2", order = 0),
                ProblemTestCase(problemId = problems[0].id!!, input = "1 2", output = "3", order = 1),
                ProblemTestCase(problemId = problems[0].id!!, input = "1 3", output = "4", order = 2),
                ProblemTestCase(problemId = problems[0].id!!, input = "1 4", output = "5", order = 3),
                ProblemTestCase(problemId = problems[0].id!!, input = "1 5", output = "6", order = 4),
                ProblemTestCase(problemId = problems[0].id!!, input = "1 6", output = "7", order = 5),
                ProblemTestCase(problemId = problems[0].id!!, input = "1 7", output = "8", order = 6),
                ProblemTestCase(problemId = problems[0].id!!, input = "1 8", output = "9", order = 7),
                ProblemTestCase(problemId = problems[0].id!!, input = "1 9", output = "10", order = 8),
                ProblemTestCase(problemId = problems[0].id!!, input = "2 1", output = "3", order = 9),
                ProblemTestCase(problemId = problems[0].id!!, input = "2 2", output = "4", order = 10),
                ProblemTestCase(problemId = problems[0].id!!, input = "2 3", output = "5", order = 11),
                ProblemTestCase(problemId = problems[0].id!!, input = "2 4", output = "6", order = 12),
                ProblemTestCase(problemId = problems[0].id!!, input = "2 5", output = "7", order = 13),
                ProblemTestCase(problemId = problems[0].id!!, input = "2 6", output = "8", order = 14),
                ProblemTestCase(problemId = problems[0].id!!, input = "2 7", output = "9", order = 15),
                ProblemTestCase(problemId = problems[0].id!!, input = "2 8", output = "10", order = 16),
                ProblemTestCase(problemId = problems[0].id!!, input = "2 9", output = "11", order = 17),
                ProblemTestCase(problemId = problems[0].id!!, input = "3 1", output = "4", order = 18),
                ProblemTestCase(problemId = problems[0].id!!, input = "3 2", output = "5", order = 19),
                ProblemTestCase(problemId = problems[0].id!!, input = "3 3", output = "6", order = 20),
                ProblemTestCase(problemId = problems[0].id!!, input = "3 4", output = "7", order = 21),
                ProblemTestCase(problemId = problems[0].id!!, input = "3 5", output = "8", order = 22),
                ProblemTestCase(problemId = problems[0].id!!, input = "3 6", output = "9", order = 23),
                ProblemTestCase(problemId = problems[0].id!!, input = "3 7", output = "10", order = 24),
                ProblemTestCase(problemId = problems[0].id!!, input = "3 8", output = "11", order = 25),
                ProblemTestCase(problemId = problems[0].id!!, input = "3 9", output = "12", order = 26),
                ProblemTestCase(problemId = problems[0].id!!, input = "4 1", output = "5", order = 27),
                ProblemTestCase(problemId = problems[0].id!!, input = "4 2", output = "6", order = 28),
                ProblemTestCase(problemId = problems[0].id!!, input = "4 3", output = "7", order = 29),
                ProblemTestCase(problemId = problems[0].id!!, input = "4 4", output = "8", order = 30),
                ProblemTestCase(problemId = problems[0].id!!, input = "4 5", output = "9", order = 31),
                ProblemTestCase(problemId = problems[0].id!!, input = "4 6", output = "10", order = 32),
                ProblemTestCase(problemId = problems[0].id!!, input = "4 7", output = "11", order = 33),
                ProblemTestCase(problemId = problems[0].id!!, input = "4 8", output = "12", order = 34),
                ProblemTestCase(problemId = problems[0].id!!, input = "4 9", output = "13", order = 35),
                ProblemTestCase(problemId = problems[0].id!!, input = "5 1", output = "6", order = 36),
                ProblemTestCase(problemId = problems[0].id!!, input = "5 2", output = "7", order = 37),
                ProblemTestCase(problemId = problems[0].id!!, input = "5 3", output = "8", order = 38),
                ProblemTestCase(problemId = problems[0].id!!, input = "5 4", output = "9", order = 39),
                ProblemTestCase(problemId = problems[0].id!!, input = "5 5", output = "10", order = 40),
                ProblemTestCase(problemId = problems[0].id!!, input = "5 6", output = "11", order = 41),
                ProblemTestCase(problemId = problems[0].id!!, input = "5 7", output = "12", order = 42),
                ProblemTestCase(problemId = problems[0].id!!, input = "5 8", output = "13", order = 43),
                ProblemTestCase(problemId = problems[0].id!!, input = "5 9", output = "14", order = 44),
                ProblemTestCase(problemId = problems[0].id!!, input = "6 1", output = "7", order = 45),
                ProblemTestCase(problemId = problems[0].id!!, input = "6 2", output = "8", order = 46),
                ProblemTestCase(problemId = problems[0].id!!, input = "6 3", output = "9", order = 47),
                ProblemTestCase(problemId = problems[0].id!!, input = "6 4", output = "10", order = 48),
                ProblemTestCase(problemId = problems[0].id!!, input = "6 5", output = "11", order = 49),
                ProblemTestCase(problemId = problems[0].id!!, input = "6 6", output = "12", order = 50),
                ProblemTestCase(problemId = problems[0].id!!, input = "6 7", output = "13", order = 51),
                ProblemTestCase(problemId = problems[0].id!!, input = "6 8", output = "14", order = 52),
                ProblemTestCase(problemId = problems[0].id!!, input = "6 9", output = "15", order = 53),
                ProblemTestCase(problemId = problems[0].id!!, input = "7 1", output = "8", order = 54),
                ProblemTestCase(problemId = problems[0].id!!, input = "7 2", output = "9", order = 55),
                ProblemTestCase(problemId = problems[0].id!!, input = "7 3", output = "10", order = 56),
                ProblemTestCase(problemId = problems[0].id!!, input = "7 4", output = "11", order = 57),
                ProblemTestCase(problemId = problems[0].id!!, input = "7 5", output = "12", order = 58),
                ProblemTestCase(problemId = problems[0].id!!, input = "7 6", output = "13", order = 59),
                ProblemTestCase(problemId = problems[0].id!!, input = "7 7", output = "14", order = 60),
                ProblemTestCase(problemId = problems[0].id!!, input = "7 8", output = "15", order = 61),
                ProblemTestCase(problemId = problems[0].id!!, input = "7 9", output = "16", order = 62),
                ProblemTestCase(problemId = problems[0].id!!, input = "8 1", output = "9", order = 63),
                ProblemTestCase(problemId = problems[0].id!!, input = "8 2", output = "10", order = 64),
                ProblemTestCase(problemId = problems[0].id!!, input = "8 3", output = "11", order = 65),
                ProblemTestCase(problemId = problems[0].id!!, input = "8 4", output = "12", order = 66),
                ProblemTestCase(problemId = problems[0].id!!, input = "8 5", output = "13", order = 67),
                ProblemTestCase(problemId = problems[0].id!!, input = "8 6", output = "14", order = 68),
                ProblemTestCase(problemId = problems[0].id!!, input = "8 7", output = "15", order = 69),
                ProblemTestCase(problemId = problems[0].id!!, input = "8 8", output = "16", order = 70),
                ProblemTestCase(problemId = problems[0].id!!, input = "8 9", output = "17", order = 71),
                ProblemTestCase(problemId = problems[0].id!!, input = "9 1", output = "10", order = 72),
                ProblemTestCase(problemId = problems[0].id!!, input = "9 2", output = "11", order = 73),
                ProblemTestCase(problemId = problems[0].id!!, input = "9 3", output = "12", order = 74),
                ProblemTestCase(problemId = problems[0].id!!, input = "9 4", output = "13", order = 75),
                ProblemTestCase(problemId = problems[0].id!!, input = "9 5", output = "14", order = 76),
                ProblemTestCase(problemId = problems[0].id!!, input = "9 6", output = "15", order = 77),
                ProblemTestCase(problemId = problems[0].id!!, input = "9 7", output = "16", order = 78),
                ProblemTestCase(problemId = problems[0].id!!, input = "9 8", output = "17", order = 79),
                ProblemTestCase(problemId = problems[0].id!!, input = "9 9", output = "18", order = 80),
                // 피보나치
                ProblemTestCase(problemId = problems[1].id!!, input = "10", output = "55", order = 0),
                ProblemTestCase(problemId = problems[1].id!!, input = "0", output = "0", order = 1),
                ProblemTestCase(problemId = problems[1].id!!, input = "45", output = "1134903170", order = 2),
                // 팰린드롬
                ProblemTestCase(problemId = problems[2].id!!, input = "level", output = "YES", order = 0),
                ProblemTestCase(problemId = problems[2].id!!, input = "hello", output = "NO", order = 1),
                ProblemTestCase(problemId = problems[2].id!!, input = "a", output = "YES", order = 2),
            )
        testCases.forEach { problemTestCaseRepository.save(it) }
    }

    private suspend fun initProblemTags(
        problems: List<Problem>,
        tags: List<Tag>,
    ) {
        val tagMap = tags.associateBy { it.name }
        val problemTags =
            listOf(
                problems[0].id!! to listOf("구현", "수학"),
                problems[1].id!! to listOf("다이나믹 프로그래밍", "수학"),
                problems[2].id!! to listOf("문자열", "구현"),
                problems[3].id!! to listOf("그래프"),
                problems[4].id!! to listOf("이분탐색"),
                problems[5].id!! to listOf("다이나믹 프로그래밍"),
                problems[6].id!! to listOf("자료구조"),
                problems[7].id!! to listOf("수학"),
                problems[8].id!! to listOf("문자열", "브루트포스"),
                problems[9].id!! to listOf("그래프", "그리디"),
            )
        problemTags.forEach { (problemId, tagNames) ->
            tagNames.forEach { tagName ->
                tagMap[tagName]?.let { tag ->
                    problemTagRepository.insert(problemId, tag.id!!)
                }
            }
        }
    }

    private suspend fun initProblemStats(problems: List<Problem>) {
        val stats =
            listOf(
                ProblemStats(
                    problemId = problems[0].id!!,
                    submissionCount = 1523,
                    acceptedCount = 1245,
                    userCount = 892,
                    acceptedUserCount = 845,
                ),
                ProblemStats(
                    problemId = problems[1].id!!,
                    submissionCount = 987,
                    acceptedCount = 654,
                    userCount = 543,
                    acceptedUserCount = 432,
                ),
                ProblemStats(
                    problemId = problems[2].id!!,
                    submissionCount = 756,
                    acceptedCount = 523,
                    userCount = 412,
                    acceptedUserCount = 356,
                ),
                ProblemStats(
                    problemId = problems[3].id!!,
                    submissionCount = 432,
                    acceptedCount = 187,
                    userCount = 298,
                    acceptedUserCount = 145,
                ),
                ProblemStats(
                    problemId = problems[4].id!!,
                    submissionCount = 654,
                    acceptedCount = 421,
                    userCount = 387,
                    acceptedUserCount = 312,
                ),
                ProblemStats(
                    problemId = problems[5].id!!,
                    submissionCount = 521,
                    acceptedCount = 234,
                    userCount = 312,
                    acceptedUserCount = 189,
                ),
                ProblemStats(
                    problemId = problems[6].id!!,
                    submissionCount = 389,
                    acceptedCount = 267,
                    userCount = 234,
                    acceptedUserCount = 198,
                ),
                ProblemStats(
                    problemId = problems[7].id!!,
                    submissionCount = 298,
                    acceptedCount = 156,
                    userCount = 187,
                    acceptedUserCount = 123,
                ),
                ProblemStats(
                    problemId = problems[8].id!!,
                    submissionCount = 234,
                    acceptedCount = 98,
                    userCount = 156,
                    acceptedUserCount = 78,
                ),
                ProblemStats(
                    problemId = problems[9].id!!,
                    submissionCount = 187,
                    acceptedCount = 67,
                    userCount = 123,
                    acceptedUserCount = 54,
                ),
            )
        stats.forEach { problemStatsRepository.save(it) }
    }

    private suspend fun initContests(users: List<User>): List<Contest> {
        val now = LocalDateTime.now()
        val contests =
            listOf(
                Contest(
                    title = "Solve Weekly Contest #1",
                    description = "매주 진행되는 Solve 위클리 대회입니다. 누구나 참가할 수 있습니다.",
                    hostId = users[0].id!!,
                    startAt = now.minusHours(1),
                    endAt = now.plusHours(2),
                    type = ContestType.PUBLIC,
                    scoringType = ScoringType.IOI,
                    scoreboardType = ScoreboardType.REALTIME,
                    isRated = true,
                ),
                Contest(
                    title = "Solve Monthly Challenge",
                    description = "월간 챌린지 대회입니다. 고난이도 문제들로 구성되어 있습니다.",
                    hostId = users[0].id!!,
                    startAt = now.plusMinutes(1),
                    endAt = now.plusMinutes(1).plusHours(3),
                    type = ContestType.PUBLIC,
                    scoringType = ScoringType.IOI,
                    scoreboardType = ScoreboardType.FREEZE,
                    freezeMinutes = 30,
                    isRated = true,
                ),
                Contest(
                    title = "Private Practice Contest",
                    description = "비공개 연습 대회입니다.",
                    hostId = users[1].id!!,
                    startAt = now.minusDays(1),
                    endAt = now.minusDays(1).plusHours(2),
                    type = ContestType.PRIVATE,
                    inviteCode = "SOLVE2024",
                    scoringType = ScoringType.ICPC,
                    scoreboardType = ScoreboardType.REALTIME,
                    isRated = false,
                ),
            )
        return contests.map { contestRepository.save(it) }
    }

    private suspend fun initContestProblems(
        contests: List<Contest>,
        problems: List<Problem>,
    ) {
        val contestProblems =
            listOf(
                // Weekly Contest #1
                ContestProblem(contestId = contests[0].id!!, problemId = problems[0].id!!, order = 0, score = 100),
                ContestProblem(contestId = contests[0].id!!, problemId = problems[2].id!!, order = 1, score = 200),
                ContestProblem(contestId = contests[0].id!!, problemId = problems[4].id!!, order = 2, score = 300),
                // Monthly Challenge
                ContestProblem(contestId = contests[1].id!!, problemId = problems[3].id!!, order = 0, score = 250),
                ContestProblem(contestId = contests[1].id!!, problemId = problems[5].id!!, order = 1, score = 300),
                ContestProblem(contestId = contests[1].id!!, problemId = problems[9].id!!, order = 2, score = 450),
                // Private Practice
                ContestProblem(contestId = contests[2].id!!, problemId = problems[1].id!!, order = 0),
                ContestProblem(contestId = contests[2].id!!, problemId = problems[6].id!!, order = 1),
            )
        contestProblems.forEach { contestProblemRepository.save(it) }
    }

    private suspend fun initContestParticipants(
        contests: List<Contest>,
        users: List<User>,
    ) {
        val now = LocalDateTime.now()
        val participants =
            listOf(
                ContestParticipant(
                    contestId = contests[0].id!!,
                    userId = users[1].id!!,
                    totalScore = 600,
                    penalty = 45,
                    joinedAt = now.minusMinutes(30),
                ),
                ContestParticipant(
                    contestId = contests[0].id!!,
                    userId = users[2].id!!,
                    totalScore = 300,
                    penalty = 67,
                    joinedAt = now.minusMinutes(45),
                ),
                ContestParticipant(
                    contestId = contests[0].id!!,
                    userId = users[3].id!!,
                    totalScore = 600,
                    penalty = 32,
                    joinedAt = now.minusMinutes(50),
                ),
                ContestParticipant(
                    contestId = contests[2].id!!,
                    userId = users[1].id!!,
                    totalScore = 2,
                    penalty = 120,
                    joinedAt = now.minusDays(1).plusMinutes(10),
                ),
            )
        participants.forEach { contestParticipantRepository.save(it) }
    }

    private suspend fun initWorkbooks(users: List<User>): List<Workbook> {
        val workbooks =
            listOf(
                Workbook(title = "초보자를 위한 기초 문제집", description = "프로그래밍을 처음 시작하는 분들을 위한 기초 문제 모음입니다.", authorId = users[0].id!!),
                Workbook(title = "DP 마스터 가이드", description = "다이나믹 프로그래밍의 기초부터 심화까지 다루는 문제집입니다.", authorId = users[1].id!!),
                Workbook(title = "코딩테스트 대비 필수 문제", description = "취업 준비생을 위한 코딩테스트 필수 유형 문제 모음입니다.", authorId = users[3].id!!),
            )
        return workbooks.map { workbookRepository.save(it) }
    }

    private suspend fun initWorkbookProblems(
        workbooks: List<Workbook>,
        problems: List<Problem>,
    ) {
        val workbookProblems =
            listOf(
                // 초보자 문제집
                WorkbookProblem(workbookId = workbooks[0].id!!, problemId = problems[0].id!!, order = 0),
                WorkbookProblem(workbookId = workbooks[0].id!!, problemId = problems[2].id!!, order = 1),
                WorkbookProblem(workbookId = workbooks[0].id!!, problemId = problems[4].id!!, order = 2),
                // DP 마스터 가이드
                WorkbookProblem(workbookId = workbooks[1].id!!, problemId = problems[1].id!!, order = 0),
                WorkbookProblem(workbookId = workbooks[1].id!!, problemId = problems[5].id!!, order = 1),
                // 코테 필수 문제
                WorkbookProblem(workbookId = workbooks[2].id!!, problemId = problems[3].id!!, order = 0),
                WorkbookProblem(workbookId = workbooks[2].id!!, problemId = problems[7].id!!, order = 1),
                WorkbookProblem(workbookId = workbooks[2].id!!, problemId = problems[9].id!!, order = 2),
            )
        workbookProblems.forEach { workbookProblemRepository.save(it) }
    }

    private suspend fun initUserActivities(users: List<User>) {
        val today = LocalDate.now()
        val activities =
            listOf(
                UserActivity(userId = users[1].id!!, date = today, solvedCount = 2, submissionCount = 3),
                UserActivity(userId = users[1].id!!, date = today.minusDays(1), solvedCount = 1, submissionCount = 2),
                UserActivity(userId = users[1].id!!, date = today.minusDays(2), solvedCount = 3, submissionCount = 5),
                UserActivity(userId = users[2].id!!, date = today, solvedCount = 1, submissionCount = 1),
                UserActivity(userId = users[3].id!!, date = today, solvedCount = 1, submissionCount = 1),
                UserActivity(userId = users[4].id!!, date = today, solvedCount = 0, submissionCount = 1),
            )
        activities.forEach { userActivityRepository.save(it) }
    }
}
