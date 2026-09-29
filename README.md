# ERP Api Service App

## Getting Started

To start your own new application, **clone the repository with all submodules**:

```bash
git clone --recurse-submodules https://github.com/gersimuca/resource-planning.git
```

> If you already cloned the repository without `--recurse-submodules`, initialize submodules with:

```bash
git submodule update --init --recursive
```

### Pull latest changes including submodules

```bash
git pull --recurse-submodules
```

Or update submodules separately:

```bash
git submodule update --remote --merge
```

> Submodules are separate repositories embedded in this project (e.g., `resource-planning-ui-service`). Cloning without them may cause the application to fail.

---
## Local Environment Setup (Required)

Before starting the application locally, initialize the local environment using the provided Ansible setup.

This setup:

* prepares the local ERP environment
* generates the required `.env` file
* creates local runtime configuration
* prepares dependencies for local execution

### 1. Install Ansible

#### macOS

Install using Homebrew:

```bash
brew install ansible
```

Verify installation:

```bash
ansible --version
```

---

#### Ubuntu / Pop!_OS / Debian

```bash
sudo apt update
sudo apt install ansible -y
```

Verify installation:

```bash
ansible --version
```

---

#### Windows (Recommended: WSL)

Install WSL:

```powershell
wsl --install
```

Open Ubuntu and install Ansible:

```bash
sudo apt update
sudo apt install ansible -y
```

Verify installation:

```bash
ansible --version
```

---

### 2. Generate local configuration

Run the local Ansible playbook:

```bash
ansible-playbook \
  -i ansible/inventories/local/hosts.yml \
  ansible/playbooks/local.yml
```

This command automatically generates the local `.env` configuration.

Verify the file exists:

macOS / Linux:

```bash
ls ansible/.runtime/erp/.env
```

Windows PowerShell:

```powershell
Get-ChildItem ansible\.runtime\erp\.env
```

Inspect generated values:

```bash
cat ansible/.runtime/erp/.env
```

---

### 3. Load generated environment variables

Before starting the application, load the generated `.env`.

macOS / Linux:

```bash
set -a
source ansible/.runtime/erp/.env
set +a
```

Windows PowerShell:

```powershell
Get-Content ansible\.runtime\erp\.env | foreach {
  if ($_ -match '^(.*?)=(.*)$') {
    [Environment]::SetEnvironmentVariable($matches[1], $matches[2], "Process")
  }
}
```

---

### 4. Start local infrastructure

It is recommended to develop against the local SQL Server instance.

macOS / Linux:

```bash
docker compose -f docker/docker-compose.yml up -d
```

Windows:

```powershell
docker compose -f docker\docker-compose.yml up -d
```

If you want to use a Local SSO instance, refer to:

```text
docker/sso/import/ERP-realm.json
```

---

### 5. Start the Spring Boot application

#### IntelliJ (recommended for development)

Create a Run Configuration and make sure environment variables are loaded before starting.

#### Maven

```bash
./mvnw spring-boot:run
```

or

```bash
mvn spring-boot:run
```

#### Gradle

```bash
./gradlew bootRun
```

Application startup should display:

```text
Started Application in X seconds
```

---

### Optional: Automated start script (testing only)

If you only want to start the backend quickly for testing:

macOS / Linux:

```bash
node scripts/start.js [use-local-sso]
```

Windows:

```powershell
node scripts\start.js [use-local-sso]
```

Requires Node.js installed locally.

---

### Authorization

Authorization is done using the OpenID connect support of *Spring* security and the ERP *Keycloak*.
The `erp-api-service` uses a configured `erp` client that has multiple users.

Check out the `requests.http` file where you can authenticate as one of the two and fire up requests against the API.

### Health Services

This application uses *Spring Boot Actuator* to provide basic health checks.

| ***Route***                    | ***Description***                           |
|--------------------------------|---------------------------------------------|
| ```/customerStatus/health/liveness```  | The application is up and running.          |
| ```/customerStatus/health/readiness``` | The application is ready to serve requests. |

### Databases

We use *Flyway* for the database migrations. For database changes, please create a new migration file
with a version, it will be applied at the next app start.

To run a *SQL Sever* instance locally just run ```$ docker-compose -f docker/docker-compose.yml up -d```.
This database is getting pre-filled at application start up.

### OpenAPI Documentation

