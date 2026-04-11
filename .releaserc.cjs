module.exports = {
  branches: ['main'],
  tagFormat: 'v${version}',
  plugins: [
    ['@semantic-release/commit-analyzer', {
      preset: 'conventionalcommits',
      releaseRules: [
        { breaking: true, release: 'major' },
        { type: 'feat', release: 'minor' },
        { type: 'fix', release: 'patch' },
        { type: 'perf', release: 'patch' },
        { type: 'revert', release: 'patch' },
        { type: 'docs', release: false },
        { type: 'style', release: false },
        { type: 'chore', release: false },
        { type: 'refactor', release: false },
        { type: 'test', release: false },
        { type: 'build', release: false },
        { type: 'ci', release: false },
        { type: 'ops', release: false },
      ],
    }],
    ['@semantic-release/release-notes-generator', {
      preset: 'conventionalcommits',
    }],
    ['@semantic-release/changelog', {
      changelogFile: 'CHANGELOG.md',
    }],
    ['@semantic-release/exec', {
      prepareCmd: "sed -i 's/^version=.*/version=${nextRelease.version}/' gradle.properties",
    }],
    ['@semantic-release/git', {
      assets: ['CHANGELOG.md', 'gradle.properties'],
      message: 'chore(release): ${nextRelease.version} [skip ci]\n\n${nextRelease.notes}',
    }],
    '@semantic-release/github',
  ],
};
