# Go Live Checklist - ERP Digital Contract

## Overview
This is a working document to be checked for completeness.

**Note:**
- Take external dependencies into considerations
- Add general risk register, business requirements, technical and administrative tasks

## Priorities

**LOW** → due diligence for go-live, but does not block it
**MEDIUM** → important for go-live, can potentially interfere or cause problems
**CRITICAL** → absolutely critical for go-live, complete blocker if not done

## Technical Tasks

*This checklist is based on Warranty Checklist and CDMS checklist. Other checklists as inspiration: EDCC, EMMP, CoCa, Data2Credits*

| Task | Priority | Type | Notes | Due Date | Status | Responsible |
|------|----------|------|-------|-----------|--------|-|
| Tech stack and technologies used are documented | LOW | Compliance | Ongoing documentation<br>Compliant to Tech Stack / ERP.Platform | Go-Live date | OPEN ||
| i/application integration | LOW | Compliance | Digital Contract must be listed in i/application. Interface documentation. Set application to live.<br>enter PROD URL and check if it's shown in i/application | Go-Live date | OPEN ||
| Integrate SAP interface (SAP SD Customer/Debitor)<br>Technical Users for K04 & P04 | CRITICAL | Availability | | week prior to Go-Live date | OPEN | |
| General Release & Deployment (CICD)<br>Jenkins Pipeline setup & tested<br>Test proper deployment process for the product, including testing, and production<br>Must build images only in nonprod namespaces<br>Expected build image released and running on production | CRITICAL | CICD | Jenkins ready for dev & test, prod is in preparation | week prior to Go-Live date | OPEN | |
| Integrated helm chart testing in Jenkins build pipelines | MEDIUM | Quality | | week prior to Go-Live date | OPEN | |
| Release rollback plan (for deployments after go live) - what happens if critical errors occur from one deployment to<br>CICD deployment rollback (re-deploy old version) | MEDIUM | Deployment | Requires proper CICD setup and alignment in engineering team<br>Challenge is database | week prior to Go-Live date | OPEN | |
| Ensure database backups are enabled and recoverable | MEDIUM | Backup | AzureSQL servers are redundant and replicated by default in production environment. | week prior to Go-Live date | OPEN | |
| Ensure general system availability for go live<br>Databases up & reachable<br>Frontend and backend reachable, proper application configuration<br>Prod interfaces are reachable | CRITICAL | Availability | | 1 week prior to Go-Live date | OPEN | |
| IDM configured prod roles<br>Add all relevant users to IDM prod (send U-numbers to IDM service if not present)<br>Assign users to new IDM roles | CRITICAL | Users | • Test Go-Live : User in Kons hinzufügen + Rollen vergeben | Go-Live date (not before §5 approval) | OPEN | |
| OpenShift - pod settings are sufficiently configured (requests, limits, etc.) | CRITICAL | Deployment | Check monitoring in OpenShift & Grafana to ensure sufficient resource availability<br>• Test Go-Live : @ Böhm, Christian | 1 week prior to Go-Live date | OPEN | |
| OpenShift - Should implement a Service for all deployments. | CRITICAL | Deployment | A Service automatically load balances incoming requests across all available replicas | 1 week prior to Go-Live date | OPEN | |
| OpenShift - resilience: application needs to tolerate losing pods<br>must use replicas >= 2 for production deployments<br>must use PodPriority (prod/nonprod) in deployments<br>should implement PodDisruptionBudget to guide the cluster for possible disruptions | MEDIUM | Availability | Ensures continuous availability of the service, even if a pod crashes. | 1 week prior to Go-Live date | OPEN | |
| Monitoring interfaces to ensure they are functioning.<br>The following interfaces may be live during production (CoCa, SD, CDmS) | MEDIUM | Monitoring | Use Spring Boot metrics & Grafana<br>Scan log files | week prior to Go-Live date | OPEN | |
| Setup proper logging<br>must include ERP common schema attributes: dealStage,erpappid & log-message<br>should use use severity as log level attribute<br>should use WARN or ERROR as severity for production | MEDIUM | Monitoring | Kibana setup | week prior to Go-Live date | OPEN | |
| Ensure §5 documented deletion policies are feasible and work | MEDIUM | Compliance | All deletion concepts listed in §5 document must be implemented | week prior to Go-Live date | OPEN | |
| Check snyk for image vulnerabilities at least two weeks prior to go live | LOW | Security | | 1 week prior to Go-Live date | OPEN | |
| SonarQube issues fixed (scan should pass on develop / master) | MEDIUM | Quality | Issues here could mean bugs, errors | 1 week prior to Go-Live date | OPEN | |
| Renovate Bot integration works properly | MEDIUM | Security | Ensures security fixes | week prior to Go-Live date | OPEN | |
| Linting and testing is integrated in pipeline<br>code formatted with google-java-format-plugin on BE<br>code formatted with intellij prettier plugin | LOW | Quality | Ensures maintainability of code | 1 week prior to Go-Live date | OPEN | |
| READMEs in all repositories | LOW | Quality | Ensures documentation for all services | 1 week prior to Go-Live date | OPEN | |
| Commits follow conventional commit message specification | LOW | Quality | Ensures quality and traceability in development work | 1 week prior to Go-Live date | OPEN | |
| git-flow is implemented as branching model | LOW | CICD | | 1 week prior to Go-Live date | OPEN | |
| Main branches are protected | LOW | Security | Ensures stability & security on released build<br>Protection works for the front-end | 1 week prior to Go-Live date | OPEN | |
| Publish API to Azure APIM Mangement if possible<br>lint API specification and check how much work it would be to make it publishable<br>integrate linting and publishing in pipeline | LOW | Quality | Ensures compliance with ERP API guidelines<br>Create Jira ticket @ Böhm, Christian | 1 week prior to Go-Live date | OPEN | |
| dbo schema is not used for application, instead create a specific schema for each application | LOW | Security | Ensures security compliance for DB use; avoiding default database-owner (dbo) schema is standard practice.<br>Possibly issue with service principles (investigate) | 1 week prior to Go-Live date | OPEN | |
| Runs as expected on MS Edge Windows 10 | MEDIUM | Compliance | Targeted main browser & OS (ERP employee machines) | 1 week prior to Go-Live date | OPEN | |

