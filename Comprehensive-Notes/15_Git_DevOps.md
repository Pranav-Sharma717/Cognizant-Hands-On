# 15 — Git, GitHub & DevOps Fundamentals

> Maps to [`Week 6/Week6.md`](../Week%206/Week6.md) and [`Week 6/git commands .txt`](../Week%206/git%20commands%20.txt).
> **References:** [Pro Git Book](https://git-scm.com/book/en/v2), [GitHub Docs](https://docs.github.com/), [Docker Docs](https://docs.docker.com/)

---

# PART 1 — Git

## 1.1 What is Git?
A **distributed version control system (DVCS)** created by Linus Torvalds (2005) for the Linux kernel. Every clone is a full repository with complete history — you can commit, branch and view history offline.

| | Centralised (SVN) | **Distributed (Git)** |
|---|---|---|
| History | on the server only | full copy in every clone |
| Offline work | ❌ | ✅ |
| Branching | expensive | **cheap and instant** |
| Single point of failure | yes | no |

**Git vs GitHub** ⭐ — Git is the **tool** (runs locally); GitHub/GitLab/Bitbucket are **hosting platforms** that add pull requests, issues, CI/CD, code review and access control.

## 1.2 The three areas + the remote ⭐
```
 Working Directory ──git add──► Staging Area ──git commit──► Local Repo ──git push──► Remote
   (your files)      (index)        (.git)                                        (GitHub)
        ◄────────────────── git checkout / restore ──────────┘
        ◄────────────── git pull (= fetch + merge) ──────────────────────────────────┘
```
| Area | Meaning |
|---|---|
| **Working directory** | the files you edit |
| **Staging area (index)** | changes selected for the next commit |
| **Local repository** | committed history in `.git/` |
| **Remote** | the shared repository (`origin`) |

## 1.3 Setup (from the repo's command log)
```bash
git --version
git config --global user.name "G V Pranav Sharma"
git config --global user.email "you@example.com"
git config --global core.editor "code --wait"
git config --global init.defaultBranch main
git config --global --list
git config --global -e                 # edit the config file
```

## 1.4 Everyday workflow ⭐
```bash
git init                               # create a new repo
git clone <url>                        # copy an existing one

git status                             # ⭐ run this constantly
git status -s                          # short form
git add file.txt                       # stage one file
git add .                              # stage everything in the current directory
git add -p                             # ⭐ stage interactively, hunk by hunk
git restore --staged file.txt          # unstage (modern)
git reset HEAD file.txt                # unstage (older syntax)
git restore file.txt                   # discard working-directory changes

git commit -m "Add employee REST endpoints"
git commit -am "msg"                   # stage tracked files + commit
git commit --amend                     # fix the last commit (⚠️ rewrites history)

git log --oneline --graph --decorate --all   # ⭐ the one to memorise
git log -p file.java                   # history with diffs for a file
git log --author="Pranav" --since="2 weeks ago"
git show <commit>
git diff                               # working dir vs staging
git diff --staged                      # staging vs last commit
git diff main feature                  # between branches
git blame file.java                    # who changed each line
```

### Writing good commit messages
```
feat(employee): add salary filter to the search endpoint

Adds a `minSalary` query parameter backed by a derived repository
method. Indexed on (dept_id, salary) to keep the query sargable.

Closes #142
```
**Conventional Commits** prefixes: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`, `perf`, `ci`.
Rules: imperative mood ("Add", not "Added"), ≤50-char subject, blank line, then the *why* in the body.

## 1.5 Branching ⭐
```bash
git branch                             # list local
git branch -a                          # include remote-tracking
git branch feature/employee-search     # create
git switch feature/employee-search     # ⭐ modern (Git 2.23+)
git switch -c feature/new              # create + switch
git checkout -b feature/new            # older equivalent
git branch -d old-branch               # delete (safe — refuses if unmerged)
git branch -D old-branch               # force delete
git branch -m new-name                 # rename
```
A branch is just a **movable pointer to a commit** — that's why branching is instant and cheap.

## 1.6 Merging vs Rebasing ⭐⭐
```bash
git switch main
git merge feature/x                    # merge feature into main
git merge --no-ff feature/x            # always create a merge commit (preserves the branch)
git merge --squash feature/x           # collapse into one staged change

git switch feature/x
git rebase main                        # replay my commits on top of main
git rebase -i HEAD~3                   # interactive: squash/reword/drop (not available in this tool env)
```
```
BEFORE            MERGE                          REBASE
main:  A—B—C      A—B—C———M                      A—B—C—D'—E'
            \          \ /                          (linear history)
feat:        D—E        D—E
```
| | Merge | Rebase |
|---|---|---|
| History | preserves the true branch topology | **linear, clean** |
| Creates | a merge commit | rewritten commits (new SHAs) |
| Safe on shared branches | ✅ | **❌ never rebase pushed/shared history** |
| Conflicts | resolved once | possibly once per replayed commit |

⭐ **The Golden Rule of Rebasing:** *never rebase commits that others have already pulled.* It rewrites SHAs and breaks everyone's history.
**Common team convention:** rebase your local feature branch to stay current, then merge into main with a PR.

## 1.7 Merge conflicts ⭐ (Week 6 exercise)
Occur when two branches modify the **same lines** of the same file.
```
<<<<<<< HEAD
<message>Main version</message>
=======
<message>Updated branch version</message>
>>>>>>> GitWork
```
**Resolution:**
```bash
git merge GitWork              # → CONFLICT
git status                     # lists "both modified" files
# 1. Open each file, choose/combine the content, delete the <<<< ==== >>>> markers
git add hello.xml              # 2. mark as resolved
git commit                     # 3. complete the merge (or: git merge --continue)

git merge --abort              # bail out and go back
git mergetool                  # use a visual tool (p4merge, VS Code, meld)
git config --global merge.tool p4merge
```
**Prevention:** small focused commits · pull/rebase often · communicate on shared files · avoid formatting-only changes mixed with logic.

## 1.8 Remotes
```bash
git remote -v
git remote add origin https://github.com/Pranav-Sharma717/Cognizant-Handson.git
git remote set-url origin <new-url>

git fetch origin                       # download, do NOT merge
git pull origin main                   # = fetch + merge
git pull --rebase origin main          # = fetch + rebase (cleaner history)
git push -u origin main                # push + set upstream tracking
git push origin --delete old-branch
```
⭐ **`fetch` vs `pull`:** `fetch` only updates remote-tracking branches so you can inspect before integrating; `pull` fetches **and** immediately merges/rebases into your branch.

## 1.9 Undoing things ⭐
```bash
git restore file.txt                   # discard working changes
git restore --staged file.txt          # unstage
git commit --amend                     # rewrite the last commit

git reset --soft  HEAD~1               # undo commit, KEEP changes staged
git reset --mixed HEAD~1               # undo commit, keep changes unstaged (default)
git reset --hard  HEAD~1               # ⚠️ undo commit AND DISCARD changes

git revert <commit>                    # ✅ create a NEW commit that undoes it — safe for shared branches

git reflog                             # ⭐ your safety net — shows every HEAD move
git reset --hard HEAD@{2}              # recover from a bad reset
```
| | `reset` | `revert` |
|---|---|---|
| History | rewrites it | adds a new commit |
| Shared branches | ❌ dangerous | ✅ safe |
| Use | local cleanup | undoing a pushed commit |

## 1.10 Stashing
```bash
git stash                 # shelve uncommitted work
git stash push -m "wip: search filter"
git stash list
git stash pop             # reapply and remove from the stash
git stash apply           # reapply and keep
git stash drop / clear
git stash -u              # include untracked files
```

## 1.11 `.gitignore` (Week 6 exercise)
```gitignore
# Build output
target/
build/
*.class
*.jar

# Logs
*.log
logs/
*.bak

# IDE
.idea/
*.iml
.vscode/
.settings/

# Node
node_modules/
npm-debug.log*

# OS
.DS_Store
Thumbs.db

# Secrets — NEVER commit these ⭐
.env
application-local.properties
*.pem
*.key
```
⚠️ `.gitignore` only affects **untracked** files. To stop tracking something already committed:
```bash
git rm --cached application.log
git rm -r --cached node_modules/
```
> **This repo needs this:** `Main.class`, `design-patterns/target/` and `myfirstreact/node_modules/` are compiled/installed artefacts that shouldn't be in version control.

## 1.12 Tags & releases
```bash
git tag v1.0.0                              # lightweight
git tag -a v1.0.0 -m "First release"        # annotated (preferred — has metadata)
git tag -l
git push origin v1.0.0
git push origin --tags
```
**Semantic Versioning** — `MAJOR.MINOR.PATCH`: breaking change / backwards-compatible feature / backwards-compatible fix.

## 1.13 Branching strategies ⭐
| Strategy | Shape | Suits |
|---|---|---|
| **Git Flow** | `main`, `develop`, `feature/*`, `release/*`, `hotfix/*` | scheduled releases, enterprise |
| **GitHub Flow** | `main` + short-lived feature branches + PRs | continuous deployment |
| **GitLab Flow** | GitHub Flow + environment branches (`staging`, `production`) | staged environments |
| **Trunk-Based** | everyone commits to `main` behind feature flags | high-maturity CI/CD teams |

**Pull Request workflow (what you'll actually do at Cognizant)**
```
1. git switch -c feature/JIRA-123-employee-search
2. commit small, focused changes
3. git push -u origin feature/JIRA-123-employee-search
4. Open a PR on GitHub → describe the change, link the ticket
5. Automated checks run (build, tests, SonarQube)
6. Peer code review → address comments
7. Squash & merge into main → delete the branch
```

## 1.14 Useful extras
```bash
git cherry-pick <commit>      # apply one commit from another branch
git bisect start / bad / good # binary-search for the commit that introduced a bug
git clean -fd                 # ⚠️ delete untracked files/dirs
git shortlog -sn              # commits per author
git archive -o release.zip HEAD
```

---

# PART 2 — DevOps Fundamentals

## 2.1 What is DevOps?
A culture and set of practices uniting **Dev**elopment and **Op**erations to shorten the delivery cycle and improve reliability through automation and shared ownership.

```
  PLAN → CODE → BUILD → TEST → RELEASE → DEPLOY → OPERATE → MONITOR ─┐
    ▲                                                                 │
    └─────────────────────── feedback ────────────────────────────────┘
```
**Pillars:** CI/CD · Infrastructure as Code · automated testing · monitoring & observability · collaboration · security ("DevSecOps").
**DORA metrics:** deployment frequency · lead time for changes · change failure rate · mean time to restore.

## 2.2 CI / CD ⭐
| Term | Meaning |
|---|---|
| **Continuous Integration** | Every push triggers an automatic build + test run against the shared mainline — catches integration bugs immediately |
| **Continuous Delivery** | Every passing build is *deployable*; a human approves the release |
| **Continuous Deployment** | Every passing build is deployed to production **automatically** |

**A typical Java pipeline**
```
git push → checkout → mvn clean verify (compile + unit + integration tests)
        → SonarQube quality gate → build a Docker image → push to a registry
        → deploy to DEV → smoke tests → (approval) → STAGING → PROD
```

**GitHub Actions example**
```yaml
name: CI
on:
  push:    { branches: [main] }
  pull_request: { branches: [main] }
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with: { java-version: '17', distribution: 'temurin', cache: maven }
      - name: Build and test
        run: mvn -B clean verify
      - name: Upload coverage
        uses: actions/upload-artifact@v4
        with: { name: jacoco, path: target/site/jacoco }
```
**Tools:** Jenkins (the enterprise standard, `Jenkinsfile`), GitHub Actions, GitLab CI, Azure DevOps, CircleCI.

## 2.3 Containers — Docker ⭐
**Problem it solves:** *"it works on my machine"*. A container packages the app **plus its runtime, libraries and config** into a portable image.

| Container | VM |
|---|---|
| Shares the host kernel | Full guest OS |
| MBs, starts in seconds | GBs, starts in minutes |
| Less isolation | Stronger isolation |

```dockerfile
# Multi-stage build — small final image
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline           # cache the dependency layer
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```
```bash
docker build -t employee-service:1.0 .
docker run -d -p 8080:8080 --name emp -e SPRING_PROFILES_ACTIVE=prod employee-service:1.0
docker ps         docker logs -f emp        docker exec -it emp sh
docker stop emp   docker rm emp             docker images     docker rmi <id>
```
```yaml
# docker-compose.yml — the whole stack locally
services:
  mysql:
    image: mysql:8
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: cognizant_fse
    ports: ["3306:3306"]
    volumes: ["mysqldata:/var/lib/mysql"]
  app:
    build: .
    ports: ["8080:8080"]
    depends_on: [mysql]
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/cognizant_fse
volumes: { mysqldata: }
```
```bash
docker compose up -d      docker compose logs -f      docker compose down -v
```
**Terms:** image (blueprint) · container (running instance) · Dockerfile (build recipe) · registry (Docker Hub/ECR) · volume (persistent data) · network · layer (cached build step).

## 2.4 Orchestration — Kubernetes (awareness level)
Automates deployment, scaling and healing of containers.
**Concepts:** Pod (smallest unit, 1+ containers) · Deployment (declarative replica management + rolling updates) · Service (stable network endpoint + load balancing) · Ingress (HTTP routing) · ConfigMap/Secret (configuration) · Namespace · HPA (autoscaling).
Spring Boot fits naturally: `/actuator/health/liveness` and `/readiness` map to Kubernetes probes.

## 2.5 Cloud fundamentals
| Model | You manage | Example |
|---|---|---|
| **IaaS** | OS, runtime, app | EC2, Azure VM |
| **PaaS** | app only | Elastic Beanstalk, App Service, Heroku |
| **SaaS** | nothing | Gmail, Salesforce |
| **FaaS** | a function | AWS Lambda, Azure Functions |

**Key AWS services for a Java dev:** EC2 (compute) · S3 (object storage) · RDS (managed MySQL) · ECS/EKS (containers) · Lambda · API Gateway · CloudWatch (logs/metrics) · IAM (access) · SQS/SNS (messaging) · Secrets Manager.
**Benefits:** elasticity, pay-per-use, no capital expenditure, global reach, managed services.

## 2.6 IaC & configuration
**Infrastructure as Code** — infrastructure defined in version-controlled files: Terraform (multi-cloud, declarative), CloudFormation (AWS), Ansible (configuration management), Helm (Kubernetes packaging).
Benefits: reproducible environments, peer-reviewable infrastructure, no click-ops drift.

## 2.7 Quality & security in the pipeline
- **SonarQube** — static analysis: bugs, code smells, coverage, duplication, security hotspots. A **quality gate** can fail the build.
- **OWASP Dependency-Check / Snyk / Dependabot** — flag vulnerable dependencies (this is how Log4Shell was caught).
- **Secrets management** — never commit credentials; use environment variables, AWS Secrets Manager, HashiCorp Vault, or Spring Cloud Config with encryption.
- **Artifact repository** — Nexus/Artifactory for internal JARs.

## 2.8 Deployment strategies
| Strategy | How |
|---|---|
| **Recreate** | stop old, start new (downtime) |
| **Rolling** | replace instances gradually (Kubernetes default) |
| **Blue-Green** | two identical environments; switch traffic; instant rollback |
| **Canary** | route a small % of traffic to the new version first |
| **Feature flags** | deploy dark, enable per user/segment |

---

## ⭐ Interview Gold — Git & DevOps Rapid Fire

| Q | A |
|---|---|
| Git vs GitHub? | A distributed VCS tool vs a hosting platform adding PRs, issues, CI/CD, review. |
| Centralised vs distributed VCS? | Server-only history vs a full repo copy in every clone (offline work, cheap branching). |
| The three areas of Git? | Working directory → staging area (index) → local repository (+ remote). |
| `git fetch` vs `git pull`? | Fetch downloads only; pull = fetch + merge (or rebase). |
| `git merge` vs `git rebase`? | Preserves topology with a merge commit vs replays commits for a linear history. Never rebase shared branches. |
| `git reset` vs `git revert`? | Rewrites history (local only) vs adds an inverse commit (safe on shared branches). |
| `reset --soft` vs `--mixed` vs `--hard`? | Keep staged / keep unstaged / discard everything. |
| How do you resolve a merge conflict? | Open the file, choose/combine content, remove the markers, `git add`, `git commit`. |
| How do you undo a pushed commit? | `git revert <sha>` — never force-push a rewritten shared history. |
| How do you recover a lost commit? | `git reflog` to find the SHA, then `git reset --hard <sha>` or `git cherry-pick`. |
| What is `.gitignore`, and what if a file is already tracked? | Excludes untracked files; use `git rm --cached <file>` to stop tracking. |
| What is `git stash` for? | Temporarily shelve uncommitted work to switch context. |
| Branching strategies? | Git Flow, GitHub Flow, GitLab Flow, trunk-based. |
| What is a pull request? | A request to merge a branch, providing a place for review, discussion and automated checks. |
| What is a good commit message? | Imperative, ≤50-char subject, body explaining *why*, linked issue; Conventional Commits prefixes. |
| What is DevOps? | Culture + automation uniting dev and ops to deliver faster and more reliably. |
| CI vs CD vs CD? | Continuous Integration / Delivery (deployable, manual gate) / Deployment (fully automatic). |
| What does a CI pipeline do for a Java project? | Checkout → `mvn clean verify` → quality gate → build & push a Docker image → deploy. |
| What is Docker and why? | Containerisation — packages the app with its runtime for consistent, portable, fast-starting environments. |
| Container vs VM? | Shares the host kernel, MBs, seconds vs full guest OS, GBs, minutes. |
| What is a Dockerfile / image / container? | Build recipe / immutable blueprint / running instance. |
| Why multi-stage Docker builds? | Build with the full JDK+Maven, ship only the JRE + JAR — much smaller, smaller attack surface. |
| What is Kubernetes for? | Orchestrating containers: scheduling, scaling, self-healing, rolling updates, service discovery. |
| IaaS vs PaaS vs SaaS? | You manage app+runtime+OS / just the app / nothing. |
| What is Infrastructure as Code? | Declaring infrastructure in versioned files (Terraform/CloudFormation) for reproducibility. |
| Blue-green vs canary? | Two full environments with an instant traffic switch vs a gradual percentage rollout. |
| How do you manage secrets? | Env vars, Vault/Secrets Manager, encrypted config — never in Git. |
| What is SonarQube? | Static analysis for bugs, smells, coverage and security, enforced by a quality gate in CI. |

---

## 📚 References
- Pro Git Book (free, the definitive reference): https://git-scm.com/book/en/v2
- Git reference manual: https://git-scm.com/docs
- GitHub Docs: https://docs.github.com/
- Learn Git Branching (interactive visual practice): https://learngitbranching.js.org/
- Oh Shit, Git!?! (recovery recipes): https://ohshitgit.com/
- Atlassian Git Tutorials: https://www.atlassian.com/git/tutorials
- Conventional Commits: https://www.conventionalcommits.org/
- Semantic Versioning: https://semver.org/
- Docker Docs: https://docs.docker.com/
- Kubernetes Docs: https://kubernetes.io/docs/home/
- GitHub Actions: https://docs.github.com/en/actions
- Jenkins: https://www.jenkins.io/doc/
- Terraform: https://developer.hashicorp.com/terraform/docs
- DORA / State of DevOps: https://dora.dev/
- *The Phoenix Project* / *Accelerate* — the DevOps culture books
