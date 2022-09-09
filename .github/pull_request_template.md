<!--
Thank you for supporting us with your Pull Request! 🙌 ❤️
Before submitting, please take the time to check the points below and provide some descriptive information.

Remove the checklist after fulfilling all the relevant points, and before creating the PR, thank you.
-->

### Checklist

__Thank you for this PR! Please consider the following:__

* To the _Community_ :heart::
    * Please link to an issue ticket where your code change has been greenlit, otherwise it's
      unlikely it can be merged.
    * Use a descriptive title: {task_name} (closes #{issue_number}),
      e.g.: `Use logger (closes # 41)`.
    * If this PR comes from a
      fork, please [allow edits from maintainers](https://help.github.com/en/github/collaborating-with-issues-and-pull-requests/allowing-changes-to-a-pull-request-branch-created-from-a-fork)
    * The PR can not contain changes in localization files, e.g. `values-[LANGUAGE-CODE]/strings/*`
      and `/` or `assets/*`). Localizations are usually provided and approved by the UA team.
    * Before submitting a PR, please make sure that you target the active `release/*.*.*` branch
      which can be determined by the active milestone on GitHub
    * Include `COMMUNITY` in the PR title such as "PR fixes issue ### (COMMUNITY)".
* To the _Maintainers_ :coffee::
    * Title structure: `Jira ticket name (EXPOSUREAPP-XXXX)`.
    * Set labels: `maintainers` and (`bug`or `text change`).
    * Update Jira status: `In Review`.
    * Fulfill internal `Acceptance Criteria`.
    * If mentioned in the Jira ticket link all corresponding GitHub issues in the sidebar.
* To _everyone_ :world_map::
    * Describe your changes in detail, if you changed the UI, screenshots or GIFs would be awesome!
    * Short step-by-step instructions help the reviewer test your changes, e.g. how to navigate to a
      new UI element you added.
    * The PR _won't be reviewed_ if CI is failing or if there are merge conflicts. If CI is still
      failing mark the PR as a draft and write a little comment on your status.
    * Provide at least a few unit and/or instrumentation tests.
    * Use a meaningful branch name. Use either `fix` or `feature` as prefix for your branch,
      e.g. `fix/prevent-npe-on-device-rotation-issue_123`
    * Test your changes thoroughly. Only open PRs which you think are ready to be merged. If you
      explicitly need feedback mark the PR as `DRAFT` on GitHub.
    * Don't introduce unrelated code reformatting (e.g., on-save hooks in your IDE)
    * Remove this checklist before creating your pull request.

### Description

<!-- 
Please be brief in describing which issue is solved by your PR or which enhancement it brings. Link related issues!
-->

### Steps to reproduce

<!--
How can your changes be tested?
1. First step
2. Second step
 -->