## Business Tasks

| Task | Priority | Type | Notes | Due Date | Status | Responsible |
|------|----------|------|-------|-----------|--------|-|
| S&P penetration test completed | CRITICAL | Compliance | see Confluence page | Go-Live date | OPEN | |
| §5 documentation approved by legal department | CRITICAL | Compliance | see Confluence page | Go-Live date | OPEN | |
| Business requirements assessment (BIA) approved by RISQ team | CRITICAL | Compliance | see Confluence page | Go-Live date | OPEN | |
| Approval from TEC & Procurement | MEDIUM | Users | • Get approval from relevant stakeholders | Go-Live date | OPEN | |
| Profit process update | LOW | Compliance | Click in context on Project resolution to draft change. Go through approval process, Controller itself can also be the relevant option. | After Go-Live | OPEN | |
| LearnIT, optional | LOW | Compliance | Ref. onboarding new users | | OPEN | |
| Purpose DIC demo session | LOW | Users | | After Go-Live | OPEN | |
| Create support concept | MEDIUM | Users | Functional Email chain | Go-Live date | OPEN | |
| Access to relevant tools (Confluence, Jira, Email, team channels) | MEDIUM | Users | Access granted to relevant team members | | OPEN | |
| Document process of generating a new user and all applications | MEDIUM | Users | IDM tool includes general instruction. Description in ERP.wiki will be added. | 1 week prior to Go-Live date | OPEN | |
| Create permission for the users | MEDIUM | Users | Names, Usernames, roles<br>• for edit user IDM roles & rights<br>• approval of edit Master users to have permission | 1 week prior to Go-Live date | OPEN | |
| User Acceptance testing completed at different process stages | CRITICAL | Quality | • Check that pricing flow works correctly / pricing calculation is proper for data flow – To be discussed with Martin Barrow | 2 week prior to Go-Live date | OPEN | |
| Train key users | | | | | OPEN | |
| Global Export Control when geo free does not work as 'global', according to requirements | MEDIUM | Security | | 1 week prior to Go-Live date | OPEN | |
| Training and coaching with key provider's phase (key users, expert access, are ongoing) | MEDIUM | Workflow | | | OPEN | |
| Communication to all involved parties about application customerStatus and timeline separately first are set go live S&P / CDMS | | | | | OPEN | |
