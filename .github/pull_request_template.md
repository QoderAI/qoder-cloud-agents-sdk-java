## Change

Describe the problem and resulting public behavior. For a new API, identify its contract source and the Forward coverage entry.

Describe any Go / Python / TypeScript behavior impact, or explain why this change is Java-specific.
For incompatible changes, describe the previous behavior and the required migration.

## Validation

- [ ] SDK build, tests and Javadoc pass.
- [ ] Independent examples consume the installed JAR and pass.
- [ ] Java 8 package check passes; changed runtime behavior is checked on the relevant JDKs.
- [ ] Documentation and changelog reflect public behavior changes.
- [ ] `make quality` passes on JDK 17+ (format, versions, changelog and local links).
- [ ] Any version change updates both POMs and the matching changelog entry.

List actual JDKs and commands, and any live scenarios run. Explain failures or untested scope; mock tests are not live verification. Keep tokens, SA Keys and signed URLs out of this PR.

For a QA candidate, record source SHA, artifact SHA-256 and the run/report link.
State skipped or unexecuted scenarios separately from passing tests. Human QA approval is separate from automated results.
