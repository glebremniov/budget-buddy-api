## [0.7.0](https://github.com/budget-buddy-org/budget-buddy-api/compare/v0.6.1...v0.7.0) (2026-04-17)

### Features

* add TransactionType schema and type filter for list transactions ([#122](https://github.com/budget-buddy-org/budget-buddy-api/issues/122)) ([7e8fb32](https://github.com/budget-buddy-org/budget-buddy-api/commit/7e8fb32a422b2e74c43a0d352081a5e7da71c1d7))

## [0.6.1](https://github.com/budget-buddy-org/budget-buddy-api/compare/v0.6.0...v0.6.1) (2026-04-16)

### Bug Fixes

* list transactions ordered by date and created at ([#121](https://github.com/budget-buddy-org/budget-buddy-api/issues/121)) ([b21f001](https://github.com/budget-buddy-org/budget-buddy-api/commit/b21f001a0d1f7e1a4e7ea26fa31978151b847648))

## [0.6.0](https://github.com/budget-buddy-org/budget-buddy-api/compare/v0.5.0...v0.6.0) (2026-04-15)

### Features

* add category sorting by name and update dev token validity ([#120](https://github.com/budget-buddy-org/budget-buddy-api/issues/120)) ([35fea9b](https://github.com/budget-buddy-org/budget-buddy-api/commit/35fea9b1ff41cec60ac010b298117ffe1c0609a5))

## [0.5.0](https://github.com/budget-buddy-org/budget-buddy-api/compare/v0.4.0...v0.5.0) (2026-04-12)

### Features

* standardize service read methods with @Transactional(readOnly = true) ([#114](https://github.com/budget-buddy-org/budget-buddy-api/issues/114)) ([0263f1d](https://github.com/budget-buddy-org/budget-buddy-api/commit/0263f1d7dba296e2a9e3ba4f9d60fba1c18c449e)), closes [#84](https://github.com/budget-buddy-org/budget-buddy-api/issues/84)

## [0.4.0](https://github.com/budget-buddy-org/budget-buddy-api/compare/v0.3.0...v0.4.0) (2026-04-12)

### Features

* align GlobalExceptionHandler with RFC 9457 recommendations ([#112](https://github.com/budget-buddy-org/budget-buddy-api/issues/112)) ([101b582](https://github.com/budget-buddy-org/budget-buddy-api/commit/101b58268b76ff9fb104a8983035cab432e19751)), closes [#95](https://github.com/budget-buddy-org/budget-buddy-api/issues/95)

## [0.3.0](https://github.com/budget-buddy-org/budget-buddy-api/compare/v0.2.3...v0.3.0) (2026-04-12)

### Features

* enforce password complexity at registration ([#86](https://github.com/budget-buddy-org/budget-buddy-api/issues/86)) ([a16a364](https://github.com/budget-buddy-org/budget-buddy-api/commit/a16a3641af5a90d0aa5b9de7f565bec828c39021)), closes [#79](https://github.com/budget-buddy-org/budget-buddy-api/issues/79)

## [0.2.3](https://github.com/budget-buddy-org/budget-buddy-api/compare/v0.2.2...v0.2.3) (2026-04-12)

### Bug Fixes

* **ci:** approve esbuild build scripts for pnpm v10 ([#110](https://github.com/budget-buddy-org/budget-buddy-api/issues/110)) ([dfe4ddd](https://github.com/budget-buddy-org/budget-buddy-api/commit/dfe4dddddee7c19b82ad0b6bc9e2d99b54ab593a))

## [0.2.2](https://github.com/budget-buddy-org/budget-buddy-api/compare/v0.2.1...v0.2.2) (2026-04-11)

### Bug Fixes

* fix gradle/actions SHA typo in docker-publish.yaml ([#108](https://github.com/budget-buddy-org/budget-buddy-api/issues/108)) ([4695895](https://github.com/budget-buddy-org/budget-buddy-api/commit/4695895b83aed7d37a0301336a7358aba807e5ed))

## [0.2.1](https://github.com/budget-buddy-org/budget-buddy-api/compare/v0.2.0...v0.2.1) (2026-04-11)

### Bug Fixes

* support JsonNullable in PATCH and add mapper regression tests ([#107](https://github.com/budget-buddy-org/budget-buddy-api/issues/107)) ([3e26c0d](https://github.com/budget-buddy-org/budget-buddy-api/commit/3e26c0d5d658cdf6b3f0f1b257a301100b9e0aa1)), closes [#94](https://github.com/budget-buddy-org/budget-buddy-api/issues/94)
