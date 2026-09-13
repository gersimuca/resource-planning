# Security Policy

## Supported versions

Security fixes are applied to actively maintained versions of the project.

| Version        | Supported                 |
| -------------- | ------------------------- |
| develop        | Yes                       |
| Older releases | Depends on release status |

## Reporting a vulnerability

Please do not report security vulnerabilities through public GitHub issues.

Security vulnerabilities should be reported privately to the project maintainers.

Include:

* A description of the vulnerability
* Affected component
* Steps to reproduce
* Potential impact
* Suggested mitigation, if known

## What to avoid

Do not include the following in a vulnerability report unless absolutely necessary:

* Production credentials
* Access tokens
* Passwords
* Private keys
* Personal data

## Response

The maintainers will investigate the report and determine the appropriate remediation and disclosure process.

## Security best practices

Contributors should:

* Never commit secrets
* Keep dependencies up to date
* Review Renovate dependency updates
* Run the project's security checks before release
* Avoid logging credentials or sensitive information
* Validate and authorize all externally supplied input