*OpenAPI* documentation is generated via [springdoc](https://github.com/springdoc/springdoc-openapi).
The JSON specification is available
at [repository](../openapi/erp-openapi-contract.yaml)

## Configuration

The application should be configured via environment variables.
Environment variables will overwrite certain dealStage/deployment specific properties in the `application.yaml`.
In OpenShift the environment variables are provided via the `DeploymentConfig`.

| Variable               | Effect                                                                                | Value for local dev                                                                           |
|------------------------|---------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------|
| `DB_JDBC_URL`          | JDBC connection string.                                                               | `jdbc:sqlserver://localhost:1433;database=erp_db;encrypt=false;trustServerCertificate=false;` |
| `DB_USERNAME`          | Database username.                                                                    | `erp_db_user`                                                                                 |
| `DB_PASSWORD`          | Database password.                                                                    | `secretL0calPassword`                                                                         |
| `DB_JDBC_DRIVER`       | The JDBC driver, such as \*com.microsoft.sqlserver.jdbc.SQLServerDriver\* (Azure DB). | `com.microsoft.sqlserver.jdbc.SQLServerDriver`                                                |
| `SSO_CLIENT_ID`        | The SSO client.                                                                       | `erp`                                                                                         |
| `SSO_JWK_SET_URI`      | URL to the SSO certificate endpoint.                                                  | `http://localhost:8180/auth/realms/ERP/protocol/openid-connect/certs`                         |
| `CORS_ALLOWED_ORIGINS` | Comma separated allowed CORS origin URLs.                                             | `http://localhost:4200`                                                                       |
| `LOG_LEVEL`            | Sets the \*Spring\* root log level.                                                   |                                                                                               | |
| `SSO_JWT_TOKEN_URI`    | SSO Token URI.                                                                        | `http://localhost:8180/auth/realms/ERP/protocol/openid-connect/token`                         |
| `FLYWAY_LOCATION`      | Flyway folders to be imported                                                         | `db/migration,db/importMigration`                                                             |

## Development

### Starting local infrastructure

It is recommended to develop against the local *SQL Server* instance. To make it available, execute the following
command:

- `docker compose -f docker/docker-compose.yml up -d` (macOS and Linux)
- `docker compose -f docker\docker-compose.yml up -d` (Windows)

If you want to use a *Local SSO* instance, please refer to the Git
repository [erp-local-sso](./docker/sso/import/ERP-realm.json) and
follow the instructions.

### Starting the service

#### IntelliJ (for development only)

If you are using IntelliJ, you should create run configurations and make sure to configure the
appropriate [environment variables](#Configuration) to each configuration.

#### Automated start script (for testing purposes only)

If you only want to start the service for testing purposes (e.g. if you want to connect the frontend to it), you can use
an automated script that starts the service and initializes the local SQL Server. Simply execute the following command:

- `node scripts/start.js [use-local-sso]` (macOS and Linux)
- `node scripts\start.js [use-local-sso]` (Windows)

Note that this requires Node.js to be installed and configured correctly on your local development machine. The
parameter `use-local-sso` starts the service using
the [erp-local-sso](./docker/sso/import/ERP-realm.json).


### Logging

The stash logging in live environments can be enabled by setting the correct values for the Openshift environment
properties, as per the below table.
The configuration is done in [logback.xml](src/main/resources/logback.xml).

| Variable                  | Example value | Description                                          |
| ------------------------- | ------------- | ---------------------------------------------------- |
| `ERP_LOGGING_ENABLED`     | `true`        | Toggles TCP JSON logging to Logstash                 |
| `ERP_LOGGING_REMOTE_HOST` | `localhost`   | Hostname or IP where Logstash is listening           |
| `ERP_LOGGING_REMOTE_PORT` | `15000`       | Published port mapped to Logstash TCP input          |
| `HOSTNAME`                | `erp-local`   | Identifier for this application instance             |
| `APP_ID`                  | `erp-backend` | Logical application ID                               |
| `ERP_LOGGING_NAMESPACE`   | `local`       | Environment namespace (e.g., `local`, `dev`, `prod`) |
| `ERP_LOGGING_STAGE`       | `dev`         | Deployment dealStage                                     |

#### Setting up Kibana Data View

After you start your ELK stack with Docker Compose (including Elasticsearch, Logstash, and Kibana), follow these steps once to register the logs index in Kibana:

1. Open your browser to `http://localhost:5601` and click **Stack Management** (gear icon).
2. Select **Data Views** from the left menu.
3. Click **Create data view**.
4. In **Name**, enter:

   ```text
   erp-app-logs-*
   ```
5. In **Index pattern**, also enter:

   ```text
   erp-app-logs-*
   ```
6. Choose **@timestamp** for the **Time field**.
7. Click **Create data view**.

Now navigate to **Discover**, select **erp-app-logs-**\* in the index list and set the time range (e.g., Last 24 hours). Your application logs will appear in JSON format, searchable and filterable.

## SonarQube Analysis

The project uses the `jacoco` plugin for code coverage and integrates with SonarQube for static code analysis.
Make sure your local SonarQube server is running at http://localhost:9000.
You need to create your own SONAR_TOKEN in your SonarQube account settings.
To perform a local analysis and send the report to SonarQube, run the following command:

```bash
mvn clean install -P coverage sonar:sonar \
  -Dsonar.projectKey=ERP \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=SONAR_TOKEN

